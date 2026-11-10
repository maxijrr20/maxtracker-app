import type { HTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

type TonoCard = 'normal' | 'foco'

/**
 * El borde se elige con `tono` (no con className): dos clases que fijan la misma propiedad no se resuelven por el
 * orden en que se escriben. `foco` = superficie "encendida" (sesión en curso, PR): una por pantalla como mucho.
 */
const TONOS: Record<TonoCard, string> = {
  normal: 'borde-degradado',
  foco: 'borde-degradado-acento',
}

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  tono?: TonoCard
}

/** Superficie elevada: translúcida con blur, micro-borde en degradado y sombra direccional. Relleno compacto. */
export function Card({ tono = 'normal', className, ...resto }: CardProps) {
  return <div className={cx('superficie p-3', TONOS[tono], className)} {...resto} />
}
