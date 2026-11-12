import { LoaderCircle } from 'lucide-react'

export function Spinner({ etiqueta = 'Cargando…' }: { etiqueta?: string }) {
  return (
    <div role="status" className="flex items-center justify-center gap-3 py-10 text-texto-suave">
      <LoaderCircle aria-hidden className="size-5 animate-spin text-texto-suave" />
      <span>{etiqueta}</span>
    </div>
  )
}
