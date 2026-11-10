import { Dumbbell, ListChecks, Play, RotateCw } from 'lucide-react'
import { lazy, Suspense, useState } from 'react'

import { listarEjerciciosConHistorial, obtenerProgreso, obtenerResumenMetricas } from '../api/metricas'
import { buscarSesionEnCurso } from '../api/sesiones'
import { useAuth } from '../auth/authContext'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Card } from '../components/atoms/Card'
import { Spinner } from '../components/atoms/Spinner'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { StatTile } from '../components/molecules/StatTile'
import { RecordsList } from '../components/organisms/RecordsList'
import { SesionActivaCard } from '../components/organisms/SesionActivaCard'
import { useCarga } from '../hooks/useCarga'
import { usePeso } from '../hooks/usePeso'
import { variacionPorcentual } from '../lib/formato'

const SEMANAS = 12

// recharts pesa ~400 kB: se carga aparte, solo cuando se muestran los gráficos (el resto de la app no lo descarga).
const GraficoVolumenSemanal = lazy(() =>
  import('../components/organisms/GraficoVolumenSemanal').then((m) => ({ default: m.GraficoVolumenSemanal })),
)
const GraficoProgreso = lazy(() =>
  import('../components/organisms/GraficoProgreso').then((m) => ({ default: m.GraficoProgreso })),
)

function detallePeriodo(sesiones: number, series: number): string {
  return `${sesiones} ${sesiones === 1 ? 'sesión' : 'sesiones'} · ${series} series`
}

export function DashboardPage() {
  const { formato: formatoPeso } = usePeso()
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
      <div className="flex flex-col gap-1">
        <Titulo>Hola{nombre ? `, ${nombre}` : ''}</Titulo>
        <Texto suave>Tu progreso de un vistazo.</Texto>
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
            <Card className="flex flex-col gap-4">
              <div className="flex items-center gap-3">
                <span className="grid size-tactil place-items-center rounded-xl bg-acento text-sobre-acento">
                  <Dumbbell aria-hidden className="size-6" />
                </span>
                <div>
                  <Titulo nivel={2}>¿Listo para entrenar?</Titulo>
                  <Texto suave tamano="sm">
                    Elige una rutina o empieza un entrenamiento libre.
                  </Texto>
                </div>
              </div>
              <ButtonLink to="/entrenamiento" tamano="lg" anchoCompleto icono={ListChecks}>
                Empezar a entrenar
              </ButtonLink>
            </Card>
          )}

          <section aria-labelledby="titulo-volumen" className="flex flex-col gap-3">
            <h2 id="titulo-volumen" className="sr-only">
              Volumen de entrenamiento
            </h2>
            <div className="grid grid-cols-1 gap-3 min-[400px]:grid-cols-2">
              <StatTile
                etiqueta="Volumen esta semana"
                valor={formatoPeso(datos.resumen.semanaActual.volumenKg)}
                variacion={variacionPorcentual(
                  datos.resumen.semanaActual.volumenKg,
                  datos.resumen.semanaAnterior.volumenKg,
                )}
                referencia="vs. semana anterior"
                detalle={detallePeriodo(datos.resumen.semanaActual.sesiones, datos.resumen.semanaActual.series)}
              />
              <StatTile
                etiqueta="Volumen este mes"
                valor={formatoPeso(datos.resumen.mesActual.volumenKg)}
                variacion={variacionPorcentual(datos.resumen.mesActual.volumenKg, datos.resumen.mesAnterior.volumenKg)}
                referencia="vs. mes anterior"
                detalle={detallePeriodo(datos.resumen.mesActual.sesiones, datos.resumen.mesActual.series)}
              />
            </div>
            <p className="text-xs text-texto-suave">
              Volumen = peso × repeticiones. Compara periodos en curso con periodos completos: al inicio de la
              semana o del mes es normal que salga más bajo.
            </p>
          </section>

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
