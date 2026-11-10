import { http } from './http'
import type { EjercicioConHistorial, ProgresoEjercicio, ResumenMetricas } from './types'

export async function obtenerResumenMetricas(semanas = 12, signal?: AbortSignal): Promise<ResumenMetricas> {
  const { data } = await http.get<ResumenMetricas>('/metricas/resumen', { params: { semanas }, signal })
  return data
}

export async function listarEjerciciosConHistorial(signal?: AbortSignal): Promise<EjercicioConHistorial[]> {
  const { data } = await http.get<EjercicioConHistorial[]>('/metricas/ejercicios', { signal })
  return data
}

export async function obtenerProgreso(ejercicioId: number, signal?: AbortSignal): Promise<ProgresoEjercicio> {
  const { data } = await http.get<ProgresoEjercicio>(`/metricas/ejercicios/${ejercicioId}/progreso`, { signal })
  return data
}
