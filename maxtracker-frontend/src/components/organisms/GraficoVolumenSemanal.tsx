import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

import type { VolumenSemanal } from '../../api/types'
import { COLOR_GRAFICO, FUENTE_MONO } from '../../lib/coloresGrafico'
import { usePeso } from '../../hooks/usePeso'
import { fechaCorta } from '../../lib/formato'
import { Card } from '../atoms/Card'
import { TablaGrafico } from '../molecules/TablaGrafico'
import { TooltipGrafico } from '../molecules/TooltipGrafico'

interface Punto {
  semana: string
  volumen: number
  sesiones: number
}

/**
 * Volumen (kg) por semana: columnas de una sola serie (sin leyenda: el título dice qué es), de 24 px como máximo,
 * extremo redondeado de 4 px y base recta. Tooltip por columna y tabla equivalente.
 */
export function GraficoVolumenSemanal({ semanas }: { semanas: VolumenSemanal[] }) {
  const { formato: formatoPeso, formatoCompacto: formatoPesoCompacto } = usePeso()
  const datos: Punto[] = semanas.map((s) => ({
    semana: `Sem. ${fechaCorta(s.inicioSemana)}`,
    volumen: s.volumenKg,
    sesiones: s.sesiones,
  }))

  return (
    <Card className="flex flex-col gap-2">
      <div className="flex items-baseline justify-between gap-2">
        <h2 className="rotulo">Volumen por semana</h2>
        <span className="text-xs text-texto-suave">Peso × reps · {semanas.length} sem.</span>
      </div>

      {/* Marcas en gris azulado; la semana actual (foco) en cian. */}
      <div className="h-44" role="img" aria-label={`Gráfico de volumen semanal de las últimas ${semanas.length} semanas`}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={datos} margin={{ top: 8, right: 4, bottom: 0, left: 0 }} barCategoryGap={2}>
            <CartesianGrid vertical={false} stroke={COLOR_GRAFICO.rejilla} />
            <XAxis
              dataKey="semana"
              tickFormatter={(valor: string) => valor.replace('Sem. ', '')}
              tick={{ fill: COLOR_GRAFICO.textoEje, fontSize: 11, fontFamily: FUENTE_MONO }}
              axisLine={false}
              tickLine={false}
              interval="preserveStartEnd"
              minTickGap={16}
            />
            <YAxis
              tickFormatter={formatoPesoCompacto}
              tick={{ fill: COLOR_GRAFICO.textoEje, fontSize: 11, fontFamily: FUENTE_MONO }}
              axisLine={false}
              tickLine={false}
              width={44}
              allowDecimals={false}
            />
            <Tooltip
              cursor={{ fill: 'rgba(255, 255, 255, 0.04)' }}
              content={({ active, payload }) => {
                const punto = payload?.[0]?.payload as Punto | undefined
                return (
                  <TooltipGrafico
                    activo={active}
                    valor={punto ? formatoPeso(punto.volumen) : undefined}
                    etiqueta={punto?.semana}
                    serie="volumen"
                    detalle={punto ? `${punto.sesiones} ${punto.sesiones === 1 ? 'sesión' : 'sesiones'}` : undefined}
                  />
                )
              }}
            />
            <Bar
              dataKey="volumen"
              fill={COLOR_GRAFICO.marca}
              activeBar={{ fill: COLOR_GRAFICO.marcaActiva }}
              radius={[4, 4, 0, 0]}
              maxBarSize={24}
              isAnimationActive={false}
            >
              {datos.map((punto, i) => (
                <Cell key={punto.semana} fill={i === datos.length - 1 ? COLOR_GRAFICO.foco : COLOR_GRAFICO.marca} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <TablaGrafico
        titulo="Volumen por semana"
        columnas={['Semana del', 'Volumen', 'Sesiones']}
        filas={semanas.map((s) => [fechaCorta(s.inicioSemana), formatoPeso(s.volumenKg), String(s.sesiones)])}
      />
    </Card>
  )
}
