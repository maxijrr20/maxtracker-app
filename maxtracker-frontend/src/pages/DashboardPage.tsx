import { Dumbbell, ListChecks, Play, RotateCw } from 'lucide-react'
import { lazy, Suspense, useState } from 'react'

import { listarEjerciciosConHistorial, obtenerProgreso, obtenerResumenMetricas } from '../api/metricas'
import { buscarSesionEnCurso } from '../api/sesiones'
import { useAuth } from '../auth/authContext'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Card } from '../components/atoms/Card'
import { Spinner } from '../components/atoms/Spinner'
import { Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { StatTile } from '../components/molecules/StatTile'
import { RecordsList } from '../components/organisms/RecordsList'
import { SesionActivaCard } from '../components/organisms/SesionActivaCard'
import { useCarga } from '../hooks/useCarga'
import { usePeso } from '../hooks/usePeso'
import { variacionPorcentual } from '../lib/formato'

const SEMANAS = 12

const FORMATO_ENTERO = new Intl.NumberFormat('es', { maximumFractionDigits: 0 })
const formatoEntero = (valor: number) => FORMATO_ENTERO.format(valor)

// recharts pesa ~400 kB: se carga aparte, solo cuando se muestran los gráficos (el resto de la app no lo descarga).
const GraficoVolumenSemanal = lazy(() =>
  import('../components/organisms/GraficoVolumenSemanal').then((m) => ({ default: m.GraficoVolumenSemanal })),
)
const GraficoProgreso = lazy(() =>
  import('../components/organisms/GraficoProgreso').then((m) => ({ default: m.GraficoProgreso })),
)

function detallePeriodo(sesiones: number, series: number): string {
  return `${sesiones} ses. · ${series} series`
}

export function DashboardPage() {
  const { desdeKg, unidad } = usePeso()
  const { email } = useAuth()
  const { datos, cargando, error, recargar } = useCarga(async (signal) => {
    const [enCurso, resumen, ejercicios] = await Promise.all([
      buscarSesionEnCurso(signal),
      obtenerResumenMetricas(SEMANAS, signal),
      listarEjerciciosConHistorial(signal),
    ])
    return { enCurso, resumen, ejercicios }
  }, [])

  // Ejercicio del gráfico de progreso: el elegido, o el entrenado más recientemente.
  const [elegido, setElegido] = useState<number | null>(null)
  const ejercicioId = elegido ?? datos?.ejercicios[0]?.ejercicioId ?? null
  const progreso = useCarga(
    (signal) => (ejercicioId === null ? Promise.resolve(undefined) : obtenerProgreso(ejercicioId, signal)),
    [ejercicioId],
  )

  const nombre = email?.split('@')[0]

  return (
    <>
      <div className="flex flex-col gap-0.5">
        <span className="rotulo">Tu progreso</span>
        <Titulo>Hola{nombre ? `, ${nombre}` : ''}</Titulo>
      </div>

      {cargando && !datos && <Spinner etiqueta="Cargando tus métricas…" />}

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
          {datos.enCurso ? (
            <SesionActivaCard
              sesion={datos.enCurso}
              accion={
                <ButtonLink to="/entrenamiento" tamano="lg" anchoCompleto icono={Play}>
                  Continuar entrenamiento
                </ButtonLink>
              }
            />
          ) : (
            <Card className="flex flex-col gap-3">
              <div className="flex items-center gap-3">
                <Dumbbell aria-hidden className="size-5 shrink-0 text-zinc-500" />
                <div className="min-w-0">
                  <h2 className="text-base font-bold leading-tight">¿Listo para entrenar?</h2>
                  <p className="text-sm text-texto-suave">Elige una rutina o entrena libre.</p>
                </div>
              </div>
              <ButtonLink to="/entrenamiento" tamano="lg" anchoCompleto icono={ListChecks}>
                Empezar a entrenar
              </ButtonLink>
            </Card>
          )}

          {/* Volumen del periodo: dos cifras en una sola superficie, con divisor. */}
          <Card className="flex flex-col gap-2 px-0">
            <h2 id="titulo-volumen" className="sr-only">
              Volumen de entrenamiento
            </h2>
            <div className="grid grid-cols-2 divide-x divide-zinc-800/60">
              <StatTile
                etiqueta="Semana"
                valor={formatoEntero(desdeKg(datos.resumen.semanaActual.volumenKg))}
                unidad={unidad}
                variacion={variacionPorcentual(
                  datos.resumen.semanaActual.volumenKg,
                  datos.resumen.semanaAnterior.volumenKg,
                )}
                referencia="frente a la semana anterior"
                detalle={detallePeriodo(datos.resumen.semanaActual.sesiones, datos.resumen.semanaActual.series)}
              />
              <StatTile
                etiqueta="Mes"
                valor={formatoEntero(desdeKg(datos.resumen.mesActual.volumenKg))}
                unidad={unidad}
                variacion={variacionPorcentual(datos.resumen.mesActual.volumenKg, datos.resumen.mesAnterior.volumenKg)}
                referencia="frente al mes anterior"
                detalle={detallePeriodo(datos.resumen.mesActual.sesiones, datos.resumen.mesActual.series)}
              />
            </div>
            <p className="px-3 text-xs text-zinc-500">
              Volumen = peso × reps. Los periodos en curso se comparan con periodos completos.
            </p>
          </Card>

          <Suspense fallback={<Spinner etiqueta="Cargando gráficos…" />}>
            <GraficoVolumenSemanal semanas={datos.resumen.volumenSemanal} />

            <GraficoProgreso
              ejercicios={datos.ejercicios}
              ejercicioId={ejercicioId}
              onCambiarEjercicio={setElegido}
              progreso={progreso.datos}
              cargando={progreso.cargando}
              error={progreso.error}
            />
          </Suspense>

          <RecordsList records={datos.resumen.records} />
        </>
      )}
    </>
  )
}
