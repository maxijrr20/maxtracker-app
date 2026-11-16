import { Check, ChevronLeft, ChevronRight, Plus, X, type LucideIcon } from 'lucide-react'

import type { DiaConstancia, EstadoDia } from '../../api/types'
import { cx } from '../../lib/cx'
import { DIAS_SEMANA, diaIso, nombreMes } from '../../lib/diasSemana'
import { Card } from '../atoms/Card'
import { IconButton } from '../atoms/IconButton'

/**
 * Cada estado lleva color e ícono/forma (nunca solo color): verde = entrenado, rojo = fallado, cian = hoy (estado
 * vivo), borde punteado = planificado o extra.
 */
const ESTILOS: Record<EstadoDia, { clases: string; icono?: LucideIcon; colorIcono?: string; texto: string }> = {
  ENTRENADO: {
    clases: 'bg-exito/15 text-texto ring-1 ring-inset ring-exito/45',
    icono: Check,
    colorIcono: 'text-exito',
    texto: 'entrenado',
  },
  EXTRA: {
    clases: 'bg-exito/[0.07] text-texto border border-dashed border-exito/55',
    icono: Plus,
    colorIcono: 'text-exito',
    texto: 'entrenado (día extra)',
  },
  FALLADO: {
    clases: 'bg-peligro/10 text-texto-medio ring-1 ring-inset ring-peligro/40',
    icono: X,
    colorIcono: 'text-peligro',
    texto: 'fallado',
  },
  PENDIENTE: { clases: 'text-texto ring-2 ring-inset ring-acento', texto: 'toca entrenar hoy' },
  PLANIFICADO: { clases: 'text-texto-suave border border-dashed border-linea', texto: 'día de entrenamiento' },
  DESCANSO: { clases: 'text-texto-tenue', texto: 'descanso' },
}

const LEYENDA: EstadoDia[] = ['ENTRENADO', 'EXTRA', 'FALLADO', 'PENDIENTE', 'PLANIFICADO']

const formatoDia = new Intl.DateTimeFormat('es', { weekday: 'long', day: 'numeric', month: 'long', timeZone: 'UTC' })

function etiquetaDia(dia: DiaConstancia, esHoy: boolean): string {
  const [anio, mes, numero] = dia.fecha.split('-').map(Number)
  const fecha = formatoDia.format(new Date(Date.UTC(anio, mes - 1, numero)))
  return `${fecha}${esHoy ? ' (hoy)' : ''}: ${ESTILOS[dia.estado].texto}`
}

export interface CalendarioConstanciaProps {
  /** "AAAA-MM" */
  mes: string
  dias: DiaConstancia[]
  /** "AAAA-MM-DD" en la zona horaria del usuario. */
  hoy: string
  onAnterior: () => void
  onSiguiente: () => void
  puedeSiguiente: boolean
  /** Atenúa el mes mientras carga otro (sin saltos de layout). */
  cargando?: boolean
}

/** Mes en una grilla de lunes a domingo con el estado de cada día y su leyenda. */
export function CalendarioConstancia({
  mes,
  dias,
  hoy,
  onAnterior,
  onSiguiente,
  puedeSiguiente,
  cargando = false,
}: CalendarioConstanciaProps) {
  const huecos = dias.length > 0 ? diaIso(dias[0].fecha) - 1 : 0

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-2">
        <IconButton icono={ChevronLeft} etiqueta="Mes anterior" onClick={onAnterior} className="-ml-2" />
        <h2 className="text-base font-bold first-letter:uppercase" aria-live="polite">
          {nombreMes(mes)}
        </h2>
        <IconButton
          icono={ChevronRight}
          etiqueta="Mes siguiente"
          onClick={onSiguiente}
          disabled={!puedeSiguiente}
          className="-mr-2"
        />
      </div>

      <div className={cx('flex flex-col gap-1.5 transition-opacity', cargando && 'opacity-50')}>
        <div aria-hidden className="grid grid-cols-7 gap-1.5 text-center">
          {DIAS_SEMANA.map((d) => (
            <span key={d.iso} className="rotulo">
              {d.corto}
            </span>
          ))}
        </div>
        <ol className="grid grid-cols-7 gap-1.5" aria-label={`Días de ${nombreMes(mes)}`}>
          {Array.from({ length: huecos }, (_, i) => (
            <li key={`hueco-${i}`} aria-hidden />
          ))}
          {dias.map((dia) => {
            const estilo = ESTILOS[dia.estado]
            const Icono = estilo.icono
            const esHoy = dia.fecha === hoy
            return (
              <li
                key={dia.fecha}
                aria-label={etiquetaDia(dia, esHoy)}
                className={cx(
                  'flex aspect-square flex-col items-center justify-center gap-0.5 rounded-2xl',
                  estilo.clases,
                )}
              >
                <span aria-hidden className={cx('font-mono text-sm tabular-nums', esHoy ? 'font-black' : 'font-bold')}>
                  {Number(dia.fecha.slice(8))}
                </span>
                {Icono ? (
                  <Icono aria-hidden className={cx('size-3', estilo.colorIcono)} strokeWidth={3} />
                ) : (
                  // Mantiene la altura de la celda alineada con las que tienen ícono.
                  <span aria-hidden className={cx('size-1.5 rounded-full', esHoy ? 'bg-acento' : 'bg-transparent')} />
                )}
              </li>
            )
          })}
        </ol>
      </div>

      <ul aria-label="Leyenda" className="flex flex-wrap gap-x-4 gap-y-2 border-t border-linea-suave pt-3">
        {LEYENDA.map((estado) => {
          const estilo = ESTILOS[estado]
          const Icono = estilo.icono
          return (
            <li key={estado} className="flex items-center gap-1.5 text-xs text-texto-suave">
              <span aria-hidden className={cx('grid size-4 place-items-center rounded-md', estilo.clases)}>
                {Icono && <Icono className={cx('size-2.5', estilo.colorIcono)} strokeWidth={3} />}
              </span>
              {estado === 'PENDIENTE' ? 'Hoy' : estilo.texto.charAt(0).toUpperCase() + estilo.texto.slice(1)}
            </li>
          )
        })}
      </ul>
    </Card>
  )
}
