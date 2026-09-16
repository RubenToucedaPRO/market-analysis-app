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
- Ejecución: `mvn test` → `Tests run: 1023, Failures: 0, Errors: 0, BUILD SUCCESS`
  (relevantes `ManageStrategyServiceTest,StrategyControllerTest`: 22/22 en verde).
- Sin `lenient` nuevo; Mockito estricto en verde.

## Advertencias de SonarQube o arquitectura

- Textos por `MessageSource` + `messages.properties` (cero hardcode en el controlador).
- Logging SLF4J (`info` al guardar, `warn` solo si hay degradados); sin `System.out`.
- `UpdateStrategyResult` es solo transporte (Lombok `@Data/@Builder`, sin lógica),
  igual que el resto de DTOs de `application/dto`.
- Complejidad trivial y anidamiento < 4; el controlador solo traduce
  resultado → flash (sin lógica de negocio) con `null`-guard en la lista.
- Nota: el puerto `ManageStrategyUseCase` (en `domain/port/in`) importa el DTO de
  aplicación, pero es el patrón preexistente del proyecto (ya usaba `StrategyDTO`);
  no se introduce una violación nueva.

## Próximos pasos sugeridos

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Ve a `/strategies`, abre la estrategia "Precio superior a 20" y pulsa
  Guardar sin cambiar nada → debes ver una caja AMARILLA de aviso debajo del
  menú con el texto "Estrategia actualizada. Plan de riesgo no calculable en:
  AAPL." (AAPL está apto pero sin plan: verificado hoy en BD).
2. Ve a `/strategies`, abre la estrategia "Precio superior a SMA20" y pulsa
  Guardar sin cambiar nada → debes ver una caja VERDE de éxito con el texto
  "Estrategia actualizada correctamente." (CDE tiene plan con R:R y FLEX no es
  apto, así que no hay degradados: resultado visible DISTINTO del paso 1).
3. Abre el detalle del ticker AAPL en Análisis → la sección de riesgo muestra
  el aviso de plan no disponible, coherente con la caja amarilla del paso 1.
  (Si en el paso 1 ves verde en vez de amarilla, el mercado se movió desde la
  última evaluación: dime y lo miramos, no es un fallo de la prueba.)

### B. Para interpretar el aviso (informativo, NO es una tarea)

- Si la caja amarilla nombra un ticker, no es un fallo: ese ticker cumple las
  reglas pero su precio está por debajo del objetivo, y la app (solo abre
  largos, es decir, solo gana si el precio sube) no puede calcular su plan.
  Se quedará degradado hasta que el precio recupere. No hay nada que arreglar.
