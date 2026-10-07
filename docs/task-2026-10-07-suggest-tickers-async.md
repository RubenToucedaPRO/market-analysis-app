# Task 2026-10-07 — `suggest-tickers` async + polling (fix 504 Nginx)

- **Rama:** `feature/suggest-tickers-async` (desde `main` = `e6b3baa`)
- **Alcance:** `POST /strategies/{id}/suggest-tickers` pasa de síncrono-bloqueante a async (202 + job + polling). Sin cambios en lógica de negocio (`SuggestTickersService` intacto) ni en esquema BD.
- **Fecha:** 2026-10-07.

---

## 1. Resumen de la tarea

El endpoint `POST /strategies/{id}/suggest-tickers` ejecutaba todo el análisis en el hilo HTTP: Finviz + 20 tickers × (Finnhub + Polygon con throttle 5/min ≈ ≥240s) de forma secuencial. Nginx corta a los 60s (`proxy_read_timeout` por defecto) → **504 Gateway Time-out** en producción, aunque la app seguía trabajando.

Corrección previa descartada como fix principal: subir timeouts de Nginx (frágil: conexión abierta 4+ min, F5 duplica trabajo, sin feedback).

Solución implementada (opción B1): el POST crea un job en ms y redirige a `detail?jobId=`; un worker en segundo plano ejecuta el flujo existente y persiste el snapshot (como antes); la página pregunta el estado cada 5s y recarga al terminar. Todas las peticiones HTTP duran segundos → Nginx nunca corta. Sin JS también funciona degradado (recarga manual muestra el snapshot terminado).

Hallazgo de investigación: el flujo **no hace llamadas LLM** (el tiempo viene 100% del throttle Polygon); `OpenrouterAdapter` solo se usa desde `getValorationIA`.

---

## 2. Código generado

**Nuevos** (`application/job/`, `application/dto/`):

| Fichero | Contenido |
|---|---|
| `application/job/SuggestJobStatus.java` | enum `PENDING/RUNNING/DONE/FAILED` (terminales se purgan por TTL) |
| `application/job/SuggestTickerJob.java` | holder de estado thread-safe (transiciones `synchronized`, `volatile`) + `isExpired(now, ttl)`; solo guarda resumen (counts), el resultado vive en el snapshot de BD |
| `application/job/SuggestJobRejectedException.java` | cola llena → el controlador muestra "ocupado, reintenta" en vez de bloquear |
| `application/job/SuggestTickerJobService.java` | `submitSuggestionJob` (no bloqueante + **dedup**: reutiliza job activo de la estrategia), worker con try/catch → `FAILED`, `getJob`, `purgeExpiredJobs` vía `@Scheduled` (usa el `@EnableScheduling` existente) |
| `application/dto/SuggestJobStatusDTO.java` | contrato JSON del polling (`jobId, strategyId, status, startedAt, finishedAt, suggested/discardedCount, message` localizado) |
| `static/js/suggest-job-poll.js` | polling `fetch` cada 5s (solo si hay banner): spinner + elapsed, `DONE` → reload, `FAILED` → alerta `danger` con `textContent` (XSS-safe), 404/no-JSON → alerta `warning`. Verifica `content-type` antes de parsear (por si responde el login) |

**Modificados:**

| Fichero | Cambio |
|---|---|
| `presentation/controller/StrategyController.java` | POST: crea job y `redirect:/strategies/{id}?jobId=` (+ ramas `unavailable`/`busy`); nuevo `GET /suggest-jobs/{jobId}` JSON (`200` / `404`); mensajes FAILED/DONE localizados en el controlador |
| `presentation/util/WebConstants.java` | `PARAM_SUGGEST_JOB_ID = "?jobId="` (cero strings mágicos) |
| `infrastructure/config/BeanConfig.java` | `TaskExecutor suggestTickerExecutor` (**1 hilo**, cola 10, `AbortPolicy`) + bean `suggestTickerJobService`; coherente con `@Value`-con-defaults del proyecto |
| `config/application.properties` | `suggest.executor.pool-size/queue-capacity/thread-prefix`, `suggest.job.ttl-minutes/cleanup-interval-ms` (comentados, sin secretos) |
| `messages.properties` | 6 claves `strategy.suggestion.job.*` (started/running/done/failed/busy/interrupted); sin textos hardcodeados |
| `templates/strategies/detail.html` | banner `alert-info` con spinner + elapsed (solo si `?jobId=`), div de alerta, `<script th:src="@{/js/suggest-job-poll.js}">`. Sin SpEL larga; el form POST sigue siendo form (cero problemas CSRF en JS: el polling es GET) |

---

## 3. Decisiones técnicas tomadas

1. **Worker reutiliza el caso de uso existente sin tocarlo.** `SuggestTickerJobService` llama al bean `SuggestTickersUseCase` inyectado (proxy Spring → transacción nueva en el hilo worker, pues `suggestTickers()` es `@Transactional`). Cero cambios en `SuggestTickersService`, cero riesgo en reglas de negocio.
2. **Executor de 1 hilo**, no pool amplio: el presupuesto Polygon (5/min) es compartido; en paralelo todos los jobs irían más lentos. Cola acotada (10) + `AbortPolicy` → "ocupado" explícito en vez de bloqueo silencioso.
3. **Dedup por estrategia** (decisión de usuario): segundo submit con job activo devuelve el mismo `jobId`. Ahorra 20 llamadas Polygon y evita resultados duplicados.
4. **Registro en memoria** (decisión de usuario, `ConcurrentHashMap` + purga `@Scheduled`): jobs efímeros; el resultado persiste en BD igualmente. Al reiniciar el contenedor el polling recibe 404 → la página muestra "interrumpido, reintentar". Alternativa BD descartada por coste.
5. **Progreso grueso** (spinner + tiempo, sin 12/20): evita instrumentar el servicio. El conteo fino queda como mejora futura.
6. **PRG preservado**: el POST sigue devolviendo redirect (no 202 directo), así los flashes (`started`/`busy`/`unavailable`) y el degradado sin-JS funcionan igual que antes.
7. **Sin `@EnableAsync`**: se usa `TaskExecutor` directamente (más control que `@Async`, sin proxies extra). `@EnableScheduling` ya existía.
8. **Seguridad**: el endpoint de estado cae bajo `.anyRequest().authenticated()` (sin cambios en `SecurityConfig`); el mensaje de error técnico queda solo en logs, la UI recibe clave i18n.
9. **Nginx sin cambios**: con B1 los timeouts del Nivel 1 son innecesarios; no se toca `tfm.conf`.

---

## 4. Cobertura de tests y pruebas añadidas

- **Nuevos** `SuggestTickerJobServiceTest` (10 tests): submit no bloqueante, dedup, jobs por estrategia, worker DONE con conteos APTO/NO_APTO, FAILED con `errorDetail`, rechazo de cola (job olvidado), `getJob` desconocido/vacío, purga TTL, transiciones ilegales (`IllegalStateException`), expiración.
- **Reescritos** (comportamiento POST cambió): `StrategyControllerTest.testSuggestTickersFromMarketSuccess/Empty` → submit con `?jobId=`, `never()` al caso de uso en hilo, caso `busy`, caso `unavailable`, y 3 del endpoint de estado (RUNNING/DONE-localizado/404).
- **Nuevos MockMvc** en `StrategyControllerViewTest`: POST redirige con `jobId` + flash, banner renderizado con `data-status-url`, JSON de estado, 404; reescrito el test de flash de trazabilidad al nuevo flujo.
- **Sin tocar**: `SuggestTickersServiceTest`, contrato del puerto, resto de la suite.
- **Suite completa: 1105 tests, 0 failures, 0 errors, BUILD SUCCESS.**
- **Cobertura código nuevo (JaCoCo):** `SuggestJobStatus` 100%, `SuggestJobRejectedException` 100%, `SuggestTickerJob` 100% líneas (16/18 branches), `SuggestTickerJobService` 100% líneas (19/22 branches). DTO excluido por convención del proyecto (`application/dto/**` fuera del reporte en `pom.xml`), igual que `BeanConfig` (`infrastructure/config/**`).
- **Sin `lenient` en Mockito** (stubs estrictos; el log `suggest_job_failed` visible en un test es salida esperada del path FAILED, no un fallo).
- **Bug manual-testing** (`suggest-job-poll.js`): `banner.dataset('...')` usado como función (es propiedad) lanzaba `TypeError` antes del `setInterval` → reloj congelado en `0:00` y polling muerto. Corregido con `getAttribute` + guardia null + eliminada variable muerta `pollTimer`. Verificado con `node --check` y retest en navegador.

---

## 5. Advertencias de SonarQube / arquitectura

- Constructor de `StrategyController` pasa a 5 parámetros (< 7, S107 OK); complejidad de los métodos nuevos baja (S3776 OK); sin lambdas anidadas profundas (S134 OK).
- Sin SpEL >120 caracteres ni operadores múltiples en `detail.html`; `param.jobId[0]` indexado simple; JS en fichero propio (cero lógica en vistas).
- Hexagonal respetada: job/orquestación en Application, transporte en DTO, detalles (executor, scheduling) en Infrastructure; dominio intacto.
- `switch` sobre enum con `default` (exhaustivo ante futuros estados).
- Pendiente ideal (no bloqueante): `shellcheck`/Sonar en CI ya lo cubre el pipeline del repo.

---

## 6. Próximos pasos sugeridos

### A. Probar ahora (obligatorio)
1. `POST /strategies/48/suggest-tickers` → redirect en <1s con banner "Calculando…".
2. Esperar ~4-6 min → recarga automática con resultados (sin 504).
3. F5 a mitad → reutiliza el job (mismo `jobId`, sin duplicar llamadas).
4. `GET /strategies/suggest-jobs/xxx` sin login → 302 a login.
5. En prod: `curl -I https://tfm.rubentouceda.es/strategies/48/suggest-tickers` ya no aplica (es POST); probar el flujo real en navegador + `docker compose logs app | grep suggest_job_`.

### B. Ideas futuras (no hacer ahora)
1. Progreso fino 12/20 instrumentando el servicio.
2. Persistir jobs en BD si los reinicios a mitad de job molestan.
3. Caché de Polygon en BD (B2 de la lista anterior) para bajar de 4 min.
4. `shellcheck` no aplica (sin scripts nuevos); `Nivel 1` de timeouts Nginx **cancelado** por innecesario con B1.
