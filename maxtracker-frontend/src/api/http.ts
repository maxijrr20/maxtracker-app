import axios, { AxiosError, AxiosHeaders, type InternalAxiosRequestConfig } from 'axios'

import { borrarSesion, leerToken } from '../lib/tokenStorage'
import type { ProblemDetail } from './types'

export const API_URL: string = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

/**
 * Cliente HTTP único de la app: agrega el JWT y normaliza los errores a {@link ApiError}.
 *
 * Timeout amplio: el backend corre en el plan gratuito de Render, que se suspende sin tráfico y tarda ~30-60 s en
 * arrancar. Con 15 s la primera petición del día fallaba aunque el servidor estuviera despertando bien.
 */
export const http = axios.create({
  baseURL: API_URL,
  timeout: 90_000,
  headers: { Accept: 'application/json, application/problem+json' },
})

// --- Espera larga: el servidor probablemente está despertando ---

/** A partir de cuánto tiempo una petición se considera "lenta" (una respuesta normal tarda < 1 s). */
const UMBRAL_ESPERA_LARGA_MS = 5_000

/** Rutas que tardan varios segundos aunque el servidor esté despierto (no indican un arranque en frío). */
function esRutaLentaPorNaturaleza(url: string | undefined): boolean {
  return url?.startsWith('/analisis') ?? false
}

const temporizadores = new WeakMap<InternalAxiosRequestConfig, number>()
const peticionesLentas = new Set<InternalAxiosRequestConfig>()
const oyentesEspera = new Set<() => void>()

function notificarEspera() {
  oyentesEspera.forEach((oyente) => oyente())
}

/** true mientras alguna petición lleva más de {@link UMBRAL_ESPERA_LARGA_MS} sin responder. */
export function hayEsperaLarga(): boolean {
  return peticionesLentas.size > 0
}

/** Se notifica cada vez que cambia {@link hayEsperaLarga}. Devuelve la función para desuscribirse. */
export function alCambiarEsperaLarga(oyente: () => void): () => void {
  oyentesEspera.add(oyente)
  return () => {
    oyentesEspera.delete(oyente)
  }
}

function vigilarEspera(config: InternalAxiosRequestConfig) {
  if (esRutaLentaPorNaturaleza(config.url)) return
  const id = window.setTimeout(() => {
    peticionesLentas.add(config)
    notificarEspera()
  }, UMBRAL_ESPERA_LARGA_MS)
  temporizadores.set(config, id)
}

function terminarEspera(config: InternalAxiosRequestConfig | undefined) {
  if (!config) return
  const id = temporizadores.get(config)
  if (id !== undefined) {
    window.clearTimeout(id)
    temporizadores.delete(config)
  }
  if (peticionesLentas.delete(config)) notificarEspera()
}

/** Error de la API con el mensaje listo para mostrar al usuario. */
export class ApiError extends Error {
  readonly status: number | null
  readonly problema: ProblemDetail | null

  constructor(mensaje: string, status: number | null, problema: ProblemDetail | null) {
    super(mensaje)
    this.name = 'ApiError'
    this.status = status
    this.problema = problema
  }
}

// --- 401: sesión inválida o expirada ---

type OyenteNoAutorizado = () => void
const oyentes = new Set<OyenteNoAutorizado>()

/** Se notifica cuando una petición autenticada responde 401. Devuelve la función para desuscribirse. */
export function alNoAutorizado(oyente: OyenteNoAutorizado): () => void {
  oyentes.add(oyente)
  return () => {
    oyentes.delete(oyente)
  }
}

/** En /auth/* un 401 significa "credenciales incorrectas", no "sesión expirada". */
function esRutaDeAuth(url: string | undefined): boolean {
  return url?.startsWith('/auth/') ?? false
}

// --- Interceptores ---

http.interceptors.request.use((config) => {
  const token = leerToken()
  if (token && !esRutaDeAuth(config.url)) {
    const headers = AxiosHeaders.from(config.headers)
    headers.set('Authorization', `Bearer ${token}`)
    config.headers = headers
  }
  vigilarEspera(config)
  return config
})

http.interceptors.response.use(
  (respuesta) => {
    terminarEspera(respuesta.config)
    return respuesta
  },
  (error: unknown) => {
    if (error instanceof AxiosError) terminarEspera(error.config)
    // Petición cancelada (p. ej. el componente se desmontó): no es un error para el usuario.
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    if (!(error instanceof AxiosError)) {
      return Promise.reject(new ApiError('Ocurrió un error inesperado.', null, null))
    }
    const status = error.response?.status ?? null
    if (status === 401 && !esRutaDeAuth(error.config?.url)) {
      borrarSesion()
      oyentes.forEach((oyente) => oyente())
    }
    return Promise.reject(aApiError(error))
  },
)

function aApiError(error: AxiosError): ApiError {
  if (!error.response) {
    const mensaje =
      error.code === AxiosError.ECONNABORTED
        ? 'El servidor no respondió a tiempo. Revisa tu conexión e intenta nuevamente.'
        : 'No se pudo conectar con el servidor. Revisa tu conexión.'
    return new ApiError(mensaje, null, null)
  }
  const datos = error.response.data
  const problema = esProblemDetail(datos) ? datos : null
  const mensaje = problema?.detail ?? problema?.title ?? 'Ocurrió un error inesperado.'
  return new ApiError(mensaje, error.response.status, problema)
}

function esProblemDetail(datos: unknown): datos is ProblemDetail {
  return typeof datos === 'object' && datos !== null && ('detail' in datos || 'title' in datos)
}
