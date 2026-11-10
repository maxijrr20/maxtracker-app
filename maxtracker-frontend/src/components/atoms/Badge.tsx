import type { ReactNode } from 'react'

import { cx } from '../../lib/cx'

type Tono = 'neutro' | 'acento' | 'peso' | 'rir' | 'exito'

const TONOS: Record<Tono, string> = {
  neutro: 'bg-superficie-alta text-zinc-200',
  acento: 'bg-lime-400/15 text-lime-300',
  peso: 'bg-sky-400/15 text-sky-200',
  rir: 'bg-amber-400/15 text-amber-200',
  exito: 'bg-emerald-400/15 text-emerald-300',
}

/** Etiqueta compacta e informativa (no interactiva): peso, RIR, objetivos, estado. */
export function Badge({ tono = 'neutro', children, className }: { tono?: Tono; children: ReactNode; className?: string }) {
  return (
    <span
      className={cx(
        'inline-flex items-center gap-1 rounded-lg px-2.5 py-1 text-sm font-semibold tabular-nums',
        TONOS[tono],
        className,
      )}
    >
      {children}
    </span>
  )
}

const FORMATO_PESO = new Intl.NumberFormat('es', { maximumFractionDigits: 2 })

export function PesoBadge({ kg }: { kg: number }) {
  return <Badge tono="peso">{FORMATO_PESO.format(kg)} kg</Badge>
}

/** RIR = repeticiones en reserva (0 = al fallo). */
export function RirBadge({ rir }: { rir: number }) {
  return (
    <Badge tono="rir">
      <abbr title="Repeticiones en reserva" className="no-underline">
        RIR
      </abbr>{' '}
      {rir}
    </Badge>
  )
}
