import { cx } from '../../lib/cx'
import { iniciales } from '../../lib/iniciales'

type TamanoAvatar = 'sm' | 'lg'

const TAMANOS: Record<TamanoAvatar, string> = {
  sm: 'size-9 text-sm',
  lg: 'size-20 text-2xl',
}

/** Avatar con degradado lima → esmeralda e iniciales (decorativo: el texto cercano dice de quién es). */
export function Avatar({ email, tamano = 'sm', className }: { email: string | null; tamano?: TamanoAvatar; className?: string }) {
  return (
    <span
      aria-hidden
      className={cx(
        'grid shrink-0 select-none place-items-center rounded-full bg-linear-to-br from-lime-300 via-lime-400 to-emerald-500',
        'font-bold tracking-wide text-zinc-950 shadow-neon ring-2 ring-lime-300/30',
        TAMANOS[tamano],
        className,
      )}
    >
      {iniciales(email)}
    </span>
  )
}
