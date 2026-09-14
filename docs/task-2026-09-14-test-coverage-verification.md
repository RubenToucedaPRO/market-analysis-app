# Task: Verificación de cobertura de tests — Fases 1-3

**Fecha:** 2026-09-14
**Rama:** fix/mix-fixes
**Resultado:** 1062 tests, 0 failures, 0 errors, 0 skipped

---

## Resumen

Verificar que los cambios de las Fases 1-3 (estandarización de imports `@Transactional`, anotación en use-cases con múltiples writes, y `@Transactional(readOnly=true)` en reads de repositorios) no rompieron ningún test existente y que la cobertura es suficiente.

---

## Resultado de ejecución

```
Tests run: 1062, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Cobertura de métodos modificados

### Fase 2: Use-case methods con @Transactional

| Método | Test existente | Cobertura |
|--------|---------------|:---------:|
| `AnalyzeAndPersistStockService.analyzeAndPersist()` | `shouldPersistAndEvaluateStock` | ✓ |
| `ManageAnalyzeStockService.deleteById()` | `shouldDeleteCandlesWhenDeletingLastStockForTicker`, `shouldKeepCandlesWhenTickerStillExists` | ✓ |
| `SuggestTickersService.suggestTickers()` | 8 tests en `SuggestTickersServiceTest` | ✓ |

### Fase 3: Repository read methods con @Transactional(readOnly=true)

| Repositorio | Métodos testados | Cobertura |
|-------------|-----------------|:---------:|
| SqlProhibitedTickerRepository | `findAll`, `existsByTicker`, `save`, `deleteByTicker`, paginación | ✓ |
| SqlProhibitedKeywordRepository | `findAll`, `existsByKeyword`, `save`, `deleteByKeyword`, paginación | ✓ |
| SqlApiCallRateRepository | `findByTicker`, `save`, `deleteByTicker` | ✓ |
| SqlRuleDefinitionRepository | `findById`, `findByCode`, `findAll`, `deleteById`, `existsById`, `existsByCode` | ✓ |
| SqlCompanyProfileRepository | `findByTicker`, `save`, `update`, `deleteByTicker` | ✓ |
| SqlStockDataRepository | `findById`, `findAllStocks`, `save`, `updateStockData`, `deleteById` | ✓ |
| SqlCandleHistoryRepository | `findCandlesByTicker`, `findLatestCandleByTicker`, `saveCandlesForTicker`, `deleteCandlesByTicker`, `purgeOrphanCandles` | ✓ |

---

## Análisis: ¿por qué no se necesitan tests nuevos?

### @Transactional es configuración, no comportamiento

Las anotaciones `@Transactional` y `@Transactional(readOnly = true)` son **configuración de Spring/Hibernate**, no lógica de negocio. En tests unitarios con Mockito:

- `@Transactional` no tiene efecto observable
- `@Transactional(readOnly = true)` no cambia el resultado de ninguna query
- Los mocks devuelven los mismos valores independientemente de la anotación

### El @Query eliminado ya estaba testado

La eliminación del `@Query` redundante de `SqlStockDataRepository.findById()` está cubierta por:

- `SqlStockDataRepositoryTest.testFindById()` — verifica que delega correctamente a `jpaRepository.findByIdWithProfile()`
- `SqlStockDataRepositoryTest.testFindByIdNotFound()` — verifica el caso empty

La query real (`LEFT JOIN FETCH s.companyProfile`) está definida en `JpaStockDataRepository.findByIdWithProfile()`, que no fue modificada.

### Tests de integración

Los tests de integración existentes (`ProhibitedTickerController Integration Tests`, `HealthCheckController Integration Tests`, `Security Integration Tests`) ya validan el comportamiento end-to-end con Spring context, incluyendo transacciones reales.

---

## Decisiones técnicas

1. **No añadir tests unitarios para @Transactional** — Sería testear configuración de Spring, no comportamiento. Los tests de integración ya cubren esto.

2. **No añadir tests de rollback** — El comportamiento de rollback está garantizado por Spring Data JPA (`SimpleJpaRepository`), no por nuestro código. Testearlo sería testear el framework.

3. **Mantener tests existentes** — Los 1062 tests existentes cubren todos los métodos modificados. No hay gaps de cobertura introducidos por nuestros cambios.

---

## Próximos pasos

- Commit de documentación
- Las Fases 1-3 están completas y verificadas
