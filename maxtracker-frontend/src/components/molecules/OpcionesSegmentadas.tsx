import type { LucideIcon } from 'lucide-react'
import { useId } from 'react'

import { cx } from '../../lib/cx'

export interface OpcionSegmentada<T extends string | number> {
  valor: T
  etiqueta: string
  descripcion?: string
  icono?: LucideIcon
}

export interface OpcionesSegmentadasProps<T extends string | number> {
  etiqueta: string
  ayuda?: string
  opciones: OpcionSegmentada<T>[]
  valor: T
  onCambiar: (valor: T) => void
  /** 'fila': opciones cortas en una fila (kg/lb, segundos). 'lista': tarjetas apiladas con descripción. */
  disposicion?: 'fila' | 'lista'
  /** Cifras en monoespaciada (segundos, unidades). */
  mono?: boolean
}

/**
 * Selector de una opción entre pocas, sobre radios nativos ocultos: flechas del teclado, lector de pantalla y
 * formularios funcionan solos. Cada opción mide al menos 48 px; la elegida se marca en cian tenue.
 */
export function OpcionesSegmentadas<T extends string | number>({
  etiqueta,
  ayuda,
  opciones,
  valor,
  onCambiar,
  disposicion = 'fila',
  mono = false,
}: OpcionesSegmentadasProps<T>) {
  const nombre = useId()
  const idAyuda = `${nombre}-ayuda`

  return (
    <fieldset className="flex flex-col gap-2" aria-describedby={ayuda ? idAyuda : undefined}>
      <legend className="mb-2 text-sm font-medium text-texto">{etiqueta}</legend>
      <div
        className={cx(
          disposicion === 'fila'
            ? 'grid gap-1 rounded-2xl border border-linea bg-casilla p-1'
            : 'flex flex-col gap-2',
        )}
        style={disposicion === 'fila' ? { gridTemplateColumns: `repeat(${opciones.length}, minmax(0, 1fr))` } : undefined}
      >
        {opciones.map((opcion) => {
          const elegida = opcion.valor === valor
          const Icono = opcion.icono
          return (
            <label
              key={String(opcion.valor)}
              className={cx(
                'relative flex cursor-pointer select-none transition-[transform,background-color,color,border-color] duration-150 active:scale-[0.98]',
                'has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-acento',
                disposicion === 'fila'
                  ? 'min-h-tactil items-center justify-center rounded-2xl px-2 text-center font-semibold'
                  : 'min-h-14 items-start gap-3 rounded-2xl border p-3',
                disposicion === 'fila' &&
                  (elegida
                    ? 'bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40'
                    : 'text-texto-suave hover:bg-white/[0.04] hover:text-texto'),
                disposicion === 'lista' &&
                  (elegida
                    ? 'border-acento/50 bg-acento/[0.06]'
                    : 'border-linea bg-casilla/60 hover:border-texto-tenue'),
              )}
            >
              <input
                type="radio"
                name={nombre}
                value={String(opcion.valor)}
                checked={elegida}
                onChange={() => onCambiar(opcion.valor)}
                className="sr-only"
              />
              {disposicion === 'fila' ? (
                <span className={cx(mono && 'font-mono')}>{opcion.etiqueta}</span>
              ) : (
                <>
                  {Icono && (
                    <span
                      className={cx(
                        'grid size-9 shrink-0 place-items-center rounded-xl transition-colors duration-150',
                        elegida ? 'bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40' : 'bg-white/[0.05] text-texto-suave',
                      )}
                    >
                      <Icono aria-hidden className="size-5" />
                    </span>
                  )}
                  <span className="flex min-w-0 flex-1 flex-col gap-0.5">
                    <span className="font-bold text-texto">{opcion.etiqueta}</span>
                    {opcion.descripcion && <span className="text-sm text-texto-suave">{opcion.descripcion}</span>}
                  </span>
                  {/* Marca visible de la elegida además del color. */}
                  <span
                    aria-hidden
                    className={cx(
                      'mt-0.5 grid size-5 shrink-0 place-items-center rounded-full border-2 transition-colors duration-150',
                      elegida ? 'border-acento' : 'border-texto-tenue',
                    )}
                  >
                    {elegida && <span className="size-2.5 rounded-full bg-acento" />}
                  </span>
                </>
              )}
            </label>
          )
        })}
      </div>
      {ayuda && (
        <p id={idAyuda} className="text-sm text-texto-suave">
          {ayuda}
        </p>
      )}
    </fieldset>
  )
}
