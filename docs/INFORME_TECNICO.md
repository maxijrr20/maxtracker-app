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
                                                  +--HTTPS--> [ Gemini (Google AI) ]  (análisis IA, Día 7)
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
| `SesionEntrenamiento` → `sesion_entrenamiento` | `id`, `usuario_id`, `rutina_id` (opcional), `fecha` DATE, `notas` VARCHAR(1000), `creado_en`, `iniciada_en`, `finalizada_en` (V2) | FK `usuario_id` → `usuario` `ON DELETE CASCADE`; FK `rutina_id` → `rutina` `ON DELETE SET NULL` (borrar una rutina conserva el historial). Sus series se guardan y borran en cascada. `finalizada_en` NULL = en curso; CHECK `finalizada_en >= iniciada_en` |
| `SerieRegistro` → `serie_registro` | `id`, `sesion_id`, `ejercicio_id`, `numero_serie` SMALLINT, `peso_kg` NUMERIC(6,2), `repeticiones` SMALLINT, `rir` SMALLINT (opcional) | FK `sesion_id` → `sesion_entrenamiento` `ON DELETE CASCADE`; FK `ejercicio_id` → `ejercicio` (restringe el borrado). `(sesion_id, ejercicio_id, numero_serie)` único; `peso_kg` ≥ 0, `repeticiones` > 0, `rir` entre 0 y 10 |

**Decisiones de diseño**

* **Orden de los ejercicios de una rutina:** `rutina_ejercicio` **no** tiene `UNIQUE (rutina_id, orden)`. Al quitar o reordenar ejercicios, Hibernate actualiza las filas restantes antes de borrar la eliminada y esa restricción fallaría. El orden lo mantiene la entidad `Rutina` (`agregarEjercicio` numera al final y `quitarEjercicio` renumera 1..n) y se lee con `@OrderBy("orden ASC")`.
* **Número de serie:** `SesionEntrenamiento.registrarSerie(...)` asigna `numero_serie` correlativo **por ejercicio** dentro de la sesión: el siguiente al **mayor** existente (no a la cantidad), así borrar una serie intermedia no repite un número y no viola el `UNIQUE` (quedan huecos: 1, 3, 4).
* **Sesión en curso / finalizada:** se inicia al crearla (`iniciada_en`, reloj del servidor) y se cierra con `POST /finalizar`. Una sesión finalizada no admite agregar ni quitar series (409); fecha y notas sí se pueden editar.
* **CRUD de rutinas (Día 4):** `PUT` reemplaza la lista completa de ejercicios (los ítems se borran y se vuelven a crear, así que sus filas cambian de id). Todos los ejercicios se resuelven (con `ResolutorEjercicio`, igual que las series) **antes** de modificar la rutina: si uno no existe (404) o wger falla (502), la rutina queda como estaba.
* **Sesión basada en una rutina:** `SesionDto.ejerciciosRutina` se calcula a partir de la rutina **actual** (no es una copia): si la rutina se edita, el plan de la sesión cambia; si se elimina, queda vacío. Las series ya registradas nunca cambian. Se carga en una consulta aparte de las series para no cruzar dos colecciones en el mismo `JOIN`.
* **Índices:** las FK no se indexan solas en PostgreSQL, así que se crean `ix_rutina_usuario`, `ix_rutina_ejercicio_rutina_orden`, `ix_rutina_ejercicio_ejercicio`, `ix_sesion_usuario_fecha`, `ix_sesion_rutina`, `ix_serie_sesion` e `ix_serie_ejercicio` (este último para las consultas de progreso).

**Repositorios** (`com.maxtracker.api.repository`)

| Repositorio | Consultas propias |
|---|---|
| `UsuarioRepository` | `buscarPorEmail`, `existeEmail` (normalizan el email antes de consultar) |
| `EjercicioRepository` | `findByWgerId` (copia local de wger), `findByUsuarioIdOrderByNombreAsc` (personalizados del usuario), `findAccesible(id, usuarioId)` (copia de wger o personalizado propio) |
| `RutinaRepository` | `findByUsuarioIdOrderByNombreAsc`; `findByIdAndUsuarioId`; `findConEjerciciosByIdAndUsuarioId` (con `@EntityGraph`: rutina + ejercicios en una consulta) |
| `SesionEntrenamientoRepository` | `findByUsuarioIdOrderByFechaDescIdDesc` (historial paginado); `findConSeriesByIdAndUsuarioId` (con `@EntityGraph`) |
| `SerieRegistroRepository` | `findHistorial(usuarioId, ejercicioId)`; proyecciones `SerieHistorial` para métricas y análisis (`findHistorialCompleto`, `findHistorialDesde`, `findHistorialDeEjercicio`, `findHistorialDeEjerciciosDesde`) |

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
* **Creación de la copia (Día 3):** al registrar una serie con `wgerId`, `ResolutorEjercicio` busca la copia local; si no existe, consulta wger y la guarda en una transacción propia (`REQUIRES_NEW`). Si dos peticiones la crean a la vez, la que pierde en el `UNIQUE (wger_id)` relee la copia ganadora sin invalidar su transacción. Con `ejercicioId` se acepta una copia de wger o un personalizado **propio**; el de otro usuario responde 404.

### 4.4 Migraciones con Flyway

* El esquema se versiona como scripts SQL en `/maxtracker-backend/src/main/resources/db/migration/`. Flyway los aplica al arrancar y registra lo aplicado en la tabla `flyway_schema_history`.
* **`V1__esquema_inicial.sql`** crea las seis tablas, sus FK, restricciones `UNIQUE`/`CHECK` e índices. El SQL es compatible con PostgreSQL y con H2 en modo PostgreSQL.
* **`V2__estado_sesion.sql`** agrega `iniciada_en` (las filas existentes toman su `creado_en`) y `finalizada_en` a `sesion_entrenamiento`, con el CHECK `ck_sesion_finalizada`.
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

**Perfil `test`** (`src/test/resources/application-test.properties`): H2 en memoria con `MODE=PostgreSQL`. Flyway aplica las mismas migraciones y Hibernate la valida, así los tests no necesitan Supabase ni internet.

## 5. Contrato de la API (v1)

| Método | Endpoint | Día | Descripción |
|---|---|---|---|
| GET | `/api/v1/ejercicios` | 1 | Catálogo paginado (`pagina`, `tamanio` 1–100, `categoria`) |
| GET | `/api/v1/ejercicios/{id}` | 1 | Detalle de un ejercicio |
| POST | `/api/v1/auth/register` | 3 | Crea la cuenta y devuelve un JWT (201; 409 si el email existe; 403 si el registro está deshabilitado) |
| POST | `/api/v1/auth/login` | 3 | Obtiene JWT (401 si las credenciales no son válidas) |
| GET | `/api/v1/sesiones` | 3 | Historial paginado del usuario (`pagina`, `tamanio` 1–100), sin series |
| GET | `/api/v1/rutinas` | 4 | Rutinas del usuario ordenadas por nombre, con `cantidadEjercicios` (sin el detalle) |
| POST | `/api/v1/rutinas` | 4 | Crea una rutina: `nombre` (1–100), `descripcion` (≤500), `ejercicios` opcional (≤30, en orden): cada uno `wgerId` **o** `ejercicioId`, `seriesObjetivo` (1–20) y `repeticionesObjetivo` (1–999) opcionales. 201 + `Location` |
| GET/PUT/DELETE | `/api/v1/rutinas/{id}` | 4 | Detalle con ejercicios / reemplazo completo (incluida la lista de ejercicios) / eliminar (204; las sesiones que la usaban quedan con `rutinaId = null`) |
| POST | `/api/v1/sesiones` | 3–4 | Inicia una sesión (`fecha` opcional, por defecto hoy en `APP_ZONA_HORARIA`; `rutinaId`, `notas`). 201 + `Location`. Con `rutinaId`, la respuesta incluye `ejerciciosRutina` (plan de la rutina para precargar la sesión) |
| GET/PUT/DELETE | `/api/v1/sesiones/{id}` | 3 | Detalle con series / editar fecha y notas / eliminar (204) |
| POST | `/api/v1/sesiones/{id}/finalizar` | 3 | Finaliza la sesión (409 si ya lo estaba) |
| POST | `/api/v1/sesiones/{id}/series` | 3 | Registra una serie: `wgerId` **o** `ejercicioId`, `pesoKg` (0–9999,99), `repeticiones` (1–999), `rir` (0–10, opcional). 201; 409 si la sesión está finalizada |
| DELETE | `/api/v1/sesiones/{id}/series/{serieId}` | 3 | Quita una serie (204; 409 si la sesión está finalizada) |
| GET | `/api/v1/metricas/resumen` | 7 | Volumen (Σ peso × reps), sesiones y series de la semana y el mes actuales y anteriores; volumen por semana (`semanas` 4–52, por defecto 12, incluidas las semanas vacías); récords personales |
| GET | `/api/v1/metricas/ejercicios` | 7 | Ejercicios con series, del usado más recientemente al más antiguo |
| GET | `/api/v1/metricas/ejercicios/{id}/progreso` | 7 | Un punto por sesión: peso máx., volumen y 1RM estimado (Epley). 404 si el ejercicio no es accesible |
| POST | `/api/v1/analisis/rutina` | 7 | Análisis con IA (Gemini): sobrecarga progresiva por ejercicio, ajuste de RIR y sugerencias. Cuerpo opcional: `rutinaId` **o** `sesionId`, `dias` (7–90, por defecto 28), `enfoque` (`FUERZA`/`HIPERTROFIA`/`MANTENIMIENTO`), `unidadPeso` (`KG`/`LB`, unidad de la respuesta). Cabecera opcional `X-Gemini-Api-Key`: clave personal del usuario, usada en lugar de la del servidor y nunca guardada ni registrada. 422 sin series en el periodo; 429 + `Retry-After` si se pide otro antes de 20 s; 503 si la IA no está configurada o falla |
| GET | `/actuator/health` | 1 | Health check (Render) |

Errores: `application/problem+json` con `type`, `title`, `status`, `detail`, `timestamp`.

### 5.1 Métricas y análisis con IA (Día 7)

**Métricas.** Las consultas devuelven proyecciones planas (`SerieHistorial`: sesión, fecha, ejercicio, peso, reps, RIR) y los cálculos viven en `CalculadoraMetricas`, una clase de funciones puras (sin JPA, reloj ni Spring) probada con datos en memoria.

* Volumen = Σ peso × repeticiones; las series con peso 0 (peso corporal) no suman volumen.
* Semana de lunes a domingo, en la zona de `APP_ZONA_HORARIA`.
* Récord por ejercicio = mayor peso; a igual peso, más repeticiones; se fecha la primera vez que se logró.
* 1RM estimado con Epley: `peso × (1 + reps/30)`; con 1 repetición es el peso. Es una estimación: pierde precisión por encima de ~10 repeticiones.

**Análisis con IA (Clean Architecture).**

| Capa | Pieza | Responsabilidad |
|---|---|---|
| Controlador | `AnalisisController` | `POST /api/v1/analisis/rutina`, validación del cuerpo |
| Caso de uso | `AnalisisService` → `IaAnalisisService` | Arma el historial desde las series del usuario (no lo recibe del cliente) y aplica el límite de frecuencia (`LimitadorAnalisis`) |
| Puerto | `AsistenteEntrenamiento` (+ `ContextoAnalisis`, `RecomendacionIa`) | Contrato con el "entrenador virtual"; no sabe de Gemini |
| Adaptador | `ia.gemini.GeminiAsistente` | `POST models/{modelo}:generateContent` con salida JSON por esquema; valida y acota la respuesta |

* La lectura va en una transacción de solo lectura que termina **antes** de llamar a la IA, para no ocupar una conexión del pool (5) mientras el modelo responde.
* El prompt se acota: como máximo 12 ejercicios y sus últimas 8 sesiones.
* El 1RM inicial y final de cada ejercicio se precalcula y se le pasa al modelo.
* **Privacidad:** en el plan gratuito, Google puede usar los datos para mejorar sus productos. Solo se envían ejercicios, fechas, pesos, repeticiones y RIR, nunca el email ni datos de la cuenta (lo verifica `AnalisisIntegrationTest`).
* **Seguridad:** la clave va en la cabecera `x-goog-api-key` (nunca en la URL ni en los logs) y solo existe en el backend.
* **Inyección de instrucciones:** los nombres de ejercicio pueden ser texto del usuario. Van como datos JSON y la instrucción de sistema pide ignorar instrucciones dentro de ellos.
* **Robustez:** sin `GEMINI_API_KEY` la app arranca igual y el análisis responde 503. Cuota agotada (429 de Google), clave inválida, modelo retirado o JSON cortado → 503 con un mensaje claro. El modelo se cambia con `GEMINI_MODEL` (por defecto `gemini-3.5-flash-lite`).
* `generateContent` es la API "legacy" de Gemini (sigue soportada); migrar a la Interactions API solo afectaría a `GeminiAsistente`.

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
* **Día 8:** Despliegue: backend en Render (Docker, plan gratuito, región Oregón como Supabase) definido en `render.yaml` (Blueprint), frontend en Vercel (Root Directory `maxtracker-frontend`), variables de entorno (`VITE_API_URL`, `CORS_ALLOWED_ORIGINS`, credenciales de BD, `JWT_SECRET` aleatorio) y registro cerrado en producción (`AUTH_REGISTRO_HABILITADO=false`). ✅
  * API: `https://maxtracker-api.onrender.com` (health en `/actuator/health`)
  * App: `https://maxtracker-nine.vercel.app`
  *Commits:* `ci: configurar despliegue y variables de produccion` · `ci: agregar blueprint de render para el backend`
* **Día 9:** README profesional (badges, diagrama, capturas, decisiones técnicas). *Commit:* `docs: actualizar readme profesional del proyecto`

### Semana 4 — Holgura y mejoras opcionales

* Registro **offline**: cola local (IndexedDB) que sincroniza al recuperar señal.
* Mantener activo el backend (ver §9) y pruebas reales en el gimnasio.

## 9. Riesgos Conocidos y Mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Render (plan gratuito) suspende el servicio sin tráfico; el primer request tarda | Espera al abrir la app en el gimnasio | Workflow `.github/workflows/keep-alive.yml` (GitHub Actions) que hace `GET /actuator/health` cada 10 min y falla (aviso por email) si no responde `"status":"UP"`; las 750 h/mes del plan free alcanzan para 1 servicio 24/7 (~744 h). **Límites:** el cron de GitHub es en UTC y puede retrasarse o saltarse ejecuciones (algún arranque en frío sigue siendo posible), y GitHub desactiva los workflows programados tras 60 días sin commits (reactivar en Actions > Keep-alive > *Enable workflow*). Respaldo: estado de "despertando servidor" en la PWA |
| Supabase gratuito pausa proyectos inactivos | BD no disponible tras días sin uso | El mismo workflow keep-alive: `/actuator/health` consulta la BD en cada ping, lo que genera actividad diaria muy por debajo del umbral de ~7 días. Mismos límites (retrasos del cron y desactivación tras 60 días sin actividad en el repo); si aun así se pausa, reactivar desde el panel de Supabase |
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
| API de análisis IA | Gemini (Google AI Studio, plan gratuito) | $0 |
| Hosting frontend (PWA) | Vercel (Hobby) | $0 |
| Hosting backend (API) | Render (plan gratuito, Docker) | $0 |
| Catálogo de ejercicios | wger (API pública) | $0 |
| **Total inversión operativa** | | **$19.990 CLP / mes** |

## 11. Conclusión e Instalación en Dispositivos

Al finalizar el plan, MaxTracker estará operativo y protegido por autenticación. Desde Safari en iOS, la opción **"Agregar a la pantalla de inicio"** instala la PWA a pantalla completa, sincronizada con la base de datos en la nube y lista para usarse en el gimnasio. El repositorio documentará cada decisión técnica, con tests y CI visibles, como pieza de portafolio.
