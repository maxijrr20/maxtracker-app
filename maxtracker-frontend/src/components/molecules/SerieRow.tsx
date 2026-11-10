import { Trash2 } from 'lucide-react'

import type { Serie } from '../../api/types'
import { Badge, PesoBadge, RirBadge } from '../atoms/Badge'
import { IconButton } from '../atoms/IconButton'

export interface SerieRowProps {
  serie: Serie
  /** Si se indica, muestra el botón para quitar la serie. */
  onEliminar?: (serie: Serie) => void
  deshabilitada?: boolean
}

/** Una serie registrada: número, peso × repeticiones y RIR. */
export function SerieRow({ serie, onEliminar, deshabilitada = false }: SerieRowProps) {
  return (
    <li className="flex min-h-tactil items-center gap-3 py-1">
      <span
        aria-label={`Serie ${serie.numeroSerie}`}
        className="grid size-9 shrink-0 place-items-center rounded-lg bg-superficie-alta text-sm font-bold tabular-nums"
      >
        {serie.numeroSerie}
      </span>
      <div className="flex flex-1 flex-wrap items-center gap-2">
        <PesoBadge kg={serie.pesoKg} />
        <span aria-hidden className="text-texto-suave">
          ×
        </span>
        <Badge>{serie.repeticiones} reps</Badge>
        {serie.rir !== null && <RirBadge rir={serie.rir} />}
      </div>
      {onEliminar && (
        <IconButton
          icono={Trash2}
          etiqueta={`Eliminar serie ${serie.numeroSerie}`}
          variante="peligro"
          disabled={deshabilitada}
          onClick={() => onEliminar(serie)}
        />
      )}
    </li>
  )
}
