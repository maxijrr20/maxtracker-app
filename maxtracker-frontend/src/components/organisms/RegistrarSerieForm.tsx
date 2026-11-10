import { Plus } from 'lucide-react'
import { useState, type FormEvent } from 'react'

import { ApiError } from '../../api/http'
import type { RefEjercicio, RegistrarSerieRequest } from '../../api/types'
import { usePeso } from '../../hooks/usePeso'
import { MAX_KG } from '../../lib/unidades'
import { Button } from '../atoms/Button'
import { Alert } from '../molecules/Alert'
import { FormField } from '../molecules/FormField'

export interface RegistrarSerieFormProps {
  /** Ejercicio local o de wger (la primera serie de un ejercicio de wger crea su copia local). */
  ejercicio: RefEjercicio
  /** Valores sugeridos (la última serie del ejercicio o el objetivo de la rutina). Peso en kg. */
  pesoInicial?: number
  repeticionesIniciales?: number
  onRegistrar: (serie: RegistrarSerieRequest) => Promise<void>
  /** Ejercicio actual de la sesión: su botón es la acción primaria (lima); el resto, secundario. */
  destacado?: boolean
}

/** Acepta coma o punto decimal (teclados en español). */
function aNumero(texto: string): number {
  return Number(texto.trim().replace(',', '.'))
}

const FORMATO_LIMITE = new Intl.NumberFormat('es', { maximumFractionDigits: 1 })

/**
 * Peso / repeticiones / RIR con las mismas reglas que RegistrarSerieRequest del backend. El peso se escribe en la
 * unidad elegida en Perfil y se envía en kg.
 */
export function RegistrarSerieForm({
  ejercicio,
  pesoInicial,
  repeticionesIniciales,
  onRegistrar,
  destacado = false,
}: RegistrarSerieFormProps) {
  const pesoUnidad = usePeso()
  const textoInicial = pesoInicial === undefined ? '' : pesoUnidad.valorEditable(pesoInicial)
  const [peso, setPeso] = useState(textoInicial)
  const [reps, setReps] = useState(repeticionesIniciales?.toString() ?? '')
  const [rir, setRir] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const valorPeso = aNumero(peso)
    const repeticiones = aNumero(reps)
    const valorRir = rir.trim() === '' ? null : aNumero(rir)
    const maximo = pesoUnidad.desdeKg(MAX_KG)

    if (peso.trim() === '' || !Number.isFinite(valorPeso) || valorPeso < 0 || valorPeso > maximo) {
      return setError(`El peso debe estar entre 0 y ${FORMATO_LIMITE.format(maximo)} ${pesoUnidad.unidad}.`)
    }
    if (!Number.isInteger(repeticiones) || repeticiones < 1 || repeticiones > 999) {
      return setError('Las repeticiones deben estar entre 1 y 999.')
    }
    if (valorRir !== null && (!Number.isInteger(valorRir) || valorRir < 0 || valorRir > 10)) {
      return setError('El RIR debe estar entre 0 y 10.')
    }

    // Sin tocar el peso precargado se reenvía el valor exacto en kg: convertir lb → kg → lb lo desplazaría
    // (80 kg → 176,4 lb → 80,01 kg).
    const pesoKg = pesoInicial !== undefined && peso === textoInicial ? pesoInicial : pesoUnidad.aKg(valorPeso)

    setError(null)
    setEnviando(true)
    try {
      await onRegistrar({ ...ejercicio, pesoKg, repeticiones, rir: valorRir })
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
        <FormField
          etiqueta={`Peso (${pesoUnidad.unidad})`}
          inputMode="decimal"
          value={peso}
          onChange={(e) => setPeso(e.target.value)}
          className="font-mono font-bold"
        />
        <FormField etiqueta="Reps" inputMode="numeric" value={reps} onChange={(e) => setReps(e.target.value)} className="font-mono font-bold" />
        <FormField etiqueta="RIR" inputMode="numeric" placeholder="–" value={rir} onChange={(e) => setRir(e.target.value)} className="font-mono font-bold" />
      </div>
      <Button type="submit" variante={destacado ? 'primario' : 'secundario'} anchoCompleto icono={Plus} cargando={enviando}>
        Registrar serie
      </Button>
    </form>
  )
}
