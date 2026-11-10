import { ListPlus } from 'lucide-react'

import type { RutinaEjercicio } from '../../api/types'
import { EjercicioCard } from '../molecules/EjercicioCard'
import { EmptyState } from '../molecules/EmptyState'

/** Ejercicios de una rutina en orden, con sus objetivos. */
export function RutinaEjercicioList({ ejercicios }: { ejercicios: RutinaEjercicio[] }) {
  if (ejercicios.length === 0) {
    return (
      <EmptyState
        icono={ListPlus}
        titulo="Rutina sin ejercicios"
        descripcion="Agrega ejercicios para usarla como plan de tus sesiones."
      />
    )
  }
  return (
    <ol className="flex flex-col gap-3">
      {ejercicios.map((ejercicio) => (
        <li key={`${ejercicio.orden}-${ejercicio.ejercicioId}`}>
          <EjercicioCard
            nombre={ejercicio.ejercicioNombre}
            categoria={ejercicio.categoria}
            orden={ejercicio.orden}
            seriesObjetivo={ejercicio.seriesObjetivo}
            repeticionesObjetivo={ejercicio.repeticionesObjetivo}
          />
        </li>
      ))}
    </ol>
  )
}
