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
 * formularios funcionan solos. Cada opción mide al menos 48 px; la elegida brilla en lima.
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
            ? 'grid gap-1 rounded-2xl border border-borde-vidrio bg-zinc-950/50 p-1'
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
                'relative flex cursor-pointer select-none transition-all duration-200 active:scale-95',
                'has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-acento',
                disposicion === 'fila'
                  ? 'min-h-tactil items-center justify-center rounded-xl px-2 text-center font-semibold'
                  : 'min-h-16 items-start gap-3 rounded-2xl border p-4',
                disposicion === 'fila' &&
                  (elegida
                    ? 'bg-acento text-sobre-acento shadow-neon'
                    : 'text-texto-suave hover:bg-white/5 hover:text-texto'),
                disposicion === 'lista' &&
                  (elegida
                    ? 'border-lime-400/50 bg-lime-500/10 shadow-neon'
                    : 'border-borde-vidrio bg-zinc-900/50 hover:border-zinc-700'),
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
                        'grid size-10 shrink-0 place-items-center rounded-xl transition-all duration-200',
                        elegida ? 'bg-acento text-sobre-acento' : 'bg-zinc-800 text-texto-suave',
                      )}
                    >
                      <Icono aria-hidden className="size-5" />
                    </span>
                  )}
                  <span className="flex min-w-0 flex-1 flex-col gap-0.5">
                    <span className={cx('font-semibold', elegida ? 'text-acento' : 'text-texto')}>{opcion.etiqueta}</span>
                    {opcion.descripcion && <span className="text-sm text-texto-suave">{opcion.descripcion}</span>}
                  </span>
                  {/* Marca visible de la elegida además del color. */}
                  <span
                    aria-hidden
                    className={cx(
                      'mt-0.5 grid size-5 shrink-0 place-items-center rounded-full border-2 transition-all duration-200',
                      elegida ? 'border-acento' : 'border-zinc-600',
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
