# Task 2026-09-18: Polygon throttler extraído (mismo patrón que Finnhub)

## Resumen
`PolygonAdapter` llevaba su freno en línea (campo `Deque`, constantes
`RATE_LIMIT_WINDOW=62000` / `MAX_CALLS_PER_MINUTE=5` y 3 métodos privados).
Se extrae a `PolygonThrottler` (`@Component` propio, misma forma que
`FinnhubThrottler` por decisión del usuario: cada API dueña de su ritmo, sin
abstracción compartida). El adapter queda solo con HTTP + mapeo.

## Código generado
- Nueva `infrastructure/external/polygon/PolygonThrottler.java`: ventana
  deslizante, `acquire()` sincronizado (espera `windowMs - elapsed` + 200ms,
  registra), valores desde properties (`polygon.ratelimit.max-calls=5`,
  `polygon.ratelimit.window-ms=62000`).
- `PolygonAdapter`: inyecta `PolygonThrottler`, una llamada `acquire()` antes
  del HTTP; eliminados campo `Deque`, 2 constantes y
  `waitForRateLimit/recordApiCall/removeExpiredTimestamps` (~40 líneas menos).
- `application.properties`: añadidas `polygon.ratelimit.*` (5/62s, margen bajo
  el límite gratuito 5/min). Comportamiento idéntico al anterior.
- Tests: nuevo `PolygonThrottlerTest` (3 tests: cupo, espera, deslizamiento);
  `PolygonAdapterTest.setUp` actualizado al nuevo constructor con cupo generoso.

## Decisiones técnicas
- Clase propia en vez de base compartida (decisión explícita del usuario).
- `acquire()` único (espera+registro atómicos) en vez de `wait+record`
  separados: ningún hilo se cuela entre medias.
- Límite/ventana por properties con defaults iguales a los antiguos fijos.

## Cobertura de tests y pruebas
- `PolygonAdapterTest + PolygonThrottlerTest`: 28 run, 0 fallos.
- Suite completa `mvn test`: 1062 tests, 0 fallos (1059 previos + 3 nuevos).

## Advertencias SonarQube / arquitectura
- Hexagonal intacto (solo Infrastructure). Sin números mágicos, `debug` para la
  espera normal, `ConcurrentLinkedDeque` + `synchronized` como antes.

## Próximos pasos sugeridos
1. Commit `fix:/test:/docs:`, push y PR a `main` tras validación.
2. Rama pendiente aparcada: `fix/traceability-th-text`.
