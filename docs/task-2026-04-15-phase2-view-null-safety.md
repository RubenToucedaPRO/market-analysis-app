# Fase 2: Null-Safety en Vistas para Campos de Riesgo

**Fecha:** 2026-04-15  
**Referencia:** `docs/riskreward.md` — Fase 2

---

## Resumen

Se corrigen errores potenciales de NPE (NullPointerException) en las vistas Thymeleaf `analysis.html` y `ticker-detail.html` cuando un ticker pasa la evaluación (`evaluationPassed = true`) pero los campos de riesgo (`riskRewardRatio`, `targetPrice`, `stopLossPrice`, `recommendedShares`) son `null`.

Esto ocurre cuando `EvaluateStrategyService` captura una excepción (como `IllegalArgumentException` o `MissingIndicatorException`) durante el cálculo del plan de riesgo — por ejemplo, cuando el SMA usado como stop-loss está por encima del precio actual.

---

## Archivos Afectados

- `src/main/resources/templates/analysis/analysis.html`
- `src/main/resources/templates/analysis/ticker-detail.html`

---

## Cambios Realizados

### `analysis.html` (líneas 243-310)

**Antes:** El bloque de métricas de riesgo (R:R, target, stop-loss, acciones sugeridas) se mostraba condicionado solo a `ticker.evaluationPassed`. Si `evaluationPassed = true` pero `riskRewardRatio = null`, Thymeleaf lanzaba NPE al intentar formatear o comparar valores nulos.

**Después:**
1. Condición del bloque de métricas cambiada a `ticker.evaluationPassed and ticker.riskRewardRatio != null`.
2. Nuevo bloque de aviso con `alert-warning` cuando `ticker.evaluationPassed and ticker.riskRewardRatio == null`, informando que la estrategia cumple reglas pero el plan de riesgo no se pudo calcular.
3. El bloque existente para `!ticker.evaluationPassed` permanece sin cambios.

### `ticker-detail.html` (líneas 138-222)

**Antes:** El bloque "Plan de Ejecución Sugerido" (precio actual, target, stop-loss, títulos, ratio R:R) se mostraba condicionado solo a `ticker.evaluationPassed`. Si `evaluationPassed = true` pero los campos de riesgo son `null`, Thymeleaf lanzaba NPE.

**Después:**
1. Condición del bloque de ejecución cambiada a `ticker.evaluationPassed and ticker.riskRewardRatio != null`.
2. Nuevo bloque "Plan de Riesgo No Disponible" con estilo `text-warning` cuando `ticker.evaluationPassed and ticker.riskRewardRatio == null`, con explicación detallada.
3. El bloque existente "Motivo del Rechazo" para `!ticker.evaluationPassed` permanece sin cambios.

---

## Decisiones Técnicas

- Se usa `ticker.riskRewardRatio != null` como condición representativa porque si el ratio es null, todos los campos de riesgo son null (el reset se hace atómicamente en `EvaluateStrategyService`).
- Los mensajes de aviso usan iconografía Bootstrap Icons (`bi-exclamation-triangle`) y clases `alert-warning` / `text-warning` consistentes con el diseño existente.
- No se usa `th:utext` — todo el texto se renderiza con `th:text` o contenido estático, cumpliendo la regla de SonarQube de seguridad.

---

## Cobertura de Tests

- Las vistas Thymeleaf se verifican manualmente o con tests de integración MockMvc.
- El escenario backend (evaluación passed con risk fields null) ya está cubierto por los tests añadidos en la Fase 1 (`EvaluateStrategyServiceTest`).
- Los cambios en la vista no requieren tests unitarios adicionales dado que son cambios de presentación condicional.

---

## Próximos Pasos

- **Fase 3:** Validación de periodos SMA mediante `RuleCapabilityCatalog` (refactorizar `StrategyObjective.validate()` y `RiskRewardCalculator.resolveSmaValue()` para delegar al catálogo).
- **Fase 4:** Selector dinámico de SMA en formulario reutilizando `ruleDefinitions`.
