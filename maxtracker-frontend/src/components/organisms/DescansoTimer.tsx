import { Pause, Play, RotateCcw, Timer } from 'lucide-react'
import { useEffect, useState } from 'react'

import { cx } from '../../lib/cx'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { IconButton } from '../atoms/IconButton'

const PRESETS_SEGUNDOS = [30, 60, 90, 120, 180] as const

const RADIO_ANILLO = 52
const CIRCUNFERENCIA = 2 * Math.PI * RADIO_ANILLO

export interface DescansoTimerProps {
  /** Cambiar este valor (p. ej. al registrar una serie) reinicia el descanso con la duración elegida. */
  disparador?: number
  /** Duración elegida al abrir (el descanso predeterminado del Perfil). */
  duracionInicial?: number
}

function formatear(segundos: number): string {
  const m = Math.floor(segundos / 60)
  const s = segundos % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

/**
 * Cronómetro de descanso entre series. Calcula el tiempo restante contra la hora de fin (no acumulando ticks),
 * así no se desfasa si el navegador frena los intervalos con la pantalla bloqueada. Vibra al terminar.
 */
export function DescansoTimer({ disparador, duracionInicial = 90 }: DescansoTimerProps) {
  const [duracion, setDuracion] = useState<number>(duracionInicial)
  const [finEn, setFinEn] = useState<number | null>(null)
  const [pausadoCon, setPausadoCon] = useState<number | null>(null)
  const [ahora, setAhora] = useState(() => Date.now())

  const corriendo = finEn !== null
  const restante = corriendo
    ? Math.max(0, Math.ceil((finEn - ahora) / 1000))
    : (pausadoCon ?? duracion)
  // Fracción del anillo que queda por recorrer (con decimales para que se vacíe de forma continua).
  const restanteExacto = corriendo ? Math.max(0, (finEn - ahora) / 1000) : restante
  const fraccion = duracion > 0 ? Math.min(1, restanteExacto / duracion) : 0

  // Reinicio externo (serie registrada).
  useEffect(() => {
    if (disparador === undefined || disparador === 0) return
    setPausadoCon(null)
    setAhora(Date.now())
    setFinEn(Date.now() + duracion * 1000)
    // Solo debe reaccionar al disparador, no a cambios de duración.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [disparador])

  useEffect(() => {
    if (finEn === null) return
    const intervalo = window.setInterval(() => {
      const t = Date.now()
      setAhora(t)
      if (t >= finEn) {
        setFinEn(null)
        setPausadoCon(null)
        navigator.vibrate?.([200, 100, 200])
      }
    }, 250)
    return () => window.clearInterval(intervalo)
  }, [finEn])

  function iniciar() {
    const segundos = pausadoCon ?? duracion
    setAhora(Date.now())
    setFinEn(Date.now() + segundos * 1000)
    setPausadoCon(null)
  }

  function pausar() {
    setPausadoCon(restante)
    setFinEn(null)
  }

  function reiniciar() {
    setFinEn(null)
    setPausadoCon(null)
  }

  function elegir(segundos: number) {
    setDuracion(segundos)
    setPausadoCon(null)
    if (corriendo) {
      setAhora(Date.now())
      setFinEn(Date.now() + segundos * 1000)
    }
  }

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-3">
        <h2 className="rotulo flex items-center gap-1.5">
          <Timer aria-hidden className="size-4" /> Descanso
        </h2>
        <IconButton icono={RotateCcw} etiqueta="Reiniciar descanso" onClick={reiniciar} className="-my-2 -mr-2" />
      </div>

      {/*
        Anillo de progreso compacto con la acción al lado: se vacía mientras corre (cian = estado vivo); detenido,
        queda lleno y apagado. Compacto para que el ejercicio actual siga visible sin scroll en la pantalla de entrenamiento.
      */}
      <div className="flex items-center gap-4">
        <div className="relative grid size-28 shrink-0 place-items-center">
          <svg aria-hidden viewBox="0 0 120 120" className="absolute inset-0 size-full -rotate-90">
            <circle cx="60" cy="60" r={RADIO_ANILLO} fill="none" strokeWidth="10" className="stroke-casilla" />
            <circle
              cx="60"
              cy="60"
              r={RADIO_ANILLO}
              fill="none"
              strokeWidth="10"
              strokeLinecap="round"
              strokeDasharray={CIRCUNFERENCIA}
              strokeDashoffset={CIRCUNFERENCIA * (1 - fraccion)}
              className={cx(
                'transition-[stroke-dashoffset,stroke] duration-300 ease-linear',
                corriendo ? 'stroke-acento' : 'stroke-texto-tenue',
              )}
            />
          </svg>
          <p role="timer" aria-live={restante === 0 || !corriendo ? 'polite' : 'off'} className="relative flex flex-col items-center gap-0.5">
            <span className={cx('cifra text-3xl transition-colors duration-150', corriendo ? 'text-texto' : 'text-texto-medio')}>
              {formatear(restante)}
            </span>
            <span className="text-xs text-texto-suave">{corriendo ? 'descansando' : pausadoCon !== null ? 'en pausa' : 'listo'}</span>
          </p>
        </div>

        <div className="flex min-w-0 flex-1">
          {corriendo ? (
            <Button variante="secundario" anchoCompleto icono={Pause} onClick={pausar}>
              Pausar
            </Button>
          ) : (
            <Button variante="secundario" anchoCompleto icono={Play} onClick={iniciar}>
              {pausadoCon !== null ? 'Reanudar' : 'Iniciar'}
            </Button>
          )}
        </div>
      </div>

      <div role="group" aria-label="Duración del descanso" className="grid grid-cols-5 gap-1 rounded-2xl border border-linea bg-casilla p-1">
        {PRESETS_SEGUNDOS.map((segundos) => (
          <button
            key={segundos}
            type="button"
            aria-pressed={duracion === segundos}
            onClick={() => elegir(segundos)}
            className={cx(
              'min-h-tactil rounded-xl font-mono text-sm font-bold tabular-nums transition-[transform,background-color,color] duration-150 active:scale-[0.98]',
              duracion === segundos ? 'bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40' : 'text-texto-suave hover:bg-white/[0.04] hover:text-texto',
            )}
          >
            {formatear(segundos)}
          </button>
        ))}
      </div>
    </Card>
  )
}
