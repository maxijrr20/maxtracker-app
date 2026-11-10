/**
 * Colores de los gráficos (recharts los recibe como atributos SVG). Mismos valores que los tokens
 * --color-grafico* de index.css: si cambias uno, cambia el otro.
 *
 * Marca: lime-600, validado para modo oscuro sobre zinc-900 con el validador de la skill dataviz
 * (el lime-400 de la UI queda fuera de la banda de luminosidad para marcas de datos).
 */
export const COLOR_GRAFICO = {
  marca: '#65a30d', // lime-600
  marcaActiva: '#84cc16', // lime-500: barra/punto bajo el puntero
  superficie: '#18181b', // zinc-900: anillo de los puntos y separación entre marcas
  rejilla: '#27272a', // zinc-800: hairline, un paso sobre la superficie
  cursor: '#52525b', // zinc-600: línea vertical del crosshair
  textoEje: '#a1a1aa', // zinc-400 (texto-suave)
} as const
