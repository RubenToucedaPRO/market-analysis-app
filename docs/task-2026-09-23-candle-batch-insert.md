# Task 2026-09-23 — Fix: batch insert nativo de velas (36s → ~1s)

## Resumen de la tarea

El deploy Railway funcionaba pero "añadir ticker" tardaba 41.6s. Medición con
logs de Railway (DEBUG temporal en `infrastructure.external.*`, ya revertido):
persistir 240 velas consumía **~36s de 38s totales** (12.6 → 48.4 en el log).

Causa: `SqlCandleHistoryRepository.saveCandlesForTicker` usaba
`jpaCandleRepository.saveAll()`. Sin batching JDBC (e `IDENTITY` en
`CandleEntity`, que lo impide) Hibernate emite **un INSERT por vela**: 240
viajes × ~145ms de latencia = ~35s. En local (~1ms) el mismo código tarda
0.2s, por eso allí no se notaba.

Fix: `deleteByTicker` (JPQL, sin cambios) + `JdbcTemplate.batchUpdate` con un
`INSERT INTO candles (...) VALUES (?,?,...)` nativo (un lote = un round-trip).
Sin cambios de esquema. Además `SPRING_DATASOURCE_URL` del compose lleva
`rewriteBatchedStatements=true` para que el driver reescriba el lote en un
único INSERT múltiple.

## Código generado

- `.../persistence/repository/SqlCandleHistoryRepository.java`: `saveAll` →
  `jdbcTemplate.batchUpdate(INSERT_CANDLE_SQL, batchArgs)` + javadoc con el
  porqué y la nota del flag del driver. `CandleMapper` se sigue usando en los
  métodos de lectura.
- `docker-compose.yml`: `SPRING_DATASOURCE_URL` con
  `?rewriteBatchedStatements=true`.
- `SqlCandleHistoryRepositoryTest`: adaptado al batch (orden delete→insert,
  contenido de filas en orden de columnas, no-op sin interacciones).
- `SqlCandleHistoryRepositoryBatchTest` (nuevo, `@DataJpaTest` + H2): el SQL
  real se ejecuta de verdad (persistencia, reemplazo sin duplicar, no-op con
  lista vacía). Primer test de integración del repo; necesita
  `ddl-auto=create-drop` explícito porque el perfil `dev` impone `validate`.

## Decisiones técnicas tomadas

- `JdbcTemplate.batchUpdate(sql, List<Object[]>)` (lote único) en vez de la
  variante con `int[]` (esa es para tipos de argumento, no tamaño de lote).
- `Timestamp.from(instant)` explícito para `date_time` (portable entre H2,
  MariaDB y MySQL).
- Sin `hibernate.jdbc.batch_size`: con `IDENTITY` no aplicaría igualmente.
- Sin tocar entidades ni esquema: el fix es solo infraestructura + URL.

## Cobertura de tests y pruebas añadidas

- `mvn test`: `Tests run: 1071, Failures: 0, Errors: 0` (1068 + 3 nuevos de
  integración; 1 unitario reescrito de `saveAll` a batch).
- Sin `lenient` nuevo.

## Advertencias de SonarQube o arquitectura

- SQL nativo aislado en el repositorio de infraestructura (hexagonal: el
  dominio no lo ve; el contrato `CandleHistoryRepository` no cambia).
- Constantes para SQL y sin concatenación de valores (parametrizado, sin
  riesgo de inyección).

## Próximos pasos sugeridos

- En Railway (tú, dashboard): añadir `?rewriteBatchedStatements=true` a
  `DB_URL` → `jdbc:mariadb://.../railway?allowPublicKeyRetrieval=true&rewriteBatchedStatements=true`.
  Ojo: el primer `?` ya está usado, el segundo parámetro va con `&`.
- Medir de nuevo "añadir ticker" en Railway (esperado: ~41s → ~5s).
- Borrar las 3 variables `LOGGING_LEVEL_*_DEBUG` de Railway (volver a INFO).
- Costes restantes (turno Polygon 5/min, cola OpenRouter gratis): inherentes
  a APIs gratuitas, fuera de alcance.

## Verificación runtime

- Requerida por §3.7 (compose + `SecurityConfig` no tocado aquí, pero sí
  `docker-compose.yml`): `docker compose down && docker compose up --build -d`.
- Resultado: `GET /` → 200, `GET /health` → 200 UP, `market-analysis-mysql`
  Healthy. Medición antes/después con 1 ticker: pendiente en Railway tras el
  merge (allí está la latencia real; en local ambas variantes son
  instantáneas).

## Checklist de pruebas web

### A. Probar ahora

1. Con el contenedor local: abre `http://localhost:8080/health` → JSON con
   `"status":"UP"` (la app arranca con el compose modificado).
2. En Railway tras el merge + `DB_URL` con ambos parámetros: añade un ticker
   nuevo y compara el tiempo con los 41.6s medidos (esperado ~5s). Todo lo
   demás está cubierto por tests (`SqlCandleHistoryRepositoryBatchTest`).

### B. Ideas futuras

- Batching en otras escrituras masivas si la medición lo pide (snapshots).
- Revisar timeout de lectura Polygon (10s) si sigue dando 429/timeouts.
