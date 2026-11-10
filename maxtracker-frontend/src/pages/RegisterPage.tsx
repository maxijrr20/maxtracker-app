import { Link, useLocation } from 'react-router-dom'

import { useAuth } from '../auth/authContext'
import { Texto, Titulo } from '../components/atoms/Typography'
import { AuthForm } from '../components/organisms/AuthForm'

export function RegisterPage() {
  const { registrarse } = useAuth()
  const ubicacion = useLocation()

  return (
    <section className="flex flex-col gap-6">
      <div className="flex flex-col gap-1 text-center">
        <Titulo>Crea tu cuenta</Titulo>
        <Texto suave>Empieza a registrar tus entrenamientos.</Texto>
      </div>
      <AuthForm modo="registro" onEnviar={registrarse} />
      <Texto suave className="text-center">
        ¿Ya tienes cuenta?{' '}
        <Link to="/login" state={ubicacion.state} className="inline-flex min-h-tactil items-center font-semibold text-texto underline underline-offset-4 hover:text-acento">
          Inicia sesión
        </Link>
      </Texto>
    </section>
  )
}
