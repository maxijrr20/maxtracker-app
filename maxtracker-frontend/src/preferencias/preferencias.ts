/**
 * Preferencias del usuario en este dispositivo (localStorage). Son de presentación y comodidad: los datos del
 * backend no cambian (los pesos se guardan siempre en kg). Toda lectura/escritura tolera que no haya almacenamiento.
 */

export type UnidadPeso = 'kg' | 'lb'
export type EnfoqueIa = 'FUERZA' | 'HIPERTROFIA' | 'MANTENIMIENTO'

export const DESCANSOS = [30, 60, 90, 120] as const
export type DescansoSegundos = (typeof DESCANSOS)[number]

/** 'auto' = la zona horaria del dispositivo. */
export type ZonaHoraria = 'auto' | string

export interface Preferencias {
  unidadPeso: UnidadPeso
  descansoSegundos: DescansoSegundos
  zonaHoraria: ZonaHoraria
  enfoqueIa: EnfoqueIa
}

export const PREFERENCIAS_POR_DEFECTO: Preferencias = {
  unidadPeso: 'kg',
  descansoSegundos: 90,
  zonaHoraria: 'auto',
  enfoqueIa: 'HIPERTROFIA',
}

const CLAVE_PREFERENCIAS = 'maxtracker.preferencias'
const CLAVE_GEMINI = 'maxtracker.claveGemini'

/** Formato de una clave de Google AI Studio (mismo patrón que valida el backend). */
export const PATRON_CLAVE_GEMINI = /^[A-Za-z0-9_-]{20,200}$/

function valida(valor: unknown): Partial<Preferencias> {
  if (typeof valor !== 'object' || valor === null) return {}
  const v = valor as Record<string, unknown>
  const resultado: Partial<Preferencias> = {}
  if (v.unidadPeso === 'kg' || v.unidadPeso === 'lb') resultado.unidadPeso = v.unidadPeso
  if (DESCANSOS.includes(v.descansoSegundos as DescansoSegundos)) {
    resultado.descansoSegundos = v.descansoSegundos as DescansoSegundos
  }
  if (typeof v.zonaHoraria === 'string' && esZonaValida(v.zonaHoraria)) resultado.zonaHoraria = v.zonaHoraria
  if (v.enfoqueIa === 'FUERZA' || v.enfoqueIa === 'HIPERTROFIA' || v.enfoqueIa === 'MANTENIMIENTO') {
    resultado.enfoqueIa = v.enfoqueIa
  }
  return resultado
}

export function esZonaValida(zona: string): boolean {
  if (zona === 'auto') return true
  try {
    new Intl.DateTimeFormat('es', { timeZone: zona })
    return true
  } catch {
    return false
  }
}

export function leerPreferencias(): Preferencias {
  try {
    const crudo = localStorage.getItem(CLAVE_PREFERENCIAS)
    return { ...PREFERENCIAS_POR_DEFECTO, ...(crudo ? valida(JSON.parse(crudo)) : {}) }
  } catch {
    return PREFERENCIAS_POR_DEFECTO
  }
}

export function guardarPreferencias(preferencias: Preferencias): void {
  try {
    localStorage.setItem(CLAVE_PREFERENCIAS, JSON.stringify(preferencias))
  } catch {
    // Sin almacenamiento: duran lo que la pestaña.
  }
}

export function leerClaveGemini(): string | null {
  try {
    const clave = localStorage.getItem(CLAVE_GEMINI)
    return clave && PATRON_CLAVE_GEMINI.test(clave) ? clave : null
  } catch {
    return null
  }
}

export function guardarClaveGemini(clave: string | null): void {
  try {
    if (clave) localStorage.setItem(CLAVE_GEMINI, clave)
    else localStorage.removeItem(CLAVE_GEMINI)
  } catch {
    // Sin almacenamiento: la clave vive solo en memoria.
  }
}
