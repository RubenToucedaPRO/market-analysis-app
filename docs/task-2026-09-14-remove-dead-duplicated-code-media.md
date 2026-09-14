# task-2026-09-14-remove-dead-duplicated-code-media.md

## Título
Eliminación de código redundante y muerto — Hallazgos de media prioridad

## Resumen
Segunda fase de la auditoría de código. Se eliminaron métodos muertos en repositories, se extrajeron lógicas duplicadas, y se simplificaron controllers y services. Todos los cambios mantienen las funcionalidades actuales verificadas con 1037 tests (0 fallos).

## Cambios realizados

### 1. MED-1: StockDataDTOMapper — null-check repetido 7 veces

**Archivo:** `src/main/java/com/market/analysis/application/mapper/StockDataDTOMapper.java`

Se extrajo una variable local `StrategyEvaluation se` para evitar repetir `stock.getStrategyEvaluation() != null ? stock.getStrategyEvaluation().getXxx() : null` siete veces consecutivas.

### 2. MED-3: updateTicker() vs updateTickerFromDetail() — lógica duplicada

**Archivo:** `src/main/java/com/market/analysis/presentation/controller/AnalyzeTickerController.java`

Se extrajo un método privado `performUpdate(Long id, RedirectAttributes)` que encapsula la lógica común (llamada al use case + mensaje i18n + flash attribute). Ambos endpoints POST ahora delegan a este método, diferenciando solo en la URL de redirección.

### 3. MED-4: HealthCheckController — HTTP status determinado dos veces

**Archivo:** `src/main/java/com/market/analysis/presentation/controller/HealthCheckController.java`

El Controller recalculaba el HTTP status con `HealthStatusCode.UP.getStatus().equals(response.getStatus())` cuando el `HealthCheckResponse` ya contenía `httpStatusCode` calculado por el `HealthCheckMapper`. Se simplificó para usar `HttpStatus.resolve(response.getHttpStatusCode())`. Se eliminó la importación de `HealthStatusCode`.

### 4. MED-5: RiskRewardCalculator — validación stopPrice >= entryPrice repetida 3 veces

**Archivo:** `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java`

Se extrajo un método privado `validateStopBelowEntry(BigDecimal entryPrice, BigDecimal stopPrice)` que lanza `DomainValidationException(DomainErrorCodes.STOP_ABOVE_ENTRY)`. Se usa en `calculateRiskRewardRatio()` y `calculatePositionSize()`. El método existente `validateStopLossPrice()` se mantuvo sin cambios (lanza `IllegalArgumentException` con mensaje formateado, comportamiento distinto).

### 5. MED-6: Métodos muertos en repositories — 7 métodos sin consumidores

Se eliminaron de las interfaces de dominio y sus implementaciones:

| Interfaz | Método eliminado | Razón |
|----------|-----------------|-------|
| `StockDataRepository` | `findAllStocks()` | Nunca llamado; se usa `findAllStocksVisibleInAnalysis()` |
| `StrategyRepository` | `findByName()` | Nunca llamado; carga todos y filtra en memoria |
| `RuleDefinitionRepository` | `findByCode()` | Redundante con `existsByCode()` |
| `ApiCallRateRepository` | `findByTicker()` | Nunca llamado desde producción |
| `ApiCallRateRepository` | `deleteByTicker()` | Nunca llamado desde producción |
| `CompanyProfileRepository` | `update()` | Redundante con `save()` que ya hace upsert |
| `CompanyProfileRepository` | `deleteByTicker()` | Nunca llamado desde producción |

**Tests eliminados:** 12 tests que cubrían estos métodos fueron removidos de los archivos de test correspondientes.

### 6. MED-7: AnalysisResult.validateConsistency() — nunca invocado

**Archivo:** `src/main/java/com/market/analysis/domain/model/AnalysisResult.java`

Se eliminó el método `validateConsistency()` (23 líneas). Nunca era llamado desde código de producción. Los campos `calculatedMetrics` y `ruleResults` se mantienen ya que son parte del modelo de dominio y `ruleResults` es usado internamente por `calculateComplianceRate()`.

**Tests eliminados:** 5 tests que cubrían `validateConsistency()` fueron removidos de `AnalysisResultTest.java`.

### MED-2: StrategyObjective mapping — SKIP

El duplicado de mapeo de `StrategyObjective` entre `StrategyDTOMapper` (Application layer: DTO↔Domain) y `StrategyMapper` (Infrastructure layer: Entity↔Domain) es estructural y apropiado para cada capa arquitectónica. Extraer un utilitario compartido crearía acoplamiento entre capas.

## Archivos modificados
| Archivo | Cambio |
|---------|--------|
| `StockDataDTOMapper.java` | Variable local `se` para evitar null-check repetido |
| `AnalyzeTickerController.java` | Método privado `performUpdate()` extraído |
| `HealthCheckController.java` | Usa `httpStatusCode` del DTO |
| `RiskRewardCalculator.java` | Método `validateStopBelowEntry()` extraído |
| `StockDataRepository.java` | Eliminado `findAllStocks()` |
| `SqlStockDataRepository.java` | Eliminada implementación de `findAllStocks()` |
| `StrategyRepository.java` | Eliminado `findByName()` |
| `SqlStrategyRepository.java` | Eliminada implementación de `findByName()` |
| `RuleDefinitionRepository.java` | Eliminado `findByCode()` |
| `SqlRuleDefinitionRepository.java` | Eliminada implementación de `findByCode()` |
| `ApiCallRateRepository.java` | Eliminados `findByTicker()` y `deleteByTicker()` |
| `SqlApiCallRateRepository.java` | Eliminadas implementaciones |
| `CompanyProfileRepository.java` | Eliminados `update()` y `deleteByTicker()` |
| `SqlCompanyProfileRepository.java` | Eliminadas implementaciones |
| `AnalysisResult.java` | Eliminado `validateConsistency()` |
| `AnalysisResultTest.java` | Eliminados 5 tests de validateConsistency |
| `SqlStockDataRepositoryTest.java` | Eliminados 2 tests de findAllStocks |
| `SqlStrategyRepositoryTest.java` | Eliminados 2 tests de findByName |
| `SqlRuleDefinitionRepositoryTest.java` | Eliminados 2 tests de findByCode |
| `SqlApiCallRateRepositoryTest.java` | Eliminados 4 tests de findByTicker/deleteByTicker |
| `SqlCompanyProfileRepositoryTest.java` | Eliminados 2 tests de update/deleteByTicker |

## Cobertura de tests
- **Tests ejecutados:** 1037 (vs 1054 antes de esta fase)
- **Diferencia:** -17 tests (5 AnalysisResult + 12 repositories)
- **Fallos:** 0
- **Errores:** 0

## Próximos pasos sugeridos
- Hallazgos de baja prioridad pendientes: constantes sin uso, rama DEGRADED inalcanzable, stubs innecesarios en tests
