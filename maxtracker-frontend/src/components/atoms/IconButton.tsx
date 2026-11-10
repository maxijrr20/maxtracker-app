import type { LucideIcon } from 'lucide-react'
import type { ButtonHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

export interface IconButtonProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'aria-label'> {
  icono: LucideIcon
  /** Obligatoria: el botón no tiene texto visible. */
  etiqueta: string
  variante?: 'fantasma' | 'secundario' | 'peligro'
}

const VARIANTES = {
  fantasma: 'text-texto-suave hover:bg-white/5 hover:text-texto',
  secundario: 'border border-borde-vidrio bg-zinc-800/70 text-texto hover:bg-zinc-700/70',
  peligro: 'text-peligro hover:bg-red-500/15',
} as const

/** Botón de solo ícono, cuadrado de 48x48 px aunque el ícono sea de 20-24 px. */
export function IconButton({ icono: Icono, etiqueta, variante = 'fantasma', className, type = 'button', ...resto }: IconButtonProps) {
  return (
    <button
      type={type}
      aria-label={etiqueta}
      title={etiqueta}
      className={cx(
        'inline-grid size-tactil shrink-0 place-items-center rounded-xl transition-all duration-200 active:scale-95',
        'disabled:cursor-not-allowed disabled:opacity-50',
        VARIANTES[variante],
        className,
      )}
      {...resto}
    >
      <Icono aria-hidden className="size-6" />
    </button>
  )
}
