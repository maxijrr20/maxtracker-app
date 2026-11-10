import { CircleCheck, Dumbbell, House, ListChecks, RotateCw } from 'lucide-react'
import { useState } from 'react'

import { ApiError } from '../api/http'
import { obtenerRutina } from '../api/rutinas'
import { buscarSesionEnCurso, eliminarSerie, finalizarSesion, registrarSerie } from '../api/sesiones'
import type { RegistrarSerieRequest, RutinaEjercicio, Serie, Sesion } from '../api/types'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Spinner } from '../components/atoms/Spinner'
import { Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { EjercicioCard } from '../components/molecules/EjercicioCard'
import { EmptyState } from '../components/molecules/EmptyState'
import { SerieRow } from '../components/molecules/SerieRow'
import { DescansoTimer } from '../components/organisms/DescansoTimer'
import { RegistrarSerieForm } from '../components/organisms/RegistrarSerieForm'
import { SesionActivaCard } from '../components/organisms/SesionActivaCard'
import { useCarga } from '../hooks/useCarga'

interface DatosEntrenamiento {
  sesion: Sesion | null
  nombreRutina?: string
}

interface EjercicioDeSesion {
  ejercicioId: number
  nombre: string
  plan?: RutinaEjercicio
  series: Serie[]
}

/** Plan de la rutina en orden y, al final, los ejercicios con series que no estaban en el plan. */
function agrupar(sesion: Sesion): EjercicioDeSesion[] {
  const grupos = new Map<number, EjercicioDeSesion>()
  for (const plan of sesion.ejerciciosRutina) {
    if (!grupos.has(plan.ejercicioId)) {
      grupos.set(plan.ejercicioId, { ejercicioId: plan.ejercicioId, nombre: plan.ejercicioNombre, plan, series: [] })
    }
  }
  for (const serie of sesion.series) {
    const grupo = grupos.get(serie.ejercicioId) ?? { ejercicioId: serie.ejercicioId, nombre: serie.ejercicioNombre, series: [] }
    grupo.series.push(serie)
    grupos.set(serie.ejercicioId, grupo)
  }
  return [...grupos.values()]
}

export function EntrenamientoPage() {
  const { datos, setDatos, cargando, error, recargar } = useCarga<DatosEntrenamiento>(async (signal) => {
    const sesion = await buscarSesionEnCurso(signal)
    if (!sesion?.rutinaId) return { sesion }
    // La rutina pudo borrarse o no estar disponible: el nombre es opcional.
    const nombreRutina = await obtenerRutina(sesion.rutinaId, signal).then((r) => r.nombre, () => undefined)
    return { sesion, nombreRutina }
  }, [])

  const [seriesRegistradas, setSeriesRegistradas] = useState(0)
  const [confirmandoFin, setConfirmandoFin] = useState(false)
  const [finalizando, setFinalizando] = useState(false)
  const [errorAccion, setErrorAccion] = useState<string | null>(null)

  if (cargando) return <Spinner etiqueta="Buscando tu sesión…" />

  if (error || !datos) {
    return (
      <div className="flex flex-col gap-3">
        <Alert>{error ?? 'No se pudo cargar la sesión.'}</Alert>
        <Button variante="secundario" icono={RotateCw} onClick={recargar}>
          Reintentar
        </Button>
      </div>
    )
  }

  const { sesion, nombreRutina } = datos

  if (!sesion) {
    return (
      <>
        <Titulo>Entrenamiento</Titulo>
        <EmptyState
          icono={Dumbbell}
          titulo="No hay una sesión en curso"
          descripcion="Elige una rutina para empezar: sus ejercicios se cargan automáticamente."
          accion={
            <ButtonLink to="/rutinas" tamano="lg" icono={ListChecks}>
              Elegir rutina
            </ButtonLink>
          }
        />
      </>
    )
  }

  const actualizar = (nueva: Sesion) => setDatos({ sesion: nueva, nombreRutina })

  async function registrar(serie: RegistrarSerieRequest) {
    if (!sesion) return
    const nueva = await registrarSerie(sesion.id, serie)
    actualizar({ ...sesion, series: [...sesion.series, nueva] })
    setSeriesRegistradas((n) => n + 1)
  }

  async function quitar(serie: Serie) {
    if (!sesion) return
    setErrorAccion(null)
    try {
      await eliminarSerie(sesion.id, serie.id)
      actualizar({ ...sesion, series: sesion.series.filter((s) => s.id !== serie.id) })
    } catch (causa) {
      setErrorAccion(causa instanceof ApiError ? causa.message : 'No se pudo eliminar la serie.')
    }
  }

  async function finalizar() {
    if (!sesion) return
    // Dos toques: evita finalizar por accidente (no se puede deshacer).
    if (!confirmandoFin) return setConfirmandoFin(true)
    setFinalizando(true)
    setErrorAccion(null)
    try {
      actualizar(await finalizarSesion(sesion.id))
    } catch (causa) {
      setErrorAccion(causa instanceof ApiError ? causa.message : 'No se pudo finalizar la sesión.')
    } finally {
      setFinalizando(false)
      setConfirmandoFin(false)
    }
  }

  const ejercicios = agrupar(sesion)

  return (
    <>
      <h1 className="sr-only">Sesión de entrenamiento</h1>

      <SesionActivaCard
        sesion={sesion}
        nombreRutina={nombreRutina}
        accion={
          sesion.finalizada ? (
            <ButtonLink to="/" tamano="lg" anchoCompleto icono={House}>
              Volver al inicio
            </ButtonLink>
          ) : (
            <Button
              variante={confirmandoFin ? 'primario' : 'secundario'}
              tamano="lg"
              anchoCompleto
              icono={CircleCheck}
              cargando={finalizando}
              onClick={finalizar}
              onBlur={() => setConfirmandoFin(false)}
            >
              {confirmandoFin ? 'Toca de nuevo para finalizar' : 'Finalizar sesión'}
            </Button>
          )
        }
      />

      {errorAccion && <Alert>{errorAccion}</Alert>}

      {!sesion.finalizada && <DescansoTimer disparador={seriesRegistradas} />}

      {ejercicios.length === 0 ? (
        <EmptyState
          icono={Dumbbell}
          titulo="Sesión sin ejercicios"
          descripcion="Esta sesión no tiene una rutina con ejercicios. Inicia una desde Rutinas."
        />
      ) : (
        <ol className="flex flex-col gap-3">
          {ejercicios.map((ejercicio) => {
            const ultima = ejercicio.series.at(-1)
            return (
              <li key={ejercicio.ejercicioId}>
                <EjercicioCard
                  nombre={ejercicio.nombre}
                  categoria={ejercicio.plan?.categoria}
                  orden={ejercicio.plan?.orden}
                  seriesObjetivo={ejercicio.plan?.seriesObjetivo}
                  repeticionesObjetivo={ejercicio.plan?.repeticionesObjetivo}
                  seriesHechas={ejercicio.series.length}
                >
                  {ejercicio.series.length > 0 && (
                    <ul aria-label={`Series de ${ejercicio.nombre}`} className="flex flex-col divide-y divide-borde">
                      {ejercicio.series.map((serie) => (
                        <SerieRow key={serie.id} serie={serie} onEliminar={sesion.finalizada ? undefined : quitar} />
                      ))}
                    </ul>
                  )}
                  {!sesion.finalizada && (
                    <RegistrarSerieForm
                      // Se reinicia tras cada serie para proponer los valores de la última.
                      key={`${ejercicio.ejercicioId}-${ejercicio.series.length}`}
                      ejercicioId={ejercicio.ejercicioId}
                      pesoInicial={ultima?.pesoKg}
                      repeticionesIniciales={ultima?.repeticiones ?? ejercicio.plan?.repeticionesObjetivo ?? undefined}
                      onRegistrar={registrar}
                    />
                  )}
                </EjercicioCard>
              </li>
            )
          })}
        </ol>
      )}
    </>
  )
}
