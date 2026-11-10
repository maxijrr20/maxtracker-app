import { RefreshCw, WifiOff, X } from 'lucide-react'
import { useRegisterSW } from 'virtual:pwa-register/react'

import { Button } from '../atoms/Button'
import { IconButton } from '../atoms/IconButton'

/**
 * Avisos del service worker: "lista para usar sin conexión" (una vez) y "hay una versión nueva".
 * La actualización la decide el usuario, para no recargar la página en medio de una serie.
 */
export function ActualizacionAviso() {
  const {
    offlineReady: [listaSinConexion, setListaSinConexion],
    needRefresh: [hayVersionNueva, setHayVersionNueva],
    updateServiceWorker,
  } = useRegisterSW()

  if (!listaSinConexion && !hayVersionNueva) return null

  const cerrar = () => {
    setListaSinConexion(false)
    setHayVersionNueva(false)
  }

  return (
    <div
      role="status"
      // Sobre la barra inferior en móvil; abajo a la derecha desde md.
      className="fixed inset-x-4 bottom-[calc(5.5rem+env(safe-area-inset-bottom))] z-30 mx-auto flex max-w-md items-center gap-3 rounded-2xl border border-borde bg-superficie-alta p-3 pl-4 shadow-xl shadow-black/50 md:inset-x-auto md:bottom-6 md:right-6"
    >
      {hayVersionNueva ? (
        <>
          <p className="flex-1 text-sm">Hay una versión nueva de MaxTracker.</p>
          <Button icono={RefreshCw} onClick={() => updateServiceWorker(true)}>
            Actualizar
          </Button>
        </>
      ) : (
        <p className="flex flex-1 items-center gap-2 text-sm">
          <WifiOff aria-hidden className="size-5 shrink-0 text-acento" />
          Lista para abrirse sin conexión.
        </p>
      )}
      <IconButton icono={X} etiqueta="Cerrar aviso" onClick={cerrar} />
    </div>
  )
}
