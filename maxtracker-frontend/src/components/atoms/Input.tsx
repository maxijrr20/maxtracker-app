import type { InputHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  invalido?: boolean
}

/** Campo de texto base (48 px de alto). Para formularios usar la molécula FormField, que agrega label y error. */
export function Input({ invalido = false, className, ...resto }: InputProps) {
  return (
    <input
      aria-invalid={invalido || undefined}
      className={cx(
        // text-base (16 px) evita que iOS haga zoom al enfocar.
        'min-h-tactil w-full rounded-xl border bg-zinc-900/60 px-4 text-base text-texto backdrop-blur-xl',
        'placeholder:text-zinc-500 transition-all duration-200',
        'focus:border-acento focus:outline-none focus-visible:outline-none focus:ring-2 focus:ring-acento/40',
        'disabled:opacity-50',
        invalido ? 'border-peligro' : 'border-borde-vidrio hover:border-zinc-700',
        className,
      )}
      {...resto}
    />
  )
}
