import { CircleUserRound, ClipboardList, Dumbbell, House } from 'lucide-react'

import { NavItem } from '../molecules/NavItem'

/**
 * Navegación principal de vidrio. En móvil es una barra fija abajo (al alcance del pulgar, respeta el área segura
 * de iOS); desde md pasa a ser una barra lateral con la marca arriba.
 */
export function BottomNav() {
  return (
    <nav
      aria-label="Navegación principal"
      className="fixed inset-x-0 bottom-0 z-20 border-t border-borde-vidrio bg-vidrio-barra pb-[env(safe-area-inset-bottom)] shadow-[0_-12px_32px_rgb(0_0_0/0.45)] backdrop-blur-xl
        md:inset-y-0 md:left-0 md:right-auto md:w-60 md:border-r md:border-t-0 md:pb-0 md:shadow-none"
    >
      <div className="mx-auto flex max-w-lg items-stretch gap-1 px-2 py-1 md:h-full md:max-w-none md:flex-col md:gap-1.5 md:px-3 md:py-6">
        <p className="hidden items-center gap-2 px-4 pb-6 text-xl font-bold tracking-tight md:flex">
          <span className="grid size-9 place-items-center rounded-xl bg-acento text-sobre-acento shadow-neon">
            <Dumbbell aria-hidden className="size-5" strokeWidth={2.25} />
          </span>
          <span>
            Max<span className="text-acento">Tracker</span>
          </span>
        </p>
        <NavItem a="/" exacto icono={House} etiqueta="Inicio" />
        <NavItem a="/rutinas" icono={ClipboardList} etiqueta="Rutinas" />
        <NavItem a="/entrenamiento" icono={Dumbbell} etiqueta="Entrenar" />
        <NavItem a="/perfil" icono={CircleUserRound} etiqueta="Perfil" />
      </div>
    </nav>
  )
}
