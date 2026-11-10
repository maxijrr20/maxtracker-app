import { ArrowDownRight, ArrowUpRight, Minus } from 'lucide-react'

import { cx } from '../../lib/cx'
import { Card } from '../atoms/Card'

export interface StatTileProps {
  etiqueta: string
  valor: string
  /** Variación porcentual frente a {@code referencia}; null = sin base de comparación. */
  variacion?: number | null
  /** Periodo de comparación, p. ej. "vs. semana anterior". */
  referencia?: string
  detalle?: string
  className?: string
}

/**
 * Cifra destacada con su variación. Subir volumen se muestra en verde; bajar, en gris (una semana de descarga
 * no es un error). La dirección también va en ícono y texto: el color nunca es la única señal.
 */
export function StatTile({ etiqueta, valor, variacion, referencia, detalle, className }: StatTileProps) {
  const sube = variacion !== undefined && variacion !== null && variacion > 0
  const baja = variacion !== undefined && variacion !== null && variacion < 0
  const Icono = sube ? ArrowUpRight : baja ? ArrowDownRight : Minus

  return (
    <Card className={cx('flex flex-col gap-1', className)}>
      <span className="text-sm text-texto-suave">{etiqueta}</span>
      {/* Cifras proporcionales (no tabular-nums) en números grandes sueltos. */}
      <span className="text-3xl font-semibold tracking-tight">{valor}</span>
      {variacion !== undefined && (
        <span className={cx('flex items-center gap-1 text-sm', sube ? 'text-exito' : 'text-texto-suave')}>
          <Icono aria-hidden className="size-4 shrink-0" />
          {variacion === null ? (
            <span>Sin datos para comparar</span>
          ) : (
            <span>
              {variacion > 0 ? '+' : ''}
              {variacion}% {referencia}
            </span>
          )}
        </span>
      )}
      {detalle && <span className="text-sm text-texto-suave">{detalle}</span>}
    </Card>
  )
}
