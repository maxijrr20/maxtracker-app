import { LoaderCircle } from 'lucide-react'

import { useServidorDespertando } from '../../hooks/useServidorDespertando'

/**
 * Aviso global mientras el servidor despierta (plan gratuito de Render: ~30-60 s tras un rato sin uso). Va arriba
 * para no tapar la acción primaria ni el aviso de actualización, que están abajo. Desaparece solo al responder.
 */
export function ServidorDespertandoAviso() {
  const despertando = useServidorDespertando()

  return (
    // La región viva existe siempre (vacía si no hay espera) para que el lector de pantalla anuncie el cambio.
    <div role="status" aria-live="polite" className="pointer-events-none fixed inset-x-4 top-[calc(0.75rem+env(safe-area-inset-top))] z-40 mx-auto max-w-md">
      {despertando && (
        <div className="borde-degradado-acento flex items-center gap-3 rounded-superficie bg-superficie-alta p-3 shadow-elevada-alta">
          <LoaderCircle aria-hidden className="size-5 shrink-0 animate-spin text-acento" />
          <p className="flex flex-col text-sm">
            <span className="font-semibold text-texto">Despertando el servidor…</span>
            <span className="text-texto-suave">Tras un rato sin uso puede tardar hasta un minuto.</span>
          </p>
        </div>
      )}
    </div>
  )
}
