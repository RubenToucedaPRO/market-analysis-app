# Fase 2 — Implementación de EMA (Exponential Moving Average)

## Resumen

Implementación de la **Fase 2** del plan de nuevos indicadores técnicos definido en `implementacion_reglas.md`. Esta fase añade el cálculo de EMA para 6 períodos (9, 12, 20, 26, 50, 200), propaga los valores calculados a `Stock` y hace que `RuleEvaluator` evalúe reglas con el código `EMA`.

**Fecha:** 2026-04-07  
**Rama:** `copilot/sub-pr-75`  
**Basado en:** `implementacion_reglas.md` §Fase 2

---

## Objetivo

1. Implementar `calculateEma()` en `StockHistoricalService`.
2. Calcular los 6 períodos EMA dentro de `calculateIndicators()` y empaquetar los resultados en el builder de `TechnicalIndicators`.
3. La propagación a `Stock` (vía `ManageAnalyzeStockService`) ya estaba implementada desde la Fase 1.
4. Añadir el case `"EMA"` a `RuleEvaluator.getIndicatorValue()` con el método auxiliar `getEmaValue()`.
5. Añadir el case `"EMA"` a `RuleEvaluator.formatIndicatorName()` para justificaciones legibles (`EMA9`, `EMA26`, etc.).

---

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `StockHistoricalService.java` | Nuevo método `calculateEma()` + llamadas en `calculateIndicators()` |
| `RuleEvaluator.java` | Nuevo case `EMA` en `getIndicatorValue()`, nuevo método `getEmaValue()`, case `EMA` en `formatIndicatorName()` |
| `StockHistoricalServiceTest.java` | Actualizado test Phase 1 (RSI/MACD/BB/ATR siguen null); añadida clase `EmaCalculationTests` con 7 tests |
| `RuleEvaluatorTest.java` | Añadida clase `EmaRuleTests` con 8 tests |

---

## Código Generado

### `StockHistoricalService.java` — Método `calculateEma()`

```java
BigDecimal calculateEma(List<Double> prices, int period) {
    if (prices == null || prices.size() < period) {
        return null;
    }

    // Polygon devuelve datos desc; invertir para procesar de más antiguo a más reciente
    List<Double> asc = new ArrayList<>(prices);
    Collections.reverse(asc);

    // Semilla: SMA de los primeros `period` valores (precios más antiguos)
    double seed = asc.stream()
            .limit(period)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

    double multiplier = 2.0 / (period + 1);
    double ema = seed;

    for (int i = period; i < asc.size(); i++) {
        ema = (asc.get(i) - ema) * multiplier + ema;
    }

    return BigDecimal.valueOf(ema).setScale(4, RoundingMode.HALF_UP);
}
```

**Invocación en `calculateIndicators()`:**
```java
BigDecimal ema9   = calculateEma(data.getClosingPrices(), 9);
BigDecimal ema12  = calculateEma(data.getClosingPrices(), 12);
BigDecimal ema20  = calculateEma(data.getClosingPrices(), 20);
BigDecimal ema26  = calculateEma(data.getClosingPrices(), 26);
BigDecimal ema50  = calculateEma(data.getClosingPrices(), 50);
BigDecimal ema200 = calculateEma(data.getClosingPrices(), 200);
```

### `RuleEvaluator.java` — Case `EMA`

```java
case "EMA" -> getEmaValue(param, stock);
```

```java
private BigDecimal getEmaValue(Double param, Stock stock) {
    if (param == null) return null;
    int period = param.intValue();
    return switch (period) {
        case 9   -> stock.getEma9();
        case 12  -> stock.getEma12();
        case 20  -> stock.getEma20();
        case 26  -> stock.getEma26();
        case 50  -> stock.getEma50();
        case 200 -> stock.getEma200();
        default  -> null;
    };
}
```

```java
case "EMA" -> String.format("EMA%d", param.intValue());
```

---

## Decisiones Técnicas

1. **Visibilidad package-private de `calculateEma()`**: Se mantiene consistente con `calculateSma()` y `calculateAverageVolume()`. Los tests usan `calculateIndicators()` como punto de entrada público, siguiendo el mismo patrón de los tests de SMA existentes.
2. **Inversión de lista con copia**: Se crea una `ArrayList` nueva para no modificar la lista original, evitando efectos secundarios.
3. **Semilla vía `average()`**: Equivalente a la SMA pero sin crear un objeto `BigDecimal` intermedio; más eficiente en la ruta de cálculo.
4. **Propagación ya implementada**: `ManageAnalyzeStockService` ya tenía los `stock.setEma*()` desde la Fase 1, por lo que esta fase no requiere cambios en esa clase.
5. **Períodos soportados**: 9, 12, 20, 26, 50, 200 — los períodos de alta prioridad definidos en `reglas.md`. Períodos no soportados devuelven `null` (regla falla con "missing target data").

---

## Cobertura de Tests

### `StockHistoricalServiceTest` — nuevos tests en `EmaCalculationTests`

| Test | Descripción |
|------|-------------|
| `shouldReturnNullEmaWhenInsufficientData` | Con 8 precios para EMA(9) → null |
| `shouldReturnNonNullEmaWithExactlyPeriodPrices` | Con exactamente 9 precios → no null |
| `shouldReturnConstantPriceEmaWhenAllPricesEqual` | Precios constantes → EMA = precio |
| `shouldPopulateAllEmaFieldsInCalculateIndicators` | 300 precios → 6 campos EMA no null |
| `shouldProducePositiveEmaForPositivePrices` | Precios positivos → EMA > 0 |
| `shouldReturnEmaWithScale4` | Escala = 4 decimal places |
| `shouldShowEmaFasterResponseForShorterPeriod` | EMA9 > EMA200 en tendencia alcista reciente |

### `RuleEvaluatorTest` — nuevos tests en `EmaRuleTests`

| Test | Descripción |
|------|-------------|
| `shouldPassRuleWhenPriceAboveEma9` | PRICE > EMA(9) → pasa |
| `shouldFailRuleWhenPriceBelowEma200` | PRICE > EMA(200) con precio bajo → falla |
| `shouldReturnFailedWhenEmaValueIsNull` | EMA campo null → falla con "FAILED" |
| `shouldFormatEmaNameCorrectlyInJustification` | Texto "EMA9" en justificación |
| `shouldEvaluateEmaCrossRule` | EMA(9) > EMA(26) cruce alcista |
| `shouldReturnFailedForUnsupportedEmaPeriod` | EMA(100) no soportado → falla |
| `shouldHandleAllSixSupportedEmaPeriods` | Los 6 períodos evalúan correctamente |

---

## Advertencias / SonarQube

- Sin números mágicos: los períodos EMA son parámetros del dominio (no constantes técnicas).
- Complejidad cognitiva de `calculateEma()`: < 5 (bucle simple + reversión).
- `calculateEma()` es package-private por diseño; los tests se hacen a través de la API pública `calculateIndicators()`.

---

## Criterio de Completitud — Fase 2 ✅

- [x] `calculateEma()` implementado y retorna `null` con datos insuficientes
- [x] Los 6 períodos se calculan en `calculateIndicators()`
- [x] Los valores se propagan correctamente a `Stock` via `ManageAnalyzeStockService`
- [x] `RuleEvaluator` evalúa reglas con `EMA` para los 6 períodos soportados
- [x] Tests unitarios añadidos (7 en `StockHistoricalServiceTest`, 8 en `RuleEvaluatorTest`)
- [x] `mvn test` sin errores (42 tests, 0 fallos)

---

## Próximos Pasos

- **Fase 3**: Implementar RSI(14) y RSI(30) en `StockHistoricalService` + case `RSI` en `RuleEvaluator`
- **Fase 4**: Implementar MACD (Line, Signal, Histogram) usando EMA(9), EMA(12), EMA(26) ya calculados
- **Fase 5**: Bollinger Bands (BB_UPPER20, BB_LOWER20) + ATR(14)
- **Fase 6**: Tests de cobertura completa para todas las fases
