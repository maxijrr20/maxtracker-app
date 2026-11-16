import { Flame, Trophy } from 'lucide-react'
import { useState } from 'react'

import type { ResumenConstancia as Resumen } from '../../api/types'
import { cx } from '../../lib/cx'
import { Card } from '../atoms/Card'
import { StatTile } from '../molecules/StatTile'

type Periodo = 'mes' | 'total'

export interface ResumenConstanciaProps {
  rachaActual: number | null
  mejorRacha: number | null
  resumenMes: Resumen
  resumenTotal: Resumen
}

const dias = (n: number) => (n === 1 ? 'día' : 'días')

/**
 * Rachas (cian = la actual, estado vivo; coral = la mejor, es un récord) y conteos del mes o del historial completo.
 * Sin plan no hay racha ni fallos: se invita a configurarlo (la página muestra el formulario).
 */
export function ResumenConstancia({ rachaActual, mejorRacha, resumenMes, resumenTotal }: ResumenConstanciaProps) {
  const [periodo, setPeriodo] = useState<Periodo>('mes')
  const resumen = periodo === 'mes' ? resumenMes : resumenTotal

  return (
    <Card className="flex flex-col gap-3">
      <h2 className="sr-only">Estadísticas de constancia</h2>

      {rachaActual === null || mejorRacha === null ? (
        <p className="text-sm text-texto-suave">Configura tu plan semanal para medir tu racha y tus días fallados.</p>
      ) : (
        <dl className="grid grid-cols-2 divide-x divide-linea-suave">
          <div className="flex flex-col gap-1.5 px-3 py-1">
            <dt className="rotulo flex items-center gap-1.5">
              <Flame aria-hidden className={cx('size-4', rachaActual > 0 ? 'text-acento' : 'text-texto-tenue')} />
              Racha actual
            </dt>
            <dd className="whitespace-nowrap">
              <span className={cx('cifra text-4xl', rachaActual > 0 ? 'text-acento' : 'text-texto')}>{rachaActual}</span>
              <span className="ml-1 text-xs font-semibold text-texto-suave">{dias(rachaActual)}</span>
            </dd>
          </div>
          <div className="flex flex-col gap-1.5 px-3 py-1">
            <dt className="rotulo flex items-center gap-1.5">
              <Trophy aria-hidden className="size-4 text-record" />
              Mejor racha
            </dt>
            <dd className="whitespace-nowrap">
              <span className="cifra text-4xl text-record">{mejorRacha}</span>
              <span className="ml-1 text-xs font-semibold text-texto-suave">{dias(mejorRacha)}</span>
            </dd>
          </div>
        </dl>
      )}

      <div className="flex flex-col gap-2 border-t border-linea-suave pt-3">
        <div role="group" aria-label="Periodo" className="grid grid-cols-2 gap-1 rounded-2xl border border-linea bg-casilla p-1">
          {(['mes', 'total'] as const).map((p) => (
            <button
              key={p}
              type="button"
              aria-pressed={periodo === p}
              onClick={() => setPeriodo(p)}
              className={cx(
                'min-h-tactil rounded-xl text-sm font-semibold transition-[transform,background-color,color] duration-150 active:scale-[0.98]',
                periodo === p
                  ? 'bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40'
                  : 'text-texto-suave hover:bg-white/[0.04] hover:text-texto',
              )}
            >
              {p === 'mes' ? 'Este mes' : 'Desde el inicio'}
            </button>
          ))}
        </div>
        <div className="grid grid-cols-3 divide-x divide-linea-suave">
          <StatTile etiqueta="Fuiste" valor={String(resumen.entrenados)} unidad={dias(resumen.entrenados)} />
          <StatTile etiqueta="Fallaste" valor={String(resumen.fallados)} unidad={dias(resumen.fallados)} />
          <StatTile
            etiqueta="Cumplido"
            valor={resumen.cumplimiento === null ? '–' : String(resumen.cumplimiento)}
            unidad={resumen.cumplimiento === null ? undefined : '%'}
            detalle={resumen.cumplimiento === null ? 'Sin días del plan' : 'de tu plan'}
          />
        </div>
      </div>
    </Card>
  )
}
