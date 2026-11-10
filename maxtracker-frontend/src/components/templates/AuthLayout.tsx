import { Dumbbell } from 'lucide-react'
import { Outlet } from 'react-router-dom'

/** Layout de /login y /register: columna centrada, sin navegación. */
export function AuthLayout() {
  return (
    <div className="flex min-h-dvh flex-col justify-center px-4 py-[max(2.5rem,env(safe-area-inset-top))]">
      <main className="mx-auto flex w-full max-w-sm flex-col gap-8">
        <div className="flex flex-col items-center gap-3 text-center">
          <span className="grid size-14 place-items-center rounded-2xl bg-linear-to-b from-lime-400 to-lime-500 text-sobre-acento shadow-primario">
            <Dumbbell aria-hidden className="size-9" strokeWidth={2.25} />
          </span>
          <p className="text-3xl font-black tracking-tight">
            MAX<span className="text-zinc-500">TRACKER</span>
          </p>
        </div>
        <Outlet />
      </main>
    </div>
  )
}
