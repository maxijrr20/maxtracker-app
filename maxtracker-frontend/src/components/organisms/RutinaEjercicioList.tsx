import { ListPlus } from 'lucide-react'

import type { RutinaEjercicio } from '../../api/types'
import { cx } from '../../lib/cx'
import { claseGrupo } from '../../lib/gruposMusculares'
import { Badge } from '../atoms/Badge'
import { EmptyState } from '../molecules/EmptyState'

function textoObjetivo(series: number | null, reps: number | null): string | null {
  if (series && reps) return `${series}×${reps}`
  if (series) return `${series} series`
  if (reps) return `${reps} reps`
  return null
}

/**
 * Ejercicios de una rutina como lista densa en una sola superficie: indicador vertical del grupo muscular,
 * orden en cifra mono, nombre y rótulo del grupo, objetivo en badge.
 */
export function RutinaEjercicioList({ ejercicios }: { ejercicios: RutinaEjercicio[] }) {
  if (ejercicios.length === 0) {
    return (
      <EmptyState
        icono={ListPlus}
        titulo="Rutina sin ejercicios"
        descripcion="Edítala para agregar ejercicios y usarla como plan de tus sesiones."
      />
    )
  }
  return (
    <ol className="superficie borde-degradado flex flex-col divide-y divide-zinc-800/60 px-3 py-1">
      {ejercicios.map((ejercicio) => {
        const objetivo = textoObjetivo(ejercicio.seriesObjetivo, ejercicio.repeticionesObjetivo)
        return (
          <li key={`${ejercicio.orden}-${ejercicio.ejercicioId}`} className="relative flex min-h-14 items-center gap-3 py-2 pl-3">
            <span aria-hidden className={cx('absolute inset-y-3 left-0 w-1 rounded-full', claseGrupo(ejercicio.categoria))} />
            <span className="cifra w-6 shrink-0 text-lg text-zinc-500">{String(ejercicio.orden).padStart(2, '0')}</span>
            <div className="flex min-w-0 flex-1 flex-col">
              <span className="truncate text-base font-bold leading-tight">{ejercicio.ejercicioNombre}</span>
              <span className="rotulo truncate">{ejercicio.categoria ?? 'Ejercicio'}</span>
            </div>
            {objetivo && (
              <Badge mono>
                <span className="sr-only">Objetivo </span>
                {objetivo}
              </Badge>
            )}
          </li>
        )
      })}
    </ol>
  )
}
