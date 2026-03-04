# Risk Management Integration Refactor

## Resumen de la Tarea

Refactorización coordinada en tres archivos para integrar la gestión de riesgos (`RiskRewardCalculator`) en el flujo de análisis de estrategias, garantizando inmutabilidad del registro histórico y coherencia del dominio.

---

## Cambios Realizados

### 1. `Strategy.java` — Añadido campo `objective`

- Añadido campo `private final StrategyObjective objective`.
- Actualizado el `@Builder` y el constructor para incluirlo.
- Actualizado `validateConsistency()` para validar que `objective` no sea nulo y sea válido (delegando en `objective.validate()`).

```java
if (objective == null) {
    throw new IllegalStateException("Strategy objective cannot be null");
}
objective.validate();
```

### 2. `StrategyEvaluation.java` — Campos de plan de trade y refactor de inmutabilidad

- Eliminado `@Setter`; todos los campos se asignan vía `@Builder(toBuilder = true)`.
- Añadidos campos para persistir el plan de trade calculado:
  - `BigDecimal targetPrice`
  - `BigDecimal stopLossPrice`
  - `BigDecimal riskRewardRatio`
  - `Integer recommendedShares`

El uso de `toBuilder = true` permite copiar la evaluación añadiendo el `id` de persistencia sin romper la inmutabilidad.

### 3. `EvaluateStrategyService.java` — Inyección de `RiskRewardCalculator`

- Inyectado `RiskRewardCalculator` vía constructor.
- Cuando la estrategia es **compliant** (todas las reglas pasan), se calculan:
  - `targetPrice` — precio objetivo
  - `stopLossPrice` — precio stop-loss
  - `riskRewardRatio` — ratio riesgo/recompensa
  - `recommendedShares` — número de acciones recomendadas (entero, redondeado hacia abajo)
- Si se lanza `MissingIndicatorException`, la evaluación permanece **compliant** pero los campos de riesgo quedan en `null` y se añade nota al `summary`.
- Si la estrategia **no es compliant**, los campos de riesgo quedan en `null`.

### 4. `BeanConfig.java` — Nuevo bean `RiskRewardCalculator`

- Declarado bean `riskRewardCalculator()`.
- Actualizado `evaluateStrategyService(...)` para recibir el nuevo bean.

### 5. `ManageStrategyService.java` — Adaptación por eliminación de `@Setter`

- Reemplazado `evaluation.setId(...)` por `evaluation.toBuilder().id(...).build()` para mantener compatibilidad sin romper la inmutabilidad del dominio.

---

## Decisiones Técnicas

| Decisión | Justificación |
|---|---|
| `@Builder(toBuilder = true)` | Permite crear copias con campo `id` asignado tras persistencia, sin romper inmutabilidad |
| `lenient()` en mocks de tests | Stubs de `RiskRewardCalculator` son unused en tests no-compliant; lenient evita `UnnecessaryStubbingException` |
| Campos de riesgo en `StrategyEvaluation` (no en entidad) | Cambio mínimo: solo dominio; se puede persistir en una migración posterior |
| Validación de `objective` al final de `validateConsistency()` | Garantiza backward compatibility con errores previos (nombre, descripción, reglas) |

---

## Cobertura de Tests

### `StrategyTest.java`
- ✅ `testValidateConsistencyPassesWithValidStrategy` — actualizado con `StrategyObjective` válido
- ✅ `testValidateConsistencyThrowsExceptionWhenObjectiveIsNull` — nuevo test
- ✅ `testValidateConsistencyThrowsExceptionWhenObjectiveIsInvalid` — nuevo test

### `EvaluateStrategyServiceTest.java`
- ✅ `@Mock RiskRewardCalculator` añadido con stubs lenient en `setUp()`
- ✅ Objetivo añadido a `testStrategy` y `singleRuleStrategy`
- ✅ `shouldPopulateRiskFieldsWhenCompliant` — nuevo test
- ✅ `shouldLeaveRiskFieldsNullWhenNotCompliant` — nuevo test
- ✅ `shouldLeaveRiskFieldsNullOnMissingIndicator` — nuevo test

### `ManageStrategyServiceTest.java`
- ✅ Objetivo añadido a `testStrategy` para superar validación

---

## Advertencias de Arquitectura

- Los campos `targetPrice`, `stopLossPrice`, `riskRewardRatio`, `recommendedShares` se calculan en memoria pero **no se persisten** en base de datos (la entidad `StrategyEvaluationEntity` no se ha modificado). Para persistirlos se requerirá una migración de esquema separada.

---

## Próximos Pasos Sugeridos

1. Añadir columnas de riesgo a `StrategyEvaluationEntity` y actualizar `StrategyEvaluationMapper`.
2. Añadir migration Liquibase/Flyway para las nuevas columnas.
3. Mostrar el plan de trade en la vista de detalle de ticker.
