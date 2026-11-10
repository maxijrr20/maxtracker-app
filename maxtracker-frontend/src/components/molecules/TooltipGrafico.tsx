import { COLOR_GRAFICO } from '../../lib/coloresGrafico'

export interface TooltipGraficoProps {
  activo?: boolean
  /** Valor ya formateado del punto bajo el puntero. */
  valor?: string
  /** Fecha o categoría del punto. */
  etiqueta?: string
  /** Qué mide la serie ("Peso máximo"...). */
  serie: string
  detalle?: string
}

/**
 * Tooltip de los gráficos: el valor manda (fuerte, alto contraste) y la serie/fecha acompañan.
 * La serie se identifica con un trazo corto de su color, no con una caja. React escapa el texto.
 */
export function TooltipGrafico({ activo, valor, etiqueta, serie, detalle }: TooltipGraficoProps) {
  if (!activo || valor === undefined) return null
  return (
    <div className="rounded-xl border border-borde-vidrio bg-zinc-900/90 px-3 py-2 shadow-2xl shadow-black/50 backdrop-blur-xl">
      {etiqueta && <p className="text-xs text-texto-suave">{etiqueta}</p>}
      <p className="flex items-center gap-2">
        <span aria-hidden className="h-0.5 w-3 rounded-full" style={{ backgroundColor: COLOR_GRAFICO.marca }} />
        <span className="text-base font-semibold text-texto">{valor}</span>
        <span className="text-sm text-texto-suave">{serie}</span>
      </p>
      {detalle && <p className="text-xs text-texto-suave">{detalle}</p>}
    </div>
  )
}
