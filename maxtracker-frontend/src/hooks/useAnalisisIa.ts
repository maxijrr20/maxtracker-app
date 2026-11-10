import { useCallback, useState } from 'react'

import { analizarRutina } from '../api/analisis'
import { ApiError } from '../api/http'
import type { AnalisisRutina, SolicitudAnalisis } from '../api/types'

export interface EstadoAnalisisIa {
  analisis: AnalisisRutina | null
  cargando: boolean
  error: string | null
  solicitar: () => void
}

/**
 * Pide el análisis con IA bajo demanda (cada análisis consume la cuota gratuita del proveedor, por eso no se
 * pide al cargar la página). El mensaje de error es el del backend: 422 sin datos, 429 espera, 503 IA caída.
 */
export function useAnalisisIa(solicitud: SolicitudAnalisis): EstadoAnalisisIa {
  const [analisis, setAnalisis] = useState<AnalisisRutina | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { rutinaId, sesionId, dias } = solicitud

  const solicitar = useCallback(() => {
    setCargando(true)
    setError(null)
    analizarRutina({ rutinaId, sesionId, dias })
      .then(setAnalisis)
      .catch((causa: unknown) =>
        setError(causa instanceof ApiError ? causa.message : 'No se pudo obtener el análisis.'),
      )
      .finally(() => setCargando(false))
  }, [rutinaId, sesionId, dias])

  return { analisis, cargando, error, solicitar }
}
