import { CalendarDays, Pencil, RotateCw } from 'lucide-react'
import { useState } from 'react'

import { guardarPlanEntrenamiento, obtenerConstancia } from '../api/constancia'
import { ApiError } from '../api/http'
import { Button } from '../components/atoms/Button'
import { Card } from '../components/atoms/Card'
import { Spinner } from '../components/atoms/Spinner'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { CalendarioConstancia } from '../components/organisms/CalendarioConstancia'
import { PlanSemanalForm } from '../components/organisms/PlanSemanalForm'
import { ResumenConstancia } from '../components/organisms/ResumenConstancia'
import { useCarga } from '../hooks/useCarga'
import { moverMes, resumirDias } from '../lib/diasSemana'
import { hoyEnZona } from '../lib/zonaHoraria'
import { usePreferencias } from '../preferencias/preferenciasContext'

/** /constancia: plan semanal, rachas, días fallados y calendario de los días entrenados. */
export function ConstanciaPage() {
  const { zonaHoraria } = usePreferencias()
  const hoy = hoyEnZona(zonaHoraria)
  const mesActual = hoy.slice(0, 7)
  const [mes, setMes] = useState(mesActual)
  const [editandoPlan, setEditandoPlan] = useState(false)

  const { datos, cargando, error, recargar } = useCarga((signal) => obtenerConstancia(mes, signal), [mes])

  async function guardarPlan(dias: number[]) {
    try {
      await guardarPlanEntrenamiento(dias)
    } catch (causa) {
      throw new Error(causa instanceof ApiError ? causa.message : 'No se pudo guardar el plan.')
    }
    setEditandoPlan(false)
    recargar()
  }

  return (
    <>
      <div className="flex flex-col gap-1">
        <Titulo>Constancia</Titulo>
        <Texto suave>Los días que fuiste al gym, tus faltas y tu racha.</Texto>
      </div>

      {!datos && cargando && <Spinner etiqueta="Cargando tu calendario…" />}

      {error && (
        <div className="flex flex-col gap-3">
          <Alert>{error}</Alert>
          <Button variante="secundario" icono={RotateCw} onClick={recargar}>
            Reintentar
          </Button>
        </div>
      )}

      {datos && (
        <>
          {!datos.plan || editandoPlan ? (
            <PlanSemanalForm
              inicial={datos.plan?.dias}
              onGuardar={guardarPlan}
              onCancelar={datos.plan ? () => setEditandoPlan(false) : undefined}
            />
          ) : (
            <Card className="flex items-center justify-between gap-3">
              <div className="flex min-w-0 items-center gap-3">
                <CalendarDays aria-hidden className="size-5 shrink-0 text-texto-suave" />
                <p className="flex min-w-0 flex-col">
                  <span className="rotulo">
                    Tu plan · {datos.plan.dias.length} {datos.plan.dias.length === 1 ? 'día' : 'días'} por semana
                  </span>
                  <span className="truncate font-semibold">{resumirDias(datos.plan.dias)}</span>
                </p>
              </div>
              <Button variante="fantasma" icono={Pencil} onClick={() => setEditandoPlan(true)} className="-mr-2">
                Cambiar
              </Button>
            </Card>
          )}

          <ResumenConstancia
            rachaActual={datos.rachaActual}
            mejorRacha={datos.mejorRacha}
            resumenMes={datos.resumenMes}
            resumenTotal={datos.resumenTotal}
          />

          <CalendarioConstancia
            mes={datos.mes}
            dias={datos.dias}
            hoy={hoy}
            onAnterior={() => setMes((m) => moverMes(m, -1))}
            onSiguiente={() => setMes((m) => moverMes(m, 1))}
            // El mes siguiente al actual ya no tiene nada que mostrar más allá del plan.
            puedeSiguiente={mes < mesActual}
            cargando={cargando}
          />
        </>
      )}
    </>
  )
}
