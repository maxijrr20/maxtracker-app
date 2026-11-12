import { LoaderCircle, Minus, RotateCw, Sparkles, TrendingDown, TrendingUp, type LucideIcon } from 'lucide-react'

import type { AnalisisRutina, Tendencia } from '../../api/types'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { Alert } from './Alert'

const TENDENCIAS: Record<Tendencia, { etiqueta: string; icono: LucideIcon; tono: 'exito' | 'neutro' | 'alerta' }> = {
  PROGRESA: { etiqueta: 'Progresa', icono: TrendingUp, tono: 'exito' },
  ESTANCADO: { etiqueta: 'Estancado', icono: Minus, tono: 'neutro' },
  RETROCEDE: { etiqueta: 'Retrocede', icono: TrendingDown, tono: 'alerta' },
  SIN_DATOS_SUFICIENTES: { etiqueta: 'Pocos datos', icono: Minus, tono: 'neutro' },
}

const FORMATO_GENERADO = new Intl.DateTimeFormat('es', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })

export interface TarjetaAnalisisIaProps {
  analisis: AnalisisRutina | null
  cargando: boolean
  error: string | null
  onSolicitar: () => void
  /** Texto bajo el título antes de pedir el análisis. */
  descripcion?: string
  textoBoton?: string
}

/**
 * Recomendaciones del "entrenador virtual": sobrecarga progresiva por ejercicio, ajuste de RIR y sugerencias.
 * Solo presenta datos (la página hace la llamada). Todo el texto de la IA se renderiza como texto, nunca HTML.
 */
export function TarjetaAnalisisIa({
  analisis,
  cargando,
  error,
  onSolicitar,
  descripcion = 'Analiza tus últimas semanas: sobrecarga progresiva, RIR y qué ajustar.',
  textoBoton = 'Analizar con IA',
}: TarjetaAnalisisIaProps) {
  return (
    <Card className="flex flex-col gap-3" aria-busy={cargando || undefined}>
      <div className="flex items-start gap-3">
        <span className="grid size-tactil shrink-0 place-items-center rounded-2xl bg-acento-profundo/20 text-texto">
          <Sparkles aria-hidden className="size-6" />
        </span>
        <div className="flex min-w-0 flex-col">
          <h2 className="text-lg font-semibold">Entrenador virtual</h2>
          <p className="text-sm text-texto-suave">{analisis ? analisis.alcance : descripcion}</p>
        </div>
      </div>

      {cargando && (
        <p role="status" className="flex items-center gap-3 text-texto-suave">
          <LoaderCircle aria-hidden className="size-5 animate-spin text-acento" />
          Analizando tus series… puede tardar unos segundos.
        </p>
      )}

      {error && !cargando && <Alert>{error}</Alert>}

      {analisis && !cargando && (
        <div className="flex flex-col gap-4">
          <p className="text-base leading-relaxed">{analisis.resumen}</p>

          {analisis.ejercicios.length > 0 && (
            <section aria-label="Sobrecarga progresiva por ejercicio" className="flex flex-col gap-2">
              <h3 className="rotulo">Sobrecarga progresiva</h3>
              <ul className="flex flex-col gap-2">
                {analisis.ejercicios.map((e, i) => {
                  const t = TENDENCIAS[e.tendencia] ?? TENDENCIAS.SIN_DATOS_SUFICIENTES
                  return (
                    <li key={`${e.ejercicio}-${i}`} className="flex flex-col gap-1 rounded-2xl bg-white/[0.03] px-3 py-2">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className="font-semibold">{e.ejercicio}</span>
                        <Badge tono={t.tono}>
                          <t.icono aria-hidden className="size-4" />
                          {t.etiqueta}
                        </Badge>
                      </div>
                      {e.observacion && <p className="text-sm text-texto-suave">{e.observacion}</p>}
                      {e.sugerencia && <p className="text-sm">{e.sugerencia}</p>}
                    </li>
                  )
                })}
              </ul>
            </section>
          )}

          {analisis.ajusteRir && (
            <section aria-label="Ajuste de RIR" className="flex flex-col gap-1">
              <h3 className="rotulo">Ajuste de RIR</h3>
              <p className="text-sm">{analisis.ajusteRir}</p>
            </section>
          )}

          {analisis.sugerencias.length > 0 && (
            <section aria-label="Sugerencias" className="flex flex-col gap-1">
              <h3 className="rotulo">Sugerencias</h3>
              <ul className="flex list-disc flex-col gap-1 pl-5 text-sm marker:text-acento">
                {analisis.sugerencias.map((s, i) => (
                  <li key={i}>{s}</li>
                ))}
              </ul>
            </section>
          )}

          <p className="text-xs text-texto-suave">
            {analisis.seriesAnalizadas} series de los últimos {analisis.dias} días · {analisis.modelo} ·{' '}
            {FORMATO_GENERADO.format(new Date(analisis.generadoEn))}. Orientativo: no reemplaza a un profesional.
          </p>
        </div>
      )}

      {!cargando && (
        <Button variante="secundario" anchoCompleto icono={analisis || error ? RotateCw : Sparkles} onClick={onSolicitar}>
          {analisis ? 'Volver a analizar' : error ? 'Reintentar' : textoBoton}
        </Button>
      )}
    </Card>
  )
}
