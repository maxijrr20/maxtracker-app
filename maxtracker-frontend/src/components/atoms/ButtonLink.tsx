import type { LucideIcon } from 'lucide-react'
import { Link, type LinkProps } from 'react-router-dom'

import { cx } from '../../lib/cx'
import { clasesBoton, type TamanoBoton, type VarianteBoton } from './botonEstilos'

export interface ButtonLinkProps extends LinkProps {
  variante?: VarianteBoton
  tamano?: TamanoBoton
  icono?: LucideIcon
  anchoCompleto?: boolean
}

/** Navegación con aspecto de botón (semánticamente sigue siendo un enlace). */
export function ButtonLink({
  variante = 'primario',
  tamano = 'md',
  icono: Icono,
  anchoCompleto = false,
  className,
  children,
  ...resto
}: ButtonLinkProps) {
  return (
    <Link className={cx(clasesBoton(variante, tamano, anchoCompleto), className as string | undefined)} {...resto}>
      {Icono && <Icono aria-hidden className="size-5" strokeWidth={2.25} />}
      {children}
    </Link>
  )
}
