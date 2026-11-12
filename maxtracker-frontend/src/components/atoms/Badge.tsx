import type { ReactNode } from 'react'

import { usePeso } from '../../hooks/usePeso'
import { cx } from '../../lib/cx'

type Tono = 'neutro' | 'acento' | 'record' | 'exito' | 'alerta'

const TONOS: Record<Tono, string> = {
  neutro: 'bg-white/[0.06] text-texto-medio',
  // Cian: lo vivo o urgente (RIR ≤ 1, "en curso").
  acento: 'bg-acento/12 text-acento-claro ring-1 ring-inset ring-acento/30',
  // Coral: solo récords personales.
  record: 'bg-record/12 text-record ring-1 ring-inset ring-record/30',
  exito: 'bg-exito/10 text-exito ring-1 ring-inset ring-exito/20',
  alerta: 'bg-alerta/10 text-alerta ring-1 ring-inset ring-alerta/20',
}

export interface BadgeProps {
  tono?: Tono
  /** Cifras (peso, RIR, objetivos) en monoespaciada. */
  mono?: boolean
  children: ReactNode
  className?: string
}

/** Píldora minimalista, informativa (no interactiva). */
export function Badge({ tono = 'neutro', mono = false, children, className }: BadgeProps) {
  return (
    <span
      className={cx(
        'inline-flex items-center gap-1 whitespace-nowrap rounded-full px-2 py-0.5 text-xs font-bold tabular-nums',
        mono ? 'font-mono' : 'font-sans',
        TONOS[tono],
        className,
      )}
    >
      {children}
    </span>
  )
}

/** Peso en la unidad del usuario (los datos están siempre en kg). Neutro: el peso común no es un punto de foco. */
export function PesoBadge({ kg }: { kg: number }) {
  const { formato } = usePeso()
  return (
    <Badge mono>
      {formato(kg)}
    </Badge>
  )
}

/** Umbral de "RIR bajo" (al fallo o a una repetición): único caso en que el RIR se enciende en cian. */
const RIR_BAJO = 1

/** RIR = repeticiones en reserva (0 = al fallo). */
export function RirBadge({ rir }: { rir: number }) {
  return (
    <Badge tono={rir <= RIR_BAJO ? 'acento' : 'neutro'} mono className={rir >= 4 ? 'text-texto-suave' : undefined}>
      <abbr title="Repeticiones en reserva" className="no-underline">
        RIR
      </abbr>{' '}
      {rir}
    </Badge>
  )
}
