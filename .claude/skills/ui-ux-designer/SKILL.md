---
name: ui-ux-designer
description: Sistema de diseño de MaxTracker (app de gimnasio en React + Tailwind v4). Úsala antes de crear o modificar cualquier pantalla, componente o estilo en maxtracker-frontend/ — Atomic Design (atoms/molecules/organisms/templates/pages), Mobile-First, modo oscuro Zinc 900/950, áreas táctiles de 48x48 px como mínimo, alto contraste y accesibilidad. También para revisar UI existente ("revisa el diseño", "hazlo más usable en el gym", "agrega un componente").
---

# UI/UX Designer — MaxTracker

La app se usa **en el gimnasio**: con una mano, entre series, transpirando, con poca atención y a veces con luz fuerte o muy baja. Cada decisión de diseño parte de ahí: **pocos toques, objetivos grandes, lectura de un vistazo**.

## 1. Principios (en orden de prioridad)

1. **Mobile-First.** Diseña para 360–430 px de ancho y luego amplía con `sm:` / `md:` / `lg:`. Nunca al revés. El contenido va en una columna (`max-w-2xl mx-auto px-4`).
2. **Táctil ante todo.** Todo lo interactivo mide **mínimo 48x48 px** (`min-h-tactil`, `size-tactil`). La acción principal de la pantalla usa `tamano="lg"` (56 px) y va en la zona del pulgar (abajo).
3. **Modo oscuro por defecto y único.** Fondo `zinc-950`, superficies `zinc-900`/`zinc-800`. No hay tema claro ni `dark:`: no agregues variantes `dark:`.
4. **Alto contraste.** Texto principal ≥ 7:1, texto secundario ≥ 4.5:1, bordes e íconos ≥ 3:1 (WCAG 2.2 AA/AAA). El color nunca es la única señal: acompáñalo de texto o ícono.
5. **Una acción principal por pantalla.** Solo un botón `primario` (lima) visible a la vez; el resto `secundario` o `fantasma`.
6. **Componentes atómicos reutilizables.** Antes de escribir markup nuevo, busca el componente que ya existe (sección 4). Si falta, créalo en el nivel atómico correcto.

## 2. Tokens (definidos en `maxtracker-frontend/src/index.css`, bloque `@theme`)

Usa **siempre los tokens semánticos**, no colores sueltos de Tailwind, salvo los tonos de Badge/Alert ya definidos.

| Token / utilidad | Valor | Uso |
|---|---|---|
| `bg-fondo` | zinc-950 | Fondo de la app |
| `bg-superficie` | zinc-900 | Tarjetas, inputs |
| `bg-superficie-alta` | zinc-800 | Elementos sobre tarjetas, botón secundario, chips |
| `border-borde` | zinc-800 | Bordes de tarjetas e inputs |
| `text-texto` | zinc-50 | Texto principal (≈ 19:1 sobre fondo) |
| `text-texto-suave` | zinc-400 | Texto secundario (≈ 8:1 sobre fondo; no usar zinc-500 para texto) |
| `bg-acento` / `text-acento` | lime-400 | Acción principal, estado activo, foco |
| `text-sobre-acento` | zinc-950 | Texto/ícono sobre `bg-acento` (nunca blanco sobre lima) |
| `text-peligro` | red-400 | Errores, borrar |
| `text-exito` | emerald-400 | Completado |
| `min-h-tactil`, `min-w-tactil`, `size-tactil` | 3rem (48 px) | Área táctil mínima |

**Tipografía:** pila del sistema (`font-sans`). Tamaños: cuerpo `text-base` (16 px, nunca menos en inputs para que iOS no haga zoom), secundario `text-sm`, etiquetas de nav `text-xs` como mínimo absoluto. Los números (peso, reps, tiempo) llevan `tabular-nums`.

**Forma y espacio:** radios `rounded-xl` (controles) y `rounded-2xl` (tarjetas). Espaciado en la escala de 4: `gap-2`/`gap-3` dentro de componentes, `gap-6` entre secciones de una página. Separación mínima entre objetivos táctiles: 8 px (`gap-2`).

**Movimiento:** solo `transition-colors` y `active:scale-[0.98]` como feedback de toque. `index.css` ya respeta `prefers-reduced-motion`.

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
| atom | `Badge`, `PesoBadge`, `RirBadge` | Datos compactos: peso (azul), RIR (ámbar), objetivo (lima), estado |
| atom | `Spinner` | Carga con texto (`role="status"`) |
| atom | `Card` | Superficie base |
| atom | `Titulo`, `Texto` | Tipografía semántica (h1–h3, p) |
| molecule | `FormField` | Label visible + Input + ayuda/error con `aria-describedby` |
| molecule | `Alert` | Error (`role="alert"`) o info (`role="status"`) |
| molecule | `EmptyState` | Lista vacía: ícono + título + descripción + acción |
| molecule | `NavItem` | Ítem de navegación (ícono + texto, `aria-current`) |
| molecule | `SerieRow` | Serie registrada: n.º, peso × reps, RIR, borrar |
| molecule | `EjercicioCard` | Ejercicio con objetivo y avance; `children` para series/formulario |
| organism | `AuthForm` | Login / registro con validación igual al backend |
| organism | `BottomNav` | Barra inferior en móvil, lateral desde `md` |
| organism | `RutinaEjercicioList` | Ejercicios de una rutina en orden |
| organism | `RegistrarSerieForm` | Peso / reps / RIR + registrar |
| organism | `DescansoTimer` | Cronómetro de descanso con presets, vibración al terminar |
| organism | `SesionActivaCard` | Resumen de la sesión: tiempo, series, volumen + acción |
| template | `MainLayout` | Cabecera + contenido + `BottomNav` (padding para safe-area) |
| template | `AuthLayout` | Columna centrada para /login y /register |

## 5. Patrones de la app de gimnasio

- **Navegación inferior:** 3–5 destinos, ícono **y** texto siempre visibles, ítem activo en `text-acento` con trazo más grueso. Fija abajo con `pb-[env(safe-area-inset-bottom)]`; el `<main>` reserva espacio (`pb-[calc(6rem+env(safe-area-inset-bottom))]`).
- **Registro de series:** valores precargados con la última serie (o el objetivo de la rutina) para que registrar sea **un solo toque**. Inputs numéricos con `inputMode="decimal"` (peso, acepta coma) o `"numeric"` (reps, RIR), nunca `type="number"` (rueda del mouse, flechas diminutas).
- **Acciones destructivas o irreversibles** (finalizar sesión, borrar): confirmación en dos toques o diálogo; el botón de borrar usa `variante="peligro"` y no se pone junto a la acción principal.
- **Cronómetros y números grandes:** `text-6xl font-bold tabular-nums`, calculados contra una hora de fin (no sumando ticks).
- **Estados obligatorios en toda pantalla con datos:** cargando (`Spinner`), error (`Alert` + botón "Reintentar"), vacío (`EmptyState` con la siguiente acción) y contenido.
- **Tarjetas navegables:** toda la tarjeta es el enlace (`min-h-16`), con `ChevronRight` como pista.
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
- [ ] Estados de carga, error, vacío y contenido cubiertos.
- [ ] Reutiliza componentes existentes; lo nuevo está en el nivel atómico correcto y sin llamadas a la API dentro de `components/`.
- [ ] Teclado: se puede recorrer con Tab y el foco se ve.
- [ ] `npm run build` y `npm run lint` pasan en `maxtracker-frontend/`.
