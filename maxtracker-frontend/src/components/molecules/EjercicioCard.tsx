import { Dumbbell } from 'lucide-react'
import type { ReactNode } from 'react'

import { Badge } from '../atoms/Badge'
import { Card } from '../atoms/Card'
import { Titulo } from '../atoms/Typography'

export interface EjercicioCardProps {
  nombre: string
  categoria?: string | null
  orden?: number
  seriesObjetivo?: number | null
  repeticionesObjetivo?: number | null
  /** Series hechas en la sesión actual (para mostrar el avance contra el objetivo). */
  seriesHechas?: number
  /** Contenido extra: lista de series, formulario de registro, etc. */
  children?: ReactNode
}

function textoObjetivo(series?: number | null, reps?: number | null): string | null {
  if (series && reps) return `${series} × ${reps}`
  if (series) return `${series} series`
  if (reps) return `${reps} reps`
  return null
}

/** Ejercicio con su objetivo; se usa en el detalle de rutina y en la sesión de entrenamiento. */
export function EjercicioCard({
  nombre,
  categoria,
  orden,
  seriesObjetivo,
  repeticionesObjetivo,
  seriesHechas,
  children,
}: EjercicioCardProps) {
  const objetivo = textoObjetivo(seriesObjetivo, repeticionesObjetivo)
  const completo = seriesHechas !== undefined && seriesObjetivo ? seriesHechas >= seriesObjetivo : false

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-start gap-3">
        <span className="grid size-tactil shrink-0 place-items-center rounded-xl bg-superficie-alta font-bold text-acento">
          {orden ?? <Dumbbell aria-hidden className="size-6" />}
        </span>
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <Titulo nivel={3} className="break-words">
            {nombre}
          </Titulo>
          <div className="flex flex-wrap items-center gap-2">
            {categoria && <span className="text-sm text-texto-suave">{categoria}</span>}
            {objetivo && <Badge tono="acento">Objetivo {objetivo}</Badge>}
            {seriesHechas !== undefined && seriesObjetivo ? (
              <Badge tono={completo ? 'exito' : 'neutro'}>
                {seriesHechas}/{seriesObjetivo} series
              </Badge>
            ) : null}
          </div>
        </div>
      </div>
      {children}
    </Card>
  )
}
