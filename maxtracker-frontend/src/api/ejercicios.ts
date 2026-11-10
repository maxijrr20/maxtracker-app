import { http } from './http'
import { listarRutinas, obtenerRutina } from './rutinas'
import type { EjercicioCatalogo, EjercicioLocal, Pagina } from './types'

export interface FiltroCatalogo {
  pagina: number
  /** 1–100 */
  tamanio: number
  /** ID de categoría de wger (ver lib/categorias), o undefined para todas. */
  categoria?: number
}

/**
 * Catálogo de wger. El backend solo pagina y filtra por categoría: la búsqueda por nombre
 * la hace el selector sobre las páginas ya cargadas.
 */
export async function listarEjercicios(filtro: FiltroCatalogo, signal?: AbortSignal): Promise<Pagina<EjercicioCatalogo>> {
  const { data } = await http.get<Pagina<EjercicioCatalogo>>('/ejercicios', { params: filtro, signal })
  return data
}


/**
 * Ejercicios de las rutinas del usuario, sin repetir. No hay un endpoint de "mis ejercicios": se arma a partir
 * del detalle de cada rutina (pocas por usuario, se piden en paralelo).
 */
export async function listarEjerciciosDeMisRutinas(signal?: AbortSignal): Promise<EjercicioLocal[]> {
  const resumenes = await listarRutinas(signal)
  const rutinas = await Promise.all(resumenes.map((r) => obtenerRutina(r.id, signal)))
  const porId = new Map<number, EjercicioLocal>()
  for (const item of rutinas.flatMap((r) => r.ejercicios)) {
    if (!porId.has(item.ejercicioId)) {
      porId.set(item.ejercicioId, {
        ejercicioId: item.ejercicioId,
        wgerId: item.wgerId,
        nombre: item.ejercicioNombre,
        categoria: item.categoria,
      })
    }
  }
  return [...porId.values()].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'))
}
