import { http } from './http'
import type { AnalisisRutina, SolicitudAnalisis } from './types'

/** El modelo puede tardar varios segundos: timeout más largo que el general. */
const TIMEOUT_ANALISIS_MS = 60_000

export async function analizarRutina(solicitud: SolicitudAnalisis = {}): Promise<AnalisisRutina> {
  const { data } = await http.post<AnalisisRutina>('/analisis/rutina', solicitud, { timeout: TIMEOUT_ANALISIS_MS })
  return data
}
