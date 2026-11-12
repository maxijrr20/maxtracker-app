import { LineChart as IconoLinea } from 'lucide-react'
import { useId, useState } from 'react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

import type { EjercicioConHistorial, ProgresoEjercicio, PuntoProgreso } from '../../api/types'
import { COLOR_GRAFICO, FUENTE_MONO } from '../../lib/coloresGrafico'
import { cx } from '../../lib/cx'
import { usePeso } from '../../hooks/usePeso'
import { fechaCorta } from '../../lib/formato'
import { Card } from '../atoms/Card'
import { Select } from '../atoms/Select'
import { Spinner } from '../atoms/Spinner'
import { EmptyState } from '../molecules/EmptyState'
import { TablaGrafico } from '../molecules/TablaGrafico'
import { TooltipGrafico } from '../molecules/TooltipGrafico'

type Medida = 'pesoMaxKg' | 'unoRmEstimadoKg' | 'volumenKg'

const MEDIDAS: Record<Medida, { etiqueta: string; serie: string; descripcion: string }> = {
  pesoMaxKg: { etiqueta: 'Peso máx.', serie: 'peso máximo', descripcion: 'Mayor peso levantado en cada sesión.' },
  unoRmEstimadoKg: {
    etiqueta: '1RM est.',
    serie: '1RM estimado',
    descripcion: 'Repetición máxima estimada (Epley) con tu mejor serie de cada sesión.',
  },
  volumenKg: { etiqueta: 'Volumen', serie: 'volumen', descripcion: 'Peso × repeticiones del ejercicio en cada sesión.' },
}

export interface GraficoProgresoProps {
  ejercicios: EjercicioConHistorial[]
  ejercicioId: number | null
  onCambiarEjercicio: (ejercicioId: number) => void
  progreso?: ProgresoEjercicio
  cargando: boolean
  error?: string | null
}

/**
 * Evolución de cargas de un ejercicio. Una sola medida a la vez (nunca dos ejes): se cambia con los chips.
 * Línea de 2 px con puntos de 8 px y anillo del color de la superficie; crosshair + tooltip; tabla equivalente.
 */
export function GraficoProgreso({ ejercicios, ejercicioId, onCambiarEjercicio, progreso, cargando, error }: GraficoProgresoProps) {
  const { formato: formatoPeso, formatoCompacto: formatoPesoCompacto } = usePeso()
  const [medida, setMedida] = useState<Medida>('pesoMaxKg')
  const idSelect = useId()
  const puntos = progreso?.puntos ?? []
  const ultimo = puntos.at(-1)
  const mejor = puntos.reduce<PuntoProgreso | undefined>(
    (max, p) => (max === undefined || p[medida] > max[medida] ? p : max),
    undefined,
  )

  return (
    <Card className="flex flex-col gap-2">
      <div className="flex flex-col gap-0.5">
        <h2 className="rotulo">Evolución por ejercicio</h2>
        <p className="text-xs text-texto-suave">{MEDIDAS[medida].descripcion}</p>
      </div>

      {ejercicios.length === 0 ? (
        <EmptyState
          icono={IconoLinea}
          titulo="Aún no hay progreso que mostrar"
          descripcion="Registra series en tus entrenamientos y aquí verás cómo evolucionan tus cargas."
        />
      ) : (
        <>
          {/* Filtros en una fila, arriba del gráfico. */}
          <div className="flex flex-col gap-2">
            <label htmlFor={idSelect} className="sr-only">
              Ejercicio
            </label>
            <Select
              id={idSelect}
              value={ejercicioId ?? ''}
              onChange={(e) => onCambiarEjercicio(Number(e.target.value))}
            >
              {ejercicios.map((e) => (
                <option key={e.ejercicioId} value={e.ejercicioId}>
                  {e.ejercicioNombre}
                </option>
              ))}
            </Select>
            <div role="group" aria-label="Medida" className="grid grid-cols-3 gap-1 rounded-2xl border border-linea bg-casilla p-1">
              {(Object.keys(MEDIDAS) as Medida[]).map((m) => (
                <button
                  key={m}
                  type="button"
                  aria-pressed={medida === m}
                  onClick={() => setMedida(m)}
                  className={cx(
                    'min-h-tactil rounded-xl text-sm font-semibold transition-[transform,background-color,color] duration-150 active:scale-[0.98]',
                    medida === m ? 'bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40' : 'text-texto-suave hover:bg-white/[0.04] hover:text-texto',
                  )}
                >
                  {MEDIDAS[m].etiqueta}
                </button>
              ))}
            </div>
          </div>

          {/* Cifras clave (la "etiqueta directa" de la línea): la última y la mejor; la mejor es el PR (coral). */}
          {ultimo && mejor && (
            <dl className="grid grid-cols-2 divide-x divide-linea-suave rounded-2xl bg-white/[0.03] py-2">
              <div className="flex flex-col gap-1 px-3">
                <dt className="rotulo">Última · {fechaCorta(ultimo.fecha)}</dt>
                <dd className="cifra text-2xl text-texto">{formatoPeso(ultimo[medida])}</dd>
              </div>
              <div className="flex flex-col gap-1 px-3">
                <dt className="rotulo">Mejor · {fechaCorta(mejor.fecha)}</dt>
                <dd className="cifra text-2xl text-record">{formatoPeso(mejor[medida])}</dd>
              </div>
            </dl>
          )}

          {error && <p role="alert" className="text-sm text-peligro">{error}</p>}

          {cargando && !progreso ? (
            <Spinner etiqueta="Cargando progreso…" />
          ) : puntos.length > 0 ? (
            // Al recargar se mantiene el gráfico anterior atenuado (sin saltos de layout).
            <div
              className={cx('h-44 transition-opacity', cargando && 'opacity-50')}
              role="img"
              aria-label={`Gráfico de ${MEDIDAS[medida].serie} de ${progreso?.ejercicioNombre ?? 'el ejercicio'}`}
            >
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={puntos} margin={{ top: 8, right: 12, bottom: 0, left: 0 }}>
                  <CartesianGrid vertical={false} stroke={COLOR_GRAFICO.rejilla} />
                  <XAxis
                    dataKey="fecha"
                    tickFormatter={fechaCorta}
                    tick={{ fill: COLOR_GRAFICO.textoEje, fontSize: 11, fontFamily: FUENTE_MONO }}
                    axisLine={false}
                    tickLine={false}
                    interval="preserveStartEnd"
                    minTickGap={20}
                  />
                  <YAxis
                    domain={medida === 'volumenKg' ? [0, 'auto'] : ['auto', 'auto']}
                    tickFormatter={formatoPesoCompacto}
                    tick={{ fill: COLOR_GRAFICO.textoEje, fontSize: 11, fontFamily: FUENTE_MONO }}
                    axisLine={false}
                    tickLine={false}
                    width={44}
                  />
                  <Tooltip
                    cursor={{ stroke: COLOR_GRAFICO.cursor, strokeWidth: 1 }}
                    content={({ active, payload }) => {
                      const punto = payload?.[0]?.payload as PuntoProgreso | undefined
                      return (
                        <TooltipGrafico
                          activo={active}
                          valor={punto ? formatoPeso(punto[medida]) : undefined}
                          etiqueta={punto ? fechaCorta(punto.fecha) : undefined}
                          serie={MEDIDAS[medida].serie}
                          detalle={punto ? `${punto.series} ${punto.series === 1 ? 'serie' : 'series'}` : undefined}
                        />
                      )
                    }}
                  />
                  <Line
                    type="linear"
                    dataKey={medida}
                    stroke={COLOR_GRAFICO.linea}
                    strokeWidth={2}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    // Puntos cian; el del mejor valor (PR) en coral y más grande.
                    dot={(props: { cx?: number; cy?: number; index?: number; payload?: PuntoProgreso }) => {
                      const esMejor = props.payload?.sesionId === mejor?.sesionId
                      return (
                        <circle
                          key={`punto-${props.index}`}
                          cx={props.cx}
                          cy={props.cy}
                          r={esMejor ? 5 : 4}
                          fill={esMejor ? COLOR_GRAFICO.record : COLOR_GRAFICO.linea}
                          stroke={COLOR_GRAFICO.superficie}
                          strokeWidth={2}
                        />
                      )
                    }}
                    activeDot={{ r: 6, fill: COLOR_GRAFICO.lineaActiva, stroke: COLOR_GRAFICO.superficie, strokeWidth: 2 }}
                    isAnimationActive={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          ) : (
            !cargando && <p className="text-sm text-texto-suave">Este ejercicio todavía no tiene series registradas.</p>
          )}

          {puntos.length > 0 && (
            <TablaGrafico
              titulo={`Progreso de ${progreso?.ejercicioNombre ?? 'el ejercicio'}`}
              columnas={['Fecha', 'Peso máx.', '1RM est.', 'Volumen']}
              filas={puntos.map((p) => [
                fechaCorta(p.fecha) + (puntos.filter((q) => q.fecha === p.fecha).length > 1 ? ` (#${p.sesionId})` : ''),
                formatoPeso(p.pesoMaxKg),
                formatoPeso(p.unoRmEstimadoKg),
                formatoPeso(p.volumenKg),
              ])}
            />
          )}
        </>
      )}
    </Card>
  )
}
