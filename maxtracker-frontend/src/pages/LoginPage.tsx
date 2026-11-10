import { Link, useLocation } from 'react-router-dom'

import { useAuth } from '../auth/authContext'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { AuthForm } from '../components/organisms/AuthForm'

export function LoginPage() {
  const { iniciarSesion, sesionExpirada } = useAuth()
  const ubicacion = useLocation()

  return (
    <section className="flex flex-col gap-6">
      <div className="flex flex-col gap-1 text-center">
        <Titulo>Inicia sesión</Titulo>
        <Texto suave>Registra tus series y sigue tu progreso.</Texto>
      </div>
      {sesionExpirada && <Alert tono="info">Tu sesión expiró. Vuelve a iniciar sesión para continuar.</Alert>}
      <AuthForm modo="login" onEnviar={iniciarSesion} />
      <Texto suave className="text-center">
        ¿No tienes cuenta?{' '}
        {/* Conserva el destino original (state.desde) al cambiar de formulario. */}
        <Link to="/register" state={ubicacion.state} className="inline-flex min-h-tactil items-center font-semibold text-texto underline underline-offset-4 hover:text-acento">
          Crear cuenta
        </Link>
      </Texto>
    </section>
  )
}
