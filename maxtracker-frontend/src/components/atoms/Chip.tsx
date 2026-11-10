import type { ButtonHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

export interface ChipProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'aria-pressed'> {
  activo: boolean
}

/** Filtro alternable (p. ej. grupo muscular). 48 px de alto; el estado se anuncia con aria-pressed. */
export function Chip({ activo, className, type = 'button', ...resto }: ChipProps) {
  return (
    <button
      type={type}
      aria-pressed={activo}
      className={cx(
        'inline-flex min-h-tactil shrink-0 items-center rounded-full border px-4 text-sm font-semibold transition-all duration-200 active:scale-95',
        activo
          ? 'border-lime-300/70 bg-acento text-sobre-acento shadow-neon'
          : 'border-borde-vidrio bg-zinc-900/60 text-texto hover:border-zinc-700 hover:bg-zinc-800/70',
        className,
      )}
      {...resto}
    />
  )
}
