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
 * Ítem de navegación: ícono + texto siempre visibles y aria-current. Activo: el ícono se apoya en una píldora cian
 * tenue (en móvil) o toda la fila se tiñe (desde md); inactivo en texto-suave.
 */
export function NavItem({ a, icono: Icono, etiqueta, exacto = false }: NavItemProps) {
  return (
    <NavLink
      to={a}
      end={exacto}
      className={({ isActive }) =>
        cx(
          'flex min-h-14 min-w-tactil flex-1 flex-col items-center justify-center gap-0.5 rounded-2xl px-2',
          'text-xs font-semibold transition-colors duration-150',
          'md:min-h-tactil md:flex-none md:flex-row md:justify-start md:gap-3 md:rounded-full md:px-3 md:text-sm',
          isActive ? 'text-acento-claro md:bg-acento/12' : 'text-texto-suave hover:text-texto',
        )
      }
    >
      {({ isActive }) => (
        <>
          <span
            className={cx(
              'grid h-8 w-14 place-items-center rounded-full transition-colors duration-150 md:size-auto',
              isActive ? 'bg-acento/15 md:bg-transparent' : 'bg-transparent',
            )}
          >
            <Icono aria-hidden className="size-6 md:size-5" strokeWidth={isActive ? 2.25 : 1.75} />
          </span>
          <span>{etiqueta}</span>
        </>
      )}
    </NavLink>
  )
}
