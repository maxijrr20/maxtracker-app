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
 * Ítem de navegación: ícono + texto siempre visibles. Activo en lima (punto de foco) con un indicador de 2 px
 * (arriba en móvil, a la izquierda desde md) y aria-current; inactivo en zinc-500.
 */
export function NavItem({ a, icono: Icono, etiqueta, exacto = false }: NavItemProps) {
  return (
    <NavLink
      to={a}
      end={exacto}
      className={({ isActive }) =>
        cx(
          'relative flex min-h-14 min-w-tactil flex-1 flex-col items-center justify-center gap-1 rounded-xl px-2',
          'text-xs font-semibold transition-colors duration-150',
          'md:min-h-tactil md:flex-none md:flex-row md:justify-start md:gap-3 md:px-3 md:text-sm',
          isActive ? 'text-acento md:bg-white/[0.04]' : 'text-zinc-500 hover:text-texto',
        )
      }
    >
      {({ isActive }) => (
        <>
          <span
            aria-hidden
            className={cx(
              'absolute rounded-full bg-acento transition-opacity duration-150',
              'left-1/2 top-0 h-0.5 w-6 -translate-x-1/2',
              'md:left-0 md:top-1/2 md:h-5 md:w-0.5 md:translate-x-0 md:-translate-y-1/2',
              isActive ? 'opacity-100' : 'opacity-0',
            )}
          />
          <Icono aria-hidden className="size-6 md:size-5" strokeWidth={isActive ? 2.25 : 1.75} />
          <span>{etiqueta}</span>
        </>
      )}
    </NavLink>
  )
}
