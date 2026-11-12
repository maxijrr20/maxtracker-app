import { useSyncExternalStore } from 'react'

import { alCambiarEsperaLarga, hayEsperaLarga } from '../api/http'

/**
 * true mientras alguna petición a la API lleva varios segundos sin responder: casi siempre es el backend del plan
 * gratuito de Render arrancando tras un periodo sin uso.
 */
export function useServidorDespertando(): boolean {
  return useSyncExternalStore(alCambiarEsperaLarga, hayEsperaLarga)
}
