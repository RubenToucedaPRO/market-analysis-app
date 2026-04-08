# Fase 3 — Implementación RSI (Relative Strength Index)

**Fecha:** 2026-04-07  
**Rama:** `copilot/sub-pr-75`  
**PR asociado:** Sub-PR de #75

---

## Resumen

Implementación de la Fase 3 del plan de indicadores técnicos: cálculo del RSI (Relative Strength Index) para períodos 14 y 30, propagación al modelo `Stock` y soporte en `RuleEvaluator` con código `RSI`.

---

## Archivos Modificados

| Archivo | Tipo de cambio |
|---------|---------------|
| `src/main/java/com/market/analysis/domain/service/StockHistoricalService.java` | Nuevo método `calculateRsi()` + llamadas en `calculateIndicators()` |
| `src/main/java/com/market/analysis/domain/service/RuleEvaluator.java` | Nuevo case `"RSI"`, método `getRsiValue()`, `formatIndicatorName()` |
| `src/test/java/com/market/analysis/unit/domain/service/StockHistoricalServiceTest.java` | Actualización test Phase 1 (RSI ya no es null) + nuevo `@Nested` Phase 3 RSI |
| `src/test/java/com/market/analysis/unit/domain/service/RuleEvaluatorTest.java` | Nuevo `@Nested` RSI Rule Tests |

> **Nota:** `ManageAnalyzeStockService.java` ya incluía la propagación `stock.setRsi14()` / `stock.setRsi30()` desde la Fase 1. No fue necesario modificarlo.

---

## Código Generado

### `StockHistoricalService.java` — método `calculateRsi()`

```java
BigDecimal calculateRsi(List<Double> prices, int period) {
    if (prices == null || prices.size() < period + 1) {
        return null;
    }

    // Polygon returns data desc; reverse to process oldest → newest
    List<Double> asc = new ArrayList<>(prices);
    Collections.reverse(asc);

    double totalGain = 0.0;
    double totalLoss = 0.0;

    for (int i = 1; i <= period; i++) {
        double delta = asc.get(i) - asc.get(i - 1);
        if (delta > 0) {
            totalGain += delta;
        } else {
            totalLoss += Math.abs(delta);
        }
    }

    double avgGain = totalGain / period;
    double avgLoss = totalLoss / period;

    double rsi;
    if (avgLoss == 0.0) {
        rsi = 100.0;
    } else if (avgGain == 0.0) {
        rsi = 0.0;
    } else {
        double rs = avgGain / avgLoss;
        rsi = 100.0 - (100.0 / (1.0 + rs));
    }

    return BigDecimal.valueOf(rsi).setScale(SMA_SCALE, RoundingMode.HALF_UP);
}
```

Llamadas en `calculateIndicators()`:

```java
BigDecimal rsi14 = calculateRsi(data.getClosingPrices(), 14);
BigDecimal rsi30 = calculateRsi(data.getClosingPrices(), 30);

return TechnicalIndicators.builder()
        // ... campos previos ...
        .rsi14(rsi14)
        .rsi30(rsi30)
        .build();
```

### `RuleEvaluator.java` — soporte RSI

```java
// En getIndicatorValue():
case "RSI" -> getRsiValue(param, stock);

// Nuevo método privado:
private BigDecimal getRsiValue(Double param, Stock stock) {
    if (param == null) return null;
    int period = param.intValue();
    return switch (period) {
        case 14 -> stock.getRsi14();
        case 30 -> stock.getRsi30();
        default -> null;
    };
}

// En formatIndicatorName():
case "RSI" -> String.format("RSI%d", param.intValue());
```

---

## Decisiones Técnicas

- **Algoritmo RSI simple** (promedio de ganancias/pérdidas de N deltas): conforme al plan en `implementacion_reglas.md`. No se implementó el RSI de Wilder suavizado, siguiendo el plan.
- **Inversión de lista**: los datos de Polygon vienen en orden descendente (`sort=desc`). El método invierte la copia internamente para procesar de más antiguo a más reciente.
- **Períodos soportados**: 14 y 30. El `RuleEvaluator` devuelve `null` para cualquier otro período.
- **Escala**: 4 decimales con `RoundingMode.HALF_UP`, coherente con el resto de indicadores.
- **Casos límite**:
  - `avgLoss == 0` → RSI = 100 (no hay pérdidas: mercado en subida libre).
  - `avgGain == 0` → RSI = 0 (no hay ganancias: mercado en caída libre).

---

## Cobertura de Tests y Pruebas Añadidas

### `StockHistoricalServiceTest` — Phase 3 RSI Calculation Tests (10 tests)

| Método | Descripción |
|--------|-------------|
| `shouldReturnNullRsiWhenInsufficientData` | Menos de `period+1` precios → RSI null |
| `shouldReturnNullRsiWhenPricesNull` | Lista null → RSI null |
| `shouldReturnRsi100WhenAllPricesRising` | Todos los deltas positivos → RSI = 100 |
| `shouldReturnRsi0WhenAllPricesFalling` | Todos los deltas negativos → RSI = 0 |
| `shouldCalculateRsi50WhenGainsEqualLosses` | Ganancias = pérdidas → RSI = 50 |
| `shouldCalculateRsi14WithKnownValues` | Serie determinista con resultado conocido = 50 |
| `shouldCalculateRsi30WithSufficientData` | RSI30 con ≥ 31 precios |
| `shouldPopulateBothRsiFieldsInCalculateIndicators` | RSI14 y RSI30 presentes con datos suficientes |
| `shouldReturnRsiWithScale4` | Escala = 4 decimales |
| `shouldReturnRsiInValidRange` | Valor en [0, 100] |

**Test Phase 1 actualizado:** `testRsiMacdBbAtrFieldsAreNull()` → renombrado `testMacdBbAtrFieldsAreNull()` y eliminadas las aserciones RSI (ya implementado en Fase 3).

### `RuleEvaluatorTest` — RSI Rule Tests (8 tests)

| Método | Descripción |
|--------|-------------|
| `shouldPassRuleWhenRsi14BelowOversoldThreshold` | RSI14 < 30 (sobreventa) pasa |
| `shouldPassRuleWhenRsi14AboveOverboughtThreshold` | RSI14 > 70 (sobrecompra) pasa |
| `shouldPassRuleWhenRsi30AboveFifty` | RSI30 > 50 (momentum positivo) pasa |
| `shouldFailRuleWhenRsi14IsNull` | RSI14 null → falla con "Missing" |
| `shouldFailRuleForUnsupportedRsiPeriod` | Período no soportado → falla |
| `shouldFormatRsiNameCorrectlyInJustification` | Justificación contiene "RSI14" |
| `shouldHandleBothSupportedRsiPeriods` | Períodos 14 y 30 funcionan |

**Total nuevos tests: 18 | Total suite: 59 tests — 0 fallos**

---

## Criterio de Completitud de la Fase 3

- [x] `calculateRsi()` implementado con manejo de casos límite (`avgLoss=0`, `avgGain=0`)
- [x] RSI(14) y RSI(30) calculados en `calculateIndicators()`
- [x] Propagación correcta a `Stock` (ya existente desde Fase 1)
- [x] `RuleEvaluator` evalúa `RSI` con parámetros 14 y 30
- [x] `mvn test` sin errores (59 tests, 0 fallos)

---

## Advertencias de Arquitectura

- El RSI utilizado es el cálculo simple (SMA de ganancias/pérdidas). No implementa el suavizado exponencial de Wilder. Si en el futuro se necesita el RSI de Wilder, se puede extender el método `calculateRsi()` sin romper la interfaz pública.
- Los períodos soportados (14 y 30) están hardcodeados en `getRsiValue()`. Nuevos períodos requieren modificar ese switch y añadir campos en el modelo de dominio.

---

## Próximos Pasos

- **Fase 4**: MACD (Line, Signal, Histogram) — depende de EMA(9, 12, 26) ya calculados en Fase 2.
- **Fase 5**: Bollinger Bands (BB_UPPER/BB_LOWER período 20) + ATR (período 14).
- **Fase 6**: Tests unitarios adicionales y revisión de cobertura global ≥ 80%.
