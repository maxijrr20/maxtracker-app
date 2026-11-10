import type { HTMLAttributes } from 'react'

import { cx } from '../../lib/cx'

type Nivel = 1 | 2 | 3

const NIVELES: Record<Nivel, string> = {
  1: 'text-2xl font-bold tracking-tight sm:text-3xl',
  2: 'text-xl font-semibold tracking-tight',
  3: 'text-lg font-semibold',
}

export interface TituloProps extends HTMLAttributes<HTMLHeadingElement> {
  nivel?: Nivel
}

/** Encabezado semántico: el nivel define la etiqueta (h1-h3) y el tamaño. Un solo h1 por página. */
export function Titulo({ nivel = 1, className, ...resto }: TituloProps) {
  const Etiqueta = `h${nivel}` as const
  return <Etiqueta className={cx('text-texto', NIVELES[nivel], className)} {...resto} />
}

export interface TextoProps extends HTMLAttributes<HTMLParagraphElement> {
  suave?: boolean
  tamano?: 'sm' | 'base'
}

export function Texto({ suave = false, tamano = 'base', className, ...resto }: TextoProps) {
  return (
    <p
      className={cx(tamano === 'sm' ? 'text-sm' : 'text-base', suave ? 'text-texto-suave' : 'text-texto', className)}
      {...resto}
    />
  )
}
