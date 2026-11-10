import type { ReactNode } from 'react'

import { usePeso } from '../../hooks/usePeso'
import { cx } from '../../lib/cx'

type Tono = 'neutro' | 'lima' | 'exito' | 'alerta'

const TONOS: Record<Tono, string> = {
  neutro: 'border-zinc-700/60 bg-zinc-800/70 text-zinc-200',
  // Datos de entrenamiento (peso, RIR, objetivo): lima semitransparente.
  lima: 'border-lime-500/20 bg-lime-500/10 text-lime-400',
  exito: 'border-emerald-400/20 bg-emerald-400/10 text-emerald-300',
  alerta: 'border-amber-400/20 bg-amber-400/10 text-amber-200',
}

export interface BadgeProps {
  tono?: Tono
  /** Monoespaciada, para cifras (peso, RIR, series). */
  mono?: boolean
  children: ReactNode
  className?: string
}

/** Etiqueta compacta e informativa (no interactiva) en forma de píldora. */
export function Badge({ tono = 'neutro', mono = false, children, className }: BadgeProps) {
  return (
    <span
      className={cx(
        'inline-flex items-center gap-1 rounded-full border px-3 py-1 text-sm font-semibold tabular-nums',
        mono && 'font-mono',
        TONOS[tono],
        className,
      )}
    >
      {children}
    </span>
  )
}

/** Peso en la unidad que eligió el usuario (los datos están siempre en kg). */
export function PesoBadge({ kg }: { kg: number }) {
  const { formato } = usePeso()
  return (
    <Badge tono="lima" mono>
      {formato(kg)}
    </Badge>
  )
}

/** RIR = repeticiones en reserva (0 = al fallo). */
export function RirBadge({ rir }: { rir: number }) {
  return (
    <Badge tono="lima" mono>
      <abbr title="Repeticiones en reserva" className="no-underline">
        RIR
      </abbr>{' '}
      {rir}
    </Badge>
  )
}
