import { ChevronDown } from 'lucide-react'
import type { SelectHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

/**
 * Lista desplegable nativa (en móvil abre el selector del sistema). 48 px, mismo aspecto que Input.
 * Necesita etiqueta: usar con <label> o aria-label.
 */
export function Select({ className, children, ...resto }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <div className="relative">
      <select
        className={cx(
          'min-h-tactil w-full appearance-none rounded-xl border border-zinc-800 bg-zinc-950/80 py-2 pl-3 pr-10 text-base text-texto',
          'transition-[border-color,box-shadow] duration-150 focus:border-lime-500/50 focus:outline-none focus:ring-1 focus:ring-lime-500/50',
          className,
        )}
        {...resto}
      >
        {children}
      </select>
      <ChevronDown aria-hidden className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2 text-zinc-500" />
    </div>
  )
}
