import { Dumbbell, Plus } from 'lucide-react'
import { useState } from 'react'

export interface EjercicioOpcionProps {
  nombre: string
  /** Grupo muscular y/o músculos, ya formateado. */
  detalle?: string | null
  imagenUrl?: string | null
  onElegir: () => void
}

/** Fila de resultado del buscador: toda la fila es el botón (mín. 64 px de alto). */
export function EjercicioOpcion({ nombre, detalle, imagenUrl, onElegir }: EjercicioOpcionProps) {
  const [imagenRota, setImagenRota] = useState(false)
  const conImagen = Boolean(imagenUrl) && !imagenRota

  return (
    <button
      type="button"
      onClick={onElegir}
      className="flex min-h-16 w-full items-center gap-3 rounded-2xl border border-borde bg-superficie p-2 pr-3 text-left transition-colors hover:border-zinc-700 active:bg-superficie-alta"
    >
      <span className="grid size-12 shrink-0 place-items-center overflow-hidden rounded-xl bg-white">
        {conImagen ? (
          <img
            src={imagenUrl ?? undefined}
            alt=""
            loading="lazy"
            decoding="async"
            onError={() => setImagenRota(true)}
            className="size-full object-contain"
          />
        ) : (
          <span className="grid size-full place-items-center bg-superficie-alta text-texto-suave">
            <Dumbbell aria-hidden className="size-6" />
          </span>
        )}
      </span>
      <span className="flex min-w-0 flex-1 flex-col">
        <span className="break-words font-semibold">{nombre}</span>
        {detalle && <span className="truncate text-sm text-texto-suave">{detalle}</span>}
      </span>
      <Plus aria-hidden className="size-6 shrink-0 text-acento" />
    </button>
  )
}
