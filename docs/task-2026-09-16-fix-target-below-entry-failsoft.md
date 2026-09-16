# Fix fail-soft al añadir ticker con target/stop inconsistente

## Resumen de la tarea
Al añadir un ticker con una estrategia que cumplía todas las reglas, la operación fallaba con `Target price must be greater than entry price for long positions` y el ticker no se guardaba.

Causa raíz: `EvaluateStrategyService.evaluateStrategy()` solo capturaba `MissingIndicatorException | IllegalArgumentException` en el bloque de plan de riesgo, pero `RiskRewardCalculator.calculateRiskRewardRatio()` y `validateStopBelowEntry()` lanzan `DomainValidationException` (`validation.target_below_entry`, `validation.stop_above_entry`), que extiende `RuntimeException` directamente. La excepción propagaba a `AnalyzeAndPersistStockService.analyzeAndPersist()` (transaccional) y abortaba el alta. La validación positiva de `EntryPrice.of()` era un señuelo parcial: lanza `IllegalArgumentException` y sí estaba capturada.

Comportamiento elegido por el usuario: fail-soft (guardar ticker, `compliant=true`, campos de riesgo a `null`, aviso en `summary`) + normalización de `RiskRewardCalculator` a `DomainValidationException`.

## Código generado

### 1. `domain/service/EvaluateStrategyService.java`
- `catch (MissingIndicatorException | DomainValidationException | IllegalArgumentException e)` (antes sin `DomainValidationException`).
- `DomainValidationException` ya estaba importado; sin más cambios. `IllegalArgumentException` se conserva por `EntryPrice.of()` y compatibilidad.

### 2. `domain/service/RiskRewardCalculator.java`
- Eliminados `FIELD_TARGET_PRICE`, `FIELD_STOP_PRICE`, `FIELD_CAPITAL_TO_RISK` (strings literales) e import `java.util.Locale`.
- `validatePositivePrice(BigDecimal, String errorCode)` ahora lanza `DomainValidationException(errorCode)` en vez de `IllegalArgumentException`.
- Call sites:
  - ratio: `TARGET_PRICE_ZERO`, `STOP_PRICE_ZERO`.
  - posición: `STOP_PRICE_ZERO`, `CAPITAL_ZERO`.
  - `validateAndReturnFixedPrice(fixedPrice, context)`: mapea `"Target"` → `TARGET_PRICE_ZERO`, resto → `STOP_PRICE_ZERO`.
- `validateStopLossPrice()` ahora lanza `DomainValidationException(STOP_ABOVE_ENTRY)` en vez de `IllegalArgumentException` con `String.format`; unifica con `validateStopBelowEntry()`.

### 3. `domain/exception/DomainErrorCodes.java`
- Nuevos: `TARGET_PRICE_ZERO="validation.target_price_zero"`, `STOP_PRICE_ZERO="validation.stop_price_zero"`, `CAPITAL_ZERO="validation.capital_zero"`.

### 4. `messages.properties`
- `validation.target_price_zero=Target price must be greater than zero`
- `validation.stop_price_zero=Stop price must be greater than zero`
- `validation.capital_zero=Capital to risk must be greater than zero`

## Decisiones técnicas
- Fail-soft en evaluación, fallo duro solo en creación/edición de estrategia: el precio de entrada es dato externo en el momento del alta; un `FIXED_PRICE`/`SMA` por debajo del entry no debe bloquear el ticker.
- Dominio sin `MessageSource`: en `summary` se anexa `e.getMessage()` (para `DomainValidationException` es el `errorCode`); la resolución i18n sigue en `GlobalExceptionHandler` para errores no capturados.
- `EntryPrice` se deja intacto (`IllegalArgumentException`/`NullPointerException` con `ENTRY_PRICE_NULL`): sigue cubierto por el `catch` ampliado, blast radius mínimo.
- Se mantiene `IllegalArgumentException` en el `catch` por compatibilidad y por `EntryPrice`.

## Cobertura de tests y pruebas
- `RiskRewardCalculatorTest`: 4 tests actualizados al nuevo contrato:
  - stop-loss == entry y stop-loss > entry → `DomainValidationException(STOP_ABOVE_ENTRY)` (antes `IllegalArgumentException`).
  - capital cero/negativo → `DomainValidationException(CAPITAL_ZERO)` (antes `IllegalArgumentException`).
- `EvaluateStrategyServiceTest`:
  - stop-above-entry, target-below-entry y SMA-unsupported ahora mockean `DomainValidationException` con `DomainErrorCodes` y asertan el código en `summary` (antes mockeaban `IllegalArgumentException` con texto libre, lo que ocultaba el bug).
  - 2 regresiones nuevas con calculadora real (reproducen el reporte): `FIXED_PRICE` target 140 < entry 150 y `FIXED_PRICE` stop 155 > entry 150 → `compliant=true`, riesgos `null`, `summary` contiene `Risk plan could not be calculated`.
- Verificación: `mvn -Dtest=RiskRewardCalculatorTest,EvaluateStrategyServiceTest test` → `Tests run: 63, Failures: 0, Errors: 0, BUILD SUCCESS`. Suites relacionadas `AnalyzeAndPersistStockServiceTest,ManageStrategyServiceP0Test,GlobalExceptionHandlerTest` → 29 tests OK.
- Sin `lenient()` nuevo; se reutiliza el existente documentado en el setup.

## Advertencias de SonarQube / arquitectura
- Sin strings hardcodeados nuevos: validaciones usan `DomainErrorCodes` + `messages.properties`.
- Complejidad y anidamiento sin cambios relevantes (mapeo ternario simple, sin nesting extra); constructor ≤7 params; sin lógica en Thymeleaf.
- Hexagonal respetada: dominio puro, sin dependencias de infraestructura; DTO solo como transporte (sin cambios en Application salvo el `catch` ya previsto como fail-soft).

## Próximos pasos sugeridos
- Validar visualmente el alta de ticker con objetivo fijo inconsistente y comprobar el aviso en `summary` (usar `th:text`, no `th:utext`).
- Valorar validación temprana en creación/edición de estrategia para `FIXED_PRICE` (advertencia, no bloqueo).
- Ejecutar `mvn verify` completo + SonarQube antes del merge.
