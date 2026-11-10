/**
 * Colores de los gráficos (recharts los recibe como atributos SVG). Pro Sports Dark: las marcas van en gris neutro
 * y el lima solo marca el punto de foco (semana actual, último punto, PR).
 *
 * Validado con el script de la skill dataviz en modo oscuro: zinc-500 pasa la banda de luminosidad y contraste ≥ 3:1
 * (falla el mínimo de croma a propósito: es el gris de "contexto"). El lima de foco tiene contraste ≫ 3:1.
 */
export const COLOR_GRAFICO = {
  marca: '#71717a', // zinc-500: marcas de contexto
  marcaActiva: '#a1a1aa', // zinc-400: marca bajo el puntero
  foco: '#a3e635', // lime-400: semana actual / último punto / PR
  superficie: '#0c0c0e', // anillo de los puntos (superficie efectiva sobre el fondo)
  rejilla: '#27272a', // zinc-800: hairline
  cursor: '#52525b', // zinc-600: línea vertical del crosshair
  textoEje: '#a1a1aa', // zinc-400
} as const

/** Ejes en la misma mono de las cifras (literal: recharts la pasa como atributo SVG, donde var() no es fiable). */
export const FUENTE_MONO = "'Geist Mono Variable', ui-monospace, monospace"
