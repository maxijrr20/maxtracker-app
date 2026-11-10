import { House, SearchX } from 'lucide-react'

import { ButtonLink } from '../components/atoms/ButtonLink'
import { EmptyState } from '../components/molecules/EmptyState'

export function NotFoundPage() {
  return (
    <main className="mx-auto flex min-h-dvh max-w-sm flex-col justify-center px-4">
      <EmptyState
        icono={SearchX}
        titulo="Página no encontrada"
        descripcion="La dirección no existe o fue movida."
        accion={
          <ButtonLink to="/" tamano="lg" icono={House}>
            Ir al inicio
          </ButtonLink>
        }
      />
    </main>
  )
}
