import { http } from './http'
import type { AnalisisRutina, SolicitudAnalisis } from './types'

/** El modelo puede tardar varios segundos: timeout más largo que el general. */
const TIMEOUT_ANALISIS_MS = 60_000

/** Cabecera con la clave personal de Gemini (ver CabecerasApi en el backend). */
const CABECERA_CLAVE_GEMINI = 'X-Gemini-Api-Key'

/**
 * @param claveGemini clave personal guardada en Perfil: se envía solo en esta petición; sin ella, el backend usa
 *                    la suya (GEMINI_API_KEY)
 */
export async function analizarRutina(solicitud: SolicitudAnalisis = {}, claveGemini?: string | null): Promise<AnalisisRutina> {
  const { data } = await http.post<AnalisisRutina>('/analisis/rutina', solicitud, {
    timeout: TIMEOUT_ANALISIS_MS,
    headers: claveGemini ? { [CABECERA_CLAVE_GEMINI]: claveGemini } : undefined,
  })
  return data
}
