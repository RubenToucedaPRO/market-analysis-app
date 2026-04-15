# Fase 1: Manejo Robusto de Excepciones en RiskRewardCalculator

**Fecha:** 2026-04-15  
**Referencia:** `docs/riskreward.md` — Fase 1

---

## Resumen

Implementación de la Fase 1 del plan de mejora de `RiskRewardCalculator`:
captura de `IllegalArgumentException` en `EvaluateStrategyService.evaluateStrategy()`
para evitar errores HTTP 500 cuando los cálculos de riesgo son incongruentes con los
datos actuales del ticker.

---

## Problema

`EvaluateStrategyService.evaluateStrategy()` solo capturaba `MissingIndicatorException`,
pero `RiskRewardCalculator` también lanza `IllegalArgumentException` en escenarios como:

- Stop-loss SMA por encima del precio actual (`stopLoss >= entryPrice`).
- Target SMA por debajo del precio actual (`targetPrice <= entryPrice`).
- Periodo SMA no soportado (ej. SMA99).

Estas excepciones se propagaban sin capturar, causando error 500 en la aplicación.

---

## Cambios Realizados

### `EvaluateStrategyService.java`

- Ampliado el bloque `catch` para capturar `IllegalArgumentException` junto con `MissingIndicatorException`.
- Añadido reset explícito de todos los campos de riesgo (`targetPrice`, `stopLossPrice`, `riskRewardRatio`, `recommendedShares`) a `null` en el bloque `catch`, garantizando un estado consistente (todos nulos o todos con valor, nunca parcial).

### `EvaluateStrategyServiceTest.java`

Añadidos 3 tests unitarios:

1. **`shouldLeaveRiskFieldsNullOnStopLossAboveEntry`**: Verifica que cuando `calculateStopLossPrice` lanza `IllegalArgumentException` (stop-loss > precio actual), la evaluación permanece compliant pero todos los campos de riesgo son `null` y el summary contiene explicación.
2. **`shouldLeaveRiskFieldsNullOnTargetBelowEntry`**: Verifica que cuando `calculateTargetPrice` lanza `IllegalArgumentException` (target < entry), la evaluación permanece compliant con campos de riesgo nulos.
3. **`shouldLeaveRiskFieldsNullOnUnsupportedSmaPeriod`**: Verifica que cuando `calculateTargetPrice` lanza `IllegalArgumentException` (periodo SMA no soportado), la evaluación permanece compliant con campos de riesgo nulos.

---

## Decisiones Técnicas

1. **Reset explícito en catch**: Se resetean todos los campos de riesgo a `null` en el bloque catch para garantizar consistencia. Sin esto, una excepción en `calculateStopLossPrice` dejaría `targetPrice` con un valor parcial asignado antes de la excepción.
2. **Multi-catch**: Se usa `catch (MissingIndicatorException | IllegalArgumentException e)` en vez de `catch (Exception e)` para mantener la especificidad y no ocultar otros errores inesperados.

---

## Cobertura de Tests

- 21 tests totales en `EvaluateStrategyServiceTest` (antes 18).
- 3 nuevos tests cubren los escenarios de `IllegalArgumentException`.
- Todos los tests pasan correctamente.

---

## Próximos Pasos

- **Fase 2**: Null-safety en vistas (`analysis.html`, `ticker-detail.html`).
- **Fase 3**: Validación de periodos SMA vía `RuleCapabilityCatalog`.
- **Fase 4**: Selector dinámico de SMA en formulario.
