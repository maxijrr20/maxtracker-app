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
 * Ítem de navegación con ícono + texto siempre visible. La ruta activa se marca con color lima, ícono con
 * resplandor y un indicador luminoso (arriba en la barra inferior, a la izquierda en la lateral); además
 * aria-current, así no depende solo del color.
 */
export function NavItem({ a, icono: Icono, etiqueta, exacto = false }: NavItemProps) {
  return (
    <NavLink
      to={a}
      end={exacto}
      className={({ isActive }) =>
        cx(
          'group relative flex min-h-14 min-w-tactil flex-1 flex-col items-center justify-center gap-1 rounded-xl px-2',
          'text-xs font-medium transition-all duration-200 active:scale-95',
          'md:flex-none md:flex-row md:justify-start md:gap-3 md:px-4 md:text-base',
          isActive
            ? 'text-acento md:bg-lime-500/10'
            : 'text-texto-suave hover:text-texto md:hover:bg-white/5',
        )
      }
    >
      {({ isActive }) => (
        <>
          {/* Indicador de ruta activa. */}
          <span
            aria-hidden
            className={cx(
              'absolute rounded-full bg-acento shadow-neon-fuerte transition-all duration-200',
              'top-0 left-1/2 h-0.5 -translate-x-1/2 md:top-1/2 md:left-0 md:h-6 md:w-1 md:-translate-x-0 md:-translate-y-1/2',
              isActive ? 'w-8 opacity-100' : 'w-0 opacity-0',
            )}
          />
          <Icono
            aria-hidden
            className={cx('size-6 transition-all duration-200', isActive && 'drop-shadow-neon')}
            strokeWidth={isActive ? 2.25 : 1.75}
          />
          <span className={cx(isActive && 'font-semibold')}>{etiqueta}</span>
        </>
      )}
    </NavLink>
  )
}
