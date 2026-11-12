import { cx } from '../../lib/cx'
import { iniciales } from '../../lib/iniciales'

type TamanoAvatar = 'sm' | 'lg'

const TAMANOS: Record<TamanoAvatar, string> = {
  sm: 'size-9 text-sm',
  lg: 'size-14 text-xl',
}

/** Iniciales sobre degradado índigo con un anillo cian (decorativo: el texto cercano dice de quién es). */
export function Avatar({ email, tamano = 'sm', className }: { email: string | null; tamano?: TamanoAvatar; className?: string }) {
  return (
    <span
      aria-hidden
      className={cx(
        'grid shrink-0 select-none place-items-center rounded-full bg-linear-to-br from-acento-profundo to-superficie-alta',
        'font-mono font-bold tracking-tight text-texto ring-2 ring-acento/40 shadow-elevada',
        TAMANOS[tamano],
        className,
      )}
    >
      {iniciales(email)}
    </span>
  )
}
