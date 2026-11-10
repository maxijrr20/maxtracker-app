import { CalendarCheck, Dumbbell, ListChecks, Play, RotateCw } from 'lucide-react'

import { listarSesiones, obtenerSesion } from '../api/sesiones'
import { useAuth } from '../auth/authContext'
import { Badge } from '../components/atoms/Badge'
import { Button } from '../components/atoms/Button'
import { ButtonLink } from '../components/atoms/ButtonLink'
import { Card } from '../components/atoms/Card'
import { Spinner } from '../components/atoms/Spinner'
import { Texto, Titulo } from '../components/atoms/Typography'
import { Alert } from '../components/molecules/Alert'
import { EmptyState } from '../components/molecules/EmptyState'
import { SesionActivaCard } from '../components/organisms/SesionActivaCard'
import { useCarga } from '../hooks/useCarga'

const FORMATO_FECHA = new Intl.DateTimeFormat('es', { weekday: 'short', day: 'numeric', month: 'short' })

export function DashboardPage() {
  const { email } = useAuth()
  const { datos, cargando, error, recargar } = useCarga(async (signal) => {
    const historial = await listarSesiones(0, 10, signal)
    const resumenEnCurso = historial.contenido.find((sesion) => !sesion.finalizada)
    const enCurso = resumenEnCurso ? await obtenerSesion(resumenEnCurso.id, signal) : null
    return { enCurso, recientes: historial.contenido.filter((sesion) => sesion.finalizada).slice(0, 5) }
  }, [])

  const nombre = email?.split('@')[0]

  return (
    <>
      <div className="flex flex-col gap-1">
        <Titulo>Hola{nombre ? `, ${nombre}` : ''}</Titulo>
        <Texto suave>¿Qué entrenamos hoy?</Texto>
      </div>

      {cargando && <Spinner />}

      {error && (
        <div className="flex flex-col gap-3">
          <Alert>{error}</Alert>
          <Button variante="secundario" icono={RotateCw} onClick={recargar}>
            Reintentar
          </Button>
        </div>
      )}

      {datos && (
        <>
          {datos.enCurso ? (
            <SesionActivaCard
              sesion={datos.enCurso}
              accion={
                <ButtonLink to="/entrenamiento" tamano="lg" anchoCompleto icono={Play}>
                  Continuar entrenamiento
                </ButtonLink>
              }
            />
          ) : (
            <Card className="flex flex-col gap-4">
              <div className="flex items-center gap-3">
                <span className="grid size-tactil place-items-center rounded-xl bg-acento text-sobre-acento">
                  <Dumbbell aria-hidden className="size-6" />
                </span>
                <div>
                  <Titulo nivel={2}>¿Listo para entrenar?</Titulo>
                  <Texto suave tamano="sm">
                    Elige una rutina y registra tus series.
                  </Texto>
                </div>
              </div>
              <ButtonLink to="/rutinas" tamano="lg" anchoCompleto icono={ListChecks}>
                Elegir rutina
              </ButtonLink>
            </Card>
          )}

          <section aria-labelledby="titulo-recientes" className="flex flex-col gap-3">
            <Titulo nivel={2} id="titulo-recientes">
              Últimas sesiones
            </Titulo>
            {datos.recientes.length === 0 ? (
              <EmptyState
                icono={CalendarCheck}
                titulo="Aún no hay sesiones"
                descripcion="Las sesiones que finalices aparecerán aquí."
              />
            ) : (
              <ul className="flex flex-col gap-2">
                {datos.recientes.map((sesion) => (
                  <li key={sesion.id}>
                    <Card className="flex min-h-tactil items-center justify-between gap-3 py-3">
                      <span className="font-medium capitalize">
                        {FORMATO_FECHA.format(new Date(`${sesion.fecha}T12:00:00`))}
                      </span>
                      <Badge tono="exito">Finalizada</Badge>
                    </Card>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </>
  )
}
