import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'

import * as authApi from '../api/auth'
import { alNoAutorizado } from '../api/http'
import type { AuthResponse, Credenciales } from '../api/types'
import { borrarSesion, guardarSesion, leerSesion } from '../lib/tokenStorage'
import { AuthContext, type EstadoAuth } from './authContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(() => leerSesion()?.email ?? null)
  const [sesionExpirada, setSesionExpirada] = useState(false)

  // Un 401 en cualquier petición autenticada cierra la sesión; las rutas protegidas redirigen a /login.
  useEffect(
    () =>
      alNoAutorizado(() => {
        setEmail(null)
        setSesionExpirada(true)
      }),
    [],
  )

  // El token también puede expirar sin que haya peticiones: se cierra la sesión justo al vencer.
  useEffect(() => {
    const sesion = leerSesion()
    if (!email || !sesion) return
    const restante = Date.parse(sesion.expiraEn) - Date.now()
    const temporizador = window.setTimeout(
      () => {
        borrarSesion()
        setEmail(null)
        setSesionExpirada(true)
      },
      // setTimeout acepta como máximo ~24,8 días.
      Math.min(Math.max(restante, 0), 2_147_483_647),
    )
    return () => window.clearTimeout(temporizador)
  }, [email])

  const aceptar = useCallback((respuesta: AuthResponse) => {
    guardarSesion({ token: respuesta.token, expiraEn: respuesta.expiraEn, email: respuesta.email })
    setSesionExpirada(false)
    setEmail(respuesta.email)
  }, [])

  const iniciarSesion = useCallback(
    async (credenciales: Credenciales) => aceptar(await authApi.login(credenciales)),
    [aceptar],
  )

  const registrarse = useCallback(
    async (credenciales: Credenciales) => aceptar(await authApi.registrar(credenciales)),
    [aceptar],
  )

  const cerrarSesion = useCallback(() => {
    borrarSesion()
    setSesionExpirada(false)
    setEmail(null)
  }, [])

  const valor = useMemo<EstadoAuth>(
    () => ({
      email,
      autenticado: email !== null,
      sesionExpirada,
      iniciarSesion,
      registrarse,
      cerrarSesion,
    }),
    [email, sesionExpirada, iniciarSesion, registrarse, cerrarSesion],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
