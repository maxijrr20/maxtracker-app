import { Dumbbell, Plus } from 'lucide-react'
import { useState } from 'react'

import { cx } from '../../lib/cx'
import { claseGrupo } from '../../lib/gruposMusculares'

export interface EjercicioOpcionProps {
  nombre: string
  /** Grupo muscular y/o músculos, ya formateado. */
  detalle?: string | null
  /** Grupo muscular (categoría) para el indicador de color. */
  categoria?: string | null
  imagenUrl?: string | null
  onElegir: () => void
}

/** Fila de resultado del buscador (va dentro de una lista con divisores, no como tarjeta suelta). */
export function EjercicioOpcion({ nombre, detalle, categoria, imagenUrl, onElegir }: EjercicioOpcionProps) {
  const [imagenRota, setImagenRota] = useState(false)
  const conImagen = Boolean(imagenUrl) && !imagenRota

  return (
    <button
      type="button"
      onClick={onElegir}
      className="relative flex min-h-14 w-full items-center gap-3 rounded-2xl py-1.5 pl-3 pr-1 text-left transition-colors duration-150 hover:bg-white/[0.03] active:bg-white/[0.05]"
    >
      <span aria-hidden className={cx('absolute inset-y-2.5 left-0 w-0.5 rounded-full', claseGrupo(categoria))} />
      <span className="grid size-10 shrink-0 place-items-center overflow-hidden rounded-xl bg-white">
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
          <span className="grid size-full place-items-center bg-linea text-texto-suave">
            <Dumbbell aria-hidden className="size-5" />
          </span>
        )}
      </span>
      <span className="flex min-w-0 flex-1 flex-col">
        <span className="truncate font-semibold">{nombre}</span>
        {detalle && <span className="truncate text-xs text-texto-suave">{detalle}</span>}
      </span>
      <span className="grid size-tactil shrink-0 place-items-center text-texto-suave">
        <Plus aria-hidden className="size-5" />
      </span>
    </button>
  )
}
