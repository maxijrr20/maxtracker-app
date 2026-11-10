import { CircleCheck, Dumbbell, House, ListChecks, Plus, RotateCw, X, Zap } from 'lucide-react'
import { useState } from 'react'

import { listarEjercicios, listarEjerciciosDeMisRutinas } from '../api/ejercicios'
import { ApiError } from '../api/http'
import { obtenerRutina } from '../api/rutinas'
import { buscarSesionEnCurso, eliminarSerie, finalizarSesion, iniciarSesion, registrarSerie } from '../api/sesiones'
import type { EjercicioLocal, RefEjercicio, RegistrarSerieRequest, RutinaEjercicio, Serie, Sesion } from '../api/types'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Spinner } from '../components/atoms/Spinner'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { EjercicioCard } from '../components/molecules/EjercicioCard'
import { EmptyState } from '../components/molecules/EmptyState'
import { SerieRow } from '../components/molecules/SerieRow'
import { DescansoTimer } from '../components/organisms/DescansoTimer'
import { EjercicioSelector } from '../components/organisms/EjercicioSelector'
import { RegistrarSerieForm } from '../components/organisms/RegistrarSerieForm'
import { SesionActivaCard } from '../components/organisms/SesionActivaCard'
import { useCarga } from '../hooks/useCarga'
import { claveEjercicio, type EjercicioElegido } from '../lib/ejercicioElegido'
import { guardarPendientes, leerPendientes } from '../lib/ejerciciosPendientes'

interface DatosEntrenamiento {
  sesion: Sesion | null
  nombreRutina?: string
}

interface EjercicioDeSesion {
  clave: string
  ref: RefEjercicio
  nombre: string
  categoria: string | null
  plan?: RutinaEjercicio
  series: Serie[]
  /** Agregado al vuelo y todavía sin series (solo existe en este dispositivo). */
  pendiente?: EjercicioElegido
}

/**
 * Tarjeta de la sesión que corresponde a un ejercicio. Un ejercicio de wger puede estar ya como copia local
 * (en el plan o con series), así que se compara también por wgerId.
 */
function buscarGrupo(ref: RefEjercicio, grupos: EjercicioDeSesion[]): EjercicioDeSesion | undefined {
  const { ejercicioId, wgerId } = ref
  return grupos.find((g) =>
    ejercicioId !== undefined
      ? g.ref.ejercicioId === ejercicioId
      : g.ref.wgerId === wgerId || g.plan?.wgerId === wgerId || g.series.some((s) => s.wgerId === wgerId),
  )
}

/** Plan de la rutina en orden, luego los ejercicios con series fuera del plan y al final los agregados sin series. */
function agrupar(sesion: Sesion, pendientes: EjercicioElegido[]): EjercicioDeSesion[] {
  const grupos = new Map<number, EjercicioDeSesion>()
  const nuevo = (ejercicioId: number, nombre: string, categoria: string | null): EjercicioDeSesion => ({
    clave: `local-${ejercicioId}`,
    ref: { ejercicioId },
    nombre,
    categoria,
    series: [],
  })
  for (const plan of sesion.ejerciciosRutina) {
    if (!grupos.has(plan.ejercicioId)) {
      grupos.set(plan.ejercicioId, { ...nuevo(plan.ejercicioId, plan.ejercicioNombre, plan.categoria), plan })
    }
  }
  for (const serie of sesion.series) {
    const grupo = grupos.get(serie.ejercicioId) ?? nuevo(serie.ejercicioId, serie.ejercicioNombre, null)
    grupo.series.push(serie)
    grupos.set(serie.ejercicioId, grupo)
  }
  const resultado = [...grupos.values()]
  for (const pendiente of pendientes) {
    if (!buscarGrupo(pendiente.ref, resultado)) {
      resultado.push({
        clave: claveEjercicio(pendiente.ref),
        ref: pendiente.ref,
        nombre: pendiente.nombre,
        categoria: pendiente.categoria,
        series: [],
        pendiente,
      })
    }
  }
  return resultado
}

export function EntrenamientoPage() {
  const { datos, setDatos, cargando, error, recargar } = useCarga<DatosEntrenamiento>(async (signal) => {
    const sesion = await buscarSesionEnCurso(signal)
    if (!sesion?.rutinaId) return { sesion }
    // La rutina pudo borrarse o no estar disponible: el nombre es opcional.
    const nombreRutina = await obtenerRutina(sesion.rutinaId, signal).then((r) => r.nombre, () => undefined)
    return { sesion, nombreRutina }
  }, [])

  // Solo enriquece el buscador ("Tus ejercicios"); si falla, el catálogo sigue disponible.
  const { datos: locales } = useCarga(
    (signal) => listarEjerciciosDeMisRutinas(signal).catch((): EjercicioLocal[] => []),
    [],
  )

  const [pendientesEnMemoria, setPendientesEnMemoria] = useState<{ sesionId: number; items: EjercicioElegido[] } | null>(null)
  const [selectorAbierto, setSelectorAbierto] = useState(false)
  const [seriesRegistradas, setSeriesRegistradas] = useState(0)
  const [confirmandoFin, setConfirmandoFin] = useState(false)
  const [finalizando, setFinalizando] = useState(false)
  const [iniciandoLibre, setIniciandoLibre] = useState(false)
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

  async function empezarLibre() {
    setIniciandoLibre(true)
    setErrorAccion(null)
    try {
      setDatos({ sesion: await iniciarSesion() })
      setSelectorAbierto(true)
    } catch (causa) {
      setErrorAccion(causa instanceof ApiError ? causa.message : 'No se pudo iniciar la sesión.')
    } finally {
      setIniciandoLibre(false)
    }
  }

  if (!sesion) {
    return (
      <>
        <div className="flex flex-col gap-1">
          <Titulo>Entrenamiento</Titulo>
          <Texto suave>No hay una sesión en curso.</Texto>
        </div>
        {errorAccion && <Alert>{errorAccion}</Alert>}
        <EmptyState
          icono={Dumbbell}
          titulo="¿Cómo quieres entrenar?"
          descripcion="Empieza libre y agrega ejercicios sobre la marcha, o usa una rutina con su plan precargado."
          accion={
            <div className="flex w-full flex-col gap-2 sm:w-auto">
              <Button tamano="lg" icono={Zap} cargando={iniciandoLibre} onClick={empezarLibre}>
                Entrenamiento libre
              </Button>
              <ButtonLink to="/rutinas" variante="secundario" tamano="lg" icono={ListChecks}>
                Elegir rutina
              </ButtonLink>
            </div>
          }
        />
      </>
    )
  }

  // Desde aquí hay sesión: TypeScript no estrecha `sesion` dentro de las funciones, se fija en una constante.
  const actual: Sesion = sesion
  const pendientes =
    pendientesEnMemoria?.sesionId === actual.id ? pendientesEnMemoria.items : leerPendientes(actual.id)

  function setPendientes(items: EjercicioElegido[]) {
    guardarPendientes(actual.id, items)
    setPendientesEnMemoria({ sesionId: actual.id, items })
  }

  const actualizar = (nueva: Sesion) => setDatos({ sesion: nueva, nombreRutina })

  function agregarEjercicio(ejercicio: EjercicioElegido) {
    setSelectorAbierto(false)
    const existente = buscarGrupo(ejercicio.ref, agrupar(actual, pendientes))
    if (!existente) setPendientes([...pendientes, ejercicio])
    // Lleva la vista al ejercicio (nuevo o el que ya estaba) para registrar la serie. Espera al re-render.
    const destino = existente?.clave ?? claveEjercicio(ejercicio.ref)
    requestAnimationFrame(() =>
      document.getElementById(`ejercicio-${destino}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' }),
    )
  }

  async function registrar(serie: RegistrarSerieRequest) {
    const nueva = await registrarSerie(actual.id, serie)
    actualizar({ ...actual, series: [...actual.series, nueva] })
    setSeriesRegistradas((n) => n + 1)
    // Con su primera serie, el ejercicio ya existe en la sesión: deja de estar pendiente.
    const restantes = pendientes.filter((p) =>
      p.ref.ejercicioId !== undefined ? p.ref.ejercicioId !== nueva.ejercicioId : p.ref.wgerId !== nueva.wgerId,
    )
    if (restantes.length !== pendientes.length) setPendientes(restantes)
  }

  async function quitarSerie(serie: Serie) {
    setErrorAccion(null)
    try {
      await eliminarSerie(actual.id, serie.id)
      actualizar({ ...actual, series: actual.series.filter((s) => s.id !== serie.id) })
    } catch (causa) {
      setErrorAccion(causa instanceof ApiError ? causa.message : 'No se pudo eliminar la serie.')
    }
  }

  async function finalizar() {
    // Dos toques: evita finalizar por accidente (no se puede deshacer).
    if (!confirmandoFin) return setConfirmandoFin(true)
    setFinalizando(true)
    setErrorAccion(null)
    try {
      actualizar(await finalizarSesion(actual.id))
      setPendientes([])
    } catch (causa) {
      setErrorAccion(causa instanceof ApiError ? causa.message : 'No se pudo finalizar la sesión.')
    } finally {
      setFinalizando(false)
      setConfirmandoFin(false)
    }
  }

  const ejercicios = agrupar(actual, pendientes)
  const enCurso = !actual.finalizada

  return (
    <>
      <h1 className="sr-only">Sesión de entrenamiento</h1>

      <SesionActivaCard
        sesion={actual}
        nombreRutina={nombreRutina}
        accion={
          actual.finalizada ? (
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

      {enCurso && <DescansoTimer disparador={seriesRegistradas} />}

      {ejercicios.length === 0 ? (
        <EmptyState
          icono={Dumbbell}
          titulo="Sin ejercicios todavía"
          descripcion={enCurso ? 'Agrega el primer ejercicio desde el catálogo.' : 'Esta sesión no tuvo series.'}
          accion={
            enCurso && (
              <Button tamano="lg" icono={Plus} onClick={() => setSelectorAbierto(true)}>
                Agregar ejercicio
              </Button>
            )
          }
        />
      ) : (
        <ol className="flex flex-col gap-3">
          {ejercicios.map((ejercicio) => {
            const ultima = ejercicio.series.at(-1)
            return (
              <li key={ejercicio.clave} id={`ejercicio-${ejercicio.clave}`} className="scroll-mt-20">
                <EjercicioCard
                  nombre={ejercicio.nombre}
                  categoria={ejercicio.plan?.categoria ?? ejercicio.categoria}
                  orden={ejercicio.plan?.orden}
                  seriesObjetivo={ejercicio.plan?.seriesObjetivo}
                  repeticionesObjetivo={ejercicio.plan?.repeticionesObjetivo}
                  seriesHechas={ejercicio.series.length}
                >
                  {ejercicio.series.length > 0 && (
                    <ul aria-label={`Series de ${ejercicio.nombre}`} className="flex flex-col divide-y divide-borde">
                      {ejercicio.series.map((serie) => (
                        <SerieRow key={serie.id} serie={serie} onEliminar={enCurso ? quitarSerie : undefined} />
                      ))}
                    </ul>
                  )}
                  {enCurso && (
                    <RegistrarSerieForm
                      // Se reinicia tras cada serie para proponer los valores de la última.
                      key={`${ejercicio.clave}-${ejercicio.series.length}`}
                      ejercicio={ejercicio.ref}
                      pesoInicial={ultima?.pesoKg}
                      repeticionesIniciales={ultima?.repeticiones ?? ejercicio.plan?.repeticionesObjetivo ?? undefined}
                      onRegistrar={registrar}
                    />
                  )}
                  {enCurso && ejercicio.pendiente && (
                    <Button
                      variante="fantasma"
                      icono={X}
                      onClick={() => setPendientes(pendientes.filter((p) => p !== ejercicio.pendiente))}
                    >
                      Quitar ejercicio
                    </Button>
                  )}
                </EjercicioCard>
              </li>
            )
          })}
        </ol>
      )}

      {enCurso && ejercicios.length > 0 && (
        <Button variante="secundario" tamano="lg" anchoCompleto icono={Plus} onClick={() => setSelectorAbierto(true)}>
          Agregar ejercicio
        </Button>
      )}

      <EjercicioSelector
        abierto={selectorAbierto}
        onCerrar={() => setSelectorAbierto(false)}
        onElegir={agregarEjercicio}
        buscarCatalogo={listarEjercicios}
        locales={locales}
      />
    </>
  )
}
