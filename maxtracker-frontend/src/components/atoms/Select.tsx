import { ChevronDown } from 'lucide-react'
import type { SelectHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

/**
 * Lista desplegable nativa (en móvil abre el selector del sistema, cómodo con una mano). 48 px de alto,
 * mismo aspecto que Input. Necesita etiqueta: usar con <label> o aria-label.
 */
export function Select({ className, children, ...resto }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <div className="relative">
      <select
        className={cx(
          'min-h-tactil w-full appearance-none rounded-xl border border-borde bg-superficie py-2 pl-4 pr-11 text-base text-texto',
          'focus:border-acento focus:outline-none focus-visible:outline-none focus:ring-2 focus:ring-acento/40',
          className,
        )}
        {...resto}
      >
        {children}
      </select>
      <ChevronDown aria-hidden className="pointer-events-none absolute right-4 top-1/2 size-5 -translate-y-1/2 text-texto-suave" />
    </div>
  )
}
