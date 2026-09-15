# task-2026-09-14-remove-dead-duplicated-code-baja.md

## Título
Eliminación de código muerto — Hallazgos de baja prioridad

## Resumen
Tercera fase de la auditoría de código. Se eliminaron métodos sin consumidores, constantes sin uso, una rama inalcanzable, y un campo inyectado innecesario. Todos los cambios mantienen las funcionalidades actuales verificadas con 1016 tests (0 fallos).

## Cambios realizados

### 1. LOW-1: RuleCapability — métodos sin consumidores en producción

**Archivo:** `src/main/java/com/market/analysis/domain/model/RuleCapability.java`

Se eliminaron `isOperatorAllowed(String)` y `getAllowedOperators()`. Ambos métodos solo eran invocados desde tests. La validación de operadores en `Rule.validate()` se realiza a través de `RuleCapabilityCatalog.isOperatorSupported()` directamente.

**Tests eliminados:** La clase `OperatorConstraintTests` completa de `RuleCapabilityP1Test.java` (4 tests: 2 parameterized + 2 regular).

### 2. LOW-2: Use cases prohibidos — métodos sin consumidores

**Archivos:**
- `ManageProhibitedTickerUseCase.java` — eliminado `isTickerProhibited()`
- `ManageProhibitedKeywordUseCase.java` — eliminado `isKeywordProhibited()`
- `ManageProhibitedTickerService.java` — eliminada implementación
- `ManageProhibitedKeywordService.java` — eliminada implementación

Ambos métodos existían en la interfaz pero ningún controller ni servicio los consumía. El filtro de prohibidos en `AnalyzeAndPersistStockService` usa los repositories directamente.

**Tests eliminados:** 2 tests en `ManageProhibitedTickerServiceTest` + 4 tests en `ManageProhibitedKeywordServiceTest`.

### 3. LOW-3: ApiConstants.HEADER_REFERER — constante sin uso

**Archivo:** `src/main/java/com/market/analysis/infrastructure/config/ApiConstants.java`

Se eliminó `HEADER_REFERER = "Referer"`. La constante real usada es `OPENROUTER_HEADER_REFERER = "HTTP-Referer"`.

### 4. LOW-4: WebConstants — constantes reemplazadas

**Archivo:** `src/main/java/com/market/analysis/presentation/util/WebConstants.java`

Se eliminaron `ATTR_PROHIBITED_TICKERS` y `ATTR_PROHIBITED_KEYWORDS`. Fueron reemplazadas por `ATTR_TICKER_PAGE` y `ATTR_KEYWORD_PAGE` que son las que realmente usa el `ProhibitedTickerController`.

### 5. LOW-5: BeanConfig.finnhubToken — campo inyectado sin usar

**Archivo:** `src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java`

Se eliminó el campo `@Value("${finnhub.api.token:}") private String finnhubToken`. El token de Finnhub es gestionado por otro mecanismo (FinnhubAdapter obtiene el token via su propia anotación `@Value`).

### 6. LOW-6: HealthCheckService — case "DEGRADED" inalcanzable

**Archivo:** `src/main/java/com/market/analysis/application/usecase/HealthCheckService.java`

Se eliminó `case "DEGRADED"` del switch en `generateDescription()`. El método `determineOverallStatus()` solo retorna `"UP"` o `"DOWN"` (nunca `"DEGRADED"`). Se corrigió la Javadoc de `determineOverallStatus()` para reflejar que solo retorna `"UP"` o `"DOWN"`.

## Archivos modificados
| Archivo | Cambio |
|---------|--------|
| `RuleCapability.java` | Eliminados `isOperatorAllowed()` y `getAllowedOperators()` |
| `RuleCapabilityP1Test.java` | Eliminada clase `OperatorConstraintTests` |
| `ManageProhibitedTickerUseCase.java` | Eliminado `isTickerProhibited()` |
| `ManageProhibitedKeywordUseCase.java` | Eliminado `isKeywordProhibited()` |
| `ManageProhibitedTickerService.java` | Eliminada implementación |
| `ManageProhibitedKeywordService.java` | Eliminada implementación |
| `ManageProhibitedTickerServiceTest.java` | Eliminados 2 tests |
| `ManageProhibitedKeywordServiceTest.java` | Eliminados 4 tests |
| `ApiConstants.java` | Eliminada constante `HEADER_REFERER` |
| `WebConstants.java` | Eliminadas constantes `ATTR_PROHIBITED_*` |
| `BeanConfig.java` | Eliminado campo `finnhubToken` |
| `HealthCheckService.java` | Eliminado case DEGRADED + Javadoc corregido |

## Cobertura de tests
- **Tests ejecutados:** 1016 (vs 1037 antes de esta fase)
- **Diferencia:** -21 tests (parameterized tests contabilizados individualmente)
- **Fallos:** 0
- **Errores:** 0

## Hallazgos pendientes (no incluidos en esta fase)
- **MED-7:** Patrón de paginación duplicado entre dos services ( refactor menor )
- **MED-8:** Triple validación `entryPrice > 0` (aceptable como Design by Contract)
- **MED-9:** Stubs innecesarios en ManageStrategyServiceTest (mejora de claridad)
