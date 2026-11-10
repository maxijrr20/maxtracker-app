import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'

import { cx } from '../../lib/cx'

export interface NavItemProps {
  a: string
  icono: LucideIcon
  etiqueta: string
  /** Solo coincide con la ruta exacta (para "/"). */
  exacto?: boolean
}

/**
 * Ítem de navegación con ícono + texto siempre visible (los íconos solos son ambiguos).
 * Abajo en móvil (columna) y en la barra lateral desde md (fila). Marca la ruta actual con aria-current.
 */
export function NavItem({ a, icono: Icono, etiqueta, exacto = false }: NavItemProps) {
  return (
    <NavLink
      to={a}
      end={exacto}
      className={({ isActive }) =>
        cx(
          'flex min-h-14 min-w-tactil flex-1 flex-col items-center justify-center gap-1 rounded-xl px-2 text-xs font-medium',
          'transition-colors md:flex-none md:flex-row md:justify-start md:gap-3 md:px-4 md:text-base',
          isActive ? 'text-acento md:bg-superficie-alta' : 'text-texto-suave hover:text-texto',
        )
      }
    >
      {({ isActive }) => (
        <>
          <Icono aria-hidden className="size-6" strokeWidth={isActive ? 2.5 : 2} />
          <span>{etiqueta}</span>
        </>
      )}
    </NavLink>
  )
}
