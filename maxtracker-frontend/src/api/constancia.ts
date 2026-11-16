import { http } from './http'
import type { Constancia, PlanEntrenamiento } from './types'

/** Calendario de un mes ("AAAA-MM"; sin mes = el actual) con el resumen, las rachas y el plan vigente. */
export async function obtenerConstancia(mes?: string, signal?: AbortSignal): Promise<Constancia> {
  const { data } = await http.get<Constancia>('/constancia', { params: mes ? { mes } : undefined, signal })
  return data
}

/** Crea o cambia el plan semanal (días ISO: 1 = lunes … 7 = domingo). Rige desde hoy. */
export async function guardarPlanEntrenamiento(dias: number[]): Promise<PlanEntrenamiento> {
  const { data } = await http.put<PlanEntrenamiento>('/constancia/plan', { dias })
  return data
}
