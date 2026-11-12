---
name: ui-ux-designer
description: Sistema de diseño "Pulso" de MaxTracker (app de gimnasio en React + Tailwind v4). Úsala antes de crear, modificar o revisar cualquier pantalla, componente o estilo en maxtracker-frontend/ — azul noche, superficies sólidas redondeadas, cian para la acción y lo que está vivo, coral solo para récords, anillo de descanso, cifras en monoespaciada, Atomic Design (atoms/molecules/organisms/templates/pages), Mobile-First, objetivos táctiles de 48 px y accesibilidad. También para "revisa el diseño", "hazlo más pro", "agrega un componente" o refactorizar UI existente.
---

# UI/UX Designer — MaxTracker · Pulso

MaxTracker se usa **en el gimnasio**: con una mano, entre series, con el pulso alto y poca atención. La dirección de arte es **Pulso**: azul noche profundo, superficies sólidas y redondeadas que se sienten amables, y dos colores con un significado cada uno: **cian** para lo que se hace o está vivo ahora, **coral** para los récords.

Tres reglas resumen el sistema:

1. **Azul noche y formas suaves.** Superficies sólidas (`superficie`), esquinas generosas, botones en píldora. Nada de bordes duros ni de vidrio.
2. **Los números mandan.** Toda cifra de entrenamiento va en monoespaciada (`cifra`), grande y estable.
3. **Cian = acción y vida, coral = récord.** Cada color tiene un único significado. Si una pantalla tiene más de 2–3 elementos cian sólidos a la vez, está mal.

> **Estado:** migrado desde "Pro Sports Dark" (lima sobre negro). `index.css` declara los tokens y utilidades (`superficie`, `borde-degradado(-acento)`, `cifra`, `rotulo`, `shadow-elevada(-alta)`, `shadow-primario`, `rounded-superficie`, `bg-grupo-*`). No existen ya clases `lime-*` ni `zinc-*` en `src/`: **no las reintroduzcas**; usa siempre los tokens semánticos.

---

## 1. Principios no negociables

1. **Mobile-First.** Diseña para 360–430 px y amplía con `sm:`/`md:`/`lg:`. Una columna (`max-w-2xl mx-auto px-4`).
2. **Objetivos táctiles ≥ 48×48 px** (`min-h-tactil`, `size-tactil`), separados ≥ 8 px. La densidad se logra quitando relleno, nunca achicando lo que se toca.
3. **Modo oscuro único.** Sin tema claro y sin variantes `dark:`.
4. **Contraste:** texto principal ≥ 7:1, secundario ≥ 4.5:1, íconos, bordes funcionales y marcas de datos ≥ 3:1. El color nunca es la única señal.
5. **Una acción primaria visible por pantalla**, en la zona del pulgar.
6. **Atomic Design y reutilización** (sección 9).

---

## 2. Tokens (`maxtracker-frontend/src/index.css`, bloque `@theme`)

Usa **siempre** estos tokens (con modificador de opacidad si hace falta: `bg-acento/15`), nunca colores sueltos de Tailwind.

### Planos (de atrás hacia adelante)

| Token | Hex | Uso |
|---|---|---|
| `fondo` | `#0b1020` | Fondo de la app (con halo cian/índigo en `body::before`; no repetir) y barras fijas (`bg-fondo/80` + blur) |
| `casilla` | `#0f1528` | Campos de entrada, casillas y pistas **dentro** de una superficie |
| `superficie` | `#141b2e` | Tarjetas |
| `superficie-alta` | `#1a2340` | Botón secundario, hojas modales, elementos flotantes |
| `linea-suave` | `#1c2440` | Separadores dentro de una superficie (`divide-linea-suave`) |
| `linea` | `#26304f` | Bordes de campos y contenedores |

### Texto

| Token | Hex | Contraste mínimo | Uso |
|---|---|---|---|
| `texto` | `#e8ecf7` | 13:1 | Texto y cifras principales |
| `texto-medio` | `#c3cbe0` | 9,5:1 | Texto de badges neutros, cifras en reposo |
| `texto-suave` | `#8b95b5` | 5,2:1 | Texto secundario, etiquetas, navegación inactiva |
| `texto-tenue` | `#6b7696` | 3,4:1 | **Solo** íconos inactivos, placeholders, pistas y bordes de radios. Nunca texto que haya que leer |

### Color con significado

| Token | Hex | Significado |
|---|---|---|
| `acento` / `acento-claro` | `#22d3ee` / `#67e8f9` | **Cian**: acción primaria, navegación activa, estado vivo (descanso corriendo, sesión en curso), RIR bajo, foco de teclado, selección (en versión tenue) |
| `sobre-acento` | `#062a33` | Texto sobre cian sólido (8,4:1) |
| `acento-profundo` | `#6366f1` | **Índigo**: segundo tono de la marca (degradado del logo y avatar), identidad del análisis con IA. Nunca para acciones |
| `record` | `#fb7185` | **Coral**: solo récords personales (badge "PR", cifra del récord, punto del mejor valor en gráficos) |
| `peligro` / `exito` / `alerta` | `#f87171` / `#34d399` / `#fbbf24` | Estados; siempre con ícono + texto (el texto del aviso va en `texto`, no en el color del estado) |

### Grupos musculares (indicador vertical de las tarjetas)

Paleta categórica **validada** con el script de la skill `dataviz` en modo oscuro sobre `#141b2e`: pasan las cinco comprobaciones (luminosidad 0,48–0,67, croma, CVD adyacente ΔE ≥ 10,7, visión normal ΔE ≥ 29, contraste ≥ 3:1). Usar exactamente estos valores **en este orden**; nunca cian, coral, rojo ni verde esmeralda (reservados).

| Grupo | Token | Hex |
|---|---|---|
| Pecho | `--color-grupo-pecho` | `#8b5cf6` |
| Espalda | `--color-grupo-espalda` | `#d97706` |
| Piernas | `--color-grupo-piernas` | `#2563eb` |
| Hombros | `--color-grupo-hombros` | `#65a30d` |
| Brazos | `--color-grupo-brazos` | `#c026d3` |
| Abdominales | `--color-grupo-abdominales` | `#ea580c` |
| Pantorrillas | `--color-grupo-pantorrillas` | `#6366f1` |
| Cardio | `--color-grupo-cardio` | `#a16207` |
| Sin categoría | `--color-grupo-otro` | `#4b5675` |

El color **identifica**, no informa solo: el nombre del grupo va siempre escrito al lado.

### Utilidades

| Utilidad | Qué hace |
|---|---|
| `superficie` | `rounded-superficie` (20 px) + `bg-superficie` + `shadow-elevada`. **Sin blur** (más fluido en gama baja) |
| `borde-degradado` | Borde de 1 px más claro arriba (pseudo-elemento enmascarado); lo pone `Card` con `tono="normal"` |
| `borde-degradado-acento` | Borde cian que se apaga hacia abajo: superficie **viva** (sesión en curso). Una por pantalla |
| `cifra` | `font-mono font-bold tracking-tight tabular-nums leading-none` |
| `rotulo` | `text-xs font-medium text-texto-suave`, en minúsculas normales (Pulso no grita: nada de mayúsculas con tracking) |
| `shadow-primario` | Resplandor cian bajo el botón primario |

---

## 3. Tipografía

| Rol | Clases |
|---|---|
| Cifra display | `cifra` + `text-2xl`…`text-5xl` (peso de la serie, cronómetro, récord, volumen) |
| Cifra compacta | `font-mono font-bold tabular-nums` (badges, tablas, ejes) |
| Unidad | `text-xs font-semibold text-texto-suave ml-0.5`, pegada a la cifra |
| Rótulo | `rotulo` |
| Título de tarjeta | `text-base font-bold leading-tight` |
| Título de página | `text-2xl font-bold tracking-tight` |
| Marca | "Max" en `font-bold` + "Tracker" en `font-medium text-texto-suave` (nunca en mayúsculas) |

Fuente mono: **Geist Mono Variable** empaquetada (precacheada por la PWA). Inputs siempre `text-base` o más (iOS no hace zoom).

---

## 4. Formas

| Pieza | Radio |
|---|---|
| Tarjeta, hoja modal | `rounded-superficie` (20 px) |
| Campo, casilla, segmentado | `rounded-2xl` |
| Opción dentro de un segmentado | `rounded-xl` |
| **Botones, chips, badges, navegación activa, logo, avatar** | `rounded-full` (píldora o círculo) |

---

## 5. Jerarquía del color

**Cian sólido** (`bg-acento`), solo: la acción primaria de la pantalla y la marca.
**Cian como texto/trazo** (`text-acento`, `stroke-acento`): navegación activa, anillo de descanso corriendo, borde de la sesión en curso, badge "en curso", RIR ≤ 1, foco de teclado, línea de progreso en gráficos.
**Cian tenue = selección** (`bg-acento/15 text-acento-claro ring-1 ring-inset ring-acento/40`): chip, segmento o preset elegido. Es el único estilo de "elegido": no uses blanco sólido.
**Coral**: badge "PR" (`<Badge tono="record">`), cifra y trofeo del récord reciente, punto del mejor valor en `GraficoProgreso` y cifra "Mejor".

**Prohibido en cian o coral:** pesos y repeticiones comunes, objetivos, contadores, títulos, íconos decorativos, fondos de sección, marcas normales de gráficos.

---

## 6. Patrones de componentes

### Botones (`atoms/botonEstilos.ts`) — siempre `rounded-full`

| Variante | Clases |
|---|---|
| primario | `bg-acento text-sobre-acento font-bold shadow-primario hover:bg-acento-claro` |
| secundario | `bg-superficie-alta text-texto font-semibold ring-1 ring-inset ring-white/[0.06] hover:bg-linea` |
| fantasma | `text-texto-suave font-semibold hover:bg-white/[0.05] hover:text-texto` |
| peligro | `bg-peligro/10 text-peligro font-semibold ring-1 ring-inset ring-peligro/25` |

Alturas: `md` 48 px, `lg` 56 px. `active:scale-[0.97]`. Deshabilitado: `opacity-40`.

### Inputs (`Input`, `Select`, `FormField`)

`min-h-tactil rounded-2xl bg-casilla border border-linea text-base placeholder:text-texto-tenue focus:border-acento/50 focus:ring-1 focus:ring-acento/50`. Numéricos con `font-mono` e `inputMode`, nunca `type="number"`.

### Badges (`Badge`) — tonos

`neutro` (`bg-white/[0.06] text-texto-medio`) · `acento` (cian tenue: RIR ≤ 1, "en curso") · `record` (coral tenue: "PR") · `exito` / `alerta` (tendencias del análisis, con ícono).

### Cronómetro de descanso (`DescansoTimer`)

**Anillo de progreso** (SVG, `size-28`) que se vacía mientras corre: trazo `stroke-acento` corriendo, `stroke-texto-tenue` detenido, pista `stroke-casilla`. Dentro, la cifra (`cifra text-3xl`) y el estado ("descansando", "en pausa", "listo"). El botón Iniciar/Pausar va **al lado** del anillo y los presets en un segmentado debajo. Es compacto a propósito: en 360×740 la sesión, el descanso y el ejercicio actual deben verse sin scroll.

### Navegación (`BottomNav`, `NavItem`)

Barra fija `bg-fondo/80 backdrop-blur-2xl` con hairline `border-linea-suave`. Activo: ícono sobre una **píldora** `bg-acento/15` y texto `text-acento-claro` (desde `md`, toda la fila en píldora `md:bg-acento/12`). Inactivo: `text-texto-suave`.

### Marca y avatar

Logo: círculo `bg-linear-to-br from-acento to-acento-profundo` con el ícono `Activity` de lucide en `text-sobre-acento`. Avatar: círculo `from-acento-profundo to-superficie-alta` con `ring-2 ring-acento/40`. Los íconos de la PWA salen de `public/logo.svg` (`npm run generate-pwa-assets`); el color de fondo de la PWA es `#0b1020` (`vite.config.ts`, `index.html`, `pwa-assets.config.ts`).

### Tarjetas de ejercicio y rutina, series, cifras de periodo

- Indicador vertical de 4 px con el color del grupo muscular; rótulo del grupo + título en una o dos líneas; badges a la derecha.
- Barra de progreso de 2 px: pista `bg-linea`, relleno `bg-texto-medio`, cian solo en el ejercicio actual o al completarse.
- Series (`SerieRow`): número en `cifra text-texto-suave`, peso como cifra protagonista, unidad en `texto-suave`.
- Cifras de periodo: agrupadas en una superficie con `divide-x divide-linea-suave`.

### Gráficos (con la skill `dataviz`; colores en `src/lib/coloresGrafico.ts`)

Barras de contexto en `#6b7696`, semana actual en cian, línea de progreso en cian con el punto del mejor valor en coral, rejilla `#1c2440`, ejes en mono `#8b95b5`. Nunca dos ejes Y; siempre tooltip y `TablaGrafico`.

### Superficies y densidad

`Card` = `superficie p-3` (hasta `p-4` con formulario). Una superficie por lista, filas con `divide-y divide-linea-suave`. Casillas internas `rounded-2xl bg-white/[0.03]` o `bg-casilla`. `gap-4` entre bloques de página. Nada de `p-6`, tarjetas por fila ni superficies anidadas con blur.

---

## 7. Atomic Design: dónde va cada cosa

```
src/components/
  atoms/       Una sola pieza, sin lógica de negocio ni llamadas a la API.
  molecules/   Combina átomos para una tarea concreta. Recibe datos por props.
  organisms/   Sección completa con estado propio (formularios, cronómetro, listas).
  templates/   Layouts: estructura de página con <Outlet />, sin datos.
src/pages/     Une organismos con datos reales (api/ + hooks/useCarga). Una por ruta.
```

Dependencias solo hacia abajo. Un componente por archivo, PascalCase, export nombrado, props en español (`variante`, `tamano`, `tono`, `icono`, `etiqueta`, `on<Accion>`), `className` combinado con `cx()`. Estilos compartidos en un `.ts` aparte (`atoms/botonEstilos.ts`); auxiliares en `src/lib/`.

---

## 8. Patrones de la app de gimnasio

- **Pesos** siempre en kg; se muestran con `usePeso().formato(kg)` y se convierten con `usePeso().aKg(valor)`. Nunca `"${n} kg"` a mano.
- **Registro en un toque**: valores precargados con la última serie o el objetivo.
- **Acciones irreversibles**: confirmación en dos toques; borrar en `peligro`, lejos de la primaria.
- **Estados obligatorios**: cargando (`Spinner`), error (`Alert` + "Reintentar"), vacío (`EmptyState` con la siguiente acción) y contenido.
- **Íconos**: `lucide-react`, `size-4` en badges, `size-5` en botones, `size-6` en navegación; `aria-hidden` con texto al lado.

---

## 9. Accesibilidad (no negociable)

- `aria-label` en todo control sin texto (`IconButton` lo exige); íconos decorativos `aria-hidden`.
- Un solo `<h1>` por página; jerarquía sin saltos.
- Labels visibles; errores con `aria-describedby` y `aria-invalid`.
- El `:focus-visible` global (2 px cian) nunca se elimina.
- `role="alert"` en errores, `role="status"`/`aria-live` en cargas y en el cronómetro (`role="timer"`).
- `aria-pressed` en alternancias, `aria-current` en navegación.
- `prefers-reduced-motion` anula animaciones (ya en `index.css`).
- Grupo muscular, RIR bajo y PR siempre con texto: nunca solo color.

---

## 10. Checklist antes de dar por terminada una UI

- [ ] Se ve y se usa bien a 360 px sin scroll horizontal; revisado también en `md`.
- [ ] Objetivos táctiles ≥ 48×48 px y separados ≥ 8 px.
- [ ] Solo tokens semánticos: ningún `zinc-*`, `lime-*`, `sky-*` ni hex suelto en `src/` (salvo `coloresGrafico.ts`).
- [ ] Botones, chips y badges en `rounded-full`; tarjetas en `superficie`.
- [ ] Cian solo donde dice la sección 5; coral solo en récords; selección en cian tenue.
- [ ] Cifras de entrenamiento con `cifra` (o mono bold en badges).
- [ ] Grupo muscular con la paleta validada y su nombre al lado.
- [ ] Una sola acción primaria visible, en la zona del pulgar.
- [ ] Estados de carga, error, vacío y contenido cubiertos.
- [ ] Pesos con `usePeso()`.
- [ ] Teclado: se recorre con Tab y el foco se ve.
- [ ] `npm run build` y `npm run lint` pasan en `maxtracker-frontend/`.
