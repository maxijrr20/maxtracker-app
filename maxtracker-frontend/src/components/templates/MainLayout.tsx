import { Dumbbell } from 'lucide-react'
import { Link, Outlet } from 'react-router-dom'

import { useAuth } from '../../auth/authContext'
import { Avatar } from '../atoms/Avatar'
import { BottomNav } from '../organisms/BottomNav'

/**
 * Layout de las vistas autenticadas: cabecera de vidrio, contenido y navegación (inferior en móvil, lateral desde
 * md). El avatar lleva al perfil. El padding inferior deja espacio para la barra fija y el área segura de iOS.
 */
export function MainLayout() {
  const { email } = useAuth()

  return (
    <div className="min-h-dvh md:pl-60">
      <header className="sticky top-0 z-10 border-b border-borde-vidrio bg-vidrio-barra pt-[env(safe-area-inset-top)] backdrop-blur-xl">
        <div className="mx-auto flex max-w-2xl items-center justify-between gap-3 px-4 py-1">
          {/* La marca ya está en la barra lateral desde md. */}
          <Link to="/" className="flex min-h-tactil items-center gap-2 text-lg font-bold tracking-tight md:invisible">
            <span className="grid size-8 place-items-center rounded-lg bg-acento text-sobre-acento shadow-neon">
              <Dumbbell aria-hidden className="size-4" strokeWidth={2.5} />
            </span>
            <span>
              Max<span className="text-acento">Tracker</span>
            </span>
          </Link>
          <Link
            to="/perfil"
            className="flex min-h-tactil min-w-tactil items-center justify-end gap-3 rounded-xl pl-2 transition-all duration-200 hover:opacity-90 active:scale-95"
          >
            <span className="hidden max-w-48 truncate text-sm text-texto-suave sm:inline">{email}</span>
            <Avatar email={email} />
            <span className="sr-only">Perfil y ajustes</span>
          </Link>
        </div>
      </header>

      <main className="mx-auto flex max-w-2xl flex-col gap-6 px-4 pb-[calc(6rem+env(safe-area-inset-bottom))] pt-6 md:pb-10">
        <Outlet />
      </main>

      <BottomNav />
    </div>
  )
}
