import { Minus, Plus } from 'lucide-react'
import { useId } from 'react'

import { IconButton } from '../atoms/IconButton'

export interface ContadorFieldProps {
  etiqueta: string
  /** {@code null} = sin valor (se muestra "–"). */
  valor: number | null
  min: number
  max: number
  /** Valor al tocar "+" estando vacío. */
  porDefecto: number
  paso?: number
  onCambiar: (valor: number | null) => void
}

/**
 * Número con botones − / + de 48 px: más rápido y preciso que el teclado con una mano.
 * Bajar del mínimo deja el campo vacío (objetivo opcional).
 */
export function ContadorField({ etiqueta, valor, min, max, porDefecto, paso = 1, onCambiar }: ContadorFieldProps) {
  const idEtiqueta = useId()

  const restar = () => onCambiar(valor === null || valor - paso < min ? null : valor - paso)
  const sumar = () => onCambiar(valor === null ? porDefecto : Math.min(max, valor + paso))

  return (
    <div role="group" aria-labelledby={idEtiqueta} className="flex flex-col gap-1">
      <span id={idEtiqueta} className="text-sm font-medium text-texto-suave">
        {etiqueta}
      </span>
      <div className="flex items-center justify-between gap-1 rounded-xl border border-zinc-800 bg-zinc-950/80">
        <IconButton icono={Minus} etiqueta={`Restar ${etiqueta.toLowerCase()}`} onClick={restar} disabled={valor === null} />
        <output aria-live="polite" className="cifra min-w-10 text-center text-2xl text-texto">
          {valor ?? '–'}
        </output>
        <IconButton icono={Plus} etiqueta={`Sumar ${etiqueta.toLowerCase()}`} onClick={sumar} disabled={valor === max} />
      </div>
    </div>
  )
}
