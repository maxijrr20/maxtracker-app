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
  api/          Cliente HTTP (axios) y funciones por recurso: auth, rutinas, sesiones
  auth/         AuthProvider, useAuth y guards de ruta (RequiereAuth, SoloInvitados)
  hooks/        useCarga: carga de datos con cancelación y reintento
  lib/          tokenStorage (JWT en localStorage), cx
  components/
    atoms/      Button, ButtonLink, IconButton, Input, Badge, Spinner, Card, Titulo/Texto
    molecules/  FormField, Alert, EmptyState, NavItem, SerieRow, EjercicioCard
    organisms/  AuthForm, BottomNav, RutinaEjercicioList, RegistrarSerieForm, DescansoTimer, SesionActivaCard
    templates/  MainLayout (con Bottom Navigation), AuthLayout
  pages/        Login, Register, Dashboard, Rutinas, RutinaDetalle, Entrenamiento, NotFound
```

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
| `/rutinas`, `/rutinas/:id` | Autenticado | Lista y detalle de rutinas; "Empezar entrenamiento" |
| `/entrenamiento` | Autenticado | Sesión en curso: registrar series, cronómetro de descanso, finalizar |
