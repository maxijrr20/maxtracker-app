import { cx } from '../../lib/cx'
import { iniciales } from '../../lib/iniciales'

type TamanoAvatar = 'sm' | 'lg'

const TAMANOS: Record<TamanoAvatar, string> = {
  sm: 'size-9 text-sm',
  lg: 'size-14 text-xl',
}

/** Iniciales sobre degradado zinc con un anillo lima fino (decorativo: el texto cercano dice de quién es). */
export function Avatar({ email, tamano = 'sm', className }: { email: string | null; tamano?: TamanoAvatar; className?: string }) {
  return (
    <span
      aria-hidden
      className={cx(
        'grid shrink-0 select-none place-items-center rounded-full bg-linear-to-b from-zinc-700 to-zinc-900',
        'font-mono font-black tracking-tight text-zinc-100 ring-1 ring-lime-400/50 shadow-elevada',
        TAMANOS[tamano],
        className,
      )}
    >
      {iniciales(email)}
    </span>
  )
}
