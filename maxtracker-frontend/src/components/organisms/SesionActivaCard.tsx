import { Activity, Clock } from 'lucide-react'
import { useEffect, useState, type ReactNode } from 'react'

import type { Sesion } from '../../api/types'
import { Badge } from '../atoms/Badge'
import { Card } from '../atoms/Card'
import { Titulo } from '../atoms/Typography'

const FORMATO_FECHA = new Intl.DateTimeFormat('es', { weekday: 'long', day: 'numeric', month: 'long' })
const FORMATO_KG = new Intl.NumberFormat('es', { maximumFractionDigits: 1 })

function duracion(desde: string, hasta: number): string {
  const minutos = Math.max(0, Math.floor((hasta - Date.parse(desde)) / 60_000))
  const h = Math.floor(minutos / 60)
  return h > 0 ? `${h} h ${minutos % 60} min` : `${minutos} min`
}

export interface SesionActivaCardProps {
  sesion: Sesion
  /** Nombre de la rutina, si se conoce (la sesión solo trae su id). */
  nombreRutina?: string
  /** Acción principal: "Continuar" en el inicio, "Finalizar" en la pantalla de entrenamiento. */
  accion?: ReactNode
}

/** Resumen de la sesión en curso: tiempo transcurrido, series y volumen total. */
export function SesionActivaCard({ sesion, nombreRutina, accion }: SesionActivaCardProps) {
  const [ahora, setAhora] = useState(() => Date.now())

  useEffect(() => {
    if (sesion.finalizada) return
    const intervalo = window.setInterval(() => setAhora(Date.now()), 30_000)
    return () => window.clearInterval(intervalo)
  }, [sesion.finalizada])

  const volumen = sesion.series.reduce((total, serie) => total + serie.pesoKg * serie.repeticiones, 0)
  const fin = sesion.finalizadaEn ? Date.parse(sesion.finalizadaEn) : ahora
  // Con hora y sin zona, "2026-11-10T12:00:00" se interpreta en hora local: el día mostrado es el de la sesión.
  const fecha = FORMATO_FECHA.format(new Date(`${sesion.fecha}T12:00:00`))

  return (
    <Card className="flex flex-col gap-4 border-lime-400/30">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 flex-col gap-1">
          <Badge tono={sesion.finalizada ? 'neutro' : 'acento'} className="self-start">
            <Activity aria-hidden className="size-4" />
            {sesion.finalizada ? 'Finalizada' : 'En curso'}
          </Badge>
          <Titulo nivel={2} className="break-words">
            {nombreRutina ?? 'Entrenamiento libre'}
          </Titulo>
          <p className="text-sm capitalize text-texto-suave">{fecha}</p>
        </div>
      </div>

      <dl className="grid grid-cols-3 gap-2 text-center">
        <div className="rounded-xl bg-superficie-alta p-3">
          <dt className="flex items-center justify-center gap-1 text-xs text-texto-suave">
            <Clock aria-hidden className="size-3.5" /> Tiempo
          </dt>
          <dd className="text-lg font-bold tabular-nums">{duracion(sesion.iniciadaEn, fin)}</dd>
        </div>
        <div className="rounded-xl bg-superficie-alta p-3">
          <dt className="text-xs text-texto-suave">Series</dt>
          <dd className="text-lg font-bold tabular-nums">{sesion.series.length}</dd>
        </div>
        <div className="rounded-xl bg-superficie-alta p-3">
          <dt className="text-xs text-texto-suave">Volumen</dt>
          <dd className="text-lg font-bold tabular-nums">{FORMATO_KG.format(volumen)} kg</dd>
        </div>
      </dl>

      {accion}
    </Card>
  )
}
