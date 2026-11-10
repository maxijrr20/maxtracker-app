import { http } from './http'
import type { Rutina, RutinaResumen } from './types'

export async function listarRutinas(signal?: AbortSignal): Promise<RutinaResumen[]> {
  const { data } = await http.get<RutinaResumen[]>('/rutinas', { signal })
  return data
}

export async function obtenerRutina(id: number, signal?: AbortSignal): Promise<Rutina> {
  const { data } = await http.get<Rutina>(`/rutinas/${id}`, { signal })
  return data
}
