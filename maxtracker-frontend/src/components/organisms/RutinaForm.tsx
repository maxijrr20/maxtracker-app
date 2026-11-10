import { ArrowDown, ArrowUp, ListPlus, Plus, Save, Trash2 } from 'lucide-react'
import { useState, type FormEvent } from 'react'

import { ApiError } from '../../api/http'
import type { EjercicioLocal, GuardarRutinaRequest } from '../../api/types'
import type { EjercicioElegido } from '../../lib/ejercicioElegido'
import { Button } from '../atoms/Button'
import { Card } from '../atoms/Card'
import { IconButton } from '../atoms/IconButton'
import { Alert } from '../molecules/Alert'
import { ContadorField } from '../molecules/ContadorField'
import { EmptyState } from '../molecules/EmptyState'
import { FormField } from '../molecules/FormField'
import { EjercicioSelector, type BuscarCatalogo } from './EjercicioSelector'

/** Límites del backend (CrearRutinaRequest / RutinaEjercicioRequest). */
const MAX_NOMBRE = 100
const MAX_DESCRIPCION = 500
const MAX_EJERCICIOS = 30

export interface ItemRutinaForm {
  ejercicio: EjercicioElegido
  seriesObjetivo: number | null
  repeticionesObjetivo: number | null
}

export interface ValoresRutinaForm {
  nombre: string
  descripcion: string
  ejercicios: ItemRutinaForm[]
}

export interface RutinaFormProps {
  inicial?: ValoresRutinaForm
  onGuardar: (rutina: GuardarRutinaRequest) => Promise<void>
  textoGuardar?: string
  buscarCatalogo: BuscarCatalogo
  locales?: EjercicioLocal[]
}

/** Ítem con clave estable para React (los ejercicios pueden repetirse y reordenarse). */
type ItemConClave = ItemRutinaForm & { clave: number }

let siguienteClave = 0
const conClave = (item: ItemRutinaForm): ItemConClave => ({ ...item, clave: siguienteClave++ })

/** Crear o editar una rutina: nombre, descripción y ejercicios en orden con series y repeticiones objetivo. */
export function RutinaForm({ inicial, onGuardar, textoGuardar = 'Guardar rutina', buscarCatalogo, locales }: RutinaFormProps) {
  const [nombre, setNombre] = useState(inicial?.nombre ?? '')
  const [descripcion, setDescripcion] = useState(inicial?.descripcion ?? '')
  const [items, setItems] = useState<ItemConClave[]>(() => (inicial?.ejercicios ?? []).map(conClave))
  const [selectorAbierto, setSelectorAbierto] = useState(false)
  const [errorNombre, setErrorNombre] = useState<string>()
  const [errorDescripcion, setErrorDescripcion] = useState<string>()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  function agregar(ejercicio: EjercicioElegido) {
    setItems((actuales) => [...actuales, conClave({ ejercicio, seriesObjetivo: 3, repeticionesObjetivo: 10 })])
    setSelectorAbierto(false)
  }

  function cambiar(clave: number, cambios: Partial<ItemRutinaForm>) {
    setItems((actuales) => actuales.map((item) => (item.clave === clave ? { ...item, ...cambios } : item)))
  }

  function mover(indice: number, delta: -1 | 1) {
    setItems((actuales) => {
      const destino = indice + delta
      if (destino < 0 || destino >= actuales.length) return actuales
      const copia = [...actuales]
      ;[copia[indice], copia[destino]] = [copia[destino], copia[indice]]
      return copia
    })
  }

  const quitar = (clave: number) => setItems((actuales) => actuales.filter((item) => item.clave !== clave))

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const nombreLimpio = nombre.trim()
    const errNombre = !nombreLimpio
      ? 'Ponle un nombre a la rutina.'
      : nombreLimpio.length > MAX_NOMBRE
        ? `Debe tener como máximo ${MAX_NOMBRE} caracteres.`
        : undefined
    const errDescripcion =
      descripcion.trim().length > MAX_DESCRIPCION ? `Debe tener como máximo ${MAX_DESCRIPCION} caracteres.` : undefined
    setErrorNombre(errNombre)
    setErrorDescripcion(errDescripcion)
    setErrorGeneral(null)
    if (errNombre || errDescripcion) return

    setGuardando(true)
    try {
      await onGuardar({
        nombre: nombreLimpio,
        descripcion: descripcion.trim() || null,
        ejercicios: items.map((item) => ({
          ...item.ejercicio.ref,
          seriesObjetivo: item.seriesObjetivo,
          repeticionesObjetivo: item.repeticionesObjetivo,
        })),
      })
      // La página navega al guardar: el formulario se desmonta.
    } catch (causa) {
      setErrorGeneral(causa instanceof ApiError ? causa.message : 'No se pudo guardar la rutina.')
      setGuardando(false)
    }
  }

  const lleno = items.length >= MAX_EJERCICIOS

  return (
    <>
    <form onSubmit={enviar} noValidate className="flex flex-col gap-6">
      <div className="flex flex-col gap-4">
        <FormField
          etiqueta="Nombre"
          placeholder="Ej.: Empuje, Pierna A…"
          maxLength={MAX_NOMBRE}
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          error={errorNombre}
        />
        <FormField
          etiqueta="Descripción (opcional)"
          maxLength={MAX_DESCRIPCION}
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          error={errorDescripcion}
        />
      </div>

      <section aria-labelledby="titulo-ejercicios-rutina" className="flex flex-col gap-3">
        <div className="flex items-baseline justify-between gap-3">
          <h2 id="titulo-ejercicios-rutina" className="text-xl font-semibold">
            Ejercicios
          </h2>
          <span className="text-sm tabular-nums text-texto-suave">
            {items.length}/{MAX_EJERCICIOS}
          </span>
        </div>

        {items.length === 0 ? (
          <EmptyState
            icono={ListPlus}
            titulo="Sin ejercicios todavía"
            descripcion="Agrega los ejercicios en el orden en que los harás."
          />
        ) : (
          <ol className="flex flex-col gap-3">
            {items.map((item, indice) => (
              <li key={item.clave}>
                <Card className="flex flex-col gap-3">
                  <div className="flex items-start gap-2">
                    <span className="grid size-tactil shrink-0 place-items-center rounded-xl bg-superficie-alta font-bold text-acento">
                      {indice + 1}
                    </span>
                    <div className="flex min-w-0 flex-1 flex-col pt-1">
                      <span className="break-words font-semibold">{item.ejercicio.nombre}</span>
                      {item.ejercicio.categoria && (
                        <span className="text-sm text-texto-suave">{item.ejercicio.categoria}</span>
                      )}
                    </div>
                    <IconButton
                      icono={Trash2}
                      variante="peligro"
                      etiqueta={`Quitar ${item.ejercicio.nombre}`}
                      onClick={() => quitar(item.clave)}
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    <ContadorField
                      etiqueta="Series"
                      valor={item.seriesObjetivo}
                      min={1}
                      max={20}
                      porDefecto={3}
                      onCambiar={(valor) => cambiar(item.clave, { seriesObjetivo: valor })}
                    />
                    <ContadorField
                      etiqueta="Repeticiones"
                      valor={item.repeticionesObjetivo}
                      min={1}
                      max={999}
                      porDefecto={10}
                      onCambiar={(valor) => cambiar(item.clave, { repeticionesObjetivo: valor })}
                    />
                  </div>
                  {items.length > 1 && (
                    <div className="flex justify-end gap-2">
                      <IconButton
                        icono={ArrowUp}
                        variante="secundario"
                        etiqueta={`Subir ${item.ejercicio.nombre}`}
                        disabled={indice === 0}
                        onClick={() => mover(indice, -1)}
                      />
                      <IconButton
                        icono={ArrowDown}
                        variante="secundario"
                        etiqueta={`Bajar ${item.ejercicio.nombre}`}
                        disabled={indice === items.length - 1}
                        onClick={() => mover(indice, 1)}
                      />
                    </div>
                  )}
                </Card>
              </li>
            ))}
          </ol>
        )}

        <Button variante="secundario" icono={Plus} anchoCompleto disabled={lleno} onClick={() => setSelectorAbierto(true)}>
          {lleno ? `Máximo ${MAX_EJERCICIOS} ejercicios` : 'Agregar ejercicio'}
        </Button>
      </section>

      {/* Acción principal fija sobre la barra inferior: siempre al alcance del pulgar. */}
      <div className="sticky bottom-[calc(5rem+env(safe-area-inset-bottom))] flex flex-col gap-3 md:bottom-6">
        {errorGeneral && <Alert>{errorGeneral}</Alert>}
        <Button type="submit" tamano="lg" anchoCompleto icono={Save} cargando={guardando} className="shadow-lg shadow-black/40">
          {textoGuardar}
        </Button>
      </div>
    </form>

    {/* Fuera del <form>: Enter en el buscador no debe enviar la rutina. */}
    <EjercicioSelector
      abierto={selectorAbierto}
      onCerrar={() => setSelectorAbierto(false)}
      onElegir={agregar}
      buscarCatalogo={buscarCatalogo}
      locales={locales}
    />
    </>
  )
}
