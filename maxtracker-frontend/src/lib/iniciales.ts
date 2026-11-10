/** "max.rivas@x.com" → "MR"; "max@x.com" → "MA"; sin email → "?". */
export function iniciales(email: string | null): string {
  const local = (email ?? '').split('@')[0]
  const partes = local.split(/[._\-+]+/).filter(Boolean)
  const letras = partes.length >= 2 ? partes[0][0] + partes[1][0] : local.slice(0, 2)
  return letras.toUpperCase() || '?'
}
