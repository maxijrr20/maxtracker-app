import type { ButtonHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

export interface ChipProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'aria-pressed'> {
  activo: boolean
}

/**
 * Filtro alternable (p. ej. grupo muscular). 48 px de alto; el estado se anuncia con aria-pressed.
 * Activo en blanco (no lima: un filtro no es un punto de foco).
 */
export function Chip({ activo, className, type = 'button', ...resto }: ChipProps) {
  return (
    <button
      type={type}
      aria-pressed={activo}
      className={cx(
        'inline-flex min-h-tactil shrink-0 items-center rounded-full px-4 text-sm font-semibold',
        'transition-[transform,background-color,color] duration-150 active:scale-[0.98]',
        activo
          ? 'bg-zinc-100 text-zinc-950'
          : 'bg-white/[0.04] text-texto-suave ring-1 ring-inset ring-white/10 hover:bg-white/[0.08] hover:text-texto',
        className,
      )}
      {...resto}
    />
  )
}
