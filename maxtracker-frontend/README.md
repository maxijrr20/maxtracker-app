# MaxTracker — Frontend

React 19 + Vite + TypeScript + Tailwind CSS v4. Mobile-First, modo oscuro.

## Desarrollo

```bash
npm install
npm run dev      # http://localhost:5173 (puerto fijo: es el origen permitido por CORS en el backend)
npm run build    # type-check + build de producción en dist/
npm run lint     # oxlint
```

La API se configura con `VITE_API_URL` (por defecto `http://localhost:8080/api/v1`); ver `.env.example`.

## Estructura

```
src/
  api/          Cliente HTTP (axios) y funciones por recurso: auth, ejercicios, rutinas, sesiones
  auth/         AuthProvider, useAuth y guards de ruta (RequiereAuth, SoloInvitados)
  hooks/        useCarga: carga de datos con cancelación y reintento
  lib/          tokenStorage (JWT), categorias (grupos musculares de wger), ejerciciosPendientes, cx
  components/
    atoms/      Button, ButtonLink, IconButton, Input, Chip, Badge, Spinner, Card, Titulo/Texto
    molecules/  FormField, Alert, EmptyState, NavItem, SerieRow, EjercicioCard, Sheet, ContadorField, EjercicioOpcion
    organisms/  AuthForm, BottomNav, RutinaEjercicioList, RegistrarSerieForm, DescansoTimer, SesionActivaCard,
                EjercicioSelector, RutinaForm, ActualizacionAviso
    templates/  MainLayout (con Bottom Navigation), AuthLayout
  pages/        Login, Register, Dashboard, Rutinas, RutinaDetalle, RutinaForm, Entrenamiento, NotFound
```

## PWA

- `vite-plugin-pwa` genera `manifest.webmanifest` y el service worker (`sw.js`, Workbox) en `npm run build`.
  El service worker no se activa en `npm run dev`: para probar la instalación usa `npm run build && npm run preview`.
- El service worker precachea la app (abre sin conexión). Las llamadas a la API **no** se cachean.
- Actualizaciones con `registerType: 'prompt'`: aparece un aviso "Actualizar" y la app nunca se recarga sola en medio de
  un entrenamiento.
- Íconos en `public/` generados desde `public/logo.svg` con `npm run generate-pwa-assets` (se versionan).

## Buscador de ejercicios

El backend (`GET /ejercicios`) pagina y filtra por categoría de wger, pero **no busca por texto**. Por eso
`EjercicioSelector` filtra por nombre o músculo en el cliente, sobre lo cargado:

- Con un grupo muscular elegido y texto escrito, carga la categoría completa (≤ 230 ejercicios, 3 páginas).
- En "Todos" carga de a 100 con "Buscar en más ejercicios".
- "Tus ejercicios" (los de tus rutinas) aparece primero y se envía como `ejercicioId`; los del catálogo se envían
  como `wgerId` y el backend crea la copia local la primera vez.

## Métricas y entrenador virtual (Día 7)

- `/` muestra el volumen de la semana y del mes (con su variación), volumen por semana (12 semanas), evolución por
  ejercicio (peso máximo, 1RM estimado o volumen) y récords personales. Datos de `GET /metricas/*`.
- Gráficos con `recharts`, cargados con `lazy()` (no se descargan fuera del inicio). Cada gráfico tiene tooltip y
  una tabla equivalente ("Ver como tabla").
- `TarjetaAnalisisIa` pide `POST /analisis/rutina` **a demanda** en el detalle de una rutina, y automáticamente al
  finalizar una sesión con series. La clave de Gemini vive solo en el backend.

## Entrenamiento libre

`/entrenamiento` sin sesión en curso permite empezar sin rutina y agregar ejercicios con el buscador. El backend solo
guarda series, así que los ejercicios agregados que aún no tienen series se recuerdan en `localStorage` por sesión
(`maxtracker.sesion.<id>.pendientes`) hasta registrar su primera serie o finalizar.

El sistema de diseño está documentado en la skill `.claude/skills/ui-ux-designer/SKILL.md`.

## Autenticación

- El JWT de `/auth/login` y `/auth/register` se guarda en `localStorage` (`maxtracker.sesion`) junto con su `expiraEn`.
- `api/http.ts` agrega `Authorization: Bearer <token>` a cada petición (excepto `/auth/*`).
- Un `401` en una petición autenticada borra la sesión y las rutas protegidas redirigen a `/login`, que avisa
  que la sesión expiró. Tras iniciar sesión se vuelve a la ruta que se intentaba abrir.
- Los errores del backend (`application/problem+json`) se convierten en `ApiError` con el `detail` listo para mostrar.

## Rutas

| Ruta | Acceso | Vista |
|---|---|---|
| `/login`, `/register` | Invitados | Formularios de acceso |
| `/` | Autenticado | Inicio: sesión en curso o acceso a rutinas, últimas sesiones |
| `/rutinas`, `/rutinas/:id` | Autenticado | Lista ("+ Nueva rutina") y detalle (editar, eliminar, "Empezar entrenamiento") |
| `/rutinas/nueva`, `/rutinas/:id/editar` | Autenticado | Crear / editar rutina con el buscador de ejercicios |
| `/entrenamiento` | Autenticado | Sesión en curso o entrenamiento libre: agregar ejercicios, registrar series, descanso, finalizar |
