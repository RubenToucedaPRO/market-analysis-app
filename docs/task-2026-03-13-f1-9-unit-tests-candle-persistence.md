# F1.9 – Pruebas unitarias de parseo y persistencia de velas OHLCV

## Resumen de la tarea

Implementación de F1.9 del plan `plan_regitro_tickers_polygon.md`: añadir cobertura de tests unitarios para la nueva lógica de parseo OHLCV y persistencia de velas introducida en F1.6–F1.8.

## Estado de cobertura antes de esta tarea

| Componente | Tests previos | Situación |
|---|---|---|
| `PolygonAdapterTest` | OHLCV parsing, excepciones JSON/HTTP | ✅ Completo (añadido en commits anteriores de la rama) |
| `SqlCandleHistoryRepositoryTest` | replace por ticker, lista vacía, guards | ✅ Completo (F1.5) |
| `ManageAnalyzeStockServiceTest` | `CandleHistoryPort` mockeado pero sin assertions | ❌ Faltaban 2 tests |

## Código añadido

### `ManageAnalyzeStockServiceTest`

**Imports añadidos:**
```java
import com.market.analysis.domain.model.Candle;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
```

**Test 1 — llamada a persistencia cuando hay velas:**
```java
@Test
@DisplayName("Should call saveCandlesForTicker when historical data contains candles")
void shouldPersistCandlesWhenHistoricalDataContainsCandles()
```
Verifica que `candleHistoryPort.saveCandlesForTicker("AAPL", anyList())` se invoca exactamente una vez cuando `HistoricalData` contiene al menos una vela.

**Test 2 — no llamada a persistencia cuando no hay velas:**
```java
@Test
@DisplayName("Should not call saveCandlesForTicker when historical data has no candles")
void shouldNotPersistCandlesWhenHistoricalDataHasNoCandles()
```
Verifica que `saveCandlesForTicker` nunca se invoca cuando `HistoricalData` tiene lista de velas vacía (comportamiento por defecto del `setUp()`).

## Decisiones técnicas

- Se usaron matchers `eq("AAPL")` y `anyList()` en lugar de la instancia exacta de la lista porque `Candle` no implementa `equals()` (usa `@Builder` + `@Getter` sin `@EqualsAndHashCode`).
- Los tests se añaden como métodos planos en el mismo estilo que el resto del archivo, sin nested classes adicionales, para mantener consistencia.
- El `@MockitoSettings(strictness = Strictness.LENIENT)` ya existente cubre que los mocks no usados en tests individuales no causen fallo por "unnecessary stubbing".

## Cobertura de tests resultante

| Test class | Tests | Resultado |
|---|---|---|
| `ManageAnalyzeStockServiceTest` | 21 | ✅ PASS |
| `PolygonAdapterTest` | 25 | ✅ PASS |
| `SqlCandleHistoryRepositoryTest` | 7 | ✅ PASS |
| **Total ejecutados** | **53** | ✅ BUILD SUCCESS |

## Advertencias de arquitectura

- La persistencia de velas está correctamente orquestada en el Use Case (`ManageAnalyzeStockService`), no en el adaptador. Los tests reflejan esta responsabilidad.
- Los tests de `PolygonAdapter` verifican que el adaptador devuelve velas en `HistoricalData`; los tests del servicio verifican que el Use Case llama a `CandleHistoryPort`.

## Próximos pasos

- F1.10: Prueba de integración JPA con H2 para verificar persistencia real (guardar 240 velas, reemplazar dataset, ausencia de duplicados).
