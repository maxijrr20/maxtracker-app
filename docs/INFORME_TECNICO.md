# Informe Técnico del Proyecto: MaxTracker

**Plan de Arquitectura, Diseño e Implementación — Versión 2.0 (revisada)**
**Proyecto:** MaxTracker (Aplicación Web / PWA de Registro de Entrenamiento y Sobrecarga Progresiva)
**Autor:** Maximiliano Valenzuela Barrera
**Fecha:** 9 de noviembre de 2026

---

## 1. Resumen Ejecutivo

MaxTracker es una aplicación web responsiva construida como PWA (Progressive Web App). Su objetivo es registrar sesiones de entrenamiento —ejercicio, peso (kg) y repeticiones por serie— y evaluar la **sobrecarga progresiva** a lo largo del tiempo mediante métricas y gráficas.

El desarrollo se planifica en **9 jornadas** (3 por semana, ~1,5–2 h cada una) más una **semana de holgura** reservada para imprevistos y aprendizaje. El proyecto sigue estándares de industria (arquitectura en capas, tests automatizados, CI, Conventional Commits) para servir como herramienta de uso diario y como pieza destacada del portafolio en GitHub.

## 2. Arquitectura de Software y Stack Tecnológico

Arquitectura desacoplada: cliente (PWA), API REST e infraestructura de datos son independientes y se despliegan por separado.

```
[ PWA React (Vercel) ] --HTTPS/JSON--> [ API Spring Boot (Render, Docker) ] --JDBC--> [ PostgreSQL (Supabase) ]
                                                  |
                                                  +--HTTPS--> [ API pública wger ]  (catálogo, con caché)
                                                  +--HTTPS--> [ Groq / Gemini ]      (análisis IA, Semana 3)
```

| Capa | Tecnología | Notas |
|---|---|---|
| Frontend | React + Vite, Tailwind CSS, Axios, Recharts, `vite-plugin-pwa` | Desplegado en Vercel |
| Backend | **Java 21**, **Spring Boot 3.5.x**, Spring MVC, Spring Data JPA, Bean Validation, Lombok, Actuator | Desplegado en Render vía Docker |
| Base de datos | PostgreSQL en Supabase (plan gratuito) | Conexión por *Session pooler* |
| Catálogo | API de wger consumida con `RestClient` | Caché en memoria |
| Seguridad | Spring Security + JWT (Día 3) | Ver §6 |
| Calidad | JUnit 5, Mockito, `MockRestServiceServer`, `@WebMvcTest` | CI con GitHub Actions |

> **Nota de versión:** Spring Boot 4 ya está disponible, pero el proyecto usa la línea 3.5.x por compatibilidad con la mayoría de los tutoriales y librerías. Migrar a Boot 4 queda como mejora futura.

## 3. Estructura de Proyectos y Clean Code

### 3.1 Monorepo (`maxtracker-app`)

```
maxtracker-app/
├── maxtracker-backend/
├── maxtracker-frontend/
├── docs/INFORME_TECNICO.md
├── .github/workflows/backend-ci.yml
├── .gitignore
└── README.md
```

### 3.2 Backend (`/maxtracker-backend`) — Arquitectura en capas

| Paquete | Responsabilidad |
|---|---|
| `com.maxtracker.api.config` | CORS, `RestClient`, caché, propiedades tipadas (`@ConfigurationProperties`), seguridad |
| `com.maxtracker.api.controller` | `@RestController` — solo HTTP: parámetros, validación y `ResponseEntity` |
| `com.maxtracker.api.dto` | DTOs de request/response (records) con `jakarta.validation` |
| `com.maxtracker.api.dto.wger` | DTOs que reflejan el JSON externo de wger (aislados del resto) |
| `com.maxtracker.api.exception` | Excepciones de dominio + `GlobalExceptionHandler` (RFC 9457 `ProblemDetail`) |
| `com.maxtracker.api.mapper` | Conversión Entidad ↔ DTO y wger ↔ DTO |
| `com.maxtracker.api.model` | Entidades JPA |
| `com.maxtracker.api.repository` | Interfaces `JpaRepository` |
| `com.maxtracker.api.service` | Interfaces de negocio; implementaciones en `service.impl` |

Principios aplicados: inyección por constructor, controladores sin lógica de negocio, dependencia de interfaces (DIP), DTOs inmutables (`record`), secretos solo por variables de entorno, errores con formato estándar.

### 3.3 Frontend (`/maxtracker-frontend`) — Feature-Based

* `src/assets`: estilos globales e íconos.
* `src/components`: componentes UI atómicos (Button, Input, Modal).
* `src/features`: módulos por dominio (`ejercicios`, `sesiones`, `progreso`).
* `src/hooks`: custom hooks (`useEjercicios`, `useSesiones`, `useAnalisisIA`).
* `src/services`: cliente Axios centralizado (`VITE_API_URL`) con interceptor de JWT.
* `src/utils`: formato de fechas, cálculo de 1RM estimado y volumen.

## 4. Integración con wger y Modelo de Datos

### 4.1 Catálogo de ejercicios (wger)

* **Endpoint usado:** `GET https://wger.de/api/v2/exerciseinfo/?limit={n}&offset={m}[&category={id}]`. Se usa `/exerciseinfo/` y no `/exercise/` porque este último solo devuelve IDs; los nombres, descripciones, músculos y equipamiento están en `/exerciseinfo/` (nombres dentro de `translations`).
* **Idiomas:** en wger el ID `2` es **inglés** y el ID `4` es **español**. La API resuelve el nombre en español y, si el ejercicio no tiene traducción, usa inglés como respaldo (campo `idioma` en la respuesta). Ambos IDs son configurables por variable de entorno.
* **Caché:** las respuestas se cachean en memoria (`@Cacheable`) para reducir latencia y no sobrecargar una API comunitaria.
* **Resiliencia:** timeouts de conexión (5 s) y lectura (10 s). Cualquier falla de wger se responde como `502 Bad Gateway` con mensaje claro.
* **Licencia:** los datos de wger son CC-BY-SA; se da crédito en el README y en la app.

### 4.2 Modelo de datos (Supabase) — se implementa el Día 2

| Tabla | Campos principales |
|---|---|
| `usuario` | `id`, `email` (único), `password_hash`, `creado_en` |
| `sesion` | `id`, `usuario_id` (FK), `fecha`, `notas`, `creado_en` |
| `registro_serie` | `id`, `sesion_id` (FK), `wger_ejercicio_id`, `ejercicio_personalizado_id` (FK, opcional), `numero_serie`, `peso_kg` (NUMERIC 6,2), `repeticiones`, `rir` (opcional) |
| `ejercicio_personalizado` | `id`, `usuario_id` (FK), `nombre`, `categoria` |
| `ejercicio_cache` (opcional) | `wger_id`, `nombre`, `categoria` — copia local de los ejercicios usados, para que el historial no dependa de wger |

Restricción: cada `registro_serie` referencia **o** un ejercicio de wger **o** uno personalizado (`CHECK`). Índice en `(sesion_id)` y en `(wger_ejercicio_id)` para las consultas de progreso.

El esquema se versiona como scripts SQL en el repositorio (`/maxtracker-backend/src/main/resources/db/`) y Hibernate se configura con `ddl-auto=validate`, de modo que nunca modifica tablas por su cuenta.

## 5. Contrato de la API (v1)

| Método | Endpoint | Día | Descripción |
|---|---|---|---|
| GET | `/api/v1/ejercicios` | 1 | Catálogo paginado (`pagina`, `tamanio` 1–100, `categoria`) |
| GET | `/api/v1/ejercicios/{id}` | 1 | Detalle de un ejercicio |
| POST | `/api/v1/auth/login` | 3 | Obtiene JWT |
| GET/POST | `/api/v1/sesiones` | 3 | Listar / crear sesiones |
| GET/PUT/DELETE | `/api/v1/sesiones/{id}` | 3 | Detalle / editar / eliminar |
| POST | `/api/v1/sesiones/{id}/series` | 3 | Agregar serie a una sesión |
| GET | `/api/v1/progreso/{ejercicioId}` | 7 | Serie temporal: peso máx., volumen, 1RM estimado |
| POST | `/api/v1/analisis` | 7 | Análisis IA del progreso |
| GET | `/actuator/health` | 1 | Health check (Render) |

Errores: `application/problem+json` con `type`, `title`, `status`, `detail`, `timestamp`.

## 6. Seguridad

* **Autenticación obligatoria:** aunque la app sea de uso personal, la API es pública en internet. Sin autenticación cualquiera podría leer o borrar los registros. Se implementa Spring Security con JWT (usuario único sembrado por variable de entorno, contraseña con BCrypt).
* **Secretos:** solo en variables de entorno; `.env` está en `.gitignore` y se versiona `.env.example`.
* **CORS:** restringido a los orígenes del frontend (`CORS_ALLOWED_ORIGINS`).
* **Actuator:** solo se expone `/actuator/health`, sin detalles.
* **Supabase:** la app se conecta como servidor por JDBC; no se expone la clave `anon` de Supabase en el frontend.

## 7. Calidad, Testing y CI

* **Tests unitarios** del mapper y la lógica de negocio (JUnit 5 + AssertJ).
* **Tests de integración HTTP** del cliente wger con `MockRestServiceServer` (sin depender de internet).
* **Tests de controlador** con `@WebMvcTest` + `@MockitoBean` (status, JSON y errores).
* **GitHub Actions** (`backend-ci.yml`): ejecuta `mvn verify` en cada push/PR que toque el backend. Badge en el README.
* **Conventional Commits** (`feat:`, `fix:`, `test:`, `docs:`, `ci:`, `chore:`, `refactor:`).

## 8. Cronograma de Desarrollo (3 semanas + holgura)

### Semana 1 — Backend API REST

* **Día 1:** Monorepo, proyecto Spring Boot, conexión a Supabase, integración del catálogo wger (`RestClient`, DTOs, mapper, servicio, controlador), manejo global de errores, tests y CI. ✅
  *Commits:* `chore: inicializar monorepo y proyecto spring boot` · `feat: integrar api de wger para catalogo de ejercicios`
* **Día 2:** Script SQL del esquema, entidades JPA (`Usuario`, `Sesion`, `RegistroSerie`, `EjercicioPersonalizado`), repositorios, `ddl-auto=validate`. *Commit:* `feat: crear esquema sql, entidades jpa y repositorios`
* **Día 3:** CRUD de sesiones y series con DTOs validados, Spring Security + JWT, tests de controlador. *Commit:* `feat: agregar crud de sesiones con autenticacion jwt`

### Semana 2 — Frontend React + PWA

* **Día 4:** React + Vite + Tailwind, `vite-plugin-pwa` (manifest, íconos, service worker), layout móvil. *Commit:* `feat: inicializar app react con tailwind y soporte pwa`
* **Día 5:** Pantallas: buscador de ejercicios, sesión activa y registro de series (peso/reps). *Commit:* `feat: componentes ui para registro de series`
* **Día 6:** Cliente Axios con JWT, custom hooks, login. *Commit:* `feat: conectar frontend con api rest mediante axios`

### Semana 3 — Métricas, IA y Despliegue

* **Día 7:** Endpoint de progreso (peso máx., volumen, 1RM estimado con fórmula de Epley), gráficas con Recharts y servicio de IA (Groq/Gemini) con su API key solo en el backend. *Commit:* `feat: metricas de progreso, graficas e integracion de ia`
* **Día 8:** Despliegue: backend en Render (Dockerfile ya incluido), frontend en Vercel, variables de entorno (`VITE_API_URL`, `CORS_ALLOWED_ORIGINS`, credenciales de BD, `JWT_SECRET`). *Commit:* `ci: configurar despliegue y variables de produccion`
* **Día 9:** README profesional (badges, diagrama, capturas, decisiones técnicas). *Commit:* `docs: actualizar readme profesional del proyecto`

### Semana 4 — Holgura y mejoras opcionales

* Registro **offline**: cola local (IndexedDB) que sincroniza al recuperar señal.
* Mantener activo el backend (ver §9) y pruebas reales en el gimnasio.

## 9. Riesgos Conocidos y Mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Render (plan gratuito) suspende el servicio sin tráfico; el primer request tarda | Espera al abrir la app en el gimnasio | Mostrar estado de "despertando servidor" en la PWA; ping programado al `/actuator/health` |
| Supabase gratuito pausa proyectos inactivos | BD no disponible tras días sin uso | Uso frecuente de la app; reactivar desde el panel si ocurre |
| Conexión directa de Supabase es IPv6 y Render no la soporta | Fallo de conexión en producción | Usar la cadena del **Session pooler** (IPv4) desde el Día 1 |
| wger caído o lento | Catálogo no disponible | Caché, timeouts, 502 controlado y tabla `ejercicio_cache` |
| Muchos ejercicios de wger sin traducción al español | Nombres en inglés | Respaldo a inglés + ejercicios personalizados |
| Cronograma ajustado mientras se aprende | Retraso | Semana 4 de holgura |
| Planes gratuitos de terceros pueden cambiar | Costos inesperados | Verificar condiciones vigentes antes del Día 8 (Railway ya no ofrece plan gratuito permanente) |

## 10. Presupuesto y Recursos

| Recurso / Servicio | Proveedor | Costo estimado |
|---|---|---|
| Asistente de código / arquitectura | Claude Pro | $19.990 CLP / mes |
| Base de datos PostgreSQL | Supabase (plan gratuito) | $0 |
| API de análisis IA | Groq Cloud / Gemini Flash (plan gratuito) | $0 |
| Hosting frontend (PWA) | Vercel (Hobby) | $0 |
| Hosting backend (API) | Render (plan gratuito, Docker) | $0 |
| Catálogo de ejercicios | wger (API pública) | $0 |
| **Total inversión operativa** | | **$19.990 CLP / mes** |

## 11. Conclusión e Instalación en Dispositivos

Al finalizar el plan, MaxTracker estará operativo y protegido por autenticación. Desde Safari en iOS, la opción **"Agregar a la pantalla de inicio"** instala la PWA a pantalla completa, sincronizada con la base de datos en la nube y lista para usarse en el gimnasio. El repositorio documentará cada decisión técnica, con tests y CI visibles, como pieza de portafolio.
