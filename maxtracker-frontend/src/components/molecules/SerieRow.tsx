import { Trash2 } from 'lucide-react'

import type { Serie } from '../../api/types'
import { PesoBadge, RirBadge } from '../atoms/Badge'
import { IconButton } from '../atoms/IconButton'

export interface SerieRowProps {
  serie: Serie
  /** Si se indica, muestra el botón para quitar la serie. */
  onEliminar?: (serie: Serie) => void
  deshabilitada?: boolean
}

/**
 * Una serie registrada como fila estilizada: número grande en monoespaciada (se lee a un brazo de distancia),
 * peso y RIR en píldoras lima, repeticiones en cifra fuerte.
 */
export function SerieRow({ serie, onEliminar, deshabilitada = false }: SerieRowProps) {
  return (
    <li className="group flex min-h-14 items-center gap-3 rounded-xl border border-white/5 bg-linear-to-r from-white/[0.04] to-transparent py-1.5 pl-3 pr-1 transition-all duration-200 hover:border-lime-500/20">
      <span className="flex w-12 shrink-0 flex-col items-center gap-0.5 leading-none">
        <span className="text-xs font-semibold uppercase tracking-wider text-texto-suave">Serie</span>
        <span className="font-mono text-2xl font-bold tabular-nums text-acento drop-shadow-neon">
          {String(serie.numeroSerie).padStart(2, '0')}
        </span>
      </span>
      <div className="flex flex-1 flex-wrap items-center gap-2">
        <PesoBadge kg={serie.pesoKg} />
        <span className="font-mono text-base font-bold tabular-nums text-texto">
          <span aria-hidden className="text-texto-suave">
            ×{' '}
          </span>
          {serie.repeticiones}
          <span className="ml-1 font-sans text-sm font-medium text-texto-suave">reps</span>
        </span>
        {serie.rir !== null && <RirBadge rir={serie.rir} />}
      </div>
      {onEliminar && (
        <IconButton
          icono={Trash2}
          etiqueta={`Eliminar serie ${serie.numeroSerie}`}
          variante="peligro"
          disabled={deshabilitada}
          onClick={() => onEliminar(serie)}
          className="opacity-70 group-hover:opacity-100 focus-visible:opacity-100"
        />
      )}
    </li>
  )
}
