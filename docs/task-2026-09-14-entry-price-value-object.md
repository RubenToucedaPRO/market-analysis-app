# task-2026-09-14-entry-price-value-object.md

## Título
Value Object `EntryPrice` — Eliminación de triple validación en RiskRewardCalculator

## Resumen
Se introdujo el value object `EntryPrice` como record con validación en constructor, eliminando la triple validación `entryPrice > 0` en `RiskRewardCalculator`. La validación ahora ocurre una sola vez al crear `EntryPrice.of(stock.getCurrentPrice())` en `EvaluateStrategyService`.

## Problema
`RiskRewardCalculator` validaba `entryPrice > 0` en cuatro métodos (`calculateTargetPrice`, `calculateStopLossPrice`, `calculateRiskRewardRatio`, `calculatePositionSize`). Como solo hay un caller en producción (`EvaluateStrategyService.evaluateStrategy()`), el mismo valor se validaba 4 veces.

## Solución
Value Object `EntryPrice` (record) que garantiza por construcción que el precio es positivo:

```java
public record EntryPrice(BigDecimal value) {
    public EntryPrice {
        Objects.requireNonNull(value, DomainErrorCodes.ENTRY_PRICE_NULL);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Entry price must be greater than zero");
        }
    }
    public static EntryPrice of(BigDecimal value) { return new EntryPrice(value); }
}
```

## Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `domain/model/EntryPrice.java` | **Nuevo** — value object con validación en constructor |
| `domain/service/RiskRewardCalculator.java` | 4 firmas `BigDecimal` → `EntryPrice`, eliminada validación `entryPrice` individual |
| `application/usecase/EvaluateStrategyService.java` | `EntryPrice.of(stock.getCurrentPrice())` una sola vez |
| `unit/domain/service/RiskRewardCalculatorTest.java` | Todos los `BigDecimal entryPrice` envueltos en `EntryPrice.of(...)` |

## Cobertura de tests
- **Tests ejecutados:** 1016
- **Fallos:** 0
- **Errores:** 0
