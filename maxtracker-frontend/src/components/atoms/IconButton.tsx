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
  fantasma: 'text-texto-suave hover:bg-white/[0.04] hover:text-texto',
  secundario: 'bg-white/[0.05] text-texto ring-1 ring-inset ring-white/10 hover:bg-white/[0.09]',
  peligro: 'text-texto-suave hover:bg-peligro/10 hover:text-peligro',
} as const

/** Botón de solo ícono: 48x48 px aunque el ícono sea de 20-24 px. */
export function IconButton({ icono: Icono, etiqueta, variante = 'fantasma', className, type = 'button', ...resto }: IconButtonProps) {
  return (
    <button
      type={type}
      aria-label={etiqueta}
      title={etiqueta}
      className={cx(
        'inline-grid size-tactil shrink-0 place-items-center rounded-2xl',
        'transition-[transform,background-color,color] duration-150 active:scale-[0.98]',
        'disabled:cursor-not-allowed disabled:opacity-40',
        VARIANTES[variante],
        className,
      )}
      {...resto}
    >
      <Icono aria-hidden className="size-5" />
    </button>
  )
}
