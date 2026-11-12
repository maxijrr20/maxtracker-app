import type { InputHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  invalido?: boolean
}

/** Campo base (48 px). Para formularios usar la molécula FormField, que agrega label y error. */
export function Input({ invalido = false, className, ...resto }: InputProps) {
  return (
    <input
      aria-invalid={invalido || undefined}
      className={cx(
        // text-base (16 px) evita que iOS haga zoom al enfocar.
        'min-h-tactil w-full rounded-2xl border bg-casilla px-3 text-base text-texto placeholder:text-texto-tenue',
        // Halo cian tenue al enfocar (el :focus-visible global sigue marcando el foco de teclado).
        'transition-[border-color,box-shadow] duration-150 focus:outline-none focus:ring-1',
        'disabled:opacity-40',
        invalido
          ? 'border-peligro/60 focus:ring-peligro/50'
          : 'border-linea focus:border-acento/50 focus:ring-acento/50',
        className,
      )}
      {...resto}
    />
  )
}
