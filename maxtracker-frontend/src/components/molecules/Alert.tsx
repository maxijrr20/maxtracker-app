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
        'flex items-start gap-3 rounded-2xl border p-4 text-sm',
        tono === 'error'
          ? 'border-peligro/30 bg-peligro/10 text-texto'
          : 'border-acento/30 bg-acento/10 text-texto',
      )}
    >
      <Icono aria-hidden className="mt-0.5 size-5 shrink-0" />
      <div>{children}</div>
    </div>
  )
}
