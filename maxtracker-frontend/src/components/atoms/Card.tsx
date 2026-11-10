import type { HTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

export function Card({ className, ...resto }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cx('rounded-2xl border border-borde bg-superficie p-4', className)} {...resto} />
}
