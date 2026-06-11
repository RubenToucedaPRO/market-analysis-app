# Paginación de Tickers y Keywords Prohibidos

**Fecha:** 2026-06-12  
**Rama:** `feature/prohibited-tickers-pagination`

---

## Resumen

Se implementó paginación en la vista de gestión de Tickers Prohibidos, tanto para la lista de tickers como para la lista de keywords. Ambas listas ahora se muestran de 10 en 10, con controles de navegación entre páginas.

---

## Archivos modificados

### Domain Layer
- **`domain/model/PageResult.java`** (nuevo) — Record genérico de paginación tecnológicamente agnóstico.
- **`domain/port/out/ProhibitedTickerRepository.java`** — Añadido método `findAll(int pageNumber, int pageSize)`.
- **`domain/port/out/ProhibitedKeywordRepository.java`** — Añadido método `findAll(int pageNumber, int pageSize)`.
- **`domain/port/in/ManageProhibitedTickerUseCase.java`** — Añadido método `getProhibitedTickers(int, int)`.
- **`domain/port/in/ManageProhibitedKeywordUseCase.java`** — Añadido método `getProhibitedKeywords(int, int)`.

### Application Layer
- **`application/usecase/ManageProhibitedTickerService.java`** — Implementación de `getProhibitedTickers()` delegando al repository paginado.
- **`application/usecase/ManageProhibitedKeywordService.java`** — Implementación de `getProhibitedKeywords()` delegando al repository paginado.

### Infrastructure Layer
- **`infrastructure/persistence/repository/SqlProhibitedTickerRepository.java`** — Implementación usando `PageRequest` de Spring Data.
- **`infrastructure/persistence/repository/SqlProhibitedKeywordRepository.java`** — Implementación usando `PageRequest` de Spring Data.

### Presentation Layer
- **`presentation/controller/ProhibitedTickerController.java`** — Controlador acepta `@RequestParam tickerPage` y `keywordPage`. Redirecciones conservan estado de paginación.
- **`presentation/util/WebConstants.java`** — Nuevas constantes `ATTR_TICKER_PAGE`, `ATTR_KEYWORD_PAGE`, `DEFAULT_PAGE_SIZE = 10`.

### Vista
- **`templates/prohibited-tickers/list.html`** — Bootstrap 5 pagination components para tickers y keywords. Formularios mantienen parámetros de paginación.

### Tests
- **`ProhibitedTickerControllerTest.java`** — 8 tests actualizados y nuevos: listado paginado, navegación por páginas, eliminación con parámetros de paginación.

---

## Decisiones técnicas

1. **`PageResult<T>` como record domain-level** — Evita dependencia de Spring Data en la capa de dominio. La conversión `Page` → `PageResult` ocurre en la infraestructura.

2. **Mantención de métodos `getAll*()` existentes** — Los métodos de lista completa se conservan para compatibilidad con otros consumidores (seeding, tests de integración).

3. **Parámetros de paginación en formularios** — Los hidden fields en formularios de delete y add keyword preservan la página actual del usuario.

4. **Navegación independiente** — Tickers y keywords se paginan de forma independiente (parámetros separados `tickerPage` y `keywordPage`).

---

## Cobertura de tests

- Tests existentes adaptados: 6
- Tests nuevos: 2 (navegación por página específica)
- **Total: 8 tests — Todos pasan**
- Suite completa: **1046 tests — BUILD SUCCESS**

---

## Próximos pasos sugeridos

- Considerar añadir indicador de "Mostrando X-Y de Z" para mayor claridad.
- Evaluar paginación con AJAX para evitar recarga completa de página.
- Añadir paginación a otras listas del proyecto (strategies, rule-definitions).
