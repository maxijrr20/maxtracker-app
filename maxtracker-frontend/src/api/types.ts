/** Contratos del backend (/api/v1). Mantener en sincronía con los DTOs de Spring. */

export interface AuthResponse {
  token: string
  tipo: 'Bearer'
  expiraEn: string
  email: string
}

export interface Credenciales {
  email: string
  password: string
}

/** Error RFC 9457 (application/problem+json) que devuelve el GlobalExceptionHandler. */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  timestamp?: string
}

export interface Pagina<T> {
  contenido: T[]
  pagina: number
  tamanio: number
  totalElementos: number
  totalPaginas: number
}

// --- Rutinas ---

export interface RutinaResumen {
  id: number
  nombre: string
  descripcion: string | null
  creadoEn: string
  cantidadEjercicios: number
}

export interface RutinaEjercicio {
  orden: number
  ejercicioId: number
  wgerId: number | null
  ejercicioNombre: string
  categoria: string | null
  seriesObjetivo: number | null
  repeticionesObjetivo: number | null
}

export interface Rutina {
  id: number
  nombre: string
  descripcion: string | null
  creadoEn: string
  ejercicios: RutinaEjercicio[]
}

// --- Sesiones ---

export interface Serie {
  id: number
  ejercicioId: number
  wgerId: number | null
  ejercicioNombre: string
  numeroSerie: number
  pesoKg: number
  repeticiones: number
  rir: number | null
}

export interface SesionResumen {
  id: number
  /** YYYY-MM-DD */
  fecha: string
  rutinaId: number | null
  notas: string | null
  iniciadaEn: string
  finalizadaEn: string | null
  finalizada: boolean
}

export interface Sesion extends SesionResumen {
  /** Plan de la rutina para precargar la sesión (vacío si no tiene rutina). */
  ejerciciosRutina: RutinaEjercicio[]
  series: Serie[]
}

export interface RegistrarSerieRequest {
  ejercicioId: number
  pesoKg: number
  repeticiones: number
  rir?: number | null
}
