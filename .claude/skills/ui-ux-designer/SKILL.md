---
name: ui-ux-designer
description: Sistema de diseño "Pro Sports Dark" de MaxTracker (app de gimnasio en React + Tailwind v4). Úsala antes de crear, modificar o revisar cualquier pantalla, componente o estilo en maxtracker-frontend/ — superficies elevadas con micro-borde en degradado, cifras en monoespaciada black tipo tablero, lima solo en puntos de foco, alta densidad de información, Atomic Design (atoms/molecules/organisms/templates/pages), Mobile-First, objetivos táctiles de 48 px y accesibilidad. También para "revisa el diseño", "hazlo más pro", "agrega un componente" o refactorizar UI existente.
---

# UI/UX Designer — MaxTracker · Pro Sports Dark

MaxTracker se usa **en el gimnasio**: con una mano, entre series, con el pulso alto y poca atención. La dirección de arte es la de un **reloj deportivo de gama alta o un tablero de gimnasio**: fondo casi negro, superficies que flotan con un filo de luz, números grandes y duros, y un único color que enciende solo lo que importa en este segundo.

Tres reglas resumen todo el sistema:

1. **Nada plano.** Todo contenedor es una superficie elevada: translúcida, desenfocada, con micro-borde en degradado y sombra direccional.
2. **Los números mandan.** Toda cifra de entrenamiento va en monoespaciada black, como un display.
3. **El lima se gana.** Solo se enciende en puntos de foco crítico. Si una pantalla tiene más de 2–3 elementos lima a la vez, está mal.

> **Estado:** migrado. `index.css` declara los tokens y utilidades (`superficie`, `borde-degradado(-acento)`, `cifra`, `rotulo`, `shadow-elevada(-alta)`, `shadow-primario`, `bg-grupo-*`) y la fuente Geist Mono Variable va empaquetada (precacheada por la PWA). Ya no existen `vidrio`, `shadow-neon*` ni `border-borde-vidrio`: no los reintroduzcas.

---

## 1. Principios no negociables

Mandan sobre cualquier decisión estética, incluida la densidad:

1. **Mobile-First.** Diseña para 360–430 px y amplía con `sm:`/`md:`/`lg:`. Una columna (`max-w-2xl mx-auto px-4`).
2. **Objetivos táctiles ≥ 48×48 px** (`min-h-tactil`, `size-tactil`), separados ≥ 8 px. **La densidad se logra quitando relleno y aire decorativo, nunca achicando lo que se toca.**
3. **Modo oscuro único.** Sin tema claro y sin variantes `dark:`.
4. **Contraste:** texto principal ≥ 7:1, secundario ≥ 4.5:1, íconos, bordes funcionales y marcas de datos ≥ 3:1. El color nunca es la única señal (siempre va con texto, ícono o forma).
5. **Una acción primaria visible por pantalla**, en la zona del pulgar (abajo).
6. **Atomic Design y reutilización** (sección 9): antes de escribir markup, busca el componente.

---

## 2. Filosofía visual

| Concepto | Regla |
|---|---|
| **Superficie** | `bg-zinc-900/40 backdrop-blur-2xl` sobre el fondo degradado. Nunca un relleno opaco (`bg-zinc-900`, `bg-superficie`) como tarjeta. |
| **Borde** | Micro-borde de 1 px **en degradado** (`borde-degradado`): más claro arriba (de donde viene la luz), casi invisible abajo. Nunca un `border-zinc-800` sólido alrededor de un contenedor. |
| **Sombra** | **Direccional**: la luz viene de arriba, así que la sombra cae hacia abajo y es profunda y corta (`shadow-elevada`), con un filo de luz interior arriba. Nada de sombras simétricas difusas ni de resplandores de colores en contenedores. |
| **Profundidad** | Tres planos: fondo (degradado zinc-950 → negro) · superficie (tarjeta, barra) · elemento elevado (botón primario, badge flotante, hoja modal). Cada plano un poco más claro y con más sombra. |
| **Densidad** | Información limpia y compacta: encabezados en una línea, metadatos en badges, cifras grandes y etiquetas pequeñas. Prohibidas las tarjetas masivas con mucho relleno (ver sección 8). |

---

## 3. Tokens (`maxtracker-frontend/src/index.css`, bloque `@theme`)

Usa **siempre tokens o utilidades del sistema**, no colores sueltos, salvo los listados aquí.

### Color

| Token / clase | Valor | Uso |
|---|---|---|
| fondo (`body::before`) | zinc-950 → negro, halo lima ≤ 5 % arriba | Fondo de la app (ya aplicado; no repetir) |
| `bg-zinc-900/40` | — | Superficie elevada (con `backdrop-blur-2xl`) |
| `bg-zinc-950/60` | — | Barras fijas (cabecera, navegación) |
| `bg-zinc-950/80` | — | Campos de entrada |
| `bg-white/[0.03]` | — | Casilla o fila **dentro** de una superficie (sin blur propio) |
| `border-zinc-800` | — | Borde de campos de entrada y separadores hairline (`border-zinc-800/60`) |
| `text-texto` | zinc-50 | Texto y cifras principales |
| `text-texto-suave` | zinc-400 | Texto secundario, etiquetas (nunca más oscuro que zinc-400 para texto) |
| `text-zinc-500` | — | Solo unidades junto a una cifra ("kg", "reps") y rótulos decorativos |
| `acento` | lime-400 `#a3e635` | **Solo puntos de foco** (sección 6) |
| `peligro` / `exito` / `alerta` | red-400 / emerald-400 / amber-400 | Estados; siempre con ícono + texto |

### Grupos musculares (indicador vertical de las tarjetas)

Paleta categórica **validada** con el script de la skill `dataviz` en modo oscuro sobre `#09090b`: las cinco comprobaciones pasan, con el peor par adyacente para daltonismo en ΔE 14,5. Usar exactamente estos valores **en este orden**, sin ciclar ni inventar tonos nuevos. Nunca lima, verde ni rojo: están reservados para el acento y los estados.

| Grupo (categoría wger) | Token | Hex |
|---|---|---|
| Pecho | `--color-grupo-pecho` | `#f43f5e` (rose-500) |
| Espalda | `--color-grupo-espalda` | `#0284c7` (sky-600) |
| Piernas | `--color-grupo-piernas` | `#d97706` (amber-600) |
| Hombros | `--color-grupo-hombros` | `#8b5cf6` (violet-500) |
| Brazos | `--color-grupo-brazos` | `#0d9488` (teal-600) |
| Abdominales | `--color-grupo-abdominales` | `#d946ef` (fuchsia-500) |
| Pantorrillas | `--color-grupo-pantorrillas` | `#ea580c` (orange-600) |
| Cardio | `--color-grupo-cardio` | `#3b82f6` (blue-500) |
| Sin categoría / personalizado | `--color-grupo-otro` | `#52525b` (zinc-600) |

El color **identifica**, no informa por sí solo: el nombre del grupo va siempre escrito al lado (rótulo de la tarjeta).

### Sombras, bordes y superficies (utilidades a declarar en `index.css`)

```css
@theme {
  /* Luz desde arriba: filo claro interior arriba + sombra corta y profunda hacia abajo. */
  --shadow-elevada: inset 0 1px 0 0 rgb(255 255 255 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.6),
    0 14px 28px -14px rgb(0 0 0 / 0.9);
  --shadow-elevada-alta: inset 0 1px 0 0 rgb(255 255 255 / 0.07), 0 4px 8px -4px rgb(0 0 0 / 0.7),
    0 24px 40px -18px rgb(0 0 0 / 0.95);
  /* Profundidad táctil del botón primario (no es un "glow" decorativo). */
  --shadow-primario: 0 4px 20px rgb(163 230 53 / 0.15);
}

/* Micro-borde en degradado que funciona sobre superficies translúcidas (el truco padding-box/border-box no
   sirve con fondo semitransparente: el degradado se vería a través). Pseudo-elemento enmascarado. */
@utility borde-degradado {
  position: relative;
  isolation: isolate;
  &::before {
    content: "";
    position: absolute;
    inset: 0;
    padding: 1px;
    border-radius: inherit;
    pointer-events: none;
    background: linear-gradient(to bottom, rgb(255 255 255 / 0.14), rgb(255 255 255 / 0.04) 45%, rgb(255 255 255 / 0.07));
    mask: linear-gradient(#000 0 0) content-box exclude, linear-gradient(#000 0 0);
  }
}
/* Variante de foco: la superficie "encendida" (sesión en curso, PR recién logrado). */
@utility borde-degradado-acento {
  @apply borde-degradado;
  &::before {
    background: linear-gradient(to bottom, rgb(163 230 53 / 0.55), rgb(163 230 53 / 0.08) 50%, rgb(255 255 255 / 0.05));
  }
}
@utility superficie {
  @apply borde-degradado rounded-2xl bg-zinc-900/40 shadow-elevada backdrop-blur-2xl;
}
```

`superficie` es la base de `Card`, `Sheet`, las tarjetas navegables y los avisos. **El blur va solo en contenedores de primer nivel**: las filas y casillas internas usan `bg-white/[0.03]` sin blur, porque docenas de `backdrop-filter` anidados frenan los teléfonos de gama baja.

---

## 4. Tipografía deportiva

| Rol | Clases | Ejemplos |
|---|---|---|
| **Cifra display** (dato protagonista) | `font-mono font-black tracking-tight tabular-nums leading-none` + `text-2xl`…`text-6xl` | peso de la serie, n.º de serie, cronómetro, récord, volumen del periodo |
| **Cifra compacta** (dentro de badge o tabla) | `font-mono font-bold tabular-nums` | "RIR 1", "4×8", celdas de tabla, ejes de gráficos |
| **Unidad** | `font-sans text-xs font-semibold text-zinc-500 ml-0.5` pegada a la cifra | `82,5`**`kg`**, `8`**`reps`** |
| **Rótulo** | `text-[0.6875rem]` mínimo `text-xs`, `font-semibold uppercase tracking-[0.14em] text-texto-suave` | "PECHO", "SERIE", "ÚLTIMA SESIÓN" |
| **Título de tarjeta** | `text-base font-bold leading-tight` (una línea; `truncate` o `line-clamp-2`) | nombre del ejercicio |
| **Título de página** | `text-2xl font-black tracking-tight` | "Rutinas" |
| **Cuerpo** | `text-sm`/`text-base font-sans` | descripciones, análisis de IA |

- **Fuente mono:** `--font-mono` debe tener un peso black (900) de verdad; si no, el navegador lo sintetiza y se ve borroso. Objetivo: **Geist Mono Variable** (100–900, licencia OFL) empaquetada con `@fontsource-variable/geist-mono`. Así la precachea la PWA y funciona sin conexión, sin pedir nada a Google Fonts. Respaldo: `ui-monospace, "SF Mono", "Cascadia Mono", Menlo, monospace`.
- Las cifras display usan `tabular-nums` para que el cronómetro y los pesos no "bailen" al cambiar.
- Inputs: siempre `text-base` (16 px) o más, para que iOS no haga zoom al enfocar.

---

## 5. Superficies y contenedores

| Pieza | Especificación |
|---|---|
| **Tarjeta** (`Card`) | `superficie p-3` (12 px). Hasta `p-4` solo si contiene un formulario. Nunca `p-6` o más. |
| **Tarjeta de foco** | `superficie borde-degradado-acento` → sesión en curso, PR recién logrado. Como mucho **una** por pantalla. |
| **Casilla interna** | `rounded-xl bg-white/[0.03] px-3 py-2` (sin borde ni blur) → cifras de la sesión, ítems del análisis |
| **Fila de lista** | `min-h-12 px-3` con separadores `divide-y divide-zinc-800/60` dentro de una sola superficie. **Una superficie por lista, no una tarjeta por fila.** |
| **Barra fija** (cabecera, navegación) | `bg-zinc-950/60 backdrop-blur-2xl` + hairline `border-zinc-800/60` del lado del contenido + `shadow-elevada` |
| **Hoja modal** (`Sheet`) | `superficie` con `shadow-elevada-alta`, fondo `backdrop:bg-black/70 backdrop:backdrop-blur-sm` |
| **Badge flotante** | Sobre la esquina de una superficie: `absolute -top-2 right-3` (p. ej. "PR", "EN CURSO"), con su propia sombra corta |

Los bordes y sombras de `Card` se eligen con la prop `tono` (`normal` · `foco`), nunca pasando `border-*` o `shadow-*` por `className`: dos clases que fijan la misma propiedad no se resuelven por el orden en que se escriben.

---

## 6. Jerarquía del acento (lima)

**Permitido**, y solo aquí:

| Punto de foco | Cómo |
|---|---|
| **Acción primaria** de la pantalla | Botón primario (sección 7) |
| **Navegación activa** | Ícono y texto en `text-acento` + indicador de 2 px |
| **RIR bajo** (0–1: al fallo o a una repetición) | Badge RIR en lima. RIR 2–3 → neutro; RIR ≥ 4 → neutro atenuado |
| **Récord personal (PR)** | Badge "PR", cifra del récord y marca del gráfico donde se logró |
| **Estado vivo** | Cronómetro corriendo; barra de progreso de la sesión en curso |
| **Foco de teclado** | Contorno `:focus-visible` (global) y halo de los inputs |

**Prohibido en lima:** pesos y repeticiones comunes, objetivos de la rutina, números de orden, contadores, íconos decorativos, tarjetas enteras, títulos, marcas normales de gráficos y fondos de sección. Para todo eso: zinc (texto principal o secundario) y, para identidad, el color de grupo muscular.

**Prueba rápida:** entrecierra los ojos frente a la pantalla. Lo lima tiene que ser exactamente lo que el usuario debe mirar o tocar ahora.

**Selecciones y filtros no son foco:** el chip, el segmento o la opción elegida van en blanco (`bg-zinc-100 text-zinc-950`), no en lima.

**Entrenamiento — el "ejercicio actual":** en `/entrenamiento` solo el primer ejercicio que no completó su objetivo (o el último, si todos lo completaron) recibe la acción primaria (`RegistrarSerieForm destacado`) y la barra de avance lima (`EjercicioCard enCurso`). Los demás registran con botón secundario. El cronómetro usa botones secundarios: su foco es la cifra mientras corre.

---

## 7. Patrones de componentes

### Botones (`atoms/botonEstilos.ts`)

| Variante | Clases |
|---|---|
| **primario** | `bg-linear-to-b from-lime-400 to-lime-500 text-zinc-950 font-extrabold tracking-tight shadow-primario active:scale-[0.98] transition-transform duration-150` + `hover:from-lime-300 hover:to-lime-400` |
| **secundario** | `superficie text-texto font-semibold active:scale-[0.98] transition-transform` (sin lima) |
| **fantasma** | `text-texto-suave font-semibold hover:text-texto hover:bg-white/[0.04]` |
| **peligro** | `bg-red-500/10 text-red-400 font-semibold` + hairline `border-red-500/20` |

Alturas: `md` 48 px, `lg` 56 px (acción primaria de la pantalla). Deshabilitado: `opacity-40` y sin sombra.

### Inputs y selectores (`Input`, `Select`, `FormField`)

`min-h-tactil rounded-xl bg-zinc-950/80 border border-zinc-800 px-3 text-base text-texto placeholder:text-zinc-600 focus:outline-none focus:border-lime-500/50 focus:ring-1 focus:ring-lime-500/50 transition-[border-color,box-shadow] duration-150`.

Los campos numéricos (peso, reps, RIR) van con `font-mono font-bold text-lg` y `inputMode`, nunca `type="number"`. Etiqueta siempre visible y arriba, como rótulo. El halo tenue es el foco al tocar; para teclado se mantiene además el `:focus-visible` global, que es más grueso.

### Badges (`Badge`)

Píldora minimalista: `inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-mono font-bold`.

| Tono | Uso |
|---|---|
| `neutro` | `bg-white/[0.06] text-zinc-300`: peso, reps, objetivo, conteos |
| `acento` | `bg-lime-400/10 text-lime-400 ring-1 ring-inset ring-lime-400/25`: **solo** RIR ≤ 1, PR y "en curso" |
| `exito` / `alerta` | emerald / amber al 10 % + ícono: tendencias del análisis |

Los **badges flotantes** ("PR", "EN CURSO") se apoyan en el borde de la tarjeta (sección 5).

### Tarjeta de ejercicio y de rutina (`EjercicioCard`, tarjeta de `RutinasPage`)

```
┌▌─────────────────────────────────────────┐
│▌ PECHO                      [4×8] [2/4] │  ← rótulo del grupo + badges flotantes a la derecha
│▌ Press de banca                          │  ← título en una línea
│▌ ▔▔▔▔▔▔▔▔▔▔▔▔▔▔░░░░░░░░░░░░░░░░░░░░░░░░ │  ← barra de progreso de 2 px
└──────────────────────────────────────────┘
```

- **Indicador vertical**: `absolute inset-y-3 left-0 w-1 rounded-r-full` con el color del grupo muscular (sección 3). Sin categoría: `grupo-otro`.
- **Encabezado compacto** de una o dos líneas, con el rótulo del grupo arriba, el título abajo y los badges alineados a la derecha. Sin número de orden en un cuadrado aparte: si importa, va como prefijo mono en el rótulo (`01 · PECHO`).
- **Barra de progreso**: `h-0.5 rounded-full bg-zinc-800` con el relleno en `bg-zinc-300`, que pasa a lima solo en la sesión en curso o al completarse. Se anima con `transition-[width] duration-500 ease-out`; `prefers-reduced-motion` la vuelve instantánea.
- El contenido extra (series, formulario) va debajo, en la misma superficie, separado por un hairline `border-t border-zinc-800/60 pt-2`.

### Serie (`SerieRow`)

Fila de lista de `min-h-12`, sin borde propio:

`[01] 82,5kg × 8reps [RIR 1] [🗑]`

El número de serie va en cifra display `text-xl text-zinc-500` (gris: identifica, no es foco). El peso es la cifra protagonista: `text-2xl font-black text-texto`, con la unidad pequeña. Las repeticiones van en cifra display `text-xl`. El badge RIR sigue la regla de la sección 6. El botón borrar es un `IconButton` fantasma.

### Cronómetro (`DescansoTimer`)

Cifra display `text-6xl font-mono font-black tracking-tight`, en blanco detenido y en lima corriendo. Los presets son un `OpcionesSegmentadas` compacto en mono. Es un único foco lima mientras corre.

### Cifras de periodo (`StatTile`, `SesionActivaCard`)

Rótulo arriba, cifra display `text-3xl font-black` y la variación como badge (`exito` si sube, `neutro` si baja, siempre con ícono). Se agrupan de a 2 o 3 en una sola superficie con divisores verticales `divide-x divide-zinc-800/60`, no como tarjetas separadas.

### Navegación (`BottomNav`, `NavItem`, barra lateral)

Barra fija (sección 5). Inactivo: `text-zinc-500`, ícono con trazo 1,75. Activo: `text-acento` con trazo 2,25 y un indicador de `h-0.5 w-6` lima (arriba en móvil, a la izquierda desde `md`), con `transition-colors duration-150`. Etiquetas `text-xs font-semibold`.

### Gráficos (sigue también la skill `dataviz`)

- **Marcas en gris neutro** `#71717a` (zinc-500). Lo valida el script: pasa luminosidad y contraste ≥ 3:1. Falla el mínimo de croma, y es lo esperado: es el gris de lo que pasa a segundo plano.
- **Lima solo en el punto de foco**: la semana actual, el último punto y el punto del PR.
- Rejilla hairline `#27272a`, ejes en mono `text-xs` zinc-400, sin leyenda con una sola serie, nunca dos ejes Y. Siempre con tooltip y `TablaGrafico`.
- Colores en `src/lib/coloresGrafico.ts`; recharts se carga con `lazy()`.

### Modales y avisos

Siempre `Sheet` (sobre `<dialog>`). Un `Sheet` con buscador o formulario se renderiza **fuera** de cualquier `<form>` padre. Los avisos (`ActualizacionAviso`, `Alert`) son `superficie` con ícono de estado; el texto nunca va en el color del estado.

---

## 8. Densidad (reglas de refactorización)

- **Prohibido:** tarjetas grandes con mucho relleno (`p-5`/`p-6`+), una tarjeta por cada fila de lista, íconos decorativos de 48 px dentro de tarjetas, títulos de sección con subtítulo obvio, estados vacíos que ocupan media pantalla sin acción, dos superficies anidadas con blur.
- **Relleno:** `p-3` en superficies, `px-3 py-2` en casillas, `gap-2`/`gap-3` dentro de componentes, `gap-4` entre bloques de una página (no `gap-6`+).
- **Una línea por concepto:** encabezados de tarjeta en una o dos líneas; metadatos en badges en la misma línea que el título.
- **Cifras grandes, etiquetas chicas:** el tamaño se lo lleva el dato, no la etiqueta ni el contenedor.
- **Pantalla de entrenamiento:** en 360×740 deben verse sin hacer scroll el estado de la sesión y el ejercicio actual con su formulario de serie.
- **Límite duro:** la densidad nunca baja un objetivo táctil de 48 px ni la separación de 8 px entre ellos.

---

## 9. Atomic Design: dónde va cada cosa

```
src/components/
  atoms/       Una sola pieza, sin lógica de negocio ni llamadas a la API.
  molecules/   Combina átomos para una tarea concreta. Recibe datos por props.
  organisms/   Sección completa con estado propio (formularios, cronómetro, listas).
  templates/   Layouts: estructura de página con <Outlet />, sin datos.
src/pages/     Une organismos con datos reales (api/ + hooks/useCarga). Una por ruta.
```

- Las dependencias van solo hacia abajo: `pages → templates/organisms → molecules → atoms`. Ningún componente de `components/` llama a la API; las páginas pasan callbacks como `onRegistrar` o `buscarCatalogo`.
- Las preferencias de presentación (`usePeso()`, `usePreferencias()`) se pueden usar en cualquier nivel.
- Un componente por archivo, en PascalCase y con export nombrado. Props en español (`variante`, `tamano`, `tono`, `icono`, `etiqueta`, `cargando`, `anchoCompleto`, `on<Accion>`). Acepta `className` y lo combina con `cx()`.
- Los estilos compartidos van en un `.ts` aparte (como `atoms/botonEstilos.ts`) y las funciones auxiliares en `src/lib/`: un `.tsx` de componente solo exporta componentes.

---

## 10. Patrones de la app de gimnasio

- **Pesos:** siempre viajan en kg. Se muestran con `usePeso().formato(kg)` y se convierten con `usePeso().aKg(valor)` al enviar; nunca escribas `"${n} kg"` a mano. Si el usuario no toca un peso precargado, se reenvía el valor exacto en kg.
- **Registro de series en un toque:** valores precargados con la última serie o el objetivo. Inputs con `inputMode="decimal"` (peso) o `"numeric"` (reps, RIR).
- **Fecha de una sesión nueva:** `hoyEnZona(zonaHoraria)`. El descanso inicial sale de las preferencias.
- **Acciones irreversibles** (finalizar, borrar): confirmación en dos toques; el borrar va en `peligro`, lejos de la acción primaria.
- **Estados obligatorios** en toda pantalla con datos: cargando (`Spinner`), error (`Alert` + "Reintentar"), vacío (`EmptyState` compacto con la siguiente acción) y contenido. Al recargar se mantiene el contenido anterior atenuado, sin saltos.
- **Tarjetas navegables:** toda la superficie es el enlace, con `ChevronRight` en zinc-500 como pista.
- **Ajustes** (`/perfil`): bloques compactos en superficie, con opciones en `OpcionesSegmentadas`. Se guardan al tocar, sin botón "Guardar".
- **Íconos:** `lucide-react`, `size-4` en badges, `size-5` en botones y `size-6` en navegación e `IconButton`; `aria-hidden` cuando hay texto al lado.

---

## 11. Inventario

Reutiliza antes de crear. Todos los componentes ya siguen Pro Sports Dark; la última columna resume cómo lo aplican (es la referencia al crear uno parecido).

| Nivel | Componente | Para qué | Cómo aplica Pro Sports Dark |
|---|---|---|---|
| — | `index.css` | Tokens y utilidades | Agregar `--color-grupo-*`, `--shadow-elevada(-alta)`, `--shadow-primario`, `borde-degradado(-acento)` y `superficie`; quitar `shadow-neon*` y `vidrio`; instalar la fuente mono black |
| atom | `Button` / `ButtonLink` (`botonEstilos.ts`) | Acciones | Primario con degradado lima-400→500, `font-extrabold`, `shadow-primario` y `active:scale-[0.98]`; secundario en `superficie` |
| atom | `IconButton` | Solo ícono, 48×48, `etiqueta` obligatoria | Fantasma por defecto, sin lima |
| atom | `Input` / `Select` | Campos | `bg-zinc-950/80 border-zinc-800`, halo `ring-1 lime-500/50` |
| atom | `Badge`, `PesoBadge`, `RirBadge` | Píldoras de datos | Tonos `neutro`/`acento`/`exito`/`alerta`; `PesoBadge` neutro; `RirBadge` en acento solo con RIR ≤ 1 |
| atom | `Card` | Contenedor | `superficie p-3`; `tono`: `normal` · `foco` (`borde-degradado-acento`) |
| atom | `Chip` | Filtro alternable de 48 px (`aria-pressed`) | Activo en `bg-white/10 text-texto` con ring hairline; lima solo si es el filtro de foco |
| atom | `Avatar` | Iniciales | Degradado zinc con ring lima fino (sin glow) |
| atom | `Spinner`, `Titulo`, `Texto` | Carga y tipografía | Títulos `font-black tracking-tight` |
| molecule | `FormField` | Label + Input + ayuda/error | Rótulo en mayúsculas pequeñas |
| molecule | `Alert`, `EmptyState` | Mensajes y vacío | `EmptyState` compacto (sin ícono gigante) |
| molecule | `NavItem` | Navegación | Indicador de 2 px, sin `drop-shadow` |
| molecule | `SerieRow` | Fila de serie | Sin borde propio; peso como cifra display; RIR según la regla del acento |
| molecule | `EjercicioCard` | Ejercicio con objetivo y avance | Indicador vertical por grupo, encabezado compacto, badges a la derecha, barra de 2 px |
| molecule | `Sheet` | Modal | `superficie` + `shadow-elevada-alta` |
| molecule | `ContadorField` | − / + de 48 px | Cifra display en blanco, no lima |
| molecule | `OpcionesSegmentadas` | Una opción entre pocas | Elegida en `bg-white/10` + texto blanco; lima solo como marca del radio |
| molecule | `EjercicioOpcion` | Resultado del buscador | Fila de lista dentro de una sola superficie |
| molecule | `StatTile` | Cifra de periodo | Agrupar en una superficie con `divide-x` |
| molecule | `TooltipGrafico`, `TablaGrafico` | Gráficos | Cifras en mono |
| molecule | `TarjetaAnalisisIa` | Entrenador virtual | Superficie normal; tendencias con badges `exito`/`alerta` |
| organism | `AuthForm`, `RegistrarSerieForm` | Formularios | Inputs nuevos; campos numéricos display |
| organism | `BottomNav` | Barra inferior / lateral | Barra fija `zinc-950/60` + `shadow-elevada` |
| organism | `DescansoTimer` | Cronómetro | Cifra display black; lima solo corriendo |
| organism | `SesionActivaCard` | Resumen de sesión | `tono="foco"` mientras está en curso; cifras en casillas `divide-x` |
| organism | `EjercicioSelector`, `RutinaForm`, `RutinaEjercicioList` | Rutinas | Listas en una superficie; indicador de grupo |
| organism | `GraficoVolumenSemanal`, `GraficoProgreso` | Gráficos | Marcas zinc-500 + lima solo en la semana actual / punto del mejor valor; ejes en `FUENTE_MONO` |
| organism | `RutinaEjercicioList` | Ejercicios de una rutina | Una superficie con filas: indicador de grupo, orden en cifra, rótulo del grupo, objetivo en badge |
| organism | `RecordsList` | Récords | Badge flotante "PR" lima solo en los recientes; resto neutro |
| organism | `ClaveGeminiForm`, `ActualizacionAviso` | Perfil / PWA | Superficie + inputs nuevos |
| template | `MainLayout`, `AuthLayout` | Layouts | Cabecera como barra fija; logo sin glow |

---

## 12. Accesibilidad (no negociable)

- Íconos decorativos `aria-hidden`; todo control sin texto lleva `aria-label` (`IconButton` lo exige).
- Un solo `<h1>` por página (con `sr-only` si no se ve), jerarquía h1 → h2 → h3 sin saltos.
- Labels visibles; errores asociados con `aria-describedby` y `aria-invalid`.
- El contorno global de `:focus-visible` (2 px lima) nunca se elimina: el halo tenue de los inputs lo complementa, no lo reemplaza.
- Estados dinámicos: `role="alert"` para errores, `role="status"`/`aria-live="polite"` para cargas y el cronómetro.
- `aria-pressed` en alternancias, `aria-current` en navegación (lo pone `NavLink`).
- `prefers-reduced-motion` anula animaciones y transiciones (ya está en `index.css`).
- Color de grupo, RIR bajo y PR van siempre con texto o ícono: nunca solo color.

---

## 13. Checklist antes de dar por terminada una UI

- [ ] Se ve y se usa bien a 360 px, sin scroll horizontal; revisado también en `md`.
- [ ] Objetivos táctiles ≥ 48×48 px y separados ≥ 8 px, aunque la pantalla sea densa.
- [ ] Ningún contenedor plano: `superficie` (o `Card`) con micro-borde en degradado y `shadow-elevada`; blur solo en el primer nivel.
- [ ] Relleno de tarjeta `p-3`; una superficie por lista; nada de tarjetas masivas.
- [ ] Toda cifra de entrenamiento en `font-mono font-black tracking-tight tabular-nums` (o `font-bold` dentro de badges).
- [ ] Lima solo en los puntos de foco de la sección 6, 2–3 como máximo por pantalla.
- [ ] Indicador de grupo muscular con la paleta validada, acompañado del nombre del grupo.
- [ ] Una sola acción primaria visible, en la zona del pulgar.
- [ ] Estados de carga, error, vacío y contenido cubiertos.
- [ ] Pesos con `usePeso()`; sin `"kg"` escrito a mano.
- [ ] Teclado: se recorre con Tab y el foco se ve.
- [ ] `npm run build` y `npm run lint` pasan en `maxtracker-frontend/`.
