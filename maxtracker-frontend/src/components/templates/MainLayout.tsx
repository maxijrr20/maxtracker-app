import { Activity } from 'lucide-react'
import { Link, Outlet } from 'react-router-dom'

import { useAuth } from '../../auth/authContext'
import { Avatar } from '../atoms/Avatar'
import { BottomNav } from '../organisms/BottomNav'

/**
 * Layout de las vistas autenticadas: cabecera fija compacta (marca + avatar → /perfil), contenido denso y
 * navegación (inferior en móvil, lateral desde md). El padding inferior deja espacio para la barra y el área segura.
 */
export function MainLayout() {
  const { email } = useAuth()

  return (
    <div className="min-h-dvh md:pl-56">
      <header className="sticky top-0 z-10 border-b border-linea-suave bg-fondo/80 pt-[env(safe-area-inset-top)] shadow-[0_12px_28px_-18px_rgb(0_0_0/0.9)] backdrop-blur-2xl">
        <div className="mx-auto flex max-w-2xl items-center justify-between gap-3 px-4">
          {/* La marca ya está en la barra lateral desde md. */}
          <Link to="/" className="flex min-h-tactil items-center gap-2 md:invisible">
            <span className="grid size-7 place-items-center rounded-full bg-linear-to-br from-acento to-acento-profundo text-sobre-acento">
              <Activity aria-hidden className="size-4" strokeWidth={2.5} />
            </span>
            <span className="text-base font-bold tracking-tight">
              Max<span className="font-medium text-texto-suave">Tracker</span>
            </span>
          </Link>
          <Link
            to="/perfil"
            className="flex min-h-tactil min-w-tactil items-center justify-end gap-3 rounded-2xl transition-transform duration-150 active:scale-[0.98]"
          >
            <span className="hidden max-w-48 truncate text-sm text-texto-suave sm:inline">{email}</span>
            <Avatar email={email} />
            <span className="sr-only">Perfil y ajustes</span>
          </Link>
        </div>
      </header>

      <main className="mx-auto flex max-w-2xl flex-col gap-4 px-4 pb-[calc(5.5rem+env(safe-area-inset-bottom))] pt-4 md:pb-8">
        <Outlet />
      </main>

      <BottomNav />
    </div>
  )
}
