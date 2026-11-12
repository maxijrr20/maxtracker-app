import { Activity } from 'lucide-react'
import { Outlet } from 'react-router-dom'

/** Layout de /login y /register: columna centrada, sin navegación. */
export function AuthLayout() {
  return (
    <div className="flex min-h-dvh flex-col justify-center px-4 py-[max(2.5rem,env(safe-area-inset-top))]">
      <main className="mx-auto flex w-full max-w-sm flex-col gap-8">
        <div className="flex flex-col items-center gap-3 text-center">
          <span className="grid size-14 place-items-center rounded-full bg-linear-to-br from-acento to-acento-profundo text-sobre-acento shadow-primario">
            <Activity aria-hidden className="size-8" strokeWidth={2.25} />
          </span>
          <p className="text-3xl font-bold tracking-tight">
            Max<span className="font-medium text-texto-suave">Tracker</span>
          </p>
        </div>
        <Outlet />
      </main>
    </div>
  )
}
