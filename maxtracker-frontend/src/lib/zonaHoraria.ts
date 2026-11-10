import type { ZonaHoraria } from '../preferencias/preferencias'

export function zonaDelDispositivo(): string {
  return Intl.DateTimeFormat().resolvedOptions().timeZone
}

export function zonaEfectiva(zona: ZonaHoraria): string {
  return zona === 'auto' ? zonaDelDispositivo() : zona
}

/**
 * Fecha de hoy (YYYY-MM-DD) en la zona elegida: define a qué día pertenece un entrenamiento que se empieza ahora
 * (p. ej. a las 23:30 en Santiago sigue siendo "hoy" aunque en UTC ya sea mañana).
 */
export function hoyEnZona(zona: ZonaHoraria): string {
  // en-CA formatea como YYYY-MM-DD.
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: zonaEfectiva(zona),
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date())
}

/** Zonas IANA disponibles en el navegador (o una lista mínima si no lo soporta). */
export function zonasDisponibles(): string[] {
  try {
    return Intl.supportedValuesOf('timeZone')
  } catch {
    return ['America/Santiago', 'America/Argentina/Buenos_Aires', 'America/Bogota', 'America/Lima',
      'America/Mexico_City', 'America/New_York', 'Europe/Madrid', 'UTC']
  }
}
