# Task 2026-10-07 — Valoración IA async + polling (y generalización job)

- **Rama:** `feature/ia-valoration-async` (desde `main` = `ea05c3e`)
- **Alcance:** `POST /analysis/getValorationIA` pasa a async (job + polling), y la infra job de suggest se generaliza para reutilizarla. Sin cambios en lógica de negocio ni esquema BD.
- **Fecha:** 2026-10-07.

---

## 1. Resumen de la tarea

El endpoint `POST /analysis/getValorationIA` ejecutaba la llamada al LLM en el hilo HTTP (feliz 5-20s; peor caso con reintentos, fallbacks de modelo y doble prompt: 30s-2min, superando a veces los 60s de Nginx). Además cada doble-clic quemaba cuota diaria de OpenRouter (50/día) por duplicado.

Solución (misma receta B1 que suggest-tickers): el POST crea un job en ms y redirige al detalle; un worker en segundo plano ejecuta `getValorationIA` sin tocarlo y persiste la valoración (incluido fallback); la página pregunta el estado cada 5s y recarga al terminar. Sin JS funciona degradado (recarga manual muestra la valoración guardada).

Además, por decisión explícita del usuario, la infraestructura job específica de suggest se generalizó **ahora** (núcleo compartido + fachadas finas) en vez de duplicarla.

## 2. Código generado

**Núcleo genérico nuevo** (`application/job/`):

| Fichero | Contenido |
|---|---|
| `JobStatus.java` | enum `PENDING/RUNNING/DONE/FAILED` (reemplaza `SuggestJobStatus`) |
| `JobRejectedException.java` | cola llena (reemplaza `SuggestJobRejectedException`) |
| `BackgroundJob.java` | máquina de estados thread-safe + `subjectKind/subjectId` (namespaces de dedup) + `attributes` Map para datos de cada flujo + `isExpired` |
| `BackgroundJobService.java` | `submit(kind, subjectId, JobTask)` con dedup por `(kind, subject)`, `getJob`, `findActiveJobId`, `purgeExpiredJobs` `@Scheduled`; transiciones y logging centralizados; `JobTask` functional interface |

**Fachadas** (orquestación eliminada, solo lógica de cada flujo):

| Fichero | Contenido |
|---|---|
| `SuggestTickerJobService` (reescrito, misma API) | Delega en el núcleo con kind `"suggest-tickers"`; guarda counts APTO/NO_APTO en attributes |
| `IaValorationJobService` (nuevo) | Delega con kind `"ia-valoration"`; llama a `getValorationIA` sin cambios y guarda `generated` en attributes |

**DTOs**: `SuggestJobStatusDTO` adaptado (counts desde attributes, **contrato JSON sin cambios**); `IaValorationJobStatusDTO` nuevo (`jobId, tickerId, status, startedAt, finishedAt, generated, message`; `generated` solo en `DONE`).

**Infraestructura**: `iaValorationExecutor` (1 hilo, cola 5, prefijo `ia-`) + beans `suggestBackgroundJobs`/`iaBackgroundJobs` con `@Qualifier` (estilo ya usado en adapters) + beans de fachadas; props `ia.executor.*`, `ia.job.ttl-minutes`, `job.cleanup-interval-ms` compartida (la `suggest.job.cleanup-interval-ms` vieja se elimina por muerta).

**Presentación**:
- `AnalyzeTickerController`: POST crea job y redirige limpio a `/analysis/ticker/{id}` (+ flash `started`; `busy` si cola llena); nuevo `GET /analysis/ia-jobs/{jobId}` JSON (`200`/`404`); GET detalle acepta `?ia=failed` → flash error con la clave `ticker.ia.failed` existente (preserva semántica éxito/fallo anterior).
- `ticker-detail.html`: banner + `job-poll.js` (mismo JS compartido).
- `suggest-job-poll.js` → **`job-poll.js`** con IDs neutros (`job-banner/job-alert/job-elapsed`); `detail.html` actualizado.
- `messages.properties`: claves `ticker.ia.job.*` (started/running/busy/interrupted); `validation-ia.js` sin cambios.

## 3. Decisiones técnicas tomadas

1. **Generalizar ahora** (decisión de usuario): núcleo compartido + 2 fachadas finas, en vez de duplicar ~150 líneas. Regla de tres aplicada al revés conscientemente: con 2 flujos ya compensa porque el segundo es espejo del primero.
2. **Executor separado por flujo** (1 hilo cada uno): el presupuesto Polygon y la cuota OpenRouter no deben bloquearse mutuamente; un job suggest de 5 min no para las valoraciones IA y viceversa.
3. **Dedup por `(kind, ticker)`** (decisión de usuario: reutilizar): protege la cuota 50/día frente a doble-clic/F5.
4. **Cola IA 5** (decisión de usuario): los jobs IA son cortos y la cola drena rápido; el dedup evita duplicados.
5. **Atributos string en el job genérico**: compromiso pragmático para no meter genéricos complejos; documentado en el Javadoc. Los counts de suggest se conservan en el JSON (sin breaking change del endpoint ya en prod).
6. **`generated` solo en `DONE`**: el JS recarga a `detailUrl` y añade `?ia=failed` solo si `generated === false` (en suggest ese campo no existe → reload limpio, sin afectar).
7. **Redirect limpio sin `?iaJob=`**: el lookup por ticker en el GET cubre el banner; menos estado en URLs, sin links rancios.
8. **JS compartido con IDs neutros** (decisión de usuario): un solo poller; renombrado verificado con grep + tests de vista actualizados.
9. **Sin `@EnableAsync`**: `TaskExecutor` directo como en suggest (más control, sin proxies extra). `@Scheduled` del purge vive en el núcleo (2 instancias purgan su propio mapa; inofensivo).

## 4. Cobertura de tests y pruebas añadidas

- **Nuevos**: `BackgroundJobTest` (ciclo de vida, transiciones ilegales, attributes, expiración), `BackgroundJobServiceTest` (submit/dedup por kind+subject, DONE/FAILED, reject+olvido, purge, unknowns), `IaValorationJobServiceTest` (submit/dedup/DONE con flag true-false/FAILED/reject/finder), `AnalyzeTickerControllerViewTest` (POST redirect, banner, JSON, 404).
- **Reescritos**: `SuggestTickerJobServiceTest` (fachada sobre núcleo real), 3 tests IA de `AnalyzeTickerControllerTest` → semántica async + 5 nuevos (finder/banner/failed-param/status/404), banner tests de `StrategyControllerViewTest` (IDs neutros), `AnalyzeTickerControllerRoutingTest` (+mock del bean nuevo).
- **Suite completa: 1137 tests, 0 failures, 0 errors, BUILD SUCCESS.**
- **Cobertura código nuevo (JaCoCo):** 100% líneas en las 7 clases (`BackgroundJob` branches 16/18, `BackgroundJobService` 19/19, `SuggestTickerJobService` branches 8/10). DTO/config excluidos por convención del `pom.xml`.
- **JS**: `node --check` OK; sin Sonar nuevo (sin `dataset()` como función, con `Number.isNaN`/`.includes()`).
- **Sin `lenient` en Mockito.**

## 5. Advertencias de SonarQube / arquitectura

- `AnalyzeTickerController` constructor pasa a 4 params (< 7 S107 OK); métodos pequeños; `switch`→`if` simple donde basta.
- SpEL corta en plantillas; JS en fichero propio con `textContent` (XSS-safe); sin lógica en vistas.
- Hexagonal: orquestación en Application, `BackgroundJobService` agnóstico al dominio (solo `kind`/`subjectId` opacos), detalles (executors, scheduling) en Infrastructure; dominio y `ManageAnalyzeStockService` intactos.
- `Optional` como retorno (no como campo/parámetro); `synchronized` solo en secciones críticas del mapa.

## 6. Próximos pasos sugeridos

### A. Probar ahora (obligatorio, tras merge + deploy con imagen nueva)
1. En un ticker, pulsar "Generar análisis" → redirect en <1s con banner "Generando análisis…" + reloj.
2. Esperar → recarga sola con la valoración visible (sin 504).
3. Doble-clic/F5 a mitad → mismo job, sin quemar cuota doble (`logs | grep background_job_reused`).
4. Provocar fallo LLM (o esperar fallback) → recarga con `?ia=failed` + flash de error y texto fallback visible.
5. `GET /analysis/ia-jobs/xxx` sin login → 302 a login.
6. Regresión suggest-tickers: sigue funcionando igual (banner neutro, counts en JSON intactos).

### B. Ideas futuras (no hacer ahora)
1. Caché de Polygon en BD para suggest (bajar de 4 min).
2. Persistir jobs en BD si los reinicios a mitad molestan.
3. Progreso fino 12/20 en suggest (instrumentación pendiente).
4. Pinnear `TFM_TAG` a SHA (del doc anterior).
