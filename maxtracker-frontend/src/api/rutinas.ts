import { http } from './http'
import type { GuardarRutinaRequest, Rutina, RutinaResumen } from './types'

export async function crearRutina(rutina: GuardarRutinaRequest): Promise<Rutina> {
  const { data } = await http.post<Rutina>('/rutinas', rutina)
  return data
}

/** Reemplazo completo: nombre, descripción y la lista entera de ejercicios. */
export async function actualizarRutina(id: number, rutina: GuardarRutinaRequest): Promise<Rutina> {
  const { data } = await http.put<Rutina>(`/rutinas/${id}`, rutina)
  return data
}

export async function eliminarRutina(id: number): Promise<void> {
  await http.delete(`/rutinas/${id}`)
}

export async function listarRutinas(signal?: AbortSignal): Promise<RutinaResumen[]> {
  const { data } = await http.get<RutinaResumen[]>('/rutinas', { signal })
  return data
}

export async function obtenerRutina(id: number, signal?: AbortSignal): Promise<Rutina> {
  const { data } = await http.get<Rutina>(`/rutinas/${id}`, { signal })
  return data
}
