---
name: ui-ux-designer
description: Sistema de diseño de MaxTracker (app de gimnasio en React + Tailwind v4). Úsala antes de crear o modificar cualquier pantalla, componente o estilo en maxtracker-frontend/ — Atomic Design (atoms/molecules/organisms/templates/pages), Mobile-First, modo oscuro Zinc 900/950, áreas táctiles de 48x48 px como mínimo, alto contraste y accesibilidad. También para revisar UI existente ("revisa el diseño", "hazlo más usable en el gym", "agrega un componente").
---

# UI/UX Designer — MaxTracker

La app se usa **en el gimnasio**: con una mano, entre series, transpirando, con poca atención y a veces con luz fuerte o muy baja. Cada decisión de diseño parte de ahí: **pocos toques, objetivos grandes, lectura de un vistazo**.

## 1. Principios (en orden de prioridad)

1. **Mobile-First.** Diseña para 360–430 px de ancho y luego amplía con `sm:` / `md:` / `lg:`. Nunca al revés. El contenido va en una columna (`max-w-2xl mx-auto px-4`).
2. **Táctil ante todo.** Todo lo interactivo mide **mínimo 48x48 px** (`min-h-tactil`, `size-tactil`). La acción principal de la pantalla usa `tamano="lg"` (56 px) y va en la zona del pulgar (abajo).
3. **Modo oscuro por defecto y único, con profundidad.** Fondo en degradado (zinc-900 arriba → zinc-950 → negro abajo, con un halo lima tenue) y tarjetas de **vidrio** encima. No hay tema claro ni `dark:`: no agregues variantes `dark:`.
4. **Alto contraste.** Texto principal ≥ 7:1, texto secundario ≥ 4.5:1, bordes e íconos ≥ 3:1 (WCAG 2.2 AA/AAA). El color nunca es la única señal: acompáñalo de texto o ícono.
5. **Una acción principal por pantalla.** Solo un botón `primario` (lima) visible a la vez; el resto `secundario` o `fantasma`.
6. **Componentes atómicos reutilizables.** Antes de escribir markup nuevo, busca el componente que ya existe (sección 4). Si falta, créalo en el nivel atómico correcto.

## 2. Tokens (definidos en `maxtracker-frontend/src/index.css`, bloque `@theme`)

Usa **siempre los tokens semánticos**, no colores sueltos de Tailwind, salvo los tonos de Badge/Alert ya definidos.

| Token / utilidad | Valor | Uso |
|---|---|---|
| `bg-fondo` + `body::before` | zinc-900 → zinc-950 → negro | Fondo con profundidad (ya aplicado en `index.css`; no repetir) |
| `vidrio` (utilidad) | `bg-vidrio` (zinc-900/70) + `backdrop-blur-xl` | Tarjetas, hojas, tarjetas navegables, avisos |
| `bg-vidrio-barra` | zinc-950/75 | Cabecera y barra de navegación (con `backdrop-blur-xl`) |
| `border-borde-vidrio` | zinc-800/80 | Borde de todo lo de vidrio, inputs y separadores |
| `bg-zinc-950/40` + `border-white/5` | — | Casillas dentro de una tarjeta (cifras de sesión, ítems de análisis) |
| `shadow-neon` / `shadow-neon-fuerte` | lima 20 % / 35 % | Resplandor de la acción principal, la ruta activa y lo "logrado" |
| `drop-shadow-neon` | lima | Resplandor de íconos y cifras activas (ruta activa, timer corriendo, n.º de serie) |
| `bg-superficie` / `bg-superficie-alta` | zinc-900 / zinc-800 | Solo rellenos opacos puntuales (placeholder de imagen); no para tarjetas |
| `text-texto` | zinc-50 | Texto principal (≈ 19:1 sobre fondo) |
| `text-texto-suave` | zinc-400 | Texto secundario (≈ 8:1 sobre fondo; no usar zinc-500 para texto) |
| `bg-acento` / `text-acento` | lime-400 | Acción principal, estado activo, foco |
| `text-sobre-acento` | zinc-950 | Texto/ícono sobre `bg-acento` (nunca blanco sobre lima) |
| `text-peligro` | red-400 | Errores, borrar |
| `text-exito` | emerald-400 | Completado |
| `min-h-tactil`, `min-w-tactil`, `size-tactil` | 3rem (48 px) | Área táctil mínima |

**Tipografía:** pila del sistema (`font-sans`) para texto; **`font-mono` para cifras de entrenamiento** (peso, reps, RIR, n.º de serie, cronómetro, contadores, celdas de tabla), con `tabular-nums`. Tamaños: cuerpo `text-base` (16 px, nunca menos en inputs para que iOS no haga zoom), secundario `text-sm`, etiquetas `text-xs` como mínimo absoluto. Rótulos de sección pequeños en `uppercase tracking-widest text-texto-suave`.

**Forma y espacio:** radios `rounded-xl` (controles), `rounded-2xl` (tarjetas) y `rounded-full` (badges/píldoras). Espaciado en la escala de 4: `gap-2`/`gap-3` dentro de componentes, `gap-6` entre secciones de una página. Separación mínima entre objetivos táctiles: 8 px (`gap-2`).

**Acción principal (lima):** `bg-acento` + borde `border-lime-300/70` (canto de luz) + `shadow-neon` (más fuerte al pasar) + `font-bold tracking-wide`. Está en `atoms/botonEstilos.ts`: usa `Button`/`ButtonLink`, no lo copies.

**Datos de entrenamiento en píldoras:** `Badge tono="lima" mono` → `bg-lime-500/10 text-lime-400 border border-lime-500/20 rounded-full px-3 py-1 font-mono` (peso, RIR, objetivos). `exito` (logrado), `alerta` (retroceso) y `neutro` para estados.

**Movimiento:** `transition-all duration-200` en todo lo interactivo y `active:scale-95` como respuesta táctil (`active:scale-[0.98]` en tarjetas grandes navegables). `index.css` ya respeta `prefers-reduced-motion`.

**Bordes y sombras de `Card`:** elígelos con la prop `tono` (`normal` · `lima` · `ia`), nunca pasando `border-*`/`shadow-*` por `className`: dos clases que fijan la misma propiedad no se resuelven por orden.

## 3. Atomic Design: dónde va cada cosa

```
src/components/
  atoms/       Una sola pieza, sin lógica de negocio ni llamadas a la API.
  molecules/   Combina átomos para una tarea concreta. Recibe datos por props.
  organisms/   Sección completa con estado propio (formularios, cronómetro, listas).
  templates/   Layouts: estructura de página con <Outlet />, sin datos.
src/pages/     Une organismos con datos reales (api/ + hooks/useCarga). Una por ruta.
```

Reglas de dependencia (solo hacia abajo): `pages → templates/organisms → molecules → atoms`. Un átomo nunca importa una molécula; ningún componente de `components/` llama a la API (eso lo hacen las páginas, que pasan callbacks como `onRegistrar`).

Al crear un componente:
- Nombre en PascalCase, un componente por archivo, exportación nombrada (`export function Badge`).
- Props en español, consistentes con los existentes: `variante`, `tamano`, `icono`, `etiqueta`, `cargando`, `anchoCompleto`, `on<Accion>`.
- Acepta `className` y lo combina con `cx()` (`src/lib/cx.ts`) para poder ajustarlo desde fuera.
- Estilos compartidos entre componentes van a un archivo `.ts` aparte (como `atoms/botonEstilos.ts`), no se exportan desde un `.tsx` de componente.

## 4. Inventario actual (reutiliza antes de crear)

| Nivel | Componente | Para qué |
|---|---|---|
| atom | `Button` | Acción. `variante`: primario · secundario · fantasma · peligro; `tamano`: md (48) · lg (56); `icono`, `cargando` |
| atom | `ButtonLink` | Navegación con aspecto de botón (sigue siendo `<a>`) |
| atom | `IconButton` | Solo ícono, 48x48, `etiqueta` obligatoria (aria-label) |
| atom | `Input` | Campo base; en formularios usar `FormField` |
| atom | `Badge`, `PesoBadge`, `RirBadge` | Píldoras: `lima` (+`mono`) para peso/RIR/objetivo, `exito`, `alerta`, `neutro`. `PesoBadge` usa la unidad del Perfil |
| atom | `Spinner` | Carga con texto (`role="status"`) |
| atom | `Card` | Tarjeta de vidrio; `tono`: `normal` · `lima` (resplandor, "en curso/logrado") · `ia` |
| atom | `Avatar` | Iniciales sobre degradado lima→esmeralda (decorativo, `aria-hidden`) |
| atom | `Titulo`, `Texto` | Tipografía semántica (h1–h3, p) |
| atom | `Chip` | Filtro alternable de 48 px (`aria-pressed`), p. ej. grupo muscular |
| atom | `Select` | Lista desplegable nativa de 48 px (selector del sistema en móvil) |
| molecule | `FormField` | Label visible + Input + ayuda/error con `aria-describedby` |
| molecule | `Alert` | Error (`role="alert"`) o info (`role="status"`) |
| molecule | `EmptyState` | Lista vacía: ícono + título + descripción + acción |
| molecule | `NavItem` | Ítem de navegación (ícono + texto, `aria-current`); activo: lima, ícono con resplandor e indicador luminoso |
| molecule | `SerieRow` | Fila de serie: n.º grande en mono con resplandor, peso/RIR en píldoras lima, reps en cifra fuerte, borrar |
| molecule | `EjercicioCard` | Ejercicio: orden en mono, categoría en rótulo, objetivo en píldora, barra de avance (tono `lima` al completar) |
| molecule | `OpcionesSegmentadas` | Una opción entre pocas sobre radios nativos: `fila` (kg/lb, segundos) o `lista` (tarjetas con ícono y descripción) |
| molecule | `Sheet` | Modal sobre `<dialog>`: hoja inferior en móvil, diálogo centrado desde `md`; Esc y toque fuera cierran |
| molecule | `ContadorField` | Número con − / + de 48 px (series, reps objetivo); bajar del mínimo lo deja vacío |
| molecule | `EjercicioOpcion` | Fila de resultado del buscador (imagen, nombre, grupo/músculos) |
| molecule | `StatTile` | Cifra destacada + variación (ícono y texto, nunca solo color) + detalle |
| molecule | `TooltipGrafico` | Tooltip de recharts: valor fuerte, serie y fecha secundarias, clave de línea |
| molecule | `TablaGrafico` | Los datos de un gráfico como tabla plegable (accesibilidad) |
| molecule | `TarjetaAnalisisIa` | Recomendaciones del entrenador virtual (IA); recibe estado y `onSolicitar` por props |
| organism | `AuthForm` | Login / registro con validación igual al backend |
| organism | `BottomNav` | Barra inferior en móvil, lateral desde `md` |
| organism | `RutinaEjercicioList` | Ejercicios de una rutina en orden |
| organism | `RegistrarSerieForm` | Peso / reps / RIR + registrar |
| organism | `DescansoTimer` | Cronómetro de descanso con presets, vibración al terminar |
| organism | `SesionActivaCard` | Resumen de la sesión: tiempo, series, volumen + acción |
| organism | `EjercicioSelector` | Buscador en `Sheet`: chips de grupo muscular + texto; "Tus ejercicios" y catálogo. Recibe `buscarCatalogo` por props |
| organism | `RutinaForm` | Crear/editar rutina: nombre, descripción, ejercicios con `ContadorField`, reordenar y quitar |
| organism | `ActualizacionAviso` | Avisos del service worker (versión nueva / lista sin conexión) |
| organism | `GraficoVolumenSemanal` | Columnas de volumen por semana (recharts, carga diferida) |
| organism | `GraficoProgreso` | Línea de evolución por ejercicio: una medida a la vez, selector de ejercicio |
| organism | `RecordsList` | Récords personales con marca "Nuevo" |
| organism | `ClaveGeminiForm` | Clave personal de Gemini: campo con mostrar/ocultar; guardada solo se ve enmascarada (últimos 4) |
| template | `MainLayout` | Cabecera de vidrio con avatar (→ `/perfil`) + contenido + `BottomNav` (padding para safe-area) |
| template | `AuthLayout` | Columna centrada para /login y /register |

## 5. Patrones de la app de gimnasio

- **Preferencias del usuario** (`src/preferencias/`, por dispositivo): unidad de peso, descanso predeterminado, zona horaria y enfoque de la IA. **Los pesos siempre viajan en kg**: muéstralos con `usePeso().formato(kg)` y conviértelos con `usePeso().aKg(valor)` al enviar (nunca `"${n} kg"` a mano). Al iniciar una sesión envía `fecha: hoyEnZona(zonaHoraria)`. `usePeso`/`usePreferencias` son preferencias de presentación: se pueden usar en cualquier nivel atómico.
- **Ajustes:** bloques en `Card` con encabezado (ícono lima + título + subtítulo) y opciones con `OpcionesSegmentadas`; los cambios se guardan al tocar, sin botón "Guardar".
- **Navegación inferior:** 3–5 destinos (Inicio, Rutinas, Entrenar, Perfil), ícono **y** texto siempre visibles, ítem activo en `text-acento` con ícono de trazo más grueso, `drop-shadow-neon` e indicador luminoso. Fija abajo con `pb-[env(safe-area-inset-bottom)]`; el `<main>` reserva espacio (`pb-[calc(6rem+env(safe-area-inset-bottom))]`).
- **Registro de series:** valores precargados con la última serie (o el objetivo de la rutina) para que registrar sea **un solo toque**. Inputs numéricos con `inputMode="decimal"` (peso, acepta coma) o `"numeric"` (reps, RIR), nunca `type="number"` (rueda del mouse, flechas diminutas).
- **Acciones destructivas o irreversibles** (finalizar sesión, borrar): confirmación en dos toques o diálogo; el botón de borrar usa `variante="peligro"` y no se pone junto a la acción principal.
- **Cronómetros y números grandes:** `text-6xl font-bold tabular-nums`, calculados contra una hora de fin (no sumando ticks).
- **Estados obligatorios en toda pantalla con datos:** cargando (`Spinner`), error (`Alert` + botón "Reintentar"), vacío (`EmptyState` con la siguiente acción) y contenido.
- **Tarjetas navegables:** toda la tarjeta es el enlace (`min-h-16`), con `ChevronRight` como pista.
- **Gráficos** (sigue además la skill `dataviz`): colores de `src/lib/coloresGrafico.ts` (marca lime-600 validada para
  fondo oscuro; el lime-400 de la UI no se usa en marcas de datos). Una serie = sin leyenda (el título la nombra).
  Nunca dos ejes Y: otra medida va en otro gráfico o con un selector. Barras ≤ 24 px con extremo redondeado de 4 px;
  líneas de 2 px con puntos de 8 px y anillo de la superficie; rejilla hairline. Siempre tooltip y `TablaGrafico`.
  recharts se importa con `lazy()` para no engordar el bundle principal.
- **Modales:** siempre `Sheet` (nunca un `div` posicionado a mano). Un `Sheet` que contiene su propio buscador o formulario se renderiza **fuera** de cualquier `<form>` padre, para que Enter no envíe el formulario de atrás.
- **Ajustes numéricos sin teclado** (objetivos, cantidades acotadas): `ContadorField`. Para valores medidos (peso real levantado) usa `FormField` con `inputMode`.
- **Iconos:** `lucide-react`, `size-5` en botones, `size-6` en navegación e `IconButton`, siempre `aria-hidden` si hay texto al lado.

## 6. Accesibilidad (no negociable)

- Toda imagen/ícono decorativo `aria-hidden`; todo control sin texto con `aria-label` (`IconButton` lo exige).
- Un solo `<h1>` por página (usa `sr-only` si no se ve). Jerarquía h1 → h2 → h3 sin saltos.
- Labels visibles en todos los campos; errores asociados con `aria-describedby` y `aria-invalid`.
- Foco visible: el contorno lima global de `:focus-visible` no se elimina sin reemplazo.
- Estados dinámicos anunciados: `role="alert"` para errores, `role="status"`/`aria-live="polite"` para cargas y el timer.
- `aria-pressed` en botones de alternancia (presets del timer), `aria-current` en navegación (lo pone `NavLink`).

## 7. Checklist antes de dar por terminada una UI

- [ ] Se ve y se usa bien a 360 px de ancho, sin scroll horizontal; luego revisado en `md`.
- [ ] Todos los objetivos táctiles ≥ 48x48 px y separados ≥ 8 px.
- [ ] Una sola acción `primario` visible, en la zona del pulgar.
- [ ] Solo tokens semánticos; sin `dark:`; texto secundario en `text-texto-suave` (no más oscuro).
- [ ] Superficies de vidrio (`Card`, `vidrio` + `border-borde-vidrio`), sin cajas planas `bg-superficie`; cifras en `font-mono`; pesos con `usePeso()`.
- [ ] Estados de carga, error, vacío y contenido cubiertos.
- [ ] Reutiliza componentes existentes; lo nuevo está en el nivel atómico correcto y sin llamadas a la API dentro de `components/`.
- [ ] Teclado: se puede recorrer con Tab y el foco se ve.
- [ ] `npm run build` y `npm run lint` pasan en `maxtracker-frontend/`.
