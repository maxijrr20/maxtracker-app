import { LogOut } from 'lucide-react'
import { Outlet } from 'react-router-dom'

import { useAuth } from '../../auth/authContext'
import { IconButton } from '../atoms/IconButton'
import { BottomNav } from '../organisms/BottomNav'

/**
 * Layout de las vistas autenticadas: cabecera, contenido y navegación inferior (lateral desde md).
 * El padding inferior deja espacio para la barra fija y el área segura de iOS.
 */
export function MainLayout() {
  const { email, cerrarSesion } = useAuth()

  return (
    <div className="min-h-dvh md:pl-60">
      <header className="sticky top-0 z-10 border-b border-borde bg-fondo/95 pt-[env(safe-area-inset-top)] backdrop-blur">
        <div className="mx-auto flex max-w-2xl items-center justify-between gap-3 px-4 py-1">
          <p className="text-lg font-bold tracking-tight md:invisible">
            Max<span className="text-acento">Tracker</span>
          </p>
          <div className="flex min-w-0 items-center gap-1">
            <span className="hidden truncate text-sm text-texto-suave sm:inline">{email}</span>
            <IconButton icono={LogOut} etiqueta="Cerrar sesión" onClick={cerrarSesion} />
          </div>
        </div>
      </header>

      <main className="mx-auto flex max-w-2xl flex-col gap-6 px-4 pb-[calc(6rem+env(safe-area-inset-bottom))] pt-6 md:pb-10">
        <Outlet />
      </main>

      <BottomNav />
    </div>
  )
}
