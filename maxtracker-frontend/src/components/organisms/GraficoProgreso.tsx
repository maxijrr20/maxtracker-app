import { LineChart as IconoLinea } from 'lucide-react'
import { useId, useState } from 'react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

import type { EjercicioConHistorial, ProgresoEjercicio, PuntoProgreso } from '../../api/types'
import { COLOR_GRAFICO } from '../../lib/coloresGrafico'
import { cx } from '../../lib/cx'
import { usePeso } from '../../hooks/usePeso'
import { fechaCorta } from '../../lib/formato'
import { Card } from '../atoms/Card'
import { Chip } from '../atoms/Chip'
import { Select } from '../atoms/Select'
import { Spinner } from '../atoms/Spinner'
import { Titulo } from '../atoms/Typography'
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
    <Card className="flex flex-col gap-4">
      <div>
        <Titulo nivel={2}>Evolución por ejercicio</Titulo>
        <p className="text-sm text-texto-suave">{MEDIDAS[medida].descripcion}</p>
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
          <div className="flex flex-col gap-3">
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
            <div role="group" aria-label="Medida" className="grid grid-cols-3 gap-2">
              {(Object.keys(MEDIDAS) as Medida[]).map((m) => (
                <Chip key={m} activo={medida === m} onClick={() => setMedida(m)} className="justify-center px-2">
                  {MEDIDAS[m].etiqueta}
                </Chip>
              ))}
            </div>
          </div>

          {/* Cifras clave como texto: el último valor y el mejor (la "etiqueta directa" de la línea). */}
          {ultimo && mejor && (
            <dl className="grid grid-cols-2 gap-2">
              <div className="rounded-xl border border-white/5 bg-zinc-950/40 p-3">
                <dt className="text-xs text-texto-suave">Última sesión · {fechaCorta(ultimo.fecha)}</dt>
                <dd className="font-mono text-xl font-bold">{formatoPeso(ultimo[medida])}</dd>
              </div>
              <div className="rounded-xl border border-white/5 bg-zinc-950/40 p-3">
                <dt className="text-xs text-texto-suave">Mejor · {fechaCorta(mejor.fecha)}</dt>
                <dd className="font-mono text-xl font-bold">{formatoPeso(mejor[medida])}</dd>
              </div>
            </dl>
          )}

          {error && <p role="alert" className="text-sm text-peligro">{error}</p>}

          {cargando && !progreso ? (
            <Spinner etiqueta="Cargando progreso…" />
          ) : puntos.length > 0 ? (
            // Al recargar se mantiene el gráfico anterior atenuado (sin saltos de layout).
            <div
              className={cx('h-56 transition-opacity', cargando && 'opacity-50')}
              role="img"
              aria-label={`Gráfico de ${MEDIDAS[medida].serie} de ${progreso?.ejercicioNombre ?? 'el ejercicio'}`}
            >
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={puntos} margin={{ top: 8, right: 12, bottom: 0, left: 0 }}>
                  <CartesianGrid vertical={false} stroke={COLOR_GRAFICO.rejilla} />
                  <XAxis
                    dataKey="fecha"
                    tickFormatter={fechaCorta}
                    tick={{ fill: COLOR_GRAFICO.textoEje, fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    interval="preserveStartEnd"
                    minTickGap={20}
                  />
                  <YAxis
                    domain={medida === 'volumenKg' ? [0, 'auto'] : ['auto', 'auto']}
                    tickFormatter={formatoPesoCompacto}
                    tick={{ fill: COLOR_GRAFICO.textoEje, fontSize: 12 }}
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
                    stroke={COLOR_GRAFICO.marca}
                    strokeWidth={2}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    dot={{ r: 4, fill: COLOR_GRAFICO.marca, stroke: COLOR_GRAFICO.superficie, strokeWidth: 2 }}
                    activeDot={{ r: 6, fill: COLOR_GRAFICO.marcaActiva, stroke: COLOR_GRAFICO.superficie, strokeWidth: 2 }}
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
