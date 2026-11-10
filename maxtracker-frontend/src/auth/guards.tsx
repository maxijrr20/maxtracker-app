import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from './authContext'

export interface EstadoRedireccion {
  /** Ruta a la que volver después de iniciar sesión. */
  desde?: string
}

/** Rutas que exigen sesión: sin ella, redirige a /login recordando a dónde se quería ir. */
export function RequiereAuth() {
  const { autenticado } = useAuth()
  const ubicacion = useLocation()
  if (!autenticado) {
    const estado: EstadoRedireccion = { desde: ubicacion.pathname + ubicacion.search }
    return <Navigate to="/login" replace state={estado} />
  }
  return <Outlet />
}

/**
 * /login y /register: con sesión activa se va a la ruta que se intentaba abrir, o al inicio.
 * Así, al iniciar sesión la redirección ocurre sola en cuanto cambia el estado de auth.
 */
export function SoloInvitados() {
  const { autenticado } = useAuth()
  const estado = useLocation().state as EstadoRedireccion | null
  return autenticado ? <Navigate to={estado?.desde ?? '/'} replace /> : <Outlet />
}
