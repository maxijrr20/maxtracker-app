import axios, { AxiosError, AxiosHeaders } from 'axios'

import { borrarSesion, leerToken } from '../lib/tokenStorage'
import type { ProblemDetail } from './types'

export const API_URL: string = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

/** Cliente HTTP único de la app: agrega el JWT y normaliza los errores a {@link ApiError}. */
export const http = axios.create({
  baseURL: API_URL,
  timeout: 15_000,
  headers: { Accept: 'application/json, application/problem+json' },
})

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
  return config
})

http.interceptors.response.use(
  (respuesta) => respuesta,
  (error: unknown) => {
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
        ? 'El servidor tardó demasiado en responder. Intenta nuevamente.'
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
