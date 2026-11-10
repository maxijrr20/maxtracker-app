/** Formatos de números y fechas en español, compartidos por tarjetas, gráficos y tablas. */

const KG = new Intl.NumberFormat('es', { maximumFractionDigits: 1 })
const KG_COMPACTO = new Intl.NumberFormat('es', { notation: 'compact', maximumFractionDigits: 1 })
const FECHA_CORTA = new Intl.DateTimeFormat('es', { day: 'numeric', month: 'short' })
const FECHA_LARGA = new Intl.DateTimeFormat('es', { day: 'numeric', month: 'long', year: 'numeric' })

/** "YYYY-MM-DD" → Date local a mediodía (sin zona, para que no cambie de día). */
export function aFecha(isoFecha: string): Date {
  return new Date(`${isoFecha}T12:00:00`)
}

export function formatoKg(kg: number): string {
  return `${KG.format(kg)} kg`
}

/** Para ejes: 12.950 → "13 mil". */
export function formatoKgCompacto(kg: number): string {
  return KG_COMPACTO.format(kg)
}

export function fechaCorta(isoFecha: string): string {
  return FECHA_CORTA.format(aFecha(isoFecha))
}

export function fechaLarga(isoFecha: string): string {
  return FECHA_LARGA.format(aFecha(isoFecha))
}

/** Variación porcentual redondeada, o {@code null} si no hay base de comparación. */
export function variacionPorcentual(actual: number, anterior: number): number | null {
  if (anterior <= 0) return null
  return Math.round(((actual - anterior) / anterior) * 100)
}
