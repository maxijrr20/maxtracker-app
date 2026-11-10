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
| Base de datos | PostgreSQL en Supabase (plan gratuito), migraciones con **Flyway** | Conexión por *Session pooler*; H2 en memoria (modo PostgreSQL) solo para tests |
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
| `com.maxtracker.api.dto.auth` | `RegisterRequest`, `LoginRequest`, `AuthResponse` |
| `com.maxtracker.api.exception` | Excepciones de dominio + `GlobalExceptionHandler` (RFC 9457 `ProblemDetail`) |
| `com.maxtracker.api.mapper` | Conversión Entidad ↔ DTO y wger ↔ DTO |
| `com.maxtracker.api.model` | Entidades JPA (heredan de `EntidadBase`: `id` autogenerado y `equals`/`hashCode` seguros con proxies) |
| `com.maxtracker.api.repository` | Interfaces `JpaRepository` |
| `com.maxtracker.api.security` | `JwtService`, `JwtAuthenticationFilter`, `UsuarioDetailsService`, principal `UsuarioAutenticado` y `SecurityProblemHandler` (401/403 como `ProblemDetail`) |
| `src/main/resources/db/migration` | Scripts SQL versionados de Flyway (`V1__esquema_inicial.sql`, …) |
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
* **Catálogo vs. ejercicios usados:** el catálogo se consulta en vivo a wger; cuando un ejercicio de wger se usa en una rutina o en una serie, se guarda una copia local en la tabla `ejercicio` (ver §4.3).

### 4.2 Modelo de datos (Supabase) — implementado el Día 2

Seis entidades JPA en `com.maxtracker.api.model`, cada una con su tabla. Todas las claves primarias son `BIGINT GENERATED BY DEFAULT AS IDENTITY` y las fechas de creación (`creado_en`) son `TIMESTAMP WITH TIME ZONE` (`Instant` en Java, en UTC).

```
usuario 1──* ejercicio (solo personalizados)
usuario 1──* rutina 1──* rutina_ejercicio *──1 ejercicio
usuario 1──* sesion_entrenamiento 1──* serie_registro *──1 ejercicio
rutina  1──* sesion_entrenamiento (opcional)
```

| Entidad → tabla | Campos | Relaciones y reglas |
|---|---|---|
| `Usuario` → `usuario` | `id`, `email` VARCHAR(255), `password_hash` VARCHAR(100), `creado_en` | `email` único; se guarda normalizado en minúsculas, así la unicidad no distingue mayúsculas |
| `Ejercicio` → `ejercicio` | `id`, `wger_id` BIGINT, `usuario_id`, `nombre` VARCHAR(150), `categoria` VARCHAR(50), `creado_en` | Ejercicio de wger **o** personalizado (ver §4.3). `wger_id` único; `(usuario_id, nombre)` único. FK `usuario_id` → `usuario` `ON DELETE CASCADE` |
| `Rutina` → `rutina` | `id`, `usuario_id`, `nombre` VARCHAR(100), `descripcion` VARCHAR(500), `creado_en` | FK `usuario_id` → `usuario` `ON DELETE CASCADE`. Sus ítems se guardan y borran en cascada desde la entidad (`orphanRemoval`) |
| `RutinaEjercicio` → `rutina_ejercicio` | `id`, `rutina_id`, `ejercicio_id`, `orden` SMALLINT, `series_objetivo`, `repeticiones_objetivo` | FK `rutina_id` → `rutina` `ON DELETE CASCADE`; FK `ejercicio_id` → `ejercicio` (restringe el borrado). `orden` > 0; objetivos > 0 si se informan |
| `SesionEntrenamiento` → `sesion_entrenamiento` | `id`, `usuario_id`, `rutina_id` (opcional), `fecha` DATE, `notas` VARCHAR(1000), `creado_en` | FK `usuario_id` → `usuario` `ON DELETE CASCADE`; FK `rutina_id` → `rutina` `ON DELETE SET NULL` (borrar una rutina conserva el historial). Sus series se guardan y borran en cascada |
| `SerieRegistro` → `serie_registro` | `id`, `sesion_id`, `ejercicio_id`, `numero_serie` SMALLINT, `peso_kg` NUMERIC(6,2), `repeticiones` SMALLINT, `rir` SMALLINT (opcional) | FK `sesion_id` → `sesion_entrenamiento` `ON DELETE CASCADE`; FK `ejercicio_id` → `ejercicio` (restringe el borrado). `(sesion_id, ejercicio_id, numero_serie)` único; `peso_kg` ≥ 0, `repeticiones` > 0, `rir` entre 0 y 10 |

**Decisiones de diseño**

* **Orden de los ejercicios de una rutina:** `rutina_ejercicio` **no** tiene `UNIQUE (rutina_id, orden)`. Al quitar o reordenar ejercicios, Hibernate actualiza las filas restantes antes de borrar la eliminada y esa restricción fallaría. El orden lo mantiene la entidad `Rutina` (`agregarEjercicio` numera al final y `quitarEjercicio` renumera 1..n) y se lee con `@OrderBy("orden ASC")`.
* **Número de serie:** `SesionEntrenamiento.registrarSerie(...)` asigna `numero_serie` correlativo **por ejercicio** dentro de la sesión.
* **Índices:** las FK no se indexan solas en PostgreSQL, así que se crean `ix_rutina_usuario`, `ix_rutina_ejercicio_rutina_orden`, `ix_rutina_ejercicio_ejercicio`, `ix_sesion_usuario_fecha`, `ix_sesion_rutina`, `ix_serie_sesion` e `ix_serie_ejercicio` (este último para las consultas de progreso).

**Repositorios** (`com.maxtracker.api.repository`)

| Repositorio | Consultas propias |
|---|---|
| `UsuarioRepository` | `buscarPorEmail`, `existeEmail` (normalizan el email antes de consultar) |
| `EjercicioRepository` | `findByWgerId` (copia local de wger), `findByUsuarioIdOrderByNombreAsc` (personalizados del usuario) |
| `RutinaRepository` | `findByUsuarioIdOrderByNombreAsc`; `findConEjerciciosByIdAndUsuarioId` (con `@EntityGraph`: rutina + ejercicios en una consulta) |
| `SesionEntrenamientoRepository` | `findByUsuarioIdOrderByFechaDescIdDesc` (historial paginado); `findConSeriesByIdAndUsuarioId` (con `@EntityGraph`) |
| `SerieRegistroRepository` | `findHistorial(usuarioId, ejercicioId)`: series de un ejercicio en orden cronológico, base de las métricas del Día 7 |

`RutinaEjercicio` no tiene repositorio propio: se gestiona siempre a través de `Rutina`. Las consultas por `id` incluyen el `usuarioId` para que un usuario no pueda leer datos de otro.

### 4.3 Ejercicios de wger y personalizados

Una sola tabla `ejercicio` guarda los dos tipos de ejercicio, para que rutinas y series tengan una única FK (`ejercicio_id`):

| Tipo | Cómo se crea | `wger_id` | `usuario_id` |
|---|---|---|---|
| Copia local de wger | `Ejercicio.desdeWger(wgerId, nombre, categoria)` | ID del ejercicio en wger (único) | `NULL` |
| Personalizado | `Ejercicio.personalizado(usuario, nombre, categoria)` | `NULL` | dueño del ejercicio |

* La restricción `ck_ejercicio_origen` exige que se cumpla **exactamente una** de las dos opciones: `(wger_id IS NOT NULL AND usuario_id IS NULL) OR (wger_id IS NULL AND usuario_id IS NOT NULL)`.
* La copia local guarda `nombre` y `categoria`, de modo que el historial y las rutinas se pueden mostrar aunque wger esté caído o cambie sus datos.
* Las copias de wger son compartidas por todos los usuarios (sin dueño); los personalizados pertenecen a un usuario y se borran con él. Un mismo usuario no puede repetir el nombre de un personalizado.
* Un ejercicio que ya está en una rutina o en una serie no se puede borrar (FK sin cascada), para no perder historial.

### 4.4 Migraciones con Flyway

* El esquema se versiona como scripts SQL en `/maxtracker-backend/src/main/resources/db/migration/`. Flyway los aplica al arrancar y registra lo aplicado en la tabla `flyway_schema_history`.
* **`V1__esquema_inicial.sql`** crea las seis tablas, sus FK, restricciones `UNIQUE`/`CHECK` e índices. El SQL es compatible con PostgreSQL y con H2 en modo PostgreSQL.
* Hibernate usa `ddl-auto=validate`: nunca crea ni modifica tablas, solo verifica al arrancar que las entidades coinciden con el esquema.
* **Regla:** una migración ya aplicada en Supabase no se edita; cualquier cambio va en un script nuevo (`V2__...sql`, `V3__...sql`).
* Dependencias: `flyway-core` y `flyway-database-postgresql`.

### 4.5 Configuración de la conexión

| Propiedad | Valor |
|---|---|
| `spring.datasource.url` / `username` / `password` | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (sin valor por defecto: la app no arranca sin ellas) |
| `DB_URL` | Cadena JDBC del *Session pooler* de Supabase (IPv4, puerto 5432) con `?sslmode=require` |
| `spring.datasource.hikari.maximum-pool-size` | `DB_POOL_SIZE` (5 por defecto; el plan gratuito limita las conexiones) |
| `spring.jpa.open-in-view` | `false` |
| `hibernate.jdbc.time_zone` | `UTC` |
| `spring.config.import` | Lee un `.env` local opcional (raíz del monorepo o carpeta del backend) |

**Perfil `test`** (`src/test/resources/application-test.properties`): H2 en memoria con `MODE=PostgreSQL`. Flyway aplica la misma `V1` y Hibernate la valida, así los tests no necesitan Supabase ni internet.

## 5. Contrato de la API (v1)

| Método | Endpoint | Día | Descripción |
|---|---|---|---|
| GET | `/api/v1/ejercicios` | 1 | Catálogo paginado (`pagina`, `tamanio` 1–100, `categoria`) |
| GET | `/api/v1/ejercicios/{id}` | 1 | Detalle de un ejercicio |
| POST | `/api/v1/auth/register` | 3 | Crea la cuenta y devuelve un JWT (201; 409 si el email existe; 403 si el registro está deshabilitado) |
| POST | `/api/v1/auth/login` | 3 | Obtiene JWT (401 si las credenciales no son válidas) |
| GET/POST | `/api/v1/sesiones` | 3 | Listar / crear sesiones |
| GET/PUT/DELETE | `/api/v1/sesiones/{id}` | 3 | Detalle / editar / eliminar |
| POST | `/api/v1/sesiones/{id}/series` | 3 | Agregar serie a una sesión |
| GET | `/api/v1/progreso/{ejercicioId}` | 7 | Serie temporal: peso máx., volumen, 1RM estimado |
| POST | `/api/v1/analisis` | 7 | Análisis IA del progreso |
| GET | `/actuator/health` | 1 | Health check (Render) |

Errores: `application/problem+json` con `type`, `title`, `status`, `detail`, `timestamp`.

## 6. Seguridad

* **Autenticación obligatoria:** aunque la app sea de uso personal, la API es pública en internet. Sin autenticación cualquiera podría leer o borrar los registros. Spring Security 6 en modo *stateless* (sin sesión ni cookies, CSRF desactivado) con JWT HS256 (jjwt). Solo son públicos `POST /api/v1/auth/register`, `POST /api/v1/auth/login` y `GET /actuator/health`; el resto exige `Authorization: Bearer <token>`.
* **Contraseñas:** BCrypt (8–72 caracteres). El login no distingue "email inexistente" de "contraseña incorrecta" (mismo 401) para no revelar qué cuentas existen.
* **JWT:** `sub` = email, `iss` = `maxtracker-api`, vigencia `JWT_EXPIRATION` (12 h por defecto). `JWT_SECRET` es obligatorio y debe tener al menos 32 bytes: la app no arranca con un secreto débil. Cada petición recarga el usuario, así un token de una cuenta borrada deja de servir.
* **Registro:** `AUTH_REGISTRO_HABILITADO` (por defecto `true`). Como la app es de uso personal, en producción se crea la cuenta y luego se pone en `false` (el endpoint responde 403).
* **Secretos:** solo en variables de entorno; `.env` está en `.gitignore` y se versiona `.env.example`.
* **CORS:** restringido a los orígenes del frontend (`CORS_ALLOWED_ORIGINS`).
* **Actuator:** solo se expone `/actuator/health`, sin detalles.
* **Supabase:** la app se conecta como servidor por JDBC; no se expone la clave `anon` de Supabase en el frontend.

## 7. Calidad, Testing y CI

* **Tests unitarios** del mapper y la lógica de negocio (JUnit 5 + AssertJ).
* **Tests de integración HTTP** del cliente wger con `MockRestServiceServer` (sin depender de internet).
* **Tests de controlador** con `@WebMvcTest` + `@MockitoBean` (status, JSON y errores).
* **Tests de integración de repositorios** con `@DataJpaTest` sobre H2 (perfil `test`, esquema creado por Flyway): guardado y consulta, cascadas, `ON DELETE SET NULL` y restricciones `UNIQUE`/`CHECK`.
* **GitHub Actions** (`backend-ci.yml`): ejecuta `mvn verify` en cada push/PR que toque el backend. Badge en el README.
* **Conventional Commits** (`feat:`, `fix:`, `test:`, `docs:`, `ci:`, `chore:`, `refactor:`).

## 8. Cronograma de Desarrollo (3 semanas + holgura)

### Semana 1 — Backend API REST

* **Día 1:** Monorepo, proyecto Spring Boot, integración del catálogo wger (`RestClient`, DTOs, mapper, servicio, controlador), manejo global de errores, CORS, `.env.example`, tests y CI. ✅
  *Commits:* `chore: inicializar monorepo y proyecto spring boot` · `feat: integrar api de wger para catalogo de ejercicios` · `ci: agregar workflow de backend, configuracion cors y variables de entorno`
* **Día 2:** Conexión a Supabase, migración Flyway `V1__esquema_inicial.sql`, entidades JPA (`Usuario`, `Ejercicio`, `Rutina`, `RutinaEjercicio`, `SesionEntrenamiento`, `SerieRegistro`), repositorios, `ddl-auto=validate` y tests de integración sobre H2. ✅
  *Commit:* `feat: configurar jpa y modelos de dominio para supabase`
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
| wger caído o lento | Catálogo no disponible | Caché, timeouts, 502 controlado y copia local en la tabla `ejercicio` (el historial no depende de wger) |
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
