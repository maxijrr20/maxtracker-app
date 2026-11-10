import axios from 'axios'
import { useCallback, useEffect, useState, type DependencyList } from 'react'

import { ApiError } from '../api/http'

export interface Carga<T> {
  datos: T | undefined
  cargando: boolean
  error: string | null
  /** Vuelve a ejecutar la carga (p. ej. botón "Reintentar"). */
  recargar: () => void
  /** Reemplaza los datos localmente tras una mutación, sin volver a pedirlos. */
  setDatos: (datos: T) => void
}

/**
 * Ejecuta una carga asíncrona al montar y cuando cambian las dependencias. Cancela la petición anterior
 * (AbortController) para que una respuesta vieja no pise a una nueva.
 */
export function useCarga<T>(cargar: (signal: AbortSignal) => Promise<T>, dependencias: DependencyList): Carga<T> {
  const [datos, setDatos] = useState<T>()
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [intento, setIntento] = useState(0)

  // eslint-disable-next-line react-hooks/exhaustive-deps -- las dependencias las declara quien llama
  const cargarMemo = useCallback(cargar, dependencias)

  useEffect(() => {
    const controlador = new AbortController()
    setCargando(true)
    setError(null)
    cargarMemo(controlador.signal)
      .then((resultado) => setDatos(resultado))
      .catch((causa: unknown) => {
        if (axios.isCancel(causa)) return
        setError(causa instanceof ApiError ? causa.message : 'Ocurrió un error inesperado.')
      })
      .finally(() => {
        if (!controlador.signal.aborted) setCargando(false)
      })
    return () => controlador.abort()
  }, [cargarMemo, intento])

  const recargar = useCallback(() => setIntento((n) => n + 1), [])

  return { datos, cargando, error, recargar, setDatos }
}
