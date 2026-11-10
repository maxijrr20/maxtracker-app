import { CircleAlert, Info } from 'lucide-react'
import type { ReactNode } from 'react'

import { cx } from '../../lib/cx'

type Tono = 'error' | 'info'

/** Mensaje en línea. Los errores usan role="alert" para que el lector de pantalla los anuncie. */
export function Alert({ tono = 'error', children }: { tono?: Tono; children: ReactNode }) {
  const Icono = tono === 'error' ? CircleAlert : Info
  return (
    <div
      role={tono === 'error' ? 'alert' : 'status'}
      className={cx(
        'flex items-start gap-3 rounded-xl border p-4 text-sm',
        tono === 'error'
          ? 'border-red-500/30 bg-red-500/10 text-red-200'
          : 'border-sky-500/30 bg-sky-500/10 text-sky-100',
      )}
    >
      <Icono aria-hidden className="mt-0.5 size-5 shrink-0" />
      <div>{children}</div>
    </div>
  )
}
