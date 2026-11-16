/** Días de la semana ISO-8601 (1 = lunes … 7 = domingo), igual que el backend. La semana empieza el lunes. */
export const DIAS_SEMANA = [
  { iso: 1, corto: 'L', abreviado: 'Lun', nombre: 'lunes' },
  { iso: 2, corto: 'M', abreviado: 'Mar', nombre: 'martes' },
  { iso: 3, corto: 'X', abreviado: 'Mié', nombre: 'miércoles' },
  { iso: 4, corto: 'J', abreviado: 'Jue', nombre: 'jueves' },
  { iso: 5, corto: 'V', abreviado: 'Vie', nombre: 'viernes' },
  { iso: 6, corto: 'S', abreviado: 'Sáb', nombre: 'sábado' },
  { iso: 7, corto: 'D', abreviado: 'Dom', nombre: 'domingo' },
] as const

/** Día ISO de una fecha "AAAA-MM-DD" (sin zona horaria: es una fecha de calendario). */
export function diaIso(fecha: string): number {
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const domingoCero = new Date(Date.UTC(anio, mes - 1, dia)).getUTCDay()
  return domingoCero === 0 ? 7 : domingoCero
}

/** "Lun · Mié · Vie" */
export function resumirDias(dias: number[]): string {
  return DIAS_SEMANA.filter((d) => dias.includes(d.iso))
    .map((d) => d.abreviado)
    .join(' · ')
}

/** "noviembre de 2026" */
export function nombreMes(mes: string): string {
  const [anio, numero] = mes.split('-').map(Number)
  return new Intl.DateTimeFormat('es', { month: 'long', year: 'numeric', timeZone: 'UTC' }).format(
    new Date(Date.UTC(anio, numero - 1, 1)),
  )
}

/** Mes anterior o siguiente de "AAAA-MM". */
export function moverMes(mes: string, delta: number): string {
  const [anio, numero] = mes.split('-').map(Number)
  const fecha = new Date(Date.UTC(anio, numero - 1 + delta, 1))
  return `${fecha.getUTCFullYear()}-${String(fecha.getUTCMonth() + 1).padStart(2, '0')}`
}
