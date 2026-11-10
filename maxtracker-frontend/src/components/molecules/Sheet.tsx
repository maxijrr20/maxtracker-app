import { X } from 'lucide-react'
import { useEffect, useId, useRef, type ReactNode } from 'react'

import { cx } from '../../lib/cx'
import { IconButton } from '../atoms/IconButton'

export interface SheetProps {
  abierto: boolean
  onCerrar: () => void
  titulo: string
  children: ReactNode
  /** Zona fija abajo (acción principal), fuera del área con scroll. */
  pie?: ReactNode
  className?: string
}

/**
 * Modal sobre <dialog> nativo (foco atrapado, Esc y fondo inerte gratis). En móvil es una hoja que sube
 * desde abajo y ocupa casi toda la pantalla; desde md es un diálogo centrado.
 */
export function Sheet({ abierto, onCerrar, titulo, children, pie, className }: SheetProps) {
  const ref = useRef<HTMLDialogElement>(null)
  const idTitulo = useId()

  useEffect(() => {
    const dialogo = ref.current
    if (!dialogo) return
    if (abierto && !dialogo.open) {
      dialogo.showModal()
      // Evita que la página de fondo haga scroll mientras la hoja está abierta.
      document.body.style.overflow = 'hidden'
    } else if (!abierto && dialogo.open) {
      dialogo.close()
    }
    return () => {
      document.body.style.overflow = ''
    }
  }, [abierto])

  return (
    <dialog
      ref={ref}
      aria-labelledby={idTitulo}
      // Esc: lo cierra el padre (mantiene el estado "abierto" como única fuente de verdad).
      onCancel={(e) => {
        e.preventDefault()
        onCerrar()
      }}
      // Toque en el fondo oscuro (fuera del contenido) cierra.
      onClick={(e) => {
        if (e.target === e.currentTarget) onCerrar()
      }}
      className={cx(
        'm-0 mt-auto h-[92dvh] max-h-none w-full max-w-none flex-col rounded-t-3xl border border-borde bg-fondo p-0 text-texto open:flex',
        'backdrop:bg-black/75 md:m-auto md:h-auto md:max-h-[85dvh] md:max-w-lg md:rounded-3xl',
        className,
      )}
    >
      <header className="flex items-center justify-between gap-3 border-b border-borde py-2 pl-4 pr-2">
        <h2 id={idTitulo} className="text-lg font-semibold">
          {titulo}
        </h2>
        <IconButton icono={X} etiqueta="Cerrar" onClick={onCerrar} />
      </header>
      <div className="flex-1 overflow-y-auto overscroll-contain p-4">{abierto && children}</div>
      {pie && <footer className="border-t border-borde p-4 pb-[max(1rem,env(safe-area-inset-bottom))]">{pie}</footer>}
    </dialog>
  )
}
