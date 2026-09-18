# Task 2026-09-18: Finnhub throttle — la config sí llega a Docker

## Resumen
El `@RateLimiter(name="finnhubClient")` de `FinnhubAdapter` parecía no frenar (8 llamadas en 2s con límite 5).
Diagnóstico final: el proxy CGLIB sí existía
(`provider class=...FinnhubAdapter$$SpringCGLIB$$0`), pero `config/application.properties`
nunca entraba al contenedor porque el `Dockerfile` solo copia `pom.xml + src` y
`docker-compose.yml` no monta `config/`. El limiter corría con valores por defecto.
El throttler manual del usuario (`checkPermisionFetch`) sí frenaba porque es código
en `src` y sí va dentro del jar. Se ha revertido el experimento manual y se mueve
`config/` a `src/main/resources/config/` (classpath, estándar Spring Boot) para que
`resilience4j.ratelimiter.instances.finnhubClient.*` llegue al jar. Movimiento puro,
sin cambios de contenido.

## Código generado
Ninguna clase nueva. Movimiento de ficheros (contenido idéntico, verificado con `diff`):
- `config/application.properties` → `src/main/resources/config/application.properties`
- `config/application-dev.properties` → `src/main/resources/config/application-dev.properties`
- `config/application-docker.properties` → `src/main/resources/config/application-docker.properties`
- `config/application-prod.properties` → `src/main/resources/config/application-prod.properties`
- `config/database/script-bd.sql` → `src/main/resources/config/database/script-bd.sql`

Fix en `docker-compose.yml:55` (ruta del volumen de init de MySQL, estaba mal tras el
movimiento manual):
```yaml
- ./src/main/resources/config/database/script-bd.sql:/docker-entrypoint-initdb.d/script-bd.sql:ro
```
(antes apuntaba a `./src/config/database/...`, que no existe).

Separación de temas: el fix de `strategy-traceability.html` (3x `th:text` como texto)
que había en el árbol de trabajo se ha revertido en esta rama
(`git checkout HEAD -- ...`) para respetar una-rama-un-tema. Irá en su propia rama.

## Decisiones técnicas
- Mantener `@RateLimiter` de Resilience4j (55/65s, timeout 65s) en vez del throttler
  manual: respeta DIP/SRP (el adapter no suma la responsabilidad de rate-limit),
  es declarativo y ya tiene `spring-boot-starter-aop + proxy-target-class=true`.
  El manual tenía bug de espera (`wait(edad)` en vez de `wait(60s - edad)`),
  log `WARN` con `SYMBOL` literal, `ArrayDeque` no concurrente y doble freno (5 vs 8).
- Ubicación estándar `src/main/resources/config/` en vez de `COPY config` en Dockerfile:
  Spring Boot carga solo `classpath:/application.properties` y `classpath:/config/…`;
  con `config/` dentro de resources el jar es autocontenido y Docker no necesita
  volúmenes extra. Prioridad: `classpath:/config/` gana sobre raíz.
- No se baja `maxCandidates` (100) ni se toca lógica determinista de evaluación;
  IA solo interpretativa, según AGENTS.md.

## Cobertura de tests y pruebas
- `mvn -Dtest='FinnhubAdapterTest,FinnhubMapperTest' test`: **14 run, 0 fallos**
  (6 adapter + 8 mapper). Sin tests nuevos: el cambio es solo reubicación de
  properties, sin lógica. Cobertura de `FinnhubAdapter` intacta.
- Pendiente (fase 2, otra tarea): test de integración que arranque el contexto y
  verifique que `RateLimiterRegistry` expone `finnhubClient` con `limitForPeriod=55`.

## Advertencias SonarQube / arquitectura
- Hexagonal intacto: solo se mueven properties + ruta de volumen; `domain`,
  `application` y puertos sin tocar.
- Sin `lenient` Mockito, sin field injection nueva, sin `th:utext`, sin secretos.
- Riesgo conocido: `timeoutDuration=65s` encola el hilo `exec` hasta ~4 min con
  100 tickers x 2 llamadas; el navegador puede cortar a 60-120s. Si la UX lo exige,
  plantear job background como tarea aparte (no en esta rama).

## Próximos pasos sugeridos
1. Validar en Docker que el 6º profile espera (log `ratelimiter` DEBUG) y que
   `GET /` → 200 con MySQL Healthy.
2. Rama aparte `fix/traceability-th-text` con el fix revertido aquí.
3. Valorar `FinnhubThrottler` explícito estilo `PolygonAdapter` solo si el `@RateLimiter`
   vuelve a fallar en silencio; con la config ya dentro del jar debería bastar.
4. Commit `chore:` + push + PR a `main` tras validación del usuario.
