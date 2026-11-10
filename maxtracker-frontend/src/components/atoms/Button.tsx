import { LoaderCircle, type LucideIcon } from 'lucide-react'
import type { ButtonHTMLAttributes } from 'react'

import { cx } from '../../lib/cx'
import { clasesBoton, type TamanoBoton, type VarianteBoton } from './botonEstilos'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBoton
  tamano?: TamanoBoton
  icono?: LucideIcon
  /** Deshabilita el botón y muestra un spinner en lugar del ícono. */
  cargando?: boolean
  anchoCompleto?: boolean
}

export function Button({
  variante = 'primario',
  tamano = 'md',
  icono: Icono,
  cargando = false,
  anchoCompleto = false,
  disabled,
  className,
  children,
  type = 'button',
  ...resto
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || cargando}
      aria-busy={cargando || undefined}
      className={cx(clasesBoton(variante, tamano, anchoCompleto), className)}
      {...resto}
    >
      {cargando ? (
        <LoaderCircle aria-hidden className="size-5 animate-spin" />
      ) : (
        Icono && <Icono aria-hidden className="size-5" strokeWidth={2.25} />
      )}
      {children}
    </button>
  )
}
