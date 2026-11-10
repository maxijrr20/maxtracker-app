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

/** Exactamente uno de {@code ejercicioId} (local) o {@code wgerId} (catálogo). */
export type RefEjercicio = { ejercicioId: number; wgerId?: undefined } | { wgerId: number; ejercicioId?: undefined }

export type RegistrarSerieRequest = RefEjercicio & {
  pesoKg: number
  repeticiones: number
  rir?: number | null
}

// --- Rutinas: escritura ---

export type RutinaEjercicioRequest = RefEjercicio & {
  seriesObjetivo: number | null
  repeticionesObjetivo: number | null
}

export interface GuardarRutinaRequest {
  nombre: string
  descripcion: string | null
  ejercicios: RutinaEjercicioRequest[]
}

/** Ejercicio que el usuario ya usó (copia local de wger o personalizado), identificado por su ID local. */
export interface EjercicioLocal {
  ejercicioId: number
  wgerId: number | null
  nombre: string
  categoria: string | null
}

// --- Catálogo (wger vía backend) ---

export interface EjercicioCatalogo {
  /** ID en wger (se envía como {@code wgerId}). */
  id: number
  nombre: string
  descripcion: string | null
  idioma: string | null
  categoriaId: number | null
  categoria: string | null
  musculosPrincipales: string[]
  musculosSecundarios: string[]
  equipamiento: string[]
  imagenUrl: string | null
}

// --- Métricas (/metricas) ---

/** Totales de un periodo. Volumen = Σ peso × repeticiones (kg). */
export interface Periodo {
  volumenKg: number
  sesiones: number
  series: number
}

export interface VolumenSemanal {
  /** Lunes de la semana (YYYY-MM-DD). */
  inicioSemana: string
  volumenKg: number
  sesiones: number
}

export interface RecordPersonal {
  ejercicioId: number
  wgerId: number | null
  ejercicioNombre: string
  categoria: string | null
  pesoMaxKg: number
  repeticiones: number
  /** Primera vez que se logró. */
  fecha: string
  /** Mejor 1RM estimado del historial (Epley). */
  unoRmEstimadoKg: number
}

export interface ResumenMetricas {
  semanaActual: Periodo
  semanaAnterior: Periodo
  mesActual: Periodo
  mesAnterior: Periodo
  volumenSemanal: VolumenSemanal[]
  records: RecordPersonal[]
}

export interface EjercicioConHistorial {
  ejercicioId: number
  wgerId: number | null
  ejercicioNombre: string
  categoria: string | null
  series: number
  ultimaFecha: string
}

export interface PuntoProgreso {
  sesionId: number
  fecha: string
  pesoMaxKg: number
  volumenKg: number
  unoRmEstimadoKg: number
  series: number
}

export interface ProgresoEjercicio {
  ejercicioId: number
  ejercicioNombre: string
  puntos: PuntoProgreso[]
}

// --- Análisis con IA (/analisis/rutina) ---

export type Tendencia = 'PROGRESA' | 'ESTANCADO' | 'RETROCEDE' | 'SIN_DATOS_SUFICIENTES'

export interface AnalisisEjercicio {
  ejercicio: string
  tendencia: Tendencia
  observacion: string | null
  sugerencia: string | null
}

export interface AnalisisRutina {
  alcance: string
  resumen: string
  ejercicios: AnalisisEjercicio[]
  ajusteRir: string | null
  sugerencias: string[]
  dias: number
  seriesAnalizadas: number
  modelo: string
  generadoEn: string
}

/** Como máximo uno de rutinaId o sesionId; sin ninguno, todo lo entrenado en el periodo. */
export interface SolicitudAnalisis {
  rutinaId?: number
  sesionId?: number
  /** 7–90, por defecto 28. */
  dias?: number
}
