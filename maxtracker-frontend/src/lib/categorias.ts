/**
 * Grupos musculares = categorías de wger. Los IDs son los de wger (se envían como ?categoria=) y los nombres
 * coinciden con la traducción que hace el backend (WgerEjercicioMapper.CATEGORIAS_ES).
 */
export const CATEGORIAS = [
  { id: 11, nombre: 'Pecho' },
  { id: 12, nombre: 'Espalda' },
  { id: 9, nombre: 'Piernas' },
  { id: 13, nombre: 'Hombros' },
  { id: 8, nombre: 'Brazos' },
  { id: 10, nombre: 'Abdominales' },
  { id: 14, nombre: 'Pantorrillas' },
  { id: 15, nombre: 'Cardio' },
] as const

/** Minúsculas y sin tildes, para comparar búsquedas ("PRESS banca" encuentra "Press de banca", "biceps" → "bíceps"). */
export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().trim()
}
