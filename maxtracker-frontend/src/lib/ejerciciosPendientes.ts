import type { EjercicioElegido } from './ejercicioElegido'

/**
 * Ejercicios agregados a una sesión que todavía no tienen series. El backend solo guarda series (no un "plan"
 * de la sesión), así que se recuerdan en localStorage por sesión para no perderlos al recargar o cerrar la app.
 * Desaparecen al registrar su primera serie o al finalizar la sesión. Si el almacenamiento falla, solo se pierde
 * esa comodidad.
 */

const prefijo = (sesionId: number) => `maxtracker.sesion.${sesionId}.pendientes`

function esEjercicioElegido(valor: unknown): valor is EjercicioElegido {
  if (typeof valor !== 'object' || valor === null) return false
  const v = valor as Record<string, unknown>
  const ref = v.ref as Record<string, unknown> | undefined
  return (
    typeof v.nombre === 'string' &&
    typeof ref === 'object' &&
    ref !== null &&
    (typeof ref.ejercicioId === 'number') !== (typeof ref.wgerId === 'number')
  )
}

export function leerPendientes(sesionId: number): EjercicioElegido[] {
  try {
    const crudo = localStorage.getItem(prefijo(sesionId))
    const datos: unknown = crudo ? JSON.parse(crudo) : []
    return Array.isArray(datos) ? datos.filter(esEjercicioElegido) : []
  } catch {
    return []
  }
}

export function guardarPendientes(sesionId: number, ejercicios: EjercicioElegido[]): void {
  try {
    if (ejercicios.length === 0) localStorage.removeItem(prefijo(sesionId))
    else localStorage.setItem(prefijo(sesionId), JSON.stringify(ejercicios))
  } catch {
    // Sin almacenamiento: los pendientes viven solo en memoria.
  }
}
