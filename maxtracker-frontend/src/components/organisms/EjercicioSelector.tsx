import axios from 'axios'
import { RotateCw, Search, SearchX } from 'lucide-react'
import { useDeferredValue, useEffect, useMemo, useRef, useState } from 'react'

import type { EjercicioCatalogo, EjercicioLocal, Pagina } from '../../api/types'
import { CATEGORIAS, normalizar } from '../../lib/categorias'
import type { EjercicioElegido } from '../../lib/ejercicioElegido'
import { Button } from '../atoms/Button'
import { Chip } from '../atoms/Chip'
import { Input } from '../atoms/Input'
import { Spinner } from '../atoms/Spinner'
import { Alert } from '../molecules/Alert'
import { EjercicioOpcion } from '../molecules/EjercicioOpcion'
import { EmptyState } from '../molecules/EmptyState'
import { Sheet } from '../molecules/Sheet'

const TAMANIO_PAGINA = 100

interface Pedido {
  categoriaId?: number
  pagina: number
  /** Identifica el pedido: cambia en cada carga solicitada. */
  n: number
  completa: boolean
}

export type BuscarCatalogo = (
  filtro: { pagina: number; tamanio: number; categoria?: number },
  signal: AbortSignal,
) => Promise<Pagina<EjercicioCatalogo>>

export interface EjercicioSelectorProps {
  abierto: boolean
  onCerrar: () => void
  onElegir: (ejercicio: EjercicioElegido) => void
  /** Consulta al catálogo (la página pasa la función de la API: los componentes no llaman a la API). */
  buscarCatalogo: BuscarCatalogo
  /** Ejercicios que el usuario ya usa (de sus rutinas): se muestran primero. */
  locales?: EjercicioLocal[]
  titulo?: string
}

function detalleCatalogo(ejercicio: EjercicioCatalogo): string | null {
  const partes = [ejercicio.categoria, ejercicio.musculosPrincipales.slice(0, 2).join(', ')].filter(Boolean)
  return partes.length > 0 ? partes.join(' · ') : null
}

function textoBuscable(ejercicio: EjercicioCatalogo): string {
  return normalizar(
    [ejercicio.nombre, ejercicio.categoria, ...ejercicio.musculosPrincipales, ...ejercicio.musculosSecundarios]
      .filter(Boolean)
      .join(' '),
  )
}

/**
 * Buscador de ejercicios en una hoja modal. Filtra por grupo muscular (categoría de wger, en el servidor) y por
 * nombre/músculo (en el cliente, sobre las páginas ya cargadas: el backend no tiene búsqueda por texto).
 */
export function EjercicioSelector({
  abierto,
  onCerrar,
  onElegir,
  buscarCatalogo,
  locales = [],
  titulo = 'Agregar ejercicio',
}: EjercicioSelectorProps) {
  const [texto, setTexto] = useState('')
  const [categoriaId, setCategoriaId] = useState<number | undefined>()
  const [catalogo, setCatalogo] = useState<EjercicioCatalogo[]>([])
  const [paginaSiguiente, setPaginaSiguiente] = useState(0)
  const [totalPaginas, setTotalPaginas] = useState<number | null>(null)
  const [totalElementos, setTotalElementos] = useState(0)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  // Cambiar la categoría o pedir "más" crea un pedido nuevo (n); el efecto carga desde `pagina`.
  // Con `completa`, sigue hasta la última página (para buscar texto en una categoría entera).
  const [pedido, setPedido] = useState<Pedido>({ pagina: 0, n: 0, completa: false })

  const busqueda = normalizar(useDeferredValue(texto))
  const categoria = CATEGORIAS.find((c) => c.id === categoriaId)

  // Último pedido ya cargado: al reabrir la hoja se reutiliza lo cargado en lugar de pedirlo (y anexarlo) otra vez.
  const cargado = useRef<number | null>(null)

  useEffect(() => {
    if (!abierto || cargado.current === pedido.n) return
    const controlador = new AbortController()
    setCargando(true)
    setError(null)

    async function cargar() {
      let numero = pedido.pagina
      let total: number
      do {
        const pagina = await buscarCatalogo(
          { pagina: numero, tamanio: TAMANIO_PAGINA, categoria: pedido.categoriaId },
          controlador.signal,
        )
        const primera = numero === 0
        setCatalogo((previos) => {
          if (primera) return pagina.contenido
          // Sin repetidos: una carga interrumpida (hoja cerrada) se reanuda desde su primera página.
          const ids = new Set(previos.map((e) => e.id))
          return [...previos, ...pagina.contenido.filter((e) => !ids.has(e.id))]
        })
        setPaginaSiguiente(pagina.pagina + 1)
        setTotalPaginas(pagina.totalPaginas)
        setTotalElementos(pagina.totalElementos)
        total = pagina.totalPaginas
        numero = pagina.pagina + 1
      } while (pedido.completa && numero < total)
      cargado.current = pedido.n
    }

    cargar()
      .catch((causa: unknown) => {
        if (axios.isCancel(causa)) return
        setError(causa instanceof Error ? causa.message : 'No se pudo cargar el catálogo.')
      })
      .finally(() => {
        if (!controlador.signal.aborted) setCargando(false)
      })
    return () => controlador.abort()
  }, [abierto, pedido, buscarCatalogo])

  /**
   * Con texto y un grupo muscular elegido se carga la categoría entera (≤ 3 páginas de 100), así la búsqueda
   * la cubre completa. En "Todos" (> 900 ejercicios) se pide más a demanda con el botón.
   */
  const debeCompletar = (textoActual: string, categoriaActual: number | undefined) =>
    textoActual.trim() !== '' && categoriaActual !== undefined

  function cambiarTexto(valor: string) {
    setTexto(valor)
    const hayMasPaginas = totalPaginas !== null && paginaSiguiente < totalPaginas
    if (debeCompletar(valor, categoriaId) && hayMasPaginas && !cargando) {
      setPedido((p) => ({ ...p, pagina: paginaSiguiente, n: p.n + 1, completa: true }))
    }
  }

  function elegirCategoria(id: number | undefined) {
    setCategoriaId(id)
    setCatalogo([])
    setTotalPaginas(null)
    setPaginaSiguiente(0)
    setPedido((p) => ({ categoriaId: id, pagina: 0, n: p.n + 1, completa: debeCompletar(texto, id) }))
  }

  const cargarMas = () => setPedido((p) => ({ ...p, pagina: paginaSiguiente, n: p.n + 1, completa: false }))
  const reintentar = () => setPedido((p) => ({ ...p, pagina: paginaSiguiente, n: p.n + 1 }))

  const localesFiltrados = useMemo(
    () =>
      locales.filter(
        (e) =>
          (!categoria || e.categoria === categoria.nombre) &&
          (!busqueda || normalizar(`${e.nombre} ${e.categoria ?? ''}`).includes(busqueda)),
      ),
    [locales, categoria, busqueda],
  )

  const catalogoFiltrado = useMemo(() => {
    // No repetir en el catálogo lo que ya aparece en "Tus ejercicios" (misma copia de wger).
    const wgerLocales = new Set(localesFiltrados.map((e) => e.wgerId))
    return catalogo.filter((e) => !wgerLocales.has(e.id) && (!busqueda || textoBuscable(e).includes(busqueda)))
  }, [catalogo, busqueda, localesFiltrados])

  const hayMas = totalPaginas !== null && paginaSiguiente < totalPaginas

  const sinResultados = !cargando && !error && localesFiltrados.length === 0 && catalogoFiltrado.length === 0

  function elegir(ejercicio: EjercicioElegido) {
    onElegir(ejercicio)
    setTexto('')
  }

  return (
    <Sheet abierto={abierto} onCerrar={onCerrar} titulo={titulo}>
      <div className="flex flex-col gap-4">
        <div className="relative">
          <Search aria-hidden className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-texto-suave" />
          <Input
            type="search"
            aria-label="Buscar por nombre o músculo"
            placeholder="Buscar por nombre o músculo"
            enterKeyHint="search"
            autoComplete="off"
            value={texto}
            onChange={(e) => cambiarTexto(e.target.value)}
            className="pl-12"
          />
        </div>

        {/* Scroll horizontal de chips: en móvil no caben en una fila. */}
        <div role="group" aria-label="Grupo muscular" className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1">
          <Chip activo={categoriaId === undefined} onClick={() => elegirCategoria(undefined)}>
            Todos
          </Chip>
          {CATEGORIAS.map((c) => (
            <Chip key={c.id} activo={categoriaId === c.id} onClick={() => elegirCategoria(c.id)}>
              {c.nombre}
            </Chip>
          ))}
        </div>

        {localesFiltrados.length > 0 && (
          <section aria-labelledby="titulo-locales" className="flex flex-col gap-2">
            <h3 id="titulo-locales" className="text-sm font-semibold uppercase tracking-wide text-texto-suave">
              Tus ejercicios
            </h3>
            <ul className="flex flex-col gap-2">
              {localesFiltrados.map((e) => (
                <li key={e.ejercicioId}>
                  <EjercicioOpcion
                    nombre={e.nombre}
                    detalle={e.categoria}
                    onElegir={() =>
                      elegir({ ref: { ejercicioId: e.ejercicioId }, nombre: e.nombre, categoria: e.categoria })
                    }
                  />
                </li>
              ))}
            </ul>
          </section>
        )}

        {catalogoFiltrado.length > 0 && (
          <section aria-labelledby="titulo-catalogo" className="flex flex-col gap-2">
            <h3 id="titulo-catalogo" className="text-sm font-semibold uppercase tracking-wide text-texto-suave">
              Catálogo
            </h3>
            <ul className="flex flex-col gap-2">
              {catalogoFiltrado.map((e) => (
                <li key={e.id}>
                  <EjercicioOpcion
                    nombre={e.nombre}
                    detalle={detalleCatalogo(e)}
                    imagenUrl={e.imagenUrl}
                    onElegir={() => elegir({ ref: { wgerId: e.id }, nombre: e.nombre, categoria: e.categoria })}
                  />
                </li>
              ))}
            </ul>
          </section>
        )}

        {cargando && <Spinner etiqueta="Cargando catálogo…" />}

        {error && (
          <div className="flex flex-col gap-3">
            <Alert>{error}</Alert>
            <Button variante="secundario" icono={RotateCw} onClick={reintentar}>
              Reintentar
            </Button>
          </div>
        )}

        {sinResultados && !hayMas && (
          <EmptyState
            icono={SearchX}
            titulo="Sin resultados"
            descripcion={busqueda ? 'Prueba con otro nombre o cambia el grupo muscular.' : undefined}
          />
        )}

        {hayMas && !cargando && !error && (
          <div className="flex flex-col items-center gap-2 text-center">
            {busqueda && (
              <p role="status" className="text-sm text-texto-suave">
                {sinResultados ? 'Sin coincidencias' : 'Coincidencias'} entre {catalogo.length} de {totalElementos}{' '}
                ejercicios. {categoria ? '' : 'Elegir un grupo muscular acota la búsqueda.'}
              </p>
            )}
            <Button variante="secundario" anchoCompleto onClick={cargarMas}>
              Buscar en más ejercicios
            </Button>
          </div>
        )}
      </div>
    </Sheet>
  )
}
