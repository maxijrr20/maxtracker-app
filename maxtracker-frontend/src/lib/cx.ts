/** Une clases CSS ignorando los valores falsy: cx('a', cond && 'b'). */
export function cx(...clases: Array<string | false | null | undefined>): string {
  return clases.filter(Boolean).join(' ')
}
