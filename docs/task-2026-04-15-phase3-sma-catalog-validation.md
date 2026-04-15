# Fase 3: Validación de Periodos SMA mediante RuleCapabilityCatalog

**Fecha:** 2026-04-15  
**Referencia:** `docs/riskreward.md` — Fase 3

---

## Resumen

Se implementa la Fase 3 del plan de mejora de `RiskRewardCalculator`, que elimina la duplicación de lógica SMA entre `RiskRewardCalculator` y `RuleCapabilityCatalog`, y añade validación temprana de periodos SMA en `StrategyObjective.validate()`.

---

## Cambios realizados

### 1. `StrategyObjective.validate()` — Validación de periodos SMA via catálogo

**Archivo:** `src/main/java/com/market/analysis/domain/model/StrategyObjective.java`

- Se añade método privado `validateSmaperiod(ObjectiveType, BigDecimal, String)` que consulta `RuleCapabilityCatalog.getCapability("SMA").getAllowedParams()` para validar que el periodo sea uno de los soportados.
- Se invoca al final de `validate()` para `targetType`/`targetValue` y `stopLossType`/`stopLossValue`.
- Periodos inválidos (ej. 15, 99, 100, 300) se rechazan con `IllegalArgumentException` indicando los valores permitidos.
- No afecta a tipos no-SMA (`PERCENTAGE`, `FIXED_PRICE`).

### 2. `RiskRewardCalculator.resolveSmaValue()` — Delegación al catálogo

**Archivo:** `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java`

- Se elimina el `switch` hardcodeado con periodos `20/50/200`.
- Se delega la resolución a `RuleCapabilityCatalog.getCapability("SMA").resolve(period, stock)`.
- Se distingue entre periodo no soportado (`IllegalArgumentException`) y dato faltante (`MissingIndicatorException`).
- El mensaje de error para periodos no soportados ahora incluye los periodos válidos del catálogo.

### 3. Tests de `StrategyObjectiveTest`

**Archivo:** `src/test/java/com/market/analysis/unit/domain/model/StrategyObjectiveTest.java`

Tests añadidos (9 tests nuevos):
- Validación exitosa de periodos SMA 20, 50 y 200 como target y stop-loss.
- Rechazo de periodo SMA inválido como target (100).
- Rechazo de periodo SMA inválido como stop-loss (15).
- Rechazo cuando ambos periodos SMA son inválidos (99, 300) — falla en targetValue primero.
- Verificación de que tipos no-SMA no se ven afectados por la validación.
- Estrategia mixta SMA + PERCENTAGE se valida correctamente.

### 4. Tests de `RiskRewardCalculatorTest`

**Archivo:** `src/test/java/com/market/analysis/unit/domain/service/RiskRewardCalculatorTest.java`

- Test existente actualizado: `shouldThrowExceptionForUnsupportedSmaPeriod` — se relaja la comprobación del mensaje para adaptarse al formato dinámico del catálogo.
- Tests añadidos (5 tests nuevos) en `SmaCatalogDelegationTests`:
  - Resolución de SMA20, SMA50 y SMA200 via catálogo.
  - Excepción `IllegalArgumentException` para periodo no soportado (75).
  - Excepción `MissingIndicatorException` cuando el dato SMA es null en stock.

---

## Decisiones técnicas

1. **Sin constantes duplicadas:** `StrategyObjective` y `RiskRewardCalculator` delegan al catálogo. Añadir una nueva SMA solo requiere actualizar `RuleCapabilityCatalog` (y el modelo `Stock`).
2. **Validación temprana:** Los periodos SMA inválidos se rechazan en `validate()`, antes de llegar al cálculo, evitando errores 500 en tiempo de evaluación.
3. **Compatibilidad:** Los tests existentes siguen pasando sin modificaciones no justificadas.

---

## Cobertura de tests

- **Total tests ejecutados:** 986 (todos pasan)
- **Tests añadidos:** 14 (9 en StrategyObjectiveTest + 5 en RiskRewardCalculatorTest)
- **Tests modificados:** 1 (mensaje de error actualizado)
- **Cobertura:** Todos los escenarios de validación y delegación cubiertos.

---

## Próximos pasos sugeridos

- **Fase 4:** Selector dinámico de SMA en el formulario frontend (`risk-management-fields.html` + JavaScript).
- **Fase 5:** Validaciones avanzadas de coherencia (warnings para configuraciones improbables).
