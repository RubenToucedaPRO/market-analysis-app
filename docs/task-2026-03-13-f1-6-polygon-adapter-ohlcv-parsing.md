# F1.6 – Extender el parseo de PolygonAdapter para extraer velas completas

## Resumen

Extensión del adaptador Polygon para que, a partir del mismo JSON que ya se usaba para indicadores,
extraiga además los campos OHLCV completos (o, h, l, c, v, t) y los persista como velas en base de
datos. Se implementan también F1.7 (integración de persistencia) y F1.8 (observabilidad) en la misma
entrega, ya que los tres puntos son parte del mismo flujo de PolygonAdapter.

---

## Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `src/main/java/.../external/polygon/PolygonAdapter.java` | F1.6 + F1.7 + F1.8 |
| `src/test/java/.../external/polygon/PolygonAdapterTest.java` | Nuevos grupos de tests |

---

## Decisiones técnicas

### F1.6 – Parseo OHLCV

- Se introduce el record privado `ParseResult(HistoricalData historicalData, List<Candle> candles)`
  para trasladar ambas salidas lógicas desde un único recorrido del JSON.
- `mapToHistoricalData` se renombra a `parseApiResponse` (visibilidad de paquete para
  testabilidad interna) y devuelve `ParseResult` en lugar de solo `HistoricalData`.
- La conversión del campo `t` (epoch en milisegundos) se realiza con
  `Instant.ofEpochMilli(timestampMs)`.
- Los precios open/high/low/close se convierten a `BigDecimal` mediante `BigDecimal.valueOf(double)`
  para mantener coherencia con el esquema de base de datos (`precision=18, scale=4`).
- Nodos sin campo `t` (o con `t == 0`) no generan `Candle`; sí siguen acumulando en
  `closingPrices` y `volumes`, preservando el contrato previo de `HistoricalData`.

### F1.7 – Integración de persistencia

- `fetchHistoricalData` llama al nuevo método privado `persistCandles` después del parseo exitoso.
- `SqlCandleHistoryRepository` se inyecta como dependencia en el constructor de `PolygonAdapter`
  (ambos son componentes de infraestructura; la dependencia es aceptable en arquitectura hexagonal).
- Si la lista de velas está vacía (ningún nodo tenía `t` válido), `persistCandles` retorna
  inmediatamente sin llamar a `saveCandlesForTicker`, evitando un delete innecesario en BD.

### F1.8 – Observabilidad

- `persistCandles` registra en nivel `INFO`:
  - ticker
  - número de velas persistidas
  - fecha mínima y máxima del lote (extraídas del stream de candles)
- En caso de lista vacía se registra solo un mensaje de nivel `DEBUG`.

---

## Código generado (fragmento relevante)

```java
private record ParseResult(HistoricalData historicalData, List<Candle> candles) {}

ParseResult parseApiResponse(String ticker, String jsonBody) {
    List<Double> prices = new ArrayList<>();
    List<Long> volumes = new ArrayList<>();
    List<Candle> candles = new ArrayList<>();
    try {
        JsonNode root = objectMapper.readTree(jsonBody);
        JsonNode resultsNode = root.path("results");
        if (resultsNode.isArray()) {
            for (JsonNode node : resultsNode) {
                double closePrice = node.path("c").asDouble();
                long volume = node.path("v").asLong();
                prices.add(closePrice);
                volumes.add(volume);
                long timestampMs = node.path("t").asLong();
                if (timestampMs > 0) {
                    candles.add(Candle.builder()
                            .ticker(ticker)
                            .dateTime(Instant.ofEpochMilli(timestampMs))
                            .openPrice(BigDecimal.valueOf(node.path("o").asDouble()))
                            .highPrice(BigDecimal.valueOf(node.path("h").asDouble()))
                            .lowPrice(BigDecimal.valueOf(node.path("l").asDouble()))
                            .closePrice(BigDecimal.valueOf(closePrice))
                            .volume(volume)
                            .build());
                }
            }
        }
    } catch (Exception e) {
        throw new PolygonException("Error mapping historical data for " + ticker, e);
    }
    return new ParseResult(new HistoricalData(ticker, prices, volumes, Instant.now()), candles);
}
```

---

## Cobertura de tests añadida

### `OhlcvCandleParsingTests` (F1.6)

| Test | Cubre |
|------|-------|
| `testExtractsFullOhlcv` | Todos los campos OHLCV + ticker + timestamp |
| `testConvertsTimestampToInstant` | Conversión milisegundos → Instant |
| `testSkipsCandleWithoutTimestamp` | Nodos sin `t` no generan vela; sí acumulan precios |
| `testPreservesHistoricalDataContract` | `closingPrices` y `volumes` se mantienen en orden |

### `CandlePersistenceIntegrationTests` (F1.7)

| Test | Cubre |
|------|-------|
| `testPersistsCandles_whenResponseHasTimestamps` | `saveCandlesForTicker` llamado con lista correcta |
| `testDoesNotPersistCandles_whenJsonInvalid` | JSON inválido → no se persiste |
| `testDoesNotPersistCandles_whenHttpError` | Error HTTP → no se persiste |
| `testDoesNotPersist_whenNoTimestamps` | Sin timestamps → `saveCandlesForTicker` no se llama |

**Resultado:** 611 tests, 0 fallos.

---

## Advertencias SonarQube / arquitectura

- La dependencia `SqlCandleHistoryRepository → PolygonAdapter` es infraestructura ↔ infraestructura.
  Aceptable dentro de la capa de infraestructura; sin impacto en la capa de dominio ni de aplicación.
- `BigDecimal.valueOf(double)` puede introducir imprecisión en precios con muchos decimales.
  Para los datos de Polygon (máximo 4 decimales en cierre), esto es suficiente.

---

## Próximos pasos sugeridos

- **F1.9**: Ampliar `PolygonAdapterTest` con datos de mayor volumen (>200 candles).
- **F1.10**: Test de integración JPA con H2 (guardar + reemplazar + verificar ausencia de duplicados).
- **F2.x**: Implementar endpoint y vista de gráfico de velas usando los datos ya persistidos.
