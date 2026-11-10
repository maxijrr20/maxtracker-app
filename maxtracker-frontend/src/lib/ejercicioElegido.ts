import type { RefEjercicio } from '../api/types'

/** Resultado del EjercicioSelector: cómo referenciarlo en la API y cómo mostrarlo. */
export interface EjercicioElegido {
  ref: RefEjercicio
  nombre: string
  categoria: string | null
}

/** Clave estable para listas y para detectar repetidos. */
export function claveEjercicio(ref: RefEjercicio): string {
  return ref.ejercicioId !== undefined ? `local-${ref.ejercicioId}` : `wger-${ref.wgerId}`
}
