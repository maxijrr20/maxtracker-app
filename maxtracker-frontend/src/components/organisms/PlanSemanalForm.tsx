import { Check, X } from 'lucide-react'
import { useState, type FormEvent } from 'react'

import { cx } from '../../lib/cx'
import { DIAS_SEMANA } from '../../lib/diasSemana'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { Alert } from '../molecules/Alert'
import { OpcionesSegmentadas } from '../molecules/OpcionesSegmentadas'

const CANTIDADES = [1, 2, 3, 4, 5, 6, 7].map((n) => ({ valor: n, etiqueta: String(n) }))

export interface PlanSemanalFormProps {
  /** Días ISO del plan vigente (1 = lunes … 7 = domingo), para editarlo. */
  inicial?: number[]
  /** Debe lanzar un Error con el mensaje para el usuario si falla. */
  onGuardar: (dias: number[]) => Promise<void>
  onCancelar?: () => void
}

/**
 * Plan semanal en dos preguntas: cuántos días entrenas y cuáles. Solo se puede guardar cuando la cantidad de días
 * elegidos coincide con la indicada.
 */
export function PlanSemanalForm({ inicial, onGuardar, onCancelar }: PlanSemanalFormProps) {
  const [cantidad, setCantidad] = useState(inicial?.length || 3)
  const [elegidos, setElegidos] = useState<number[]>(inicial ?? [])
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const faltan = cantidad - elegidos.length
  const completo = faltan === 0

  function alternar(dia: number) {
    setError(null)
    setElegidos((previos) =>
      previos.includes(dia) ? previos.filter((d) => d !== dia) : [...previos, dia].sort((a, b) => a - b),
    )
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (!completo) {
      return setError(faltan > 0 ? `Elige ${faltan} ${faltan === 1 ? 'día más' : 'días más'}.` : `Quita ${-faltan} ${-faltan === 1 ? 'día' : 'días'}.`)
    }
    setEnviando(true)
    setError(null)
    try {
      await onGuardar(elegidos)
    } catch (causa) {
      setError(causa instanceof Error ? causa.message : 'No se pudo guardar el plan.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Card tono={inicial ? 'normal' : 'foco'} className="p-4">
      <form onSubmit={enviar} noValidate className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold">Tu plan semanal</h2>
          <p className="text-sm text-texto-suave">
            Con tu plan medimos tus días fallados y tu racha. Si lo cambias, rige desde hoy: tus días anteriores no
            cambian.
          </p>
        </div>

        <OpcionesSegmentadas
          etiqueta="¿Cuántos días entrenas a la semana?"
          opciones={CANTIDADES}
          valor={cantidad}
          onCambiar={(n) => {
            setCantidad(n)
            setError(null)
          }}
          mono
        />

        <fieldset className="flex flex-col gap-2">
          <legend className="mb-2 text-sm font-medium text-texto">¿Qué días vas?</legend>
          {/* 4 columnas en móvil: 7 en una fila quedarían por debajo de los 48 px táctiles. */}
          <div className="grid grid-cols-4 gap-2 sm:grid-cols-7">
            {DIAS_SEMANA.map((d) => {
              const activo = elegidos.includes(d.iso)
              return (
                <button
                  key={d.iso}
                  type="button"
                  aria-pressed={activo}
                  aria-label={d.nombre}
                  onClick={() => alternar(d.iso)}
                  className={cx(
                    'min-h-tactil rounded-2xl text-sm font-semibold transition-[transform,background-color,color] duration-150 active:scale-[0.97]',
                    activo
                      ? 'bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40'
                      : 'bg-white/[0.04] text-texto-suave ring-1 ring-inset ring-white/10 hover:bg-white/[0.08] hover:text-texto',
                  )}
                >
                  {d.abreviado}
                </button>
              )
            })}
          </div>
          <p role="status" className={cx('text-sm', completo ? 'text-texto-suave' : 'text-alerta')}>
            {elegidos.length} de {cantidad} {cantidad === 1 ? 'día elegido' : 'días elegidos'}
          </p>
        </fieldset>

        {error && <Alert>{error}</Alert>}

        <div className={cx('grid gap-2', onCancelar ? 'grid-cols-2' : 'grid-cols-1')}>
          {onCancelar && (
            <Button type="button" variante="fantasma" icono={X} onClick={onCancelar}>
              Cancelar
            </Button>
          )}
          <Button type="submit" icono={Check} cargando={enviando}>
            Guardar plan
          </Button>
        </div>
      </form>
    </Card>
  )
}
