import { useEffect, useState, type ReactNode } from 'react'

import type { Sesion } from '../../api/types'
import { usePeso } from '../../hooks/usePeso'
import { Badge } from '../atoms/Badge'
import { Card } from '../atoms/Card'

const FORMATO_FECHA = new Intl.DateTimeFormat('es', { weekday: 'short', day: 'numeric', month: 'short' })
const FORMATO_ENTERO = new Intl.NumberFormat('es', { maximumFractionDigits: 0 })

/** "1:05" (h:mm) o "42" (min) para el tablero. */
function tiempo(desde: string, hasta: number): { valor: string; unidad: string } {
  const minutos = Math.max(0, Math.floor((hasta - Date.parse(desde)) / 60_000))
  return minutos >= 60
    ? { valor: `${Math.floor(minutos / 60)}:${String(minutos % 60).padStart(2, '0')}`, unidad: 'h' }
    : { valor: String(minutos), unidad: 'min' }
}

export interface SesionActivaCardProps {
  sesion: Sesion
  /** Nombre de la rutina, si se conoce (la sesión solo trae su id). */
  nombreRutina?: string
  /** Acción principal: "Continuar" en el inicio, "Finalizar" en la pantalla de entrenamiento. */
  accion?: ReactNode
}

/**
 * Tablero de la sesión: rótulo con fecha, título y badge flotante de estado; tiempo, series y volumen como cifras
 * en una franja con divisores. En curso es la superficie de foco (borde lima) de la pantalla.
 */
export function SesionActivaCard({ sesion, nombreRutina, accion }: SesionActivaCardProps) {
  const { desdeKg, unidad } = usePeso()
  const [ahora, setAhora] = useState(() => Date.now())

  useEffect(() => {
    if (sesion.finalizada) return
    const intervalo = window.setInterval(() => setAhora(Date.now()), 30_000)
    return () => window.clearInterval(intervalo)
  }, [sesion.finalizada])

  const volumenKg = sesion.series.reduce((total, serie) => total + serie.pesoKg * serie.repeticiones, 0)
  const fin = sesion.finalizadaEn ? Date.parse(sesion.finalizadaEn) : ahora
  const duracion = tiempo(sesion.iniciadaEn, fin)
  // Con hora y sin zona, "2026-11-10T12:00:00" se interpreta en hora local: el día mostrado es el de la sesión.
  const fecha = FORMATO_FECHA.format(new Date(`${sesion.fecha}T12:00:00`))

  const cifras = [
    { etiqueta: 'Tiempo', valor: duracion.valor, unidad: duracion.unidad },
    { etiqueta: 'Series', valor: String(sesion.series.length), unidad: '' },
    { etiqueta: 'Volumen', valor: FORMATO_ENTERO.format(desdeKg(volumenKg)), unidad },
  ]

  return (
    <Card tono={sesion.finalizada ? 'normal' : 'foco'} className="flex flex-col gap-3">
      {/* Badge flotante de estado, apoyado en el borde. */}
      <Badge tono={sesion.finalizada ? 'neutro' : 'acento'} className="absolute -top-2.5 right-3 shadow-elevada">
        {!sesion.finalizada && <span aria-hidden className="size-1.5 animate-pulse rounded-full bg-acento" />}
        {sesion.finalizada ? 'FINALIZADA' : 'EN CURSO'}
      </Badge>

      <div className="flex min-w-0 flex-col gap-0.5">
        <span className="rotulo">{fecha}</span>
        <h2 className="truncate text-lg font-black tracking-tight">{nombreRutina ?? 'Entrenamiento libre'}</h2>
      </div>

      <dl className="grid grid-cols-3 divide-x divide-zinc-800/60 rounded-xl bg-white/[0.03] py-2">
        {cifras.map((c) => (
          <div key={c.etiqueta} className="flex flex-col items-center gap-1 px-2">
            <dt className="rotulo">{c.etiqueta}</dt>
            <dd className="whitespace-nowrap">
              <span className="cifra text-2xl text-texto">{c.valor}</span>
              {c.unidad && <span className="ml-0.5 text-xs font-semibold text-zinc-500">{c.unidad}</span>}
            </dd>
          </div>
        ))}
      </dl>

      {accion}
    </Card>
  )
}
