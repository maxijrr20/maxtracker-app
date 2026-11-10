/**
 * Persistencia del JWT en localStorage. Todas las lecturas y escrituras toleran que el almacenamiento
 * no esté disponible (modo privado, cuota llena): en ese caso la sesión simplemente no se recuerda.
 */

const CLAVE = 'maxtracker.sesion'

export interface SesionGuardada {
  token: string
  /** ISO-8601, tal como lo devuelve el backend en {@code expiraEn}. */
  expiraEn: string
  email: string
}

function esSesionGuardada(valor: unknown): valor is SesionGuardada {
  if (typeof valor !== 'object' || valor === null) return false
  const v = valor as Record<string, unknown>
  return typeof v.token === 'string' && typeof v.expiraEn === 'string' && typeof v.email === 'string'
}

function haExpirado(sesion: SesionGuardada): boolean {
  const expira = Date.parse(sesion.expiraEn)
  return Number.isNaN(expira) || expira <= Date.now()
}

/** Sesión guardada y vigente; si expiró o está corrupta, se borra y devuelve {@code null}. */
export function leerSesion(): SesionGuardada | null {
  try {
    const crudo = localStorage.getItem(CLAVE)
    if (!crudo) return null
    const sesion: unknown = JSON.parse(crudo)
    if (!esSesionGuardada(sesion) || haExpirado(sesion)) {
      borrarSesion()
      return null
    }
    return sesion
  } catch {
    return null
  }
}

export function leerToken(): string | null {
  return leerSesion()?.token ?? null
}

export function guardarSesion(sesion: SesionGuardada): void {
  try {
    localStorage.setItem(CLAVE, JSON.stringify(sesion))
  } catch {
    // Sin almacenamiento: la sesión dura lo que dure la pestaña (queda en memoria en AuthContext).
  }
}

export function borrarSesion(): void {
  try {
    localStorage.removeItem(CLAVE)
  } catch {
    // Nada que borrar.
  }
}
