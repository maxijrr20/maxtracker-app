import { cx } from '../../lib/cx'

export type VarianteBoton = 'primario' | 'secundario' | 'fantasma' | 'peligro'
export type TamanoBoton = 'md' | 'lg'

const VARIANTES: Record<VarianteBoton, string> = {
  // Único lima de la pantalla: degradado sutil, texto extrabold y sombra de profundidad táctil.
  primario:
    'bg-linear-to-b from-lime-400 to-lime-500 font-extrabold tracking-tight text-sobre-acento shadow-primario ' +
    'hover:from-lime-300 hover:to-lime-400',
  secundario:
    'bg-zinc-900/60 font-semibold text-texto shadow-elevada ring-1 ring-inset ring-white/10 backdrop-blur-2xl ' +
    'hover:bg-zinc-800/60',
  fantasma: 'font-semibold text-texto-suave hover:bg-white/[0.04] hover:text-texto',
  peligro: 'border border-red-500/20 bg-red-500/10 font-semibold text-peligro hover:bg-red-500/15',
}

/** md = 48 px de alto (mínimo táctil); lg = 56 px para la acción principal de la pantalla. */
const TAMANOS: Record<TamanoBoton, string> = {
  md: 'min-h-tactil px-4 text-base',
  lg: 'min-h-14 px-5 text-lg',
}

/** Clases compartidas por Button y ButtonLink, para que un enlace "acción" se vea igual que un botón. */
export function clasesBoton(variante: VarianteBoton, tamano: TamanoBoton, anchoCompleto: boolean): string {
  return cx(
    'inline-flex min-w-tactil select-none items-center justify-center gap-2 rounded-xl',
    // Compresión al tocar.
    'transition-[transform,background-color,color] duration-150 active:scale-[0.98]',
    'disabled:cursor-not-allowed disabled:opacity-40 disabled:shadow-none',
    VARIANTES[variante],
    TAMANOS[tamano],
    anchoCompleto && 'w-full',
  )
}
