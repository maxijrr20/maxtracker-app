import { Dumbbell, Flame, Globe, HeartPulse, LogOut, Scale, Smartphone, Sparkles, Timer } from 'lucide-react'
import { useId, useState, type ReactNode } from 'react'

import { useAuth } from '../auth/authContext'
import { Avatar } from '../components/atoms/Avatar'
import { Badge } from '../components/atoms/Badge'
import { Button } from '../components/atoms/Button'
import { Card } from '../components/atoms/Card'
import { Select } from '../components/atoms/Select'
import { OpcionesSegmentadas } from '../components/molecules/OpcionesSegmentadas'
import { ClaveGeminiForm } from '../components/organisms/ClaveGeminiForm'
import { fechaLarga } from '../lib/formato'
import { estaInstalada } from '../lib/pwa'
import { hoyEnZona, zonaDelDispositivo, zonasDisponibles } from '../lib/zonaHoraria'
import { DESCANSOS, type DescansoSegundos, type EnfoqueIa, type UnidadPeso } from '../preferencias/preferencias'
import { usePreferencias } from '../preferencias/preferenciasContext'

/** Encabezado compacto de un bloque de ajustes: rótulo con ícono + una línea de contexto. */
function EncabezadoBloque({ icono: Icono, titulo, descripcion }: { icono: typeof Scale; titulo: string; descripcion: string }) {
  return (
    <div className="flex items-baseline justify-between gap-2">
      <h2 className="rotulo flex items-center gap-1.5">
        <Icono aria-hidden className="size-4" />
        {titulo}
      </h2>
      <p className="text-right text-xs text-zinc-500">{descripcion}</p>
    </div>
  )
}

function Separador() {
  return <hr className="border-zinc-800/60" />
}

function Ajuste({ children }: { children: ReactNode }) {
  return <div className="flex flex-col gap-2">{children}</div>
}

export function PerfilPage() {
  const { email, cerrarSesion } = useAuth()
  const preferencias = usePreferencias()
  const { unidadPeso, descansoSegundos, zonaHoraria, enfoqueIa, claveGemini, cambiar, setClaveGemini } = preferencias
  const [instalada] = useState(estaInstalada)
  const idZona = useId()
  const [zonas] = useState(zonasDisponibles)

  function salir() {
    // La clave es personal: no se queda en el dispositivo al cerrar sesión.
    setClaveGemini(null)
    cerrarSesion()
  }

  return (
    <>
      <h1 className="sr-only">Perfil y ajustes</h1>

      {/* Cabecera de perfil */}
      <Card className="flex flex-col gap-3">
        <div className="flex items-center gap-3">
          <Avatar email={email} tamano="lg" />
          <div className="flex min-w-0 flex-1 flex-col gap-1.5">
            <span className="rotulo">Tu cuenta</span>
            <p className="truncate text-base font-bold">{email}</p>
            <div className="flex flex-wrap gap-1.5">
              <Badge>
                {instalada ? <Smartphone aria-hidden className="size-3.5" /> : <Globe aria-hidden className="size-3.5" />}
                {instalada ? 'App instalada' : 'Versión web'}
              </Badge>
              <Badge>
                <Sparkles aria-hidden className="size-3.5" />
                {claveGemini ? 'IA con tu clave' : 'IA del servidor'}
              </Badge>
            </div>
          </div>
        </div>
        <Button variante="secundario" icono={LogOut} anchoCompleto onClick={salir}>
          Cerrar sesión
        </Button>
      </Card>

      {/* Preferencias */}
      <Card className="flex flex-col gap-4">
        <EncabezadoBloque icono={Scale} titulo="Preferencias" descripcion="Se guardan en este dispositivo." />

        <OpcionesSegmentadas<UnidadPeso>
          etiqueta="Unidad de peso"
          ayuda="Tus datos se guardan en kg: esto solo cambia cómo ves e ingresas las cargas."
          opciones={[
            { valor: 'kg', etiqueta: 'Kilogramos (kg)' },
            { valor: 'lb', etiqueta: 'Libras (lb)' },
          ]}
          valor={unidadPeso}
          onCambiar={(valor) => cambiar({ unidadPeso: valor })}
        />

        <Separador />

        <OpcionesSegmentadas<DescansoSegundos>
          etiqueta="Descanso predeterminado"
          ayuda="Duración con la que arranca el cronómetro entre series."
          opciones={DESCANSOS.map((s) => ({ valor: s, etiqueta: `${s}s` }))}
          valor={descansoSegundos}
          onCambiar={(valor) => cambiar({ descansoSegundos: valor })}
          mono
        />

        <Separador />

        <Ajuste>
          <label htmlFor={idZona} className="flex items-center gap-2 text-sm font-medium text-texto">
            <Timer aria-hidden className="size-4 text-texto-suave" /> Zona horaria
          </label>
          <Select id={idZona} value={zonaHoraria} onChange={(e) => cambiar({ zonaHoraria: e.target.value })}>
            <option value="auto">Automática · {zonaDelDispositivo()}</option>
            {zonas.map((zona) => (
              <option key={zona} value={zona}>
                {zona.replaceAll('_', ' ')}
              </option>
            ))}
          </Select>
          <p className="text-sm text-texto-suave">
            Define qué día cuenta como «hoy» al empezar un entrenamiento. Hoy es{' '}
            <span className="font-medium text-texto">{fechaLarga(hoyEnZona(zonaHoraria))}</span>.
          </p>
        </Ajuste>
      </Card>

      {/* Entrenador IA */}
      <Card className="flex flex-col gap-4">
        <EncabezadoBloque
          icono={Sparkles}
          titulo="Entrenador IA"
          descripcion="Cómo analiza tu progreso el entrenador virtual (Gemini)."
        />

        <OpcionesSegmentadas<EnfoqueIa>
          etiqueta="Enfoque del análisis"
          disposicion="lista"
          opciones={[
            {
              valor: 'FUERZA',
              etiqueta: 'Fuerza',
              icono: Dumbbell,
              descripcion: 'Subir tu carga máxima: 1–6 repeticiones, RIR 1–3 y descansos largos.',
            },
            {
              valor: 'HIPERTROFIA',
              etiqueta: 'Hipertrofia',
              icono: Flame,
              descripcion: 'Ganar músculo: 6–12 repeticiones cerca del fallo y progresión semana a semana.',
            },
            {
              valor: 'MANTENIMIENTO',
              etiqueta: 'Mantenimiento',
              icono: HeartPulse,
              descripcion: 'Sostener tu nivel con menos volumen y esfuerzo; estancarse no es un problema.',
            },
          ]}
          valor={enfoqueIa}
          onCambiar={(valor) => cambiar({ enfoqueIa: valor })}
        />

        <Separador />

        <Ajuste>
          <p className="text-sm font-medium text-texto">Tu clave de Gemini (opcional)</p>
          <p className="text-sm text-texto-suave">
            Con tu propia clave gratuita, los análisis usan tu cuota y no la del servidor. Se guarda solo en este
            dispositivo, viaja únicamente cuando pides un análisis y se borra al cerrar sesión. En el plan gratuito,
            Google puede usar los datos enviados (ejercicios y cargas, nunca tu email) para mejorar sus productos.
          </p>
          <ClaveGeminiForm clave={claveGemini} onGuardar={setClaveGemini} onQuitar={() => setClaveGemini(null)} />
        </Ajuste>
      </Card>
    </>
  )
}
