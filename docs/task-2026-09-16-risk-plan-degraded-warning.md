# Aviso con tickers degradados al guardar estrategia

- Fecha: 2026-09-16
- Slug: `risk-plan-degraded-warning`
- Alcance: `ManageStrategyUseCase/Service`, `StrategyController`, `messages.properties` + tests
- Base: `main` tras PR #147 (fail-soft `TARGET_BELOW_ENTRY`)

## Resumen

La PR #147 ya degrada con elegancia el plan de riesgo inviable (ticker apto, campos
de riesgo a `null`, motivo en `summary`). Faltaba el requisito de usuario: al guardar,
el aviso debe nombrar los tickers afectados para saber a cuáles ir a ver en análisis.

Ahora `updateStrategy` devuelve `UpdateStrategyResult(strategy, degradedTickers)` y el
controlador muestra `strategy.updated.partial` (warning) con los símbolos cuando hay
degradados; si no, el success habitual.

## Código generado

### 1. `application/dto/UpdateStrategyResult.java` (nuevo)

- `StrategyDTO strategy` + `List<String> degradedTickers` (Lombok `@Data/@Builder`,
  mismo estilo que el resto de DTOs; solo transporte, sin lógica).

### 2. `domain/port/in/ManageStrategyUseCase.java`

- `updateStrategy` pasa de `StrategyDTO` a `UpdateStrategyResult`.

### 3. `application/usecase/ManageStrategyService.java`

- El bucle de re-evaluación no cambia; tras persistir cada evaluación, si
  `compliant && riskRewardRatio == null` el ticker se añade a `degradedTickers`.
- `log.warn` con los símbolos cuando la lista no está vacía. No conformes
  (reglas en rojo) no generan aviso: es su estado normal, no una degradación.

### 4. `presentation/controller/StrategyController.java`

- `saveStrategy`: con degradados → `UiNotification.warning(strategy.updated.partial[{0}])`
  donde `{0}` es `"TSLA, AAPL"`; sin degradados → success como antes.

### 5. `messages.properties`

- `strategy.updated.partial=Estrategia actualizada. Plan de riesgo no calculable en: {0}.`

## Decisiones técnicas

- Resultado específico en vez de excepción "parcial": el guardado es éxito con matiz,
  no un fallo; el puerto expresa el contrato sin ensuciar el dominio.
- El detalle del motivo por ticker sigue en su `summary` (ya incluye símbolo) y en las
  vistas de análisis (`analysis.warning.risk_plan`, `ticker.detail.risk_plan_unavailable`).
- Ripple contenido: solo `StrategyController` consume el puerto; tests ajustados.

## Cobertura de tests

- `ManageStrategyServiceTest`: firmas actualizadas + nuevo
  `testUpdateStrategyCollectsDegradedTickers` (AAPL sano + TSLA degradado → `["TSLA"]`,
  ambas evaluaciones persistidas, el bucle no aborta).
- `StrategyControllerTest`: stub de `updateStrategy` adaptado + nuevo
  `testSaveStrategyUpdateWarnsDegradedTickers` (flash warning con los símbolos).
- Ejecución: `mvn test` → `Tests run: 1020, Failures: 0, Errors: 0, BUILD SUCCESS`.
- Sin `lenient` nuevo; Mockito estricto en verde.

## Advertencias y próximos pasos

- Re-guardar la estrategia del reporte debe mostrar ahora el `partial` nombrando el ticker
  en vez del error duro (ya imposible desde #147).
- Si el aviso menciona un ticker, revisar en su detalle precio actual vs SMA objetivo:
  con precio bajo la SMA, el plan seguirá degradado por diseño (solo largos).
