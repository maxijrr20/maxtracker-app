import { cx } from '../../lib/cx'

export type VarianteBoton = 'primario' | 'secundario' | 'fantasma' | 'peligro'
export type TamanoBoton = 'md' | 'lg'

const VARIANTES: Record<VarianteBoton, string> = {
  // Acción principal: lima con resplandor neón suave y borde más claro (canto de luz); se intensifica al pasar.
  primario:
    'border border-lime-300/70 bg-acento font-bold tracking-wide text-sobre-acento shadow-neon ' +
    'hover:bg-acento-hover hover:shadow-neon-fuerte',
  secundario:
    'border border-borde-vidrio bg-zinc-800/70 font-semibold text-texto backdrop-blur-xl ' +
    'hover:border-zinc-700 hover:bg-zinc-700/70',
  fantasma: 'border border-transparent bg-transparent font-semibold text-texto hover:bg-white/5',
  peligro: 'border border-red-500/25 bg-red-500/10 font-semibold text-peligro hover:bg-red-500/20',
}

/** md = 48 px de alto (mínimo táctil); lg = 56 px para la acción principal de la pantalla. */
const TAMANOS: Record<TamanoBoton, string> = {
  md: 'min-h-tactil px-5 text-base',
  lg: 'min-h-14 px-6 text-lg',
}

/** Clases compartidas por Button y ButtonLink, para que un enlace "acción" se vea igual que un botón. */
export function clasesBoton(variante: VarianteBoton, tamano: TamanoBoton, anchoCompleto: boolean): string {
  return cx(
    'inline-flex min-w-tactil select-none items-center justify-center gap-2 rounded-xl',
    // Respuesta táctil: se hunde al tocar; el resto de transiciones duran 200 ms.
    'transition-all duration-200 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 disabled:shadow-none',
    VARIANTES[variante],
    TAMANOS[tamano],
    anchoCompleto && 'w-full',
  )
}
