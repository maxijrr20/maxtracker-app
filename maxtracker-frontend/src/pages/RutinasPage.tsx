import { ChevronRight, ListChecks, Plus, RotateCw } from 'lucide-react'
import { Link } from 'react-router-dom'

import { listarRutinas } from '../api/rutinas'
import { Badge } from '../components/atoms/Badge'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Spinner } from '../components/atoms/Spinner'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { EmptyState } from '../components/molecules/EmptyState'
import { useCarga } from '../hooks/useCarga'

export function RutinasPage() {
  const { datos: rutinas, cargando, error, recargar } = useCarga((signal) => listarRutinas(signal), [])

  return (
    <>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="flex flex-col gap-1">
          <Titulo>Rutinas</Titulo>
          <Texto suave>Elige una rutina para ver sus ejercicios y empezar a entrenar.</Texto>
        </div>
        {/* Solo hay botón arriba si ya hay rutinas: con la lista vacía la acción está en el EmptyState. */}
        {rutinas && rutinas.length > 0 && (
          <ButtonLink to="/rutinas/nueva" icono={Plus} className="shrink-0">
            Nueva rutina
          </ButtonLink>
        )}
      </div>

      {cargando && <Spinner etiqueta="Cargando rutinas…" />}

      {error && (
        <div className="flex flex-col gap-3">
          <Alert>{error}</Alert>
          <Button variante="secundario" icono={RotateCw} onClick={recargar}>
            Reintentar
          </Button>
        </div>
      )}

      {rutinas && rutinas.length === 0 && (
        <EmptyState
          icono={ListChecks}
          titulo="Todavía no tienes rutinas"
          descripcion="Crea tu primera rutina con los ejercicios del catálogo y tus series objetivo."
          accion={
            <ButtonLink to="/rutinas/nueva" tamano="lg" icono={Plus}>
              Nueva rutina
            </ButtonLink>
          }
        />
      )}

      {rutinas && rutinas.length > 0 && (
        <ul className="flex flex-col gap-3">
          {rutinas.map((rutina) => (
            <li key={rutina.id}>
              {/* Toda la tarjeta es el área táctil. */}
              <Link
                to={`/rutinas/${rutina.id}`}
                className="vidrio flex min-h-16 items-center gap-3 rounded-2xl border border-borde-vidrio p-4 shadow-2xl shadow-black/40 transition-all duration-200 hover:border-lime-500/30 active:scale-[0.98]"
              >
                <div className="flex min-w-0 flex-1 flex-col gap-1">
                  <span className="break-words text-lg font-semibold">{rutina.nombre}</span>
                  {rutina.descripcion && <span className="text-sm text-texto-suave">{rutina.descripcion}</span>}
                </div>
                <Badge>
                  {rutina.cantidadEjercicios} {rutina.cantidadEjercicios === 1 ? 'ejercicio' : 'ejercicios'}
                </Badge>
                <ChevronRight aria-hidden className="size-6 shrink-0 text-texto-suave" />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
