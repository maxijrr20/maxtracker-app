import { CircleCheck, Dumbbell } from 'lucide-react'
import type { ReactNode } from 'react'

import { cx } from '../../lib/cx'
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
  if (series && reps) return `${series}×${reps}`
  if (series) return `${series} series`
  if (reps) return `${reps} reps`
  return null
}

/**
 * Ejercicio con su objetivo; se usa en el detalle de rutina y en la sesión de entrenamiento. El orden va en cifra
 * monoespaciada grande y, si hay objetivo de series, una barra de avance con resplandor al completarse.
 */
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
  const conAvance = seriesHechas !== undefined && Boolean(seriesObjetivo)
  const completo = conAvance && seriesHechas! >= seriesObjetivo!
  const porcentaje = conAvance ? Math.min(100, Math.round((seriesHechas! / seriesObjetivo!) * 100)) : 0

  return (
    <Card tono={completo ? 'lima' : 'normal'} className="flex flex-col gap-4">
      <div className="flex items-start gap-3">
        <span className="grid size-tactil shrink-0 place-items-center rounded-xl border border-lime-500/20 bg-lime-500/10 font-mono text-xl font-bold text-acento">
          {orden !== undefined ? String(orden).padStart(2, '0') : <Dumbbell aria-hidden className="size-6" />}
        </span>
        <div className="flex min-w-0 flex-1 flex-col gap-1.5">
          {categoria && (
            <span className="text-xs font-semibold uppercase tracking-widest text-texto-suave">{categoria}</span>
          )}
          <Titulo nivel={3} className="break-words leading-tight">
            {nombre}
          </Titulo>
          {objetivo && (
            <Badge tono="lima" mono className="self-start">
              Objetivo {objetivo}
            </Badge>
          )}
        </div>
      </div>

      {conAvance && (
        <div className="flex items-center gap-3">
          <div aria-hidden className="h-1.5 flex-1 overflow-hidden rounded-full bg-zinc-800">
            <div
              className={cx(
                'h-full rounded-full transition-all duration-500',
                completo ? 'bg-acento shadow-neon' : 'bg-lime-500/70',
              )}
              style={{ width: `${porcentaje}%` }}
            />
          </div>
          <span className={cx('flex items-center gap-1 font-mono text-sm font-bold tabular-nums', completo ? 'text-acento' : 'text-texto-suave')}>
            {completo && <CircleCheck aria-hidden className="size-4" />}
            {seriesHechas}/{seriesObjetivo}
            <span className="sr-only"> series hechas</span>
          </span>
        </div>
      )}

      {children}
    </Card>
  )
}
