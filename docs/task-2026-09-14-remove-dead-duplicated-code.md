# task-2026-09-14-remove-dead-duplicated-code.md

## Título
Eliminación de código muerto y duplicado de alta prioridad

## Resumen
Auditoría y limpieza de código identificó 5 hallazgos de alta prioridad: código muerto (nunca llamado), interfaces huérfanas, clases sin uso, y lógica de normalización duplicada entre capas. Todos los cambios mantienen las funcionalidades actuales verificadas con 1054 tests (0 fallos).

## Cambios realizados

### 1. HIGH-1 & HIGH-4: `Stock.applyTechnicalIndicators()` — código muerto y duplicado

**Archivo:** `src/main/java/com/market/analysis/domain/model/Stock.java`

Se eliminó el método `applyTechnicalIndicators(TechnicalIndicators)` (24 líneas). Este método nunca era invocado desde código de producción. La misma lógica existe en `AnalyzeAndPersistStockService.applyTechnicalIndicators()` que es el único consumidor real.

**Justificación:** El método en `Stock` era un duplicado no utilizado. `AnalyzeAndPersistStockService` usa su propio método privado para aplicar indicadores técnicos al stock.

### 2. HIGH-2: `EvaluateStrategyUseCase` — interfaz huérfana

**Archivo eliminado:** `src/main/java/com/market/analysis/domain/port/in/EvaluateStrategyUseCase.java`

Interfaz que definía `evaluateStrategy(Strategy, Stock) -> AnalysisResult` pero nunca era implementada por ninguna clase. `EvaluateStrategyService` es una clase concreta que no implementa esta interfaz. No existía ninguna referencia en producción ni en tests.

### 3. HIGH-3: `CompanyProfileDTOMapper` y `CompanyProfileDto` — sin uso en producción

**Archivos eliminados:**
- `src/main/java/com/market/analysis/application/mapper/CompanyProfileDTOMapper.java`
- `src/main/java/com/market/analysis/application/dto/CompanyProfileDto.java`
- `src/test/java/com/market/analysis/unit/application/mapper/CompanyProfileDTOMapperTest.java`

Clase mapper con anotación `@Component` que solo tenía tests unitarios. Ningún archivo de producción la importaba ni la usaba. El DTO `CompanyProfileDto` solo existía para este mapper.

### 4. HIGH-5: `normalizeKeyword()` duplicado entre Service y Repository

**Archivo:** `src/main/java/com/market/analysis/infrastructure/persistence/repository/SqlProhibitedKeywordRepository.java`

Se eliminó `normalizeKeyword()` del Repository junto con los métodos auxiliares `resolveCreatedAt()` y `resolveUpdatedAt()`. La normalización y validación de keywords ahora es responsabilidad exclusiva de `ManageProhibitedKeywordService.normalizeKeyword()`, que valida:
- Null/blank → `DomainValidationException`
- Longitud > 100 → `DomainValidationException`
- Normalización: `trim().toUpperCase(Locale.ROOT)`

El Repository recibe keywords ya normalizadas y actúa como capa de persistencia pura.

**Tests actualizados:** `SqlProhibitedKeywordRepositoryTest.java` — se eliminaron tests que verificaban normalización y rechazo de blanks en el repository (7→4 tests), ya que这些 comportamientos ahora son responsabilidad de la Service.

## Archivos modificados
| Archivo | Cambio |
|---------|--------|
| `Stock.java` | Eliminado método `applyTechnicalIndicators()` |
| `SqlProhibitedKeywordRepository.java` | Eliminados `normalizeKeyword()`, `resolveCreatedAt()`, `resolveUpdatedAt()` |
| `SqlProhibitedKeywordRepositoryTest.java` | Tests adaptados al nuevo contrato del repository |

## Archivos eliminados
| Archivo | Razón |
|---------|-------|
| `EvaluateStrategyUseCase.java` | Interfaz nunca implementada |
| `CompanyProfileDTOMapper.java` | Mapper nunca usado en producción |
| `CompanyProfileDto.java` | DTO sin consumidores |
| `CompanyProfileDTOMapperTest.java` | Test de clase eliminada |

## Cobertura de tests
- **Tests ejecutados:** 1054 (vs 1062 originales)
- **Diferencia:** -8 tests (4 por CompanyProfileDTOMapperTest eliminado, 4 por consolidación de tests de normalización en SqlProhibitedKeywordRepositoryTest)
- **Fallos:** 0
- **Errores:** 0

## Decisiones técnicas
1. Se mantuvo `normalizeKeyword()` en la Service (Application layer) ya que contiene la validación de negocio (longitud máxima, blank check).
2. Se eliminó la normalización del Repository (Infrastructure layer) para aplicar SRP: el repository solo persiste, no valida.
3. No se creó un utilitario compartido porque la normalización es específica del dominio de keywords prohibidas y solo la Service tiene la lógica de validación completa.

## Próximos pasos sugeridos
- Considerar los hallazgos de media prioridad identificados en la auditoría (duplicación de mapeo StrategyObjective, null-check repetido 7 veces en StockDataDTOMapper, etc.)
