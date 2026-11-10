import { createContext, useContext } from 'react'

import type { Credenciales } from '../api/types'

export interface EstadoAuth {
  /** Email del usuario autenticado, o {@code null} si no hay sesión. */
  email: string | null
  autenticado: boolean
  /** La última sesión terminó por un 401 (token expirado o inválido); se limpia al volver a entrar. */
  sesionExpirada: boolean
  iniciarSesion: (credenciales: Credenciales) => Promise<void>
  registrarse: (credenciales: Credenciales) => Promise<void>
  cerrarSesion: () => void
}

export const AuthContext = createContext<EstadoAuth | null>(null)

export function useAuth(): EstadoAuth {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return contexto
}
