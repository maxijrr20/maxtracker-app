import { cx } from '../../lib/cx'

export type VarianteBoton = 'primario' | 'secundario' | 'fantasma' | 'peligro'
export type TamanoBoton = 'md' | 'lg'

const VARIANTES: Record<VarianteBoton, string> = {
  primario: 'bg-acento text-sobre-acento hover:bg-acento-hover active:bg-acento',
  secundario: 'bg-superficie-alta text-texto hover:bg-zinc-700 active:bg-zinc-800',
  fantasma: 'bg-transparent text-texto hover:bg-superficie-alta active:bg-superficie',
  peligro: 'bg-red-500/15 text-peligro hover:bg-red-500/25 active:bg-red-500/15',
}

/** md = 48 px de alto (mínimo táctil); lg = 56 px para la acción principal de la pantalla. */
const TAMANOS: Record<TamanoBoton, string> = {
  md: 'min-h-tactil px-5 text-base',
  lg: 'min-h-14 px-6 text-lg',
}

/** Clases compartidas por Button y ButtonLink, para que un enlace "acción" se vea igual que un botón. */
export function clasesBoton(variante: VarianteBoton, tamano: TamanoBoton, anchoCompleto: boolean): string {
  return cx(
    'inline-flex min-w-tactil select-none items-center justify-center gap-2 rounded-xl font-semibold',
    'transition-colors duration-150 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50',
    VARIANTES[variante],
    TAMANOS[tamano],
    anchoCompleto && 'w-full',
  )
}
