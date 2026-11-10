import { useCallback, useState } from 'react'

import { analizarRutina } from '../api/analisis'
import { ApiError } from '../api/http'
import type { AnalisisRutina, SolicitudAnalisis } from '../api/types'
import { usePreferencias } from '../preferencias/preferenciasContext'

export interface EstadoAnalisisIa {
  analisis: AnalisisRutina | null
  cargando: boolean
  error: string | null
  solicitar: () => void
}

/**
 * Pide el análisis con IA bajo demanda (cada análisis consume la cuota gratuita del proveedor, por eso no se
 * pide al cargar la página). Usa las preferencias del Perfil: enfoque, unidad de peso y la clave personal de
 * Gemini si hay una. El mensaje de error es el del backend: 422 sin datos, 429 espera, 503 IA caída o clave mala.
 */
export function useAnalisisIa(solicitud: Pick<SolicitudAnalisis, 'rutinaId' | 'sesionId' | 'dias'>): EstadoAnalisisIa {
  const { enfoqueIa, unidadPeso, claveGemini } = usePreferencias()
  const [analisis, setAnalisis] = useState<AnalisisRutina | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { rutinaId, sesionId, dias } = solicitud

  const solicitar = useCallback(() => {
    setCargando(true)
    setError(null)
    analizarRutina(
      { rutinaId, sesionId, dias, enfoque: enfoqueIa, unidadPeso: unidadPeso === 'lb' ? 'LB' : 'KG' },
      claveGemini,
    )
      .then(setAnalisis)
      .catch((causa: unknown) =>
        setError(causa instanceof ApiError ? causa.message : 'No se pudo obtener el análisis.'),
      )
      .finally(() => setCargando(false))
  }, [rutinaId, sesionId, dias, enfoqueIa, unidadPeso, claveGemini])

  return { analisis, cargando, error, solicitar }
}
