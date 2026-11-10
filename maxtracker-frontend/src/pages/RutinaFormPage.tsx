import { ArrowLeft, RotateCw } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'

import { listarEjercicios, listarEjerciciosDeMisRutinas } from '../api/ejercicios'
import { actualizarRutina, crearRutina, obtenerRutina } from '../api/rutinas'
import type { EjercicioLocal, GuardarRutinaRequest } from '../api/types'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Spinner } from '../components/atoms/Spinner'
import { Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { RutinaForm, type ValoresRutinaForm } from '../components/organisms/RutinaForm'
import { useCarga } from '../hooks/useCarga'

/** /rutinas/nueva y /rutinas/:id/editar. */
export function RutinaFormPage() {
  const parametro = useParams().id
  const id = parametro === undefined ? null : Number(parametro)
  const navegar = useNavigate()

  const { datos, cargando, error, recargar } = useCarga(
    async (signal) => {
      const [rutina, locales] = await Promise.all([
        id === null ? Promise.resolve(null) : obtenerRutina(id, signal),
        // Solo enriquece el buscador ("Tus ejercicios"): si falla, se sigue sin ellos.
        listarEjerciciosDeMisRutinas(signal).catch((): EjercicioLocal[] => []),
      ])
      const inicial: ValoresRutinaForm | undefined = rutina
        ? {
            nombre: rutina.nombre,
            descripcion: rutina.descripcion ?? '',
            ejercicios: rutina.ejercicios.map((e) => ({
              ejercicio: { ref: { ejercicioId: e.ejercicioId }, nombre: e.ejercicioNombre, categoria: e.categoria },
              seriesObjetivo: e.seriesObjetivo,
              repeticionesObjetivo: e.repeticionesObjetivo,
            })),
          }
        : undefined
      return { inicial, locales }
    },
    [id],
  )

  async function guardar(rutina: GuardarRutinaRequest) {
    const guardada = id === null ? await crearRutina(rutina) : await actualizarRutina(id, rutina)
    navegar(`/rutinas/${guardada.id}`, { replace: true })
  }

  return (
    <>
      <ButtonLink
        to={id === null ? '/rutinas' : `/rutinas/${id}`}
        variante="fantasma"
        icono={ArrowLeft}
        className="-ml-3 self-start"
      >
        Cancelar
      </ButtonLink>

      <Titulo>{id === null ? 'Nueva rutina' : 'Editar rutina'}</Titulo>

      {cargando && <Spinner />}

      {error && (
        <div className="flex flex-col gap-3">
          <Alert>{error}</Alert>
          <Button variante="secundario" icono={RotateCw} onClick={recargar}>
            Reintentar
          </Button>
        </div>
      )}

      {datos && (
        <RutinaForm
          inicial={datos.inicial}
          onGuardar={guardar}
          textoGuardar={id === null ? 'Crear rutina' : 'Guardar cambios'}
          buscarCatalogo={listarEjercicios}
          locales={datos.locales}
        />
      )}
    </>
  )
}
