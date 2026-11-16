import { http } from './http'
import { listarRutinas, obtenerRutina } from './rutinas'
import type { EjercicioCatalogo, EjercicioLocal, NuevoEjercicioPropio, Pagina } from './types'

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

// --- Ejercicios propios (no están en wger) ---

interface EjercicioPropioResponse {
  ejercicioId: number
  nombre: string
  categoria: string | null
}

const aLocal = (e: EjercicioPropioResponse): EjercicioLocal => ({ ...e, wgerId: null })

export async function listarEjerciciosPropios(signal?: AbortSignal): Promise<EjercicioLocal[]> {
  const { data } = await http.get<EjercicioPropioResponse[]>('/ejercicios/personalizados', { signal })
  return data.map(aLocal)
}

/** 409 si ya tienes uno con ese nombre (sin distinguir mayúsculas). */
export async function crearEjercicioPropio(nuevo: NuevoEjercicioPropio): Promise<EjercicioLocal> {
  const { data } = await http.post<EjercicioPropioResponse>('/ejercicios/personalizados', nuevo)
  return aLocal(data)
}

/** 409 si se usa en una rutina o tiene series registradas. */
export async function eliminarEjercicioPropio(ejercicioId: number): Promise<void> {
  await http.delete(`/ejercicios/personalizados/${ejercicioId}`)
}

/**
 * "Tus ejercicios" del buscador: los propios más los que aparecen en tus rutinas, sin repetir y por nombre.
 * Los de las rutinas se arman a partir del detalle de cada una (pocas por usuario, se piden en paralelo).
 */
export async function listarMisEjercicios(signal?: AbortSignal): Promise<EjercicioLocal[]> {
  const [propios, resumenes] = await Promise.all([listarEjerciciosPropios(signal), listarRutinas(signal)])
  const rutinas = await Promise.all(resumenes.map((r) => obtenerRutina(r.id, signal)))
  const porId = new Map<number, EjercicioLocal>(propios.map((e) => [e.ejercicioId, e]))
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
