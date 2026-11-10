import { ArrowLeft, Play, RotateCw } from 'lucide-react'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { ApiError } from '../api/http'
import { obtenerRutina } from '../api/rutinas'
import { iniciarSesion } from '../api/sesiones'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Spinner } from '../components/atoms/Spinner'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { RutinaEjercicioList } from '../components/organisms/RutinaEjercicioList'
import { useCarga } from '../hooks/useCarga'

export function RutinaDetallePage() {
  const id = Number(useParams().id)
  const navegar = useNavigate()
  const { datos: rutina, cargando, error, recargar } = useCarga((signal) => obtenerRutina(id, signal), [id])
  const [iniciando, setIniciando] = useState(false)
  const [errorInicio, setErrorInicio] = useState<string | null>(null)

  async function empezar() {
    setIniciando(true)
    setErrorInicio(null)
    try {
      await iniciarSesion({ rutinaId: id })
      navegar('/entrenamiento')
    } catch (causa) {
      setErrorInicio(causa instanceof ApiError ? causa.message : 'No se pudo iniciar la sesión.')
      setIniciando(false)
    }
  }

  return (
    <>
      <ButtonLink to="/rutinas" variante="fantasma" icono={ArrowLeft} className="-ml-3 self-start">
        Rutinas
      </ButtonLink>

      {cargando && <Spinner etiqueta="Cargando rutina…" />}

      {error && (
        <div className="flex flex-col gap-3">
          <Alert>{error}</Alert>
          <Button variante="secundario" icono={RotateCw} onClick={recargar}>
            Reintentar
          </Button>
        </div>
      )}

      {rutina && (
        <>
          <div className="flex flex-col gap-1">
            <Titulo className="break-words">{rutina.nombre}</Titulo>
            {rutina.descripcion && <Texto suave>{rutina.descripcion}</Texto>}
          </div>

          <RutinaEjercicioList ejercicios={rutina.ejercicios} />

          {/* Acción principal fija sobre la barra inferior en móvil: siempre al alcance del pulgar. */}
          <div className="sticky bottom-[calc(5rem+env(safe-area-inset-bottom))] flex flex-col gap-3 md:bottom-6">
            {errorInicio && <Alert>{errorInicio}</Alert>}
            <Button
              tamano="lg"
              anchoCompleto
              icono={Play}
              cargando={iniciando}
              disabled={rutina.ejercicios.length === 0}
              onClick={empezar}
              className="shadow-lg shadow-black/40"
            >
              Empezar entrenamiento
            </Button>
          </div>
        </>
      )}
    </>
  )
}
