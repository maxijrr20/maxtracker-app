import { cx } from '../../lib/cx'

export type VarianteBoton = 'primario' | 'secundario' | 'fantasma' | 'peligro'
export type TamanoBoton = 'md' | 'lg'

const VARIANTES: Record<VarianteBoton, string> = {
  // Única píldora cian de la pantalla, con resplandor: la acción que toca ahora.
  primario: 'bg-acento font-bold text-sobre-acento shadow-primario hover:bg-acento-claro',
  secundario: 'bg-superficie-alta font-semibold text-texto ring-1 ring-inset ring-white/[0.06] hover:bg-linea',
  fantasma: 'font-semibold text-texto-suave hover:bg-white/[0.05] hover:text-texto',
  peligro: 'bg-peligro/10 font-semibold text-peligro ring-1 ring-inset ring-peligro/25 hover:bg-peligro/15',
}

/** md = 48 px de alto (mínimo táctil); lg = 56 px para la acción principal de la pantalla. */
const TAMANOS: Record<TamanoBoton, string> = {
  md: 'min-h-tactil px-5 text-base',
  lg: 'min-h-14 px-6 text-lg',
}

/** Clases compartidas por Button y ButtonLink, para que un enlace "acción" se vea igual que un botón. */
export function clasesBoton(variante: VarianteBoton, tamano: TamanoBoton, anchoCompleto: boolean): string {
  return cx(
    'inline-flex min-w-tactil select-none items-center justify-center gap-2 rounded-full',
    // Compresión al tocar.
    'transition-[transform,background-color,color] duration-150 active:scale-[0.97]',
    'disabled:cursor-not-allowed disabled:opacity-40 disabled:shadow-none',
    VARIANTES[variante],
    TAMANOS[tamano],
    anchoCompleto && 'w-full',
  )
}
