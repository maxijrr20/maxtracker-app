import { ChevronDown } from 'lucide-react'

export interface TablaGraficoProps {
  /** Descripción de la tabla para lectores de pantalla. */
  titulo: string
  columnas: string[]
  filas: string[][]
}

/**
 * Los mismos datos del gráfico como tabla, plegada por defecto. Hace que ningún valor dependa de ver el color
 * o de pasar el puntero (lectores de pantalla, daltonismo, impresión).
 */
export function TablaGrafico({ titulo, columnas, filas }: TablaGraficoProps) {
  return (
    <details className="group rounded-2xl bg-white/[0.03] ring-1 ring-inset ring-white/[0.06]">
      <summary className="flex min-h-tactil cursor-pointer list-none items-center justify-between gap-2 px-3 text-sm font-semibold text-texto-suave hover:text-texto [&::-webkit-details-marker]:hidden">
        Ver como tabla
        <ChevronDown aria-hidden className="size-5 transition-transform group-open:rotate-180" />
      </summary>
      <div className="max-h-72 overflow-auto border-t border-linea-suave">
        <table className="w-full text-sm">
          <caption className="sr-only">{titulo}</caption>
          <thead className="sticky top-0 bg-superficie text-left text-texto-suave">
            <tr>
              {columnas.map((columna, i) => (
                <th key={columna} scope="col" className={i === 0 ? 'px-4 py-2 font-medium' : 'px-4 py-2 text-right font-medium'}>
                  {columna}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-linea-suave font-mono font-bold">
            {filas.map((fila) => (
              <tr key={fila[0]}>
                {fila.map((celda, i) =>
                  i === 0 ? (
                    <th key={i} scope="row" className="px-4 py-2 text-left font-normal">
                      {celda}
                    </th>
                  ) : (
                    <td key={i} className="px-4 py-2 text-right tabular-nums">
                      {celda}
                    </td>
                  ),
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </details>
  )
}
