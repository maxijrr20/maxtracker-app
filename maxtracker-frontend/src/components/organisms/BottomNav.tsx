import { Dumbbell, House, ListChecks } from 'lucide-react'

import { NavItem } from '../molecules/NavItem'

/**
 * Navegación principal. En móvil es una barra fija abajo (al alcance del pulgar, respeta el área segura
 * de iOS); desde md pasa a ser una barra lateral.
 */
export function BottomNav() {
  return (
    <nav
      aria-label="Navegación principal"
      className="fixed inset-x-0 bottom-0 z-20 border-t border-borde bg-fondo/95 pb-[env(safe-area-inset-bottom)] backdrop-blur
        md:inset-y-0 md:left-0 md:right-auto md:w-60 md:border-r md:border-t-0 md:pb-0"
    >
      <div className="mx-auto flex max-w-lg items-stretch gap-1 px-2 py-1 md:h-full md:max-w-none md:flex-col md:gap-2 md:px-3 md:py-6">
        <p className="hidden px-4 pb-4 text-xl font-bold tracking-tight md:block">
          Max<span className="text-acento">Tracker</span>
        </p>
        <NavItem a="/" exacto icono={House} etiqueta="Inicio" />
        <NavItem a="/rutinas" icono={ListChecks} etiqueta="Rutinas" />
        <NavItem a="/entrenamiento" icono={Dumbbell} etiqueta="Entrenar" />
      </div>
    </nav>
  )
}
