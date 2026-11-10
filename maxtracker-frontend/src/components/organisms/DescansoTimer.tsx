import { Pause, Play, RotateCcw, Timer } from 'lucide-react'
import { useEffect, useState } from 'react'

import { cx } from '../../lib/cx'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { IconButton } from '../atoms/IconButton'

const PRESETS_SEGUNDOS = [60, 90, 120, 180] as const

export interface DescansoTimerProps {
  /** Cambiar este valor (p. ej. al registrar una serie) reinicia el descanso con la duración elegida. */
  disparador?: number
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
export function DescansoTimer({ disparador }: DescansoTimerProps) {
  const [duracion, setDuracion] = useState<number>(90)
  const [finEn, setFinEn] = useState<number | null>(null)
  const [pausadoCon, setPausadoCon] = useState<number | null>(null)
  const [ahora, setAhora] = useState(() => Date.now())

  const corriendo = finEn !== null
  const restante = corriendo
    ? Math.max(0, Math.ceil((finEn - ahora) / 1000))
    : (pausadoCon ?? duracion)

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
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="flex items-center gap-2 text-lg font-semibold">
          <Timer aria-hidden className="size-5 text-acento" /> Descanso
        </h2>
        <IconButton icono={RotateCcw} etiqueta="Reiniciar descanso" onClick={reiniciar} />
      </div>

      <p
        role="timer"
        aria-live={restante === 0 || !corriendo ? 'polite' : 'off'}
        className={cx(
          'text-center text-6xl font-bold tabular-nums tracking-tight',
          corriendo ? 'text-acento' : 'text-texto',
        )}
      >
        {formatear(restante)}
      </p>

      <div role="group" aria-label="Duración del descanso" className="grid grid-cols-4 gap-2">
        {PRESETS_SEGUNDOS.map((segundos) => (
          <button
            key={segundos}
            type="button"
            aria-pressed={duracion === segundos}
            onClick={() => elegir(segundos)}
            className={cx(
              'min-h-tactil rounded-xl text-base font-semibold tabular-nums transition-colors',
              duracion === segundos
                ? 'bg-acento text-sobre-acento'
                : 'bg-superficie-alta text-texto hover:bg-zinc-700',
            )}
          >
            {formatear(segundos)}
          </button>
        ))}
      </div>

      {corriendo ? (
        <Button variante="secundario" tamano="lg" anchoCompleto icono={Pause} onClick={pausar}>
          Pausar
        </Button>
      ) : (
        <Button tamano="lg" anchoCompleto icono={Play} onClick={iniciar}>
          {pausadoCon !== null ? 'Reanudar' : 'Iniciar descanso'}
        </Button>
      )}
    </Card>
  )
}
