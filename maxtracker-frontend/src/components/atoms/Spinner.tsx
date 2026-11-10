import { LoaderCircle } from 'lucide-react'

export function Spinner({ etiqueta = 'Cargando…' }: { etiqueta?: string }) {
  return (
    <div role="status" className="flex items-center justify-center gap-3 py-10 text-texto-suave">
      <LoaderCircle aria-hidden className="size-6 animate-spin text-acento" />
      <span>{etiqueta}</span>
    </div>
  )
}
