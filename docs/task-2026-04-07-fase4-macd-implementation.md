# Fase 4 — Implementación MACD (Moving Average Convergence Divergence)

**Fecha:** 2026-04-07  
**Rama:** `copilot/sub-pr-75`  
**PR asociado:** Sub-PR de #75

---

## Resumen

Implementación de la Fase 4 del plan de indicadores técnicos: cálculo de MACD Line, MACD Signal y MACD Histogram, propagación al modelo `Stock` y soporte en `RuleEvaluator` con códigos `MACD_LINE`, `MACD_SIGNAL` y `MACD_HIST`.

> **Dependencia satisfecha:** Fase 2 (EMA) ya completada. El MACD se calcula internamente sobre la serie completa de `Double` para evitar errores de redondeo acumulado.

---

## Archivos Modificados

| Archivo | Tipo de cambio |
|---------|---------------|
| `src/main/java/com/market/analysis/domain/service/StockHistoricalService.java` | Nuevo método `calculateMacd()` + helper `calculateEmaAsDoubles()` + llamadas en `calculateIndicators()` |
| `src/main/java/com/market/analysis/domain/service/RuleEvaluator.java` | Nuevos cases `"MACD_LINE"`, `"MACD_SIGNAL"`, `"MACD_HIST"` en `getIndicatorValue()` |
| `src/test/java/com/market/analysis/unit/domain/service/StockHistoricalServiceTest.java` | Actualización test Phase 1 (MACD ya no es null) + nuevo `@Nested` Phase 4 MACD (6 tests) |
| `src/test/java/com/market/analysis/unit/domain/service/RuleEvaluatorTest.java` | Nuevo `@Nested` MACD Rule Tests (7 tests) |

> **Nota:** `ManageAnalyzeStockService.java`, `TechnicalIndicators.java`, `Stock.java` y `StockEntity.java` ya incluían los campos y la propagación MACD desde la Fase 1. No fue necesario modificarlos.

---

## Código Generado

### `StockHistoricalService.java` — método `calculateMacd()`

```java
BigDecimal[] calculateMacd(List<Double> prices) {
    final int fast = 12;
    final int slow = 26;
    final int signal = 9;

    if (prices == null || prices.size() < slow + signal) {
        return null;
    }

    // Polygon returns data desc; reverse to process oldest → newest
    List<Double> asc = new ArrayList<>(prices);
    asc = asc.reversed();

    List<Double> emaFast = calculateEmaAsDoubles(asc, fast);
    List<Double> emaSlow = calculateEmaAsDoubles(asc, slow);

    // MACD series starts at index (slow - 1) where emaSlow first has a value
    int macdStart = slow - 1;
    List<Double> macdSeries = new ArrayList<>();
    for (int i = macdStart; i < emaFast.size(); i++) {
        macdSeries.add(emaFast.get(i) - emaSlow.get(i - macdStart));
    }

    List<Double> signalSeries = calculateEmaAsDoubles(macdSeries, signal);

    double lastMacdLine = macdSeries.get(macdSeries.size() - 1);
    double lastSignal = signalSeries.get(signalSeries.size() - 1);
    double histogram = lastMacdLine - lastSignal;

    return new BigDecimal[] {
            BigDecimal.valueOf(lastMacdLine).setScale(SMA_SCALE, RoundingMode.HALF_UP),
            BigDecimal.valueOf(lastSignal).setScale(SMA_SCALE, RoundingMode.HALF_UP),
            BigDecimal.valueOf(histogram).setScale(SMA_SCALE, RoundingMode.HALF_UP)
    };
}
```

### Helper `calculateEmaAsDoubles()`

```java
private List<Double> calculateEmaAsDoubles(List<Double> ascPrices, int period) {
    double seed = ascPrices.stream()
            .limit(period)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

    double multiplier = 2.0 / (period + 1);
    List<Double> emaSeries = new ArrayList<>();
    emaSeries.add(seed);

    for (int i = period; i < ascPrices.size(); i++) {
        double prev = emaSeries.get(emaSeries.size() - 1);
        emaSeries.add((ascPrices.get(i) - prev) * multiplier + prev);
    }

    return emaSeries;
}
```

### `RuleEvaluator.java` — soporte MACD

```java
// En getIndicatorValue() — nuevos cases sin parámetro:
case "MACD_LINE"   -> stock.getMacdLine();
case "MACD_SIGNAL" -> stock.getMacdSignal();
case "MACD_HIST"   -> stock.getMacdHistogram();
```

> Los códigos MACD no usan parámetro (`param` se ignora). El `default -> code` en `formatIndicatorName()` gestiona correctamente el formateo de estos códigos.

---

## Decisiones Técnicas

1. **Serie interna en `double` (no `BigDecimal`)**: El cálculo MACD encadena tres series EMA. Usar `BigDecimal` para las series intermedias introduciría complejidad innecesaria y no mejora la precisión del resultado final (que sí se redondea a 4 decimales al convertir).
2. **`calculateEmaAsDoubles()` helper privado**: Reutilizable para la serie EMA(fast), EMA(slow) y EMA(signal) del MACD sin duplicar lógica.
3. **Mínimo de datos = `slow + signal = 35`**: Necesario para calcular la serie MACD (al menos 1 punto) y luego la serie Signal (al menos `signal` puntos sobre ella).
4. **`macdStart = slow - 1`**: El índice en `emaFast` donde empieza la serie MACD corresponde al punto donde `emaSlow` tiene su primer valor (índice 0 = SMA seed).
5. **Tests a través de `calculateIndicators`**: Los métodos `calculateMacd()` y `calculateEmaAsDoubles()` son package-private. Los tests cubren el comportamiento a través de la API pública `calculateIndicators()`, igual que los tests de EMA (Fase 2) y RSI (Fase 3).

---

## Cobertura de Tests

### `StockHistoricalServiceTest` — Phase 4 (6 tests nuevos)

| Test | Comportamiento verificado |
|------|--------------------------|
| `calculateIndicators_shouldReturnNullMacdForInsufficientData` | Null cuando < 35 precios |
| `calculateIndicators_shouldReturnNonNullMacdForSufficientData` | No-null con ≥ 100 precios |
| `calculateIndicators_shouldReturnMacdValuesWithScaleFour` | Escala 4 en los tres valores |
| `calculateIndicators_histogramShouldEqualLineMinusSignal` | Histograma = línea − señal |
| `calculateIndicators_shouldComputeNonZeroMacdForTrendingSeries` | MACD_LINE > 0 en serie alcista |
| `calculateIndicators_shouldReturnNullMacdWhenInsufficientData` | Null con solo 10 precios |

### `RuleEvaluatorTest` — MACD Rule Tests (7 tests nuevos)

| Test | Comportamiento verificado |
|------|--------------------------|
| `shouldPassRuleWhenMacdLineAboveZero` | MACD_LINE > CONSTANT(0) pasa |
| `shouldPassRuleWhenMacdLineAboveMacdSignal` | MACD_LINE > MACD_SIGNAL pasa (cruce alcista) |
| `shouldPassRuleWhenMacdHistIsPositive` | MACD_HIST > CONSTANT(0) pasa |
| `shouldFailRuleWhenMacdLineIsNull` | Falla con justificación "Missing" cuando sin datos |
| `shouldFailRuleWhenMacdLineIsBelowZero` | MACD_LINE > 0 falla cuando línea negativa |
| `justificationShouldIncludeMacdLineCode` | Justificación incluye "MACD_LINE" |
| Test Phase 1 actualizado | `getBbAtrFieldsAreNull` ahora verifica MACD no-null |

### Resultado final

```
Tests run: 682, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

---

## Advertencias

- **Sin cambios en BD**: Los campos `macd_line`, `macd_signal`, `macd_hist` ya existían en `StockEntity` y la migración SQL se generó en la Fase 1.
- **SonarQube**: `calculateMacd()` tiene complejidad cognitiva aceptable (< 15). El helper `calculateEmaAsDoubles()` mantiene el método principal conciso.

---

## Próximos Pasos

- **Fase 5**: Implementar Bollinger Bands (`BB_UPPER`, `BB_LOWER`) y ATR (`ATR14`) en `StockHistoricalService` y `RuleEvaluator`.
