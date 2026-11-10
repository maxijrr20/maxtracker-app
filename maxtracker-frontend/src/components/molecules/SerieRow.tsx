import { Trash2 } from 'lucide-react'

import type { Serie } from '../../api/types'
import { usePeso } from '../../hooks/usePeso'
import { RirBadge } from '../atoms/Badge'
import { IconButton } from '../atoms/IconButton'

export interface SerieRowProps {
  serie: Serie
  /** Si se indica, muestra el botón para quitar la serie. */
  onEliminar?: (serie: Serie) => void
  deshabilitada?: boolean
}

/**
 * Fila de serie tipo tablero: `01  82,5kg × 8reps  [RIR 1]  🗑`. Sin borde propio (va dentro de la superficie del
 * ejercicio, separada por hairlines). El peso es la cifra protagonista; el n.º identifica, en gris.
 */
export function SerieRow({ serie, onEliminar, deshabilitada = false }: SerieRowProps) {
  const { desdeKg, unidad } = usePeso()
  const peso = new Intl.NumberFormat('es', { maximumFractionDigits: unidad === 'kg' ? 2 : 1 }).format(desdeKg(serie.pesoKg))

  return (
    <li className="flex min-h-12 items-center gap-3">
      <span className="cifra w-7 shrink-0 text-xl text-zinc-500">
        <span className="sr-only">Serie </span>
        {String(serie.numeroSerie).padStart(2, '0')}
      </span>
      {/* A 360 px puede partirse en dos líneas en lugar de desbordar. */}
      <span className="flex min-w-0 flex-1 flex-wrap items-baseline gap-x-2">
        <span className="whitespace-nowrap">
          <span className="cifra text-2xl text-texto">{peso}</span>
          <span className="ml-0.5 text-xs font-semibold text-zinc-500">{unidad}</span>
        </span>
        <span aria-hidden className="text-zinc-600">
          ×
        </span>
        <span className="whitespace-nowrap">
          <span className="cifra text-xl text-texto">{serie.repeticiones}</span>
          <span className="ml-0.5 text-xs font-semibold text-zinc-500">reps</span>
        </span>
      </span>
      {serie.rir !== null && <RirBadge rir={serie.rir} />}
      {onEliminar && (
        <IconButton
          icono={Trash2}
          etiqueta={`Eliminar serie ${serie.numeroSerie}`}
          variante="peligro"
          disabled={deshabilitada}
          onClick={() => onEliminar(serie)}
          className="-mr-2"
        />
      )}
    </li>
  )
}
