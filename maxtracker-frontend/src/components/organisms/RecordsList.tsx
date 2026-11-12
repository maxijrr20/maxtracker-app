import { Trophy } from 'lucide-react'
import { useState } from 'react'

import type { RecordPersonal } from '../../api/types'
import { usePeso } from '../../hooks/usePeso'
import { aFecha, fechaCorta } from '../../lib/formato'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { EmptyState } from '../molecules/EmptyState'

const VISIBLES = 5
const DIAS_NUEVO = 7

export interface RecordsListProps {
  records: RecordPersonal[]
}

/** Récords personales (mayor peso por ejercicio), del más reciente al más antiguo. */
export function RecordsList({ records }: RecordsListProps) {
  const { formato: formatoPeso } = usePeso()
  const [todos, setTodos] = useState(false)
  // Récords de los últimos días = "Nuevo". El instante se fija al montar (no en cada render).
  const [limiteNuevo] = useState(() => Date.now() - DIAS_NUEVO * 24 * 60 * 60 * 1000)
  const visibles = todos ? records : records.slice(0, VISIBLES)

  return (
    <Card className="flex flex-col gap-2">
      <h2 className="rotulo">Récords personales</h2>

      {records.length === 0 ? (
        <EmptyState
          icono={Trophy}
          titulo="Tus récords aparecerán aquí"
          descripcion="Cada vez que superes tu mayor peso en un ejercicio, lo verás en esta lista."
        />
      ) : (
        <>
          <ul className="flex flex-col divide-y divide-linea-suave">
            {visibles.map((record) => {
              // PR reciente = coral (color exclusivo de los récords); los demás, en neutro.
              const reciente = aFecha(record.fecha).getTime() >= limiteNuevo
              return (
                <li key={record.ejercicioId} className="flex min-h-14 items-center gap-3 py-1.5">
                  <Trophy aria-hidden className={reciente ? 'size-5 shrink-0 text-record' : 'size-5 shrink-0 text-texto-tenue'} />
                  <div className="flex min-w-0 flex-1 flex-col">
                    <span className="flex items-center gap-2">
                      <span className="truncate font-semibold">{record.ejercicioNombre}</span>
                      {reciente && (
                        <Badge tono="record" mono>
                          PR
                        </Badge>
                      )}
                    </span>
                    <span className="font-mono text-xs text-texto-suave">
                      {fechaCorta(record.fecha)} · 1RM {formatoPeso(record.unoRmEstimadoKg)}
                    </span>
                  </div>
                  <span className="shrink-0 text-right">
                    <span className={reciente ? 'cifra block text-xl text-record' : 'cifra block text-xl text-texto'}>
                      {formatoPeso(record.pesoMaxKg)}
                    </span>
                    <span className="block font-mono text-xs font-bold text-texto-suave">×{record.repeticiones}</span>
                  </span>
                </li>
              )
            })}
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
