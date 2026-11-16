import { Check, X } from 'lucide-react'
import { useId, useState, type FormEvent } from 'react'

import type { NuevoEjercicioPropio } from '../../api/types'
import { CATEGORIAS } from '../../lib/categorias'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { Select } from '../atoms/Select'
import { Alert } from '../molecules/Alert'
import { FormField } from '../molecules/FormField'

const LARGO_MAXIMO = 150

export interface NuevoEjercicioFormProps {
  /** Lo que el usuario estaba buscando: suele ser justo el nombre que quiere crear. */
  nombreInicial?: string
  /** Debe lanzar un Error con el mensaje para el usuario si falla (p. ej. nombre repetido). */
  onCrear: (nuevo: NuevoEjercicioPropio) => Promise<void>
  onCancelar: () => void
}

/**
 * Alta de un ejercicio propio (que no está en el catálogo): nombre y grupo muscular opcional. El grupo usa los mismos
 * nombres del catálogo, así el ejercicio se filtra y lleva el mismo color que los de wger.
 */
export function NuevoEjercicioForm({ nombreInicial = '', onCrear, onCancelar }: NuevoEjercicioFormProps) {
  const [nombre, setNombre] = useState(nombreInicial)
  const [categoria, setCategoria] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [errorNombre, setErrorNombre] = useState<string | undefined>()
  const idGrupo = useId()

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const limpio = nombre.trim()
    if (!limpio) return setErrorNombre('Escribe un nombre.')
    if (limpio.length > LARGO_MAXIMO) return setErrorNombre(`Máximo ${LARGO_MAXIMO} caracteres.`)
    setEnviando(true)
    setError(null)
    try {
      await onCrear({ nombre: limpio, categoria: categoria || null })
    } catch (causa) {
      setError(causa instanceof Error ? causa.message : 'No se pudo crear el ejercicio.')
      setEnviando(false)
    }
  }

  return (
    <Card tono="foco" className="p-4">
      <form onSubmit={enviar} noValidate className="flex flex-col gap-3">
        <h3 className="text-base font-bold">Nuevo ejercicio propio</h3>
        <FormField
          etiqueta="Nombre"
          value={nombre}
          onChange={(e) => {
            setNombre(e.target.value)
            setErrorNombre(undefined)
          }}
          error={errorNombre}
          maxLength={LARGO_MAXIMO}
          autoComplete="off"
          enterKeyHint="done"
          autoFocus
        />
        <div className="flex flex-col gap-2">
          <label htmlFor={idGrupo} className="rotulo">
            Grupo muscular
          </label>
          <Select id={idGrupo} value={categoria} onChange={(e) => setCategoria(e.target.value)}>
            <option value="">Sin grupo</option>
            {CATEGORIAS.map((c) => (
              <option key={c.id} value={c.nombre}>
                {c.nombre}
              </option>
            ))}
          </Select>
        </div>
        {error && <Alert>{error}</Alert>}
        <div className="grid grid-cols-2 gap-2">
          <Button type="button" variante="fantasma" icono={X} onClick={onCancelar}>
            Cancelar
          </Button>
          <Button type="submit" icono={Check} cargando={enviando}>
            Crear
          </Button>
        </div>
      </form>
    </Card>
  )
}
