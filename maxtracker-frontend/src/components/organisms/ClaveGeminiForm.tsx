import { Eye, EyeOff, KeyRound, Save, ShieldCheck, Trash2 } from 'lucide-react'
import { useId, useState, type FormEvent } from 'react'

import { PATRON_CLAVE_GEMINI } from '../../preferencias/preferencias'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { IconButton } from '../atoms/IconButton'
import { Input } from '../atoms/Input'

export interface ClaveGeminiFormProps {
  /** Clave guardada en este dispositivo, o null. */
  clave: string | null
  onGuardar: (clave: string) => void
  onQuitar: () => void
}

/**
 * Clave personal de Gemini. Una vez guardada nunca se vuelve a mostrar completa (solo los últimos 4 caracteres):
 * para cambiarla se escribe otra.
 */
export function ClaveGeminiForm({ clave, onGuardar, onQuitar }: ClaveGeminiFormProps) {
  const [editando, setEditando] = useState(clave === null)
  const [texto, setTexto] = useState('')
  const [visible, setVisible] = useState(false)
  const [error, setError] = useState<string>()
  const idCampo = useId()
  const idAyuda = `${idCampo}-ayuda`

  function guardar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const limpia = texto.trim()
    if (!PATRON_CLAVE_GEMINI.test(limpia)) {
      setError('No parece una clave de Gemini: solo letras, números, "-" y "_", de 20 a 200 caracteres.')
      return
    }
    onGuardar(limpia)
    setTexto('')
    setVisible(false)
    setError(undefined)
    setEditando(false)
  }

  if (clave && !editando) {
    return (
      <div className="flex flex-col gap-3">
        <div className="flex min-h-tactil items-center gap-3 rounded-xl border border-zinc-800 bg-zinc-950/80 px-3">
          <ShieldCheck aria-hidden className="size-5 shrink-0 text-exito" />
          <span className="flex-1 font-mono text-sm tracking-wider text-texto">
            <span className="sr-only">Clave guardada que termina en </span>
            ••••••••{clave.slice(-4)}
          </span>
          <Badge tono="exito">En uso</Badge>
        </div>
        <div className="grid grid-cols-2 gap-2">
          <Button variante="secundario" icono={KeyRound} onClick={() => setEditando(true)}>
            Cambiar
          </Button>
          <Button variante="peligro" icono={Trash2} onClick={onQuitar}>
            Quitar
          </Button>
        </div>
      </div>
    )
  }

  return (
    <form onSubmit={guardar} noValidate className="flex flex-col gap-3">
      <label htmlFor={idCampo} className="text-sm font-medium text-texto">
        Clave de API de Gemini
      </label>
      <div className="flex gap-2">
        <Input
          id={idCampo}
          type={visible ? 'text' : 'password'}
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="AIza…"
          autoComplete="off"
          autoCapitalize="none"
          spellCheck={false}
          invalido={Boolean(error)}
          aria-describedby={idAyuda}
          className="font-mono"
        />
        <IconButton
          icono={visible ? EyeOff : Eye}
          etiqueta={visible ? 'Ocultar clave' : 'Mostrar clave'}
          variante="secundario"
          onClick={() => setVisible((v) => !v)}
        />
      </div>
      <p id={idAyuda} className={error ? 'text-sm text-peligro' : 'text-sm text-texto-suave'}>
        {error ?? (
          <>
            Gratis en{' '}
            <a
              href="https://aistudio.google.com/apikey"
              target="_blank"
              rel="noopener noreferrer"
              className="font-semibold text-texto underline underline-offset-4 hover:text-acento"
            >
              aistudio.google.com/apikey
            </a>
            .
          </>
        )}
      </p>
      <div className="grid grid-cols-2 gap-2">
        {clave && (
          <Button variante="fantasma" onClick={() => { setEditando(false); setTexto(''); setError(undefined) }}>
            Cancelar
          </Button>
        )}
        <Button type="submit" variante="secundario" icono={Save} disabled={texto.trim() === ''} className={clave ? '' : 'col-span-2'}>
          Guardar clave
        </Button>
      </div>
    </form>
  )
}
