import type { UnidadPeso } from '../preferencias/preferencias'

/** Definición exacta de la libra internacional. */
export const KG_POR_LB = 0.45359237

/** El backend guarda kg con 2 decimales (peso_kg NUMERIC(6,2)): 9999,99 kg como máximo. */
export const MAX_KG = 9999.99

export function desdeKg(kg: number, unidad: UnidadPeso): number {
  return unidad === 'kg' ? kg : kg / KG_POR_LB
}

/** Valor en la unidad del usuario → kg con 2 decimales (lo que acepta el backend). */
export function aKg(valor: number, unidad: UnidadPeso): number {
  const kg = unidad === 'kg' ? valor : valor * KG_POR_LB
  return Math.round(kg * 100) / 100
}

const FORMATOS = {
  kg: new Intl.NumberFormat('es', { maximumFractionDigits: 2 }),
  lb: new Intl.NumberFormat('es', { maximumFractionDigits: 1 }),
}
const COMPACTO = new Intl.NumberFormat('es', { notation: 'compact', maximumFractionDigits: 1 })

/** "80 kg" / "176,4 lb". */
export function formatoPeso(kg: number, unidad: UnidadPeso): string {
  return `${FORMATOS[unidad].format(desdeKg(kg, unidad))} ${unidad}`
}

/** Para ejes de gráficos: 12.950 → "13 mil" (en la unidad del usuario, sin sufijo). */
export function formatoPesoCompacto(kg: number, unidad: UnidadPeso): string {
  return COMPACTO.format(desdeKg(kg, unidad))
}

/** Valor para precargar un campo: kg tal cual (hasta 2 decimales), lb con 1 decimal. */
export function valorEditable(kg: number, unidad: UnidadPeso): string {
  const valor = desdeKg(kg, unidad)
  const redondeado = unidad === 'kg' ? Math.round(valor * 100) / 100 : Math.round(valor * 10) / 10
  return String(redondeado).replace('.', ',')
}
