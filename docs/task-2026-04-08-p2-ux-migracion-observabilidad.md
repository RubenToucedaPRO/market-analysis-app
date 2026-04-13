# P2: UX guiada por capacidades, migración de datos y observabilidad

**Fecha:** 2026-04-08  
**Rama:** `copilot/sub-pr-82`  
**PR relacionado:** #82

---

## 1. Resumen de la tarea

Implementación de la fase P2 del plan de mejora de la definición de reglas. Las tres áreas de trabajo son:

- **Tarea 9** — UI guiada por capacidades: el formulario solo permite construir reglas válidas.
- **Tarea 10** — Migración de datos en arranque: limpieza automática de `rule_definitions` inconsistentes.
- **Tarea 11** — Observabilidad: logging estructurado en todos los rechazos de validación.
- **Tarea 12** — Checklist de release: tests de cobertura nuevos y documentación actualizada.

---

## 2. Cambios implementados

### Capa de dominio

| Fichero | Cambio |
|---------|--------|
| `RuleCapability.java` | Nuevo getter `isAnyParamAllowed()` para exponer el flag interno. |

### Capa de aplicación (Application)

| Fichero | Cambio |
|---------|--------|
| `RuleCapabilityDTO.java` | **Nuevo DTO** que representa una entrada del catálogo canónico: `code`, `requiresParam`, `anyParamAllowed`, `allowedParams`. |
| `RuleDefinitionDTO.java` | Añadidos `allowedParams: Set<Double>` y `anyParamAllowed: boolean`, rellenados desde el catálogo en tiempo de lectura. |
| `ManageRuleDefinitionUseCase.java` | Nuevo método `getCatalogCapabilities()` en la interfaz. |
| `ManageRuleDefinitionService.java` | Implementa `getCatalogCapabilities()`; enriquece `getAllRuleDefinitions()` y `getRuleDefinitionById()` con datos del catálogo; añade logging estructurado (`log.warn`) en `validateAgainstCatalog()`. |

### Capa de presentación (Presentation)

| Fichero | Cambio |
|---------|--------|
| `RuleDefinitionController.java` | Añadido `model.addAttribute("capabilities", ...)` en `showCreateForm()`. |
| `rule-definitions/create.html` | Código cambiado de `<input text>` a `<select>` (solo en modo creación) con opciones del catálogo; `requiresParam` se auto-establece vía JS al seleccionar el código. |
| `fragments/rule-row.html` | Los selectores de sujeto/objetivo llevan `data-requires-param`, `data-any-param` y `data-allowed-params`; el campo de parámetro muestra un `<select>` con valores fijos cuando `allowedParams` es finito (ej. SMA: 20, 50, 200), o un `<input type="number">` libre para CONSTANT/VALUE. |
| `strategy-manager.js` | `toggleParameter` extendido para gestionar select/input según `allowedParams`; `reindexRules` actualizado para los nuevos IDs. |

### Infraestructura

| Fichero | Cambio |
|---------|--------|
| `RuleDefinitionSanitizationRunner.java` | **Nuevo** `CommandLineRunner` que en cada arranque detecta y elimina definiciones con códigos no soportados y corrige flags `requires_param` inconsistentes. Idempotente. |

---

## 3. Decisiones técnicas

- **Enriquecimiento en servicio, no en mapper**: los datos de capacidad proceden del catálogo de dominio, no de la capa de persistencia; es responsabilidad del caso de uso combinarlos.
- **Doble control (select / input) en rule-row**: Thymeleaf renderiza el control correcto en el lado servidor para filas existentes; JS repite la lógica para filas añadidas dinámicamente.
- **Sanitización en arranque**: sin Flyway/Liquibase, un `CommandLineRunner` es la solución natural para mantener coherencia del dataset sin requerir scripts SQL manuales.
- **`requiresParam` deshabilitado en formulario de creación**: se evita que el usuario lo cambie a mano; el JS lo establece automáticamente al seleccionar el código.

---

## 4. Cobertura de tests

| Fichero de test | Tests añadidos | Qué cubre |
|-----------------|---------------|-----------|
| `ManageRuleDefinitionServiceP2Test.java` | 9 | `getCatalogCapabilities()`, enriquecimiento de DTOs, casos límite |
| `RuleDefinitionSanitizationRunnerTest.java` | 6 | Eliminación de códigos inválidos, corrección de flags, dataset mixto |

**Total de tests tras P2**: 946 (+ 15 respecto a P1).

---

## 5. Advertencias de arquitectura

- Los tests de controlador existentes (`RuleDefinitionControllerTest`, `StrategyControllerTest`) siguen pasando sin modificación porque el nuevo `addAttribute("capabilities", ...)` no interfiere con las verificaciones previas de tipo `RuleDefinitionDTO`.
- El `CommandLineRunner` usa el repositorio de dominio directamente (sin capa de caso de uso) para evitar dependencias circulares de Spring Boot. Esto está justificado al tratarse de mantenimiento de base de datos, no de lógica de negocio.

---

## 6. Próximos pasos sugeridos (P2 restante)

- **Tarea 10 (completar)**: revisar estrategias existentes que contengan `rules` con códigos ya eliminados de `rule_definitions`.
- **Tarea 11 (ampliar)**: añadir métricas Micrometer/Actuator para contar rechazos por código inválido.
- **Tarea 12 (checklist)**: ejecutar análisis SonarQube, revisar cobertura de ramas en `validateAgainstCatalog`.
