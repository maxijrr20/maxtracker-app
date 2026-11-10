import { ChevronRight, ListChecks, Plus, RotateCw } from 'lucide-react'
import { Link } from 'react-router-dom'

import { listarRutinas } from '../api/rutinas'
import { Badge } from '../components/atoms/Badge'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Spinner } from '../components/atoms/Spinner'
import { Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { EmptyState } from '../components/molecules/EmptyState'
import { useCarga } from '../hooks/useCarga'

export function RutinasPage() {
  const { datos: rutinas, cargando, error, recargar } = useCarga((signal) => listarRutinas(signal), [])

  return (
    <>
      <div className="flex items-end justify-between gap-3">
        <div className="flex min-w-0 flex-col gap-0.5">
          <span className="rotulo">Plantillas</span>
          <Titulo>Rutinas</Titulo>
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

      {/* Una sola superficie para toda la lista; cada fila es el área táctil completa. */}
      {rutinas && rutinas.length > 0 && (
        <ul className="superficie borde-degradado flex flex-col divide-y divide-zinc-800/60 p-1">
          {rutinas.map((rutina) => (
            <li key={rutina.id}>
              <Link
                to={`/rutinas/${rutina.id}`}
                className="flex min-h-14 items-center gap-3 rounded-xl px-3 py-2 transition-[transform,background-color] duration-150 hover:bg-white/[0.03] active:scale-[0.98]"
              >
                <div className="flex min-w-0 flex-1 flex-col">
                  <span className="truncate text-base font-bold">{rutina.nombre}</span>
                  {rutina.descripcion && <span className="truncate text-xs text-texto-suave">{rutina.descripcion}</span>}
                </div>
                <Badge mono>
                  {rutina.cantidadEjercicios}
                  <span aria-hidden className="font-sans font-semibold text-zinc-500">ej.</span>
                  <span className="sr-only">{rutina.cantidadEjercicios === 1 ? ' ejercicio' : ' ejercicios'}</span>
                </Badge>
                <ChevronRight aria-hidden className="size-5 shrink-0 text-zinc-500" />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
