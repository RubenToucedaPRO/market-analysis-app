# Fase 5 — Implementación Bollinger Bands y ATR

**Fecha:** 2026-04-07  
**Rama:** `copilot/sub-pr-75`  
**PR asociado:** Sub-PR de #75

---

## Resumen

Implementación de la Fase 5 del plan de indicadores técnicos: cálculo de Bollinger Bands (BB_UPPER20, BB_LOWER20) y Average True Range (ATR14), propagación al modelo `Stock` y soporte en `RuleEvaluator` con códigos `BB_UPPER`, `BB_LOWER` y `ATR`.

> **Dependencia satisfecha:** Fases 1-4 ya completadas. Los campos del modelo de dominio (`bbUpper20`, `bbLower20`, `atr14`) y la propagación en `ManageAnalyzeStockService` ya estaban declarados desde la Fase 1.

---

## Archivos Modificados

| Archivo | Tipo de cambio |
|---------|---------------|
| `src/main/java/com/market/analysis/domain/service/StockHistoricalService.java` | Nuevos métodos `calculateBollingerUpper()`, `calculateBollingerLower()`, `calculateBollingerBand()` (privado), `calculateAtr()` + llamadas en `calculateIndicators()` |
| `src/main/java/com/market/analysis/domain/service/RuleEvaluator.java` | Nuevos cases `"BB_UPPER"`, `"BB_LOWER"`, `"ATR"` + métodos privados `getBbValue()`, `getAtrValue()` |
| `src/test/java/com/market/analysis/unit/domain/service/StockHistoricalServiceTest.java` | Actualización test Phase 1 (BB/ATR ya no son null) + nuevo `@Nested` Phase 5 (10 tests) |
| `src/test/java/com/market/analysis/unit/domain/service/RuleEvaluatorTest.java` | Nuevo `@Nested` Phase 5 BB/ATR Rule Tests (8 tests) |

> **Nota:** `ManageAnalyzeStockService.java`, `TechnicalIndicators.java`, `Stock.java` y `StockEntity.java` ya incluían los campos y la propagación BB/ATR desde la Fase 1. No fue necesario modificarlos.

---

## Código Generado

### `StockHistoricalService.java` — método `calculateBollingerBand()`

```java
private BigDecimal calculateBollingerBand(List<Double> prices, int period, double k, boolean upper) {
    if (prices == null || prices.size() < period) {
        return null;
    }

    // Work on the first `period` values (most recent) — same order as calculateSma
    double sum = 0.0;
    for (int i = 0; i < period; i++) {
        sum += prices.get(i);
    }
    double mean = sum / period;

    double variance = 0.0;
    for (int i = 0; i < period; i++) {
        double diff = prices.get(i) - mean;
        variance += diff * diff;
    }
    variance /= period;

    double stdDev = Math.sqrt(variance);
    double band = upper ? mean + k * stdDev : mean - k * stdDev;
    return BigDecimal.valueOf(band).setScale(SMA_SCALE, RoundingMode.HALF_UP);
}
```

### `StockHistoricalService.java` — método `calculateAtr()`

```java
BigDecimal calculateAtr(List<Candle> candles, int period) {
    if (candles == null || candles.size() < period + 1) {
        return null;
    }

    List<Candle> asc = new ArrayList<>(candles);
    asc = asc.reversed();

    List<Double> trSeries = new ArrayList<>();
    for (int i = 1; i < asc.size(); i++) {
        double high = asc.get(i).getHighPrice().doubleValue();
        double low  = asc.get(i).getLowPrice().doubleValue();
        double prevClose = asc.get(i - 1).getClosePrice().doubleValue();
        double tr = Math.max(high - low,
                     Math.max(Math.abs(high - prevClose),
                              Math.abs(low  - prevClose)));
        trSeries.add(tr);
    }

    if (trSeries.size() < period) {
        return null;
    }

    // Seed: simple average of the first `period` TR values
    double atr = 0.0;
    for (int i = 0; i < period; i++) {
        atr += trSeries.get(i);
    }
    atr /= period;

    // Wilder's smoothing
    for (int i = period; i < trSeries.size(); i++) {
        atr = (atr * (period - 1) + trSeries.get(i)) / period;
    }

    return BigDecimal.valueOf(atr).setScale(SMA_SCALE, RoundingMode.HALF_UP);
}
```

### `RuleEvaluator.java` — nuevos cases

```java
case "BB_UPPER" -> getBbValue(param, stock, true);
case "BB_LOWER" -> getBbValue(param, stock, false);
case "ATR"      -> getAtrValue(param, stock);
```

---

## Decisiones Técnicas

1. **Bollinger Bands sobre datos sin invertir**: BB se calcula sobre los primeros `period` valores de la lista original (orden descendente de Polygon), que son los más recientes. Esto es consistente con `calculateSma()`, por lo que no es necesario invertir la lista para este cálculo.

2. **ATR con suavizado de Wilder**: La lista de velas se invierte (de desc a asc) para poder calcular True Range desde la más antigua a la más reciente. El seed es la media simple de los primeros `period` TR, y luego se aplica la fórmula: `ATR = (prevATR * (period-1) + TR) / period`.

3. **ATR recibe `List<Candle>`**: El cálculo requiere datos OHLCV (high, low, prevClose). `HistoricalData` ya transporta `List<Candle>` desde las fases anteriores.

4. **ATR puede ser null si no hay candles**: Si `historicalData.getCandles()` está vacío (por ejemplo, en algunos tests), `getAtr14()` devuelve `null`. Esto es el comportamiento esperado y no afecta otros indicadores.

5. **`getBbValue()` y `getAtrValue()` siguen el mismo patrón que `getSmaValue()`, `getEmaValue()` y `getRsiValue()`**: Switch sobre el período, devuelve null para períodos no soportados.

---

## Cobertura de Tests

### `StockHistoricalServiceTest.java` — `BollingerAtrCalculationTests` (10 tests)

| Test | Escenario |
|------|-----------|
| `calculateBollingerUpper_shouldReturnNullForInsufficientData` | < 20 precios → null |
| `calculateBollingerBands_shouldReturnNonNullForSufficientData` | ≥ 20 precios → no null |
| `calculateBollingerBands_shouldHaveScaleFour` | escala = 4 |
| `calculateBollingerBands_upperShouldBeGreaterThanLower` | BB_UPPER > BB_LOWER cuando stdDev > 0 |
| `calculateBollingerBands_shouldBeSymmetricAroundSma` | stdDev = 0 → BB_UPPER = BB_LOWER = SMA20 |
| `calculateAtr_shouldReturnNullForNullCandles` | candles = null → null |
| `calculateAtr_shouldReturnNullForInsufficientCandles` | < 15 velas → null |
| `calculateAtr_shouldReturnNonNullForSufficientCandles` | ≥ 15 velas → no null |
| `calculateAtr_shouldHaveScaleFour` | escala = 4 |
| `calculateAtr_shouldBePositiveForNonZeroRange` | ATR > 0 para rangos no nulos |

### `RuleEvaluatorTest.java` — `BollingerAtrRuleTests` (8 tests)

| Test | Escenario |
|------|-----------|
| `shouldPassRuleWhenPriceIsBelowBbUpper` | PRICE < BB_UPPER(20) → pass |
| `shouldPassRuleWhenPriceIsAboveBbLower` | PRICE > BB_LOWER(20) → pass |
| `shouldReturnNullForUnsupportedBbPeriod` | BB_UPPER(50) → null → fail con Missing |
| `shouldPassRuleWhenAtrIsBelowThreshold` | ATR(14) < 5.0 → pass |
| `shouldFailRuleWhenAtrIsAboveThreshold` | ATR(14) < 2.0 → fail |
| `shouldFailRuleWhenAtrIsNull` | stock sin ATR → fail con Missing |
| `shouldReturnNullForUnsupportedAtrPeriod` | ATR(30) → null → fail con Missing |

---

## Advertencias de Arquitectura

- **Ninguna**: la implementación sigue estrictamente la Arquitectura Hexagonal. El cálculo reside en el dominio puro (`StockHistoricalService`), sin dependencias de infraestructura.
- Los campos ya estaban en `TechnicalIndicators`, `Stock` y `StockEntity` desde la Fase 1.
- La propagación en `ManageAnalyzeStockService` ya estaba declarada desde la Fase 1.

---

## Criterio de Completitud (según `implementacion_reglas.md`)

- [x] `calculateBollingerUpper()` y `calculateBollingerLower()` implementados
- [x] `calculateAtr()` implementado usando `List<Candle>` con suavizado de Wilder
- [x] Todos los nuevos campos propagados a `Stock` (ya estaban desde Fase 1)
- [x] `RuleEvaluator` soporta `BB_UPPER`, `BB_LOWER`, `ATR`
- [x] `mvn compile` y `mvn test` sin errores (Java 21)

---

## Próximos Pasos

- **Fase 6**: Tests unitarios adicionales de cobertura para todos los nuevos métodos y cases (la mayor parte de la cobertura ya se añadió en esta fase).
