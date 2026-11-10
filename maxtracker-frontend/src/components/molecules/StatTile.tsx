import { ArrowDownRight, ArrowUpRight, Minus } from 'lucide-react'

import { cx } from '../../lib/cx'
import { Badge } from '../atoms/Badge'

export interface StatTileProps {
  etiqueta: string
  /** Cifra ya formateada, sin unidad (p. ej. "12.950"). */
  valor: string
  unidad?: string
  /** Variación porcentual frente a {@code referencia}; null = sin base de comparación. */
  variacion?: number | null
  /** Periodo de comparación, p. ej. "vs. sem. ant.". */
  referencia?: string
  detalle?: string
  className?: string
}

/**
 * Cifra de periodo sin contenedor propio: se agrupan 2–3 dentro de una sola superficie con divisores verticales.
 * La variación va en badge con ícono y texto (sube = éxito; baja = neutro: una descarga no es un error).
 */
export function StatTile({ etiqueta, valor, unidad, variacion, referencia, detalle, className }: StatTileProps) {
  const sube = variacion !== undefined && variacion !== null && variacion > 0
  const baja = variacion !== undefined && variacion !== null && variacion < 0
  const Icono = sube ? ArrowUpRight : baja ? ArrowDownRight : Minus

  return (
    <div className={cx('flex min-w-0 flex-col gap-1.5 px-3 py-1', className)}>
      <span className="rotulo truncate">{etiqueta}</span>
      <span className="whitespace-nowrap">
        <span className="cifra text-3xl text-texto">{valor}</span>
        {unidad && <span className="ml-1 text-xs font-semibold text-zinc-500">{unidad}</span>}
      </span>
      {variacion !== undefined && (
        <Badge tono={sube ? 'exito' : 'neutro'} mono className="self-start">
          <Icono aria-hidden className="size-3.5" />
          {variacion === null ? 's/d' : `${variacion > 0 ? '+' : ''}${variacion}%`}
          <span className="sr-only">
            {variacion === null ? ' sin datos para comparar' : ` ${referencia ?? ''}`}
          </span>
        </Badge>
      )}
      {detalle && <span className="text-xs text-texto-suave">{detalle}</span>}
    </div>
  )
}
