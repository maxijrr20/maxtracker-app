import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

export interface EmptyStateProps {
  icono: LucideIcon
  titulo: string
  descripcion?: string
  accion?: ReactNode
}

export function EmptyState({ icono: Icono, titulo, descripcion, accion }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-4 rounded-2xl border border-dashed border-borde px-6 py-10 text-center">
      <span className="grid size-14 place-items-center rounded-2xl bg-superficie-alta text-acento">
        <Icono aria-hidden className="size-7" />
      </span>
      <div className="flex flex-col gap-1">
        <h2 className="text-lg font-semibold">{titulo}</h2>
        {descripcion && <p className="text-texto-suave">{descripcion}</p>}
      </div>
      {accion}
    </div>
  )
}
