/**
 * Colores de los gráficos (recharts los recibe como atributos SVG, donde var() no es fiable: van literales).
 * Pulso: las barras de contexto en gris azulado, la serie de progreso en cian y el récord en coral.
 *
 * Contraste sobre la superficie #141b2e: marca 3,8:1, linea (cian) 9,5:1, record (coral) 6,4:1 — todos ≥ 3:1.
 */
export const COLOR_GRAFICO = {
  marca: '#6b7696', // texto-tenue: barras de contexto
  marcaActiva: '#8b95b5', // texto-suave: barra bajo el puntero
  foco: '#22d3ee', // acento: semana actual / último punto
  linea: '#22d3ee', // acento: línea de progreso de un ejercicio
  lineaActiva: '#67e8f9', // acento-claro: punto bajo el puntero
  record: '#fb7185', // record: mejor valor (PR)
  superficie: '#141b2e', // anillo de los puntos (color de la tarjeta)
  rejilla: '#1c2440', // linea-suave: hairline
  cursor: '#26304f', // linea: línea vertical del crosshair
  textoEje: '#8b95b5', // texto-suave
} as const

/** Ejes en la misma mono de las cifras (literal: recharts la pasa como atributo SVG). */
export const FUENTE_MONO = "'Geist Mono Variable', ui-monospace, monospace"
