# F1.1 – Añadir campo ticker a CandleEntity y actualizar CandleMapper

## Resumen de la tarea

Implementación del paso F1.1 del plan de registro de tickers Polygon en base de datos.

El objetivo es preparar la entidad JPA `CandleEntity` para persistir el histórico OHLCV identificado directamente por ticker, eliminando la relación `@ManyToOne` con `StockEntity` que no era utilizada por ningún código activo.

---

## Trabajo realizado

### 1. `CandleEntity.java`

- **Eliminado**: bloque `@ManyToOne(fetch = FetchType.LAZY)` / `@JoinColumn(name = "stocks_id")` y el campo `private StockEntity stock`.
- **Eliminadas**: importaciones de `FetchType`, `JoinColumn` y `ManyToOne`, que ya no son necesarias.
- **Añadido**: campo `private String ticker` para persistir el identificador del activo directamente en la tabla `candles`.

### 2. `CandleMapper.java`

- **`toEntity()`**: añadida línea `entity.setTicker(candle.getTicker())` para incluir el ticker en el mapeo dominio → entidad.
- **`toDomain()`**: añadida línea `.ticker(entity.getTicker())` en el builder para incluir el ticker en el mapeo entidad → dominio.

---

## Decisiones técnicas

- La relación con `StockEntity` se ha eliminado porque no existe código activo que la use; el campo `stock` nunca era asignado por el mapper y la FK `stocks_id` no aportaba valor al flujo de histórico.
- El campo `ticker` se añade como `String` en la entidad siguiendo el modelo de dominio `Candle` ya existente.
- No se añade anotación `@Column(nullable = false)` en este paso (queda para F1.2, que define restricciones JPA completas).

---

## Archivos afectados

- `src/main/java/com/market/analysis/infrastructure/persistence/entity/CandleEntity.java`
- `src/main/java/com/market/analysis/infrastructure/persistence/mapper/CandleMapper.java`
- `src/test/java/com/market/analysis/unit/infrastructure/persistence/mapper/CandleMapperTest.java`

---

## Cobertura de tests

Tests unitarios existentes en `CandleMapperTest` actualizados para incluir:
- Aserción de `ticker` en `testToEntity()`.
- Aserción de `ticker` en `testToDomain()`.
- Aserción de `ticker` en `testToEntityWithZeroValues()`.

Todos los tests pasan correctamente tras el cambio.

---

## Resultado

- `CandleEntity` contiene: `id`, `ticker`, `dateTime`, `openPrice`, `highPrice`, `lowPrice`, `closePrice`, `volume`.
- FK `stocks_id` eliminada. Sin dependencia de `StockEntity`.
- El mapper mapea los 7 campos en ambas direcciones sin omisiones.

---

## Próximos pasos sugeridos

1. Ejecutar **F1.2**: añadir restricciones JPA (`@Column(nullable = false, length = 10)`) y definir índice único `(ticker, dateTime)` en `CandleEntity`.
2. Ejecutar **F1.3**: crear `JpaCandleRepository` extendiendo `JpaRepository<CandleEntity, Long>`.
3. Ejecutar **F1.4**: crear `SqlCandleHistoryRepository` con método `saveCandlesForTicker`.
