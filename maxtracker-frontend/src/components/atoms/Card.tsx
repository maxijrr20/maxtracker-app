import type { HTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

type TonoCard = 'normal' | 'lima' | 'ia'

/**
 * Borde y sombra se eligen con `tono` (no con className): dos clases que fijan la misma propiedad no se resuelven
 * por el orden en que se escriben.
 */
const TONOS: Record<TonoCard, string> = {
  normal: 'border-borde-vidrio shadow-2xl shadow-black/40',
  lima: 'border-lime-400/30 shadow-neon',
  ia: 'border-sky-400/25 shadow-2xl shadow-black/40',
}

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  tono?: TonoCard
}

/** Tarjeta de vidrio: semitransparente con desenfoque, borde tenue y sombra profunda sobre el fondo degradado. */
export function Card({ tono = 'normal', className, ...resto }: CardProps) {
  return (
    <div
      className={cx('vidrio rounded-2xl border p-4', TONOS[tono], className)}
      {...resto}
    />
  )
}
