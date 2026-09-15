# Template Refactoring — Phases 1-7

## Resumen

Refactorización completa de las 12 plantillas Thymeleaf para eliminar duplicación, corregir bugs, mejorar mantenibilidad y añadir internacionalización (i18n).

## Commits

| Commit | Fase | Descripción |
|--------|------|-------------|
| `c99d35b` | Fase 1 | Corrección `lang="en"` → `lang="es"` (10 plantillas) + bug div anidado en `rule-row.html:49-60` |
| `e9aaf15` | Fase 2-3 | Creación de fragments: `head.html`, `footer.html`, `scripts.html`, `empty-state.html`, `action-buttons.html` |
| `42c05d1` | Fase 4 | Unificación `navbar.html` + `navbar-public.html` → único fragmento `navbar(isAuthenticated)` |
| `9351610` | Fase 5 | Internacionalización: ~120+ claves i18n en `messages.properties`, todas las plantillas refactorizadas |
| `02b3bdd` | Fase 7 | Fix `TemplateProcessingException` en `navbar.html` — `@{...}` dentro de ternario SpEL |

## Decisiones Técnicas

### Fase 1
- `lang="en"` → `lang="es"` en 10 plantillas (proyecto en español).
- Corrección de div anidado incorrecto en `fragments/rule-row.html:49-60`.

### Fase 2-3 — Extracción de fragments
- `head.html`: meta charset, viewport, Bootstrap CSS, Bootstrap Icons, Custom CSS.
- `footer.html`: footer unificado para todas las páginas.
- `scripts.html`: jQuery, Bootstrap JS bundle.
- `empty-state.html`: componente reutilizable para estados vacíos.
- `action-buttons.html`: patrón reutilizable de botones editar/eliminar.

### Fase 4 — Unificación de navbar
- `navbar-public.html` eliminado. Un solo fragmento `navbar(isAuthenticated)` controla visibilidad de enlaces según autenticación.
- Todos los enlaces de navegación protegidos con `sec:authorize="isAuthenticated()"`.

### Fase 5 — Internacionalización
- **120+ claves** en `messages.properties` cubriendo:
  - Páginas de error, navbar, home, login
  - Strategies (list/create/detail)
  - Analysis (list/detail/chart)
  - Rule definitions (list/create)
  - Prohibited tickers
- Confirmaciones `onclick="return confirm(...)"` reemplazadas por `th:onclick` con claves i18n: `th:onclick="'return confirm(\'' + #{key} + '\')'"`.
- `prohibited-tickers/list.html`: tokens CSRF explícitos se mantienen intactos (patrón defensa en profundidad).

### Fase 7 — Verificación CSRF
- **Auditoría**: 25 formularios analizados en 11 plantillas.
- **Resultado**: Spring Security 6.x habilita CSRF por defecto. Thymeleaf inyecta `_csrf` automáticamente para todos los formularios con `th:action`.
- `prohibited-tickers/list.html` usa tokens CSRF explícitos como defensa en profundidad.
- Los 20 formularios restantes dependen de la inyección implícita de Thymeleaf (mecanismo estándar).

### Fix navbar (bonus)
- `th:href="${isAuthenticated ? @{/analysis} : @{/}}"` causaba `SpringHrefTagProcessor` exception.
- Solución: `th:href="${isAuthenticated} ? '/analysis' : '/'"` (sin `@{...}` en ternario).

## Archivos Modificados

| Archivo | Cambios |
|---------|---------|
| `messages.properties` | +120 claves i18n |
| `fragments/navbar.html` | Unificado + i18n + fix href |
| `fragments/head.html` | Nuevo (extraído) |
| `fragments/footer.html` | Nuevo (extraído) |
| `fragments/scripts.html` | Nuevo (extraído) |
| `fragments/empty-state.html` | Nuevo (extraído) |
| `fragments/action-buttons.html` | Nuevo (extraído) |
| `fragments/rule-row.html` | Fix div anidado |
| `error.html` | i18n + fragmentos |
| `home.html` | i18n + fragmentos |
| `login.html` | i18n + fragmentos |
| `strategies/list.html` | i18n + fragmentos |
| `strategies/create.html` | i18n + fragmentos |
| `strategies/detail.html` | i18n + fragmentos |
| `analysis/analysis.html` | i18n + fragmentos |
| `analysis/ticker-detail.html` | i18n + fragmentos |
| `analysis/ticker-chart.html` | i18n + fragmentos |
| `rule-definitions/list.html` | i18n + fragmentos |
| `rule-definitions/create.html` | i18n + fragmentos |
| `prohibited-tickers/list.html` | i18n (CSRF explícito mantenido) |

## Tests

- **1062 tests ejecutados, 0 errores, 0 fallos**.
- `ProhibitedTickersTemplateSecurityTest`: verifica tokens CSRF explícitos en `prohibited-tickers/list.html`.
- `SecurityConfigTest`: verifica endpoints públicos y protegidos.

## Próximos Pasos Sugeridos

1. Evaluar migración de `confirm()` i18n a un componente modal reutilizable.
2. Considerar extracting `fragments/action-buttons.html` en formularios que aún no lo usan.
3. Añadir tests de template para las plantillas restantes (análogos a `ProhibitedTickersTemplateSecurityTest`).
