import { useCallback, useMemo, useState, type ReactNode } from 'react'

import {
  guardarClaveGemini,
  guardarPreferencias,
  leerClaveGemini,
  leerPreferencias,
  type Preferencias,
} from './preferencias'
import { PreferenciasContext, type EstadoPreferencias } from './preferenciasContext'

export function PreferenciasProvider({ children }: { children: ReactNode }) {
  const [preferencias, setPreferencias] = useState<Preferencias>(leerPreferencias)
  const [claveGemini, setClave] = useState<string | null>(leerClaveGemini)

  const cambiar = useCallback((cambios: Partial<Preferencias>) => {
    setPreferencias((actuales) => {
      const nuevas = { ...actuales, ...cambios }
      guardarPreferencias(nuevas)
      return nuevas
    })
  }, [])

  const setClaveGemini = useCallback((clave: string | null) => {
    guardarClaveGemini(clave)
    setClave(clave)
  }, [])

  const valor = useMemo<EstadoPreferencias>(
    () => ({ ...preferencias, claveGemini, cambiar, setClaveGemini }),
    [preferencias, claveGemini, cambiar, setClaveGemini],
  )

  return <PreferenciasContext.Provider value={valor}>{children}</PreferenciasContext.Provider>
}
