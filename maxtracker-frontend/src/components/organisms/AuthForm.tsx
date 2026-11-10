import { LogIn, UserPlus } from 'lucide-react'
import { useState, type FormEvent } from 'react'

import { ApiError } from '../../api/http'
import type { Credenciales } from '../../api/types'
import { Button } from '../atoms/Button'
import { Alert } from '../molecules/Alert'
import { FormField } from '../molecules/FormField'

type Modo = 'login' | 'registro'

export interface AuthFormProps {
  modo: Modo
  onEnviar: (credenciales: Credenciales) => Promise<void>
}

interface Errores {
  email?: string
  password?: string
}

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** Mismas reglas que el backend (LoginRequest / RegisterRequest), para avisar antes de enviar. */
function validar({ email, password }: Credenciales, modo: Modo): Errores {
  const errores: Errores = {}
  if (!email.trim()) errores.email = 'Ingresa tu email.'
  else if (!EMAIL_VALIDO.test(email.trim())) errores.email = 'Ingresa un email válido.'
  if (!password) errores.password = 'Ingresa tu contraseña.'
  else if (password.length > 72) errores.password = 'Debe tener como máximo 72 caracteres.'
  else if (modo === 'registro' && password.length < 8) errores.password = 'Debe tener al menos 8 caracteres.'
  return errores
}

/** Formulario de inicio de sesión o registro (email + contraseña). */
export function AuthForm({ modo, onEnviar }: AuthFormProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errores, setErrores] = useState<Errores>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const esRegistro = modo === 'registro'

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const credenciales = { email: email.trim(), password }
    const nuevosErrores = validar(credenciales, modo)
    setErrores(nuevosErrores)
    setErrorGeneral(null)
    if (Object.keys(nuevosErrores).length > 0) return

    setEnviando(true)
    try {
      await onEnviar(credenciales)
      // Al autenticarse, el guard SoloInvitados redirige: este componente se desmonta.
    } catch (causa) {
      setErrorGeneral(causa instanceof ApiError ? causa.message : 'Ocurrió un error inesperado.')
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} noValidate className="flex flex-col gap-5">
      {errorGeneral && <Alert>{errorGeneral}</Alert>}
      <FormField
        etiqueta="Email"
        type="email"
        name="email"
        autoComplete="email"
        inputMode="email"
        autoCapitalize="none"
        spellCheck={false}
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        error={errores.email}
      />
      <FormField
        etiqueta="Contraseña"
        type="password"
        name="password"
        autoComplete={esRegistro ? 'new-password' : 'current-password'}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        ayuda={esRegistro ? 'Entre 8 y 72 caracteres.' : undefined}
        error={errores.password}
      />
      <Button type="submit" tamano="lg" anchoCompleto cargando={enviando} icono={esRegistro ? UserPlus : LogIn}>
        {esRegistro ? 'Crear cuenta' : 'Entrar'}
      </Button>
    </form>
  )
}
