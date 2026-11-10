import { Trophy } from 'lucide-react'
import { useState } from 'react'

import type { RecordPersonal } from '../../api/types'
import { aFecha, fechaCorta, formatoKg } from '../../lib/formato'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { Titulo } from '../atoms/Typography'
import { EmptyState } from '../molecules/EmptyState'

const VISIBLES = 5
const DIAS_NUEVO = 7

export interface RecordsListProps {
  records: RecordPersonal[]
}

/** Récords personales (mayor peso por ejercicio), del más reciente al más antiguo. */
export function RecordsList({ records }: RecordsListProps) {
  const [todos, setTodos] = useState(false)
  // Récords de los últimos días = "Nuevo". El instante se fija al montar (no en cada render).
  const [limiteNuevo] = useState(() => Date.now() - DIAS_NUEVO * 24 * 60 * 60 * 1000)
  const visibles = todos ? records : records.slice(0, VISIBLES)

  return (
    <Card className="flex flex-col gap-3">
      <Titulo nivel={2}>Récords personales</Titulo>

      {records.length === 0 ? (
        <EmptyState
          icono={Trophy}
          titulo="Tus récords aparecerán aquí"
          descripcion="Cada vez que superes tu mayor peso en un ejercicio, lo verás en esta lista."
        />
      ) : (
        <>
          <ul className="flex flex-col divide-y divide-borde">
            {visibles.map((record) => (
              <li key={record.ejercicioId} className="flex min-h-16 items-center gap-3 py-2">
                <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-superficie-alta text-acento">
                  <Trophy aria-hidden className="size-5" />
                </span>
                <div className="flex min-w-0 flex-1 flex-col">
                  <span className="flex flex-wrap items-center gap-2 font-semibold">
                    <span className="break-words">{record.ejercicioNombre}</span>
                    {aFecha(record.fecha).getTime() >= limiteNuevo && <Badge tono="exito">Nuevo</Badge>}
                  </span>
                  <span className="text-sm text-texto-suave">
                    {fechaCorta(record.fecha)} · 1RM est. {formatoKg(record.unoRmEstimadoKg)}
                  </span>
                </div>
                <span className="shrink-0 text-right">
                  <span className="block text-lg font-bold tabular-nums">{formatoKg(record.pesoMaxKg)}</span>
                  <span className="block text-sm tabular-nums text-texto-suave">× {record.repeticiones} reps</span>
                </span>
              </li>
            ))}
          </ul>
          {records.length > VISIBLES && (
            <Button variante="fantasma" anchoCompleto onClick={() => setTodos((t) => !t)} aria-expanded={todos}>
              {todos ? 'Ver menos' : `Ver los ${records.length} récords`}
            </Button>
          )}
        </>
      )}
    </Card>
  )
}
