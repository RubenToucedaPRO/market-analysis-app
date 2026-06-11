# Task: Mejora de cobertura de tests - Prohibited Services y Repositories

**Fecha:** 2026-06-12
**Estado:** Completado

---

## Resumen

Mejora de cobertura de tests unitarios para 4 clases del dominio de tickers/keywords prohibidos, elevando todas del rango 62%-77% al **100% de cobertura de instrucciones**.

---

## Archivos modificados

### Test files

| Archivo | Tests antes | Tests despues |
|---------|-------------|---------------|
| `ManageProhibitedKeywordServiceTest.java` | 6 | 13 |
| `SqlProhibitedKeywordRepositoryTest.java` | 5 | 11 |
| `SqlProhibitedTickerRepositoryTest.java` | 6 | 9 |
| `ManageProhibitedTickerServiceTest.java` | 4 | 7 |

### Resultados JaCoCo (INSTRUCTION coverage)

| Clase | Antes | Despues |
|-------|-------|---------|
| `ManageProhibitedKeywordService` | 77.78% | **100.0%** (153/153) |
| `SqlProhibitedKeywordRepository` | 74.05% | **100.0%** (185/185) |
| `SqlProhibitedTickerRepository` | 64.23% | **100.0%** (123/123) |
| `ManageProhibitedTickerService` | 62.2% | **100.0%** (73/73) |

---

## Tests anadidos

### ManageProhibitedKeywordServiceTest (7 tests anadidos)

1. **`shouldReturnPaginatedProhibitedKeywords`** - Cubre `getProhibitedKeywords()`: pagina de resultados con contenido.
2. **`shouldReturnEmptyPageWhenNoKeywordsExist`** - Cubre `getProhibitedKeywords()` con pagina vacia.
3. **`shouldRejectNullDtoWhenAddingProhibitedKeyword`** - Cubre la validacion `KEYWORD_NULL` en `addProhibitedKeyword(null)`.
4. **`shouldRejectNullKeywordWhenCheckingIfProhibited`** - Cubre `normalizeKeyword(null)` via `isKeywordProhibited`.
5. **`shouldReturnFalseWhenKeywordIsNotProhibited`** - Cubre rama `false` de `existsByKeyword`.
6. **`shouldRejectNullKeywordWhenRemoving`** - Cubre `normalizeKeyword(null)` via `removeProhibitedKeyword`.
7. **`shouldAddKeywordPreservingOriginalCreatedAtAndUpdatedAt`** - Cubre las ramas de `createdAt`/`updatedAt` en el builder de `addProhibitedKeyword`.

### SqlProhibitedKeywordRepositoryTest (6 tests anadidos)

1. **`shouldReturnPaginatedProhibitedKeywords`** - Cubre `findAll(pageNumber, pageSize)` con contenido.
2. **`shouldReturnEmptyPaginatedResult`** - Cubre `findAll(pageNumber, pageSize)` vacio.
3. **`shouldSaveKeywordWhenCreatedAtIsNull`** - Cubre `resolveCreatedAt(null)` y `resolveUpdatedAt(null)` -> `Instant.now()`.
4. **`shouldSaveKeywordUsingCreatedAtWhenUpdatedAtIsNull`** - Cubre `resolveUpdatedAt` cuando `updatedAt` es null pero `createdAt` existe.
5. **`shouldRejectBlankKeywordOnExistsByKeyword`** - Cubre `normalizeKeyword` via `existsByKeyword`.
6. **`shouldRejectBlankKeywordOnDeleteByKeyword`** - Cubre `normalizeKeyword` via `deleteByKeyword`.

### SqlProhibitedTickerRepositoryTest (3 tests anadidos)

1. **`shouldReturnPaginatedProhibitedTickers`** - Cubre `findAll(pageNumber, pageSize)` con contenido.
2. **`shouldReturnEmptyPaginatedResult`** - Cubre `findAll(pageNumber, pageSize)` vacio.
3. **`shouldSkipSaveWhenTickerAlreadyExists`** - Cubre la rama `else` del `save()` cuando el ticker ya existe.

### ManageProhibitedTickerServiceTest (3 tests anadidos)

1. **`shouldReturnPaginatedProhibitedTickers`** - Cubre `getProhibitedTickers()` con contenido.
2. **`shouldReturnEmptyPageWhenNoTickersExist`** - Cubre `getProhibitedTickers()` con pagina vacia.
3. **`shouldReturnMultiplePagesOfProhibitedTickers`** - Cubre `getProhibitedTickers()` con metadata de pagina intermedia.

---

## Decisiones tecnicas

- Tests 100% unitarios con Mockito, sin integraucion con base de datos.
- Siguen convenciones existentes: `@DisplayName`, `@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks`.
- Cobertura verificada con JaCoCo XML report.
- 1062 tests del suite completo pasan sin fallos.

---

## Proximos pasos

- Verificar que SonarQube refleje la mejora de cobertura en el dashboard.
- Considerar tests de integracion para los repositorios SQL si se requiere mayor confianza en la capa de persistencia.
