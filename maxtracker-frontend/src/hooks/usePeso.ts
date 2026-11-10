import { useMemo } from 'react'

import { aKg, desdeKg, formatoPeso, formatoPesoCompacto, valorEditable } from '../lib/unidades'
import type { UnidadPeso } from '../preferencias/preferencias'
import { usePreferencias } from '../preferencias/preferenciasContext'

export interface Peso {
  unidad: UnidadPeso
  formato: (kg: number) => string
  formatoCompacto: (kg: number) => string
  desdeKg: (kg: number) => number
  aKg: (valor: number) => number
  valorEditable: (kg: number) => string
}

/**
 * Presentación de pesos en la unidad elegida en Perfil. Es una preferencia de presentación (como el idioma),
 * por eso cualquier nivel atómico puede usarla. Los datos siempre se envían y reciben en kg.
 */
export function usePeso(): Peso {
  const { unidadPeso } = usePreferencias()
  return useMemo(
    () => ({
      unidad: unidadPeso,
      formato: (kg) => formatoPeso(kg, unidadPeso),
      formatoCompacto: (kg) => formatoPesoCompacto(kg, unidadPeso),
      desdeKg: (kg) => desdeKg(kg, unidadPeso),
      aKg: (valor) => aKg(valor, unidadPeso),
      valorEditable: (kg) => valorEditable(kg, unidadPeso),
    }),
    [unidadPeso],
  )
}
