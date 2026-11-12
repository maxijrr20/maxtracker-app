import type { ReactNode } from 'react'

import { cx } from '../../lib/cx'
import { claseGrupo } from '../../lib/gruposMusculares'
import { Badge } from '../atoms/Badge'
import { Card } from '../atoms/Card'

export interface EjercicioCardProps {
  nombre: string
  categoria?: string | null
  orden?: number
  seriesObjetivo?: number | null
  repeticionesObjetivo?: number | null
  /** Series hechas en la sesión actual (para mostrar el avance contra el objetivo). */
  seriesHechas?: number
  /** La sesión está en curso: la barra de avance se enciende (estado vivo). */
  enCurso?: boolean
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
 * Ejercicio con encabezado compacto: indicador vertical del grupo muscular, rótulo (orden · grupo), título en una
 * línea, badges a la derecha y barra de avance de 2 px. Lo demás (series, formulario) va debajo de un hairline.
 */
export function EjercicioCard({
  nombre,
  categoria,
  orden,
  seriesObjetivo,
  repeticionesObjetivo,
  seriesHechas,
  enCurso = false,
  children,
}: EjercicioCardProps) {
  const objetivo = textoObjetivo(seriesObjetivo, repeticionesObjetivo)
  const conAvance = seriesHechas !== undefined && Boolean(seriesObjetivo)
  const completo = conAvance && seriesHechas! >= seriesObjetivo!
  const porcentaje = conAvance ? Math.min(100, Math.round((seriesHechas! / seriesObjetivo!) * 100)) : 0
  const rotulo = [orden !== undefined ? String(orden).padStart(2, '0') : null, categoria ?? 'Ejercicio']
    .filter(Boolean)
    .join(' · ')

  return (
    <Card className="flex flex-col gap-2 overflow-hidden pl-4">
      {/* Indicador vertical del grupo muscular (identidad; el nombre del grupo va en el rótulo). */}
      <span aria-hidden className={cx('absolute inset-y-3 left-0 w-1 rounded-r-full', claseGrupo(categoria))} />

      <div className="flex items-start gap-2">
        <div className="flex min-w-0 flex-1 flex-col gap-0.5">
          <span className="rotulo truncate">{rotulo}</span>
          <h3 className="line-clamp-2 text-base font-bold leading-tight text-texto">{nombre}</h3>
        </div>
        <div className="flex shrink-0 flex-col items-end gap-1">
          {objetivo && (
            <Badge mono>
              <span className="sr-only">Objetivo </span>
              {objetivo}
            </Badge>
          )}
          {conAvance && (
            <Badge tono={completo ? 'exito' : 'neutro'} mono>
              {seriesHechas}/{seriesObjetivo}
              <span className="sr-only"> series hechas</span>
            </Badge>
          )}
        </div>
      </div>

      {conAvance && (
        <div aria-hidden className="h-0.5 overflow-hidden rounded-full bg-linea">
          <div
            className={cx(
              'h-full rounded-full transition-[width] duration-500 ease-out',
              enCurso || completo ? 'bg-acento' : 'bg-texto-medio',
            )}
            style={{ width: `${porcentaje}%` }}
          />
        </div>
      )}

      {children && <div className="flex flex-col gap-2 border-t border-linea-suave pt-2">{children}</div>}
    </Card>
  )
}
