import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

export interface EmptyStateProps {
  icono: LucideIcon
  titulo: string
  descripcion?: string
  accion?: ReactNode
}

/** Estado vacío compacto: ícono chico, una línea de título, la explicación y la siguiente acción. */
export function EmptyState({ icono: Icono, titulo, descripcion, accion }: EmptyStateProps) {
  return (
    <div className="flex flex-col gap-3 rounded-2xl bg-white/[0.03] p-3 ring-1 ring-inset ring-white/[0.06]">
      <div className="flex items-start gap-3">
        <span className="grid size-10 shrink-0 place-items-center rounded-2xl bg-white/[0.05] text-texto-suave">
          <Icono aria-hidden className="size-5" />
        </span>
        <div className="flex min-w-0 flex-col gap-0.5">
          <h2 className="text-base font-bold leading-tight">{titulo}</h2>
          {descripcion && <p className="text-sm text-texto-suave">{descripcion}</p>}
        </div>
      </div>
      {accion}
    </div>
  )
}
