import { Plus } from 'lucide-react'
import { useState, type FormEvent } from 'react'

import { ApiError } from '../../api/http'
import type { RegistrarSerieRequest } from '../../api/types'
import { Button } from '../atoms/Button'
import { Alert } from '../molecules/Alert'
import { FormField } from '../molecules/FormField'

export interface RegistrarSerieFormProps {
  ejercicioId: number
  /** Valores sugeridos (la última serie del ejercicio o el objetivo de la rutina). */
  pesoInicial?: number
  repeticionesIniciales?: number
  onRegistrar: (serie: RegistrarSerieRequest) => Promise<void>
}

/** Acepta coma o punto decimal (teclados en español). */
function aNumero(texto: string): number {
  return Number(texto.trim().replace(',', '.'))
}

/** Peso / repeticiones / RIR con las mismas reglas que RegistrarSerieRequest del backend. */
export function RegistrarSerieForm({ ejercicioId, pesoInicial, repeticionesIniciales, onRegistrar }: RegistrarSerieFormProps) {
  const [peso, setPeso] = useState(pesoInicial?.toString() ?? '')
  const [reps, setReps] = useState(repeticionesIniciales?.toString() ?? '')
  const [rir, setRir] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const pesoKg = aNumero(peso)
    const repeticiones = aNumero(reps)
    const valorRir = rir.trim() === '' ? null : aNumero(rir)

    if (peso.trim() === '' || !Number.isFinite(pesoKg) || pesoKg < 0 || pesoKg > 9999.99) {
      return setError('El peso debe estar entre 0 y 9999,99 kg.')
    }
    if (!Number.isInteger(repeticiones) || repeticiones < 1 || repeticiones > 999) {
      return setError('Las repeticiones deben estar entre 1 y 999.')
    }
    if (valorRir !== null && (!Number.isInteger(valorRir) || valorRir < 0 || valorRir > 10)) {
      return setError('El RIR debe estar entre 0 y 10.')
    }

    setError(null)
    setEnviando(true)
    try {
      await onRegistrar({ ejercicioId, pesoKg: Math.round(pesoKg * 100) / 100, repeticiones, rir: valorRir })
      setRir('')
    } catch (causa) {
      setError(causa instanceof ApiError ? causa.message : 'No se pudo registrar la serie.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} noValidate className="flex flex-col gap-3">
      {error && <Alert>{error}</Alert>}
      <div className="grid grid-cols-3 gap-2">
        <FormField etiqueta="Peso (kg)" inputMode="decimal" value={peso} onChange={(e) => setPeso(e.target.value)} />
        <FormField etiqueta="Reps" inputMode="numeric" value={reps} onChange={(e) => setReps(e.target.value)} />
        <FormField etiqueta="RIR" inputMode="numeric" placeholder="–" value={rir} onChange={(e) => setRir(e.target.value)} />
      </div>
      <Button type="submit" anchoCompleto icono={Plus} cargando={enviando}>
        Registrar serie
      </Button>
    </form>
  )
}
