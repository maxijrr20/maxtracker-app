import { http } from './http'
import type { Pagina, RegistrarSerieRequest, Serie, Sesion, SesionResumen } from './types'

export async function listarSesiones(pagina = 0, tamanio = 20, signal?: AbortSignal): Promise<Pagina<SesionResumen>> {
  const { data } = await http.get<Pagina<SesionResumen>>('/sesiones', { params: { pagina, tamanio }, signal })
  return data
}

export async function obtenerSesion(id: number, signal?: AbortSignal): Promise<Sesion> {
  const { data } = await http.get<Sesion>(`/sesiones/${id}`, { signal })
  return data
}

/**
 * Sesión en curso más reciente, o {@code null}. El historial viene de la más reciente a la más antigua,
 * así que basta con revisar las primeras.
 */
export async function buscarSesionEnCurso(signal?: AbortSignal): Promise<Sesion | null> {
  const { contenido } = await listarSesiones(0, 10, signal)
  const enCurso = contenido.find((sesion) => !sesion.finalizada)
  return enCurso ? obtenerSesion(enCurso.id, signal) : null
}

/** {@code fecha} (YYYY-MM-DD): el "hoy" en la zona horaria del usuario; sin ella el backend usa la del servidor. */
export async function iniciarSesion(
  datos: { rutinaId?: number; notas?: string; fecha?: string } = {},
): Promise<Sesion> {
  const { data } = await http.post<Sesion>('/sesiones', datos)
  return data
}

export async function finalizarSesion(id: number): Promise<Sesion> {
  const { data } = await http.post<Sesion>(`/sesiones/${id}/finalizar`)
  return data
}

export async function registrarSerie(sesionId: number, serie: RegistrarSerieRequest): Promise<Serie> {
  const { data } = await http.post<Serie>(`/sesiones/${sesionId}/series`, serie)
  return data
}

export async function eliminarSerie(sesionId: number, serieId: number): Promise<void> {
  await http.delete(`/sesiones/${sesionId}/series/${serieId}`)
}
