import { createContext, useContext } from 'react'

import type { Preferencias } from './preferencias'

export interface EstadoPreferencias extends Preferencias {
  /** Clave personal de Gemini guardada en este dispositivo, o null. */
  claveGemini: string | null
  cambiar: (cambios: Partial<Preferencias>) => void
  setClaveGemini: (clave: string | null) => void
}

export const PreferenciasContext = createContext<EstadoPreferencias | null>(null)

export function usePreferencias(): EstadoPreferencias {
  const contexto = useContext(PreferenciasContext)
  if (!contexto) {
    throw new Error('usePreferencias debe usarse dentro de <PreferenciasProvider>')
  }
  return contexto
}
