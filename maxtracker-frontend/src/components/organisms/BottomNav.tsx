import { CircleUserRound, ClipboardList, Dumbbell, House } from 'lucide-react'

import { NavItem } from '../molecules/NavItem'

/**
 * Barra fija de navegación: inferior en móvil (al alcance del pulgar, respeta el área segura de iOS) y lateral desde
 * md, con la marca arriba. Hairline del lado del contenido y sombra direccional.
 */
export function BottomNav() {
  return (
    <nav
      aria-label="Navegación principal"
      className="fixed inset-x-0 bottom-0 z-20 border-t border-zinc-800/60 bg-zinc-950/60 pb-[env(safe-area-inset-bottom)] shadow-[0_-12px_28px_-14px_rgb(0_0_0/0.9)] backdrop-blur-2xl
        md:inset-y-0 md:left-0 md:right-auto md:w-56 md:border-r md:border-t-0 md:pb-0 md:shadow-elevada"
    >
      <div className="mx-auto flex max-w-lg items-stretch gap-1 px-2 md:h-full md:max-w-none md:flex-col md:gap-1 md:px-3 md:py-5">
        <p className="hidden items-center gap-2 px-3 pb-5 md:flex">
          <span className="grid size-8 place-items-center rounded-lg bg-linear-to-b from-lime-400 to-lime-500 text-sobre-acento">
            <Dumbbell aria-hidden className="size-4" strokeWidth={2.5} />
          </span>
          <span className="text-lg font-black tracking-tight">
            MAX<span className="text-zinc-500">TRACKER</span>
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
