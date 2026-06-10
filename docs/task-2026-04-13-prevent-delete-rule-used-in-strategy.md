# Prevención de borrado de regla usada en estrategia

## Resumen

Se corrige el guard de eliminación de `RuleDefinition` para que **no permita borrar una definición de regla si su código está siendo utilizado como `subjectCode` o `targetCode` en alguna regla de cualquier estrategia**. Además se mejora la experiencia de usuario devolviendo un mensaje de error en la misma página de lista en lugar de redirigir a la página de error genérica.

---

## Problema detectado

La implementación anterior en `SqlRuleDefinitionRepository.deleteById` comparaba **el ID auto-generado de `RuleEntity`** (tabla `rules`) con **el ID de `RuleDefinitionEntity`** (tabla `rule_definitions`). Estas dos tablas no tienen relación por clave foránea; la vinculación es a través del campo `code`:

| Tabla | Campo |
|---|---|
| `rule_definitions` | `code` (e.g. `"SMA"`) |
| `rules` | `subject_code`, `target_code` (referencian el mismo código) |

Por tanto la comprobación nunca podía detectar un uso real.

---

## Cambios realizados

### 1. `SqlRuleDefinitionRepository.deleteById` (Infrastructure)

- Se obtiene el `code` de la `RuleDefinitionEntity` a partir del `id`.
- Se comprueba si alguna regla de cualquier estrategia usa ese `code` como `subjectCode` o `targetCode`.
- Si se detecta uso, se lanza `IllegalArgumentException` con un mensaje claro que incluye el código afectado.

```java
String code = jpaRepository.findById(id)
        .map(RuleDefinitionEntity::getCode)
        .orElse(null);
if (code != null) {
    boolean usedInStrategy = strategyRepository.findAll().stream()
            .flatMap(strategy -> strategy.getRules().stream())
            .anyMatch(rule -> code.equals(rule.getSubjectCode()) || code.equals(rule.getTargetCode()));
    if (usedInStrategy) {
        throw new IllegalArgumentException(
                "No se puede eliminar la definición de regla '" + code
                        + "' porque está siendo usada en una o más estrategias.");
    }
}
```

### 2. `RuleDefinitionController.deleteRuleDefinition` (Presentation)

- Se añade `RedirectAttributes` al método.
- La `IllegalArgumentException` se captura y su mensaje se añade como flash attribute `errorMessage`.
- El usuario es redirigido de vuelta a `/rule-definitions` con el mensaje de error visible.

### 3. `list.html` (Templates)

- Se añade un bloque de alerta Bootstrap que muestra el flash attribute `errorMessage` cuando está presente.

---

## Decisiones técnicas

- **Capa de la comprobación**: se mantiene en la capa de infraestructura (`SqlRuleDefinitionRepository`) para ser coherente con el patrón ya establecido en `SqlStrategyRepository.deleteById` (que también valida dependencias antes de eliminar).
- **UX**: en lugar de lanzar el error hacia la página de error genérica, se usa `RedirectAttributes` para mostrar el mensaje al usuario en la misma pantalla, siguiendo el principio de mínima fricción.
- **Mensaje**: se incluye el `code` afectado para que el mensaje sea lo más informativo posible.

---

## Cobertura de tests

### `SqlRuleDefinitionRepositoryTest`

| Test | Descripción |
|---|---|
| `testDeleteById` | Borrado correcto cuando no hay estrategias usando la regla |
| `testDeleteByIdUsedAsSubject` | Lanza excepción si la regla se usa como `subjectCode` |
| `testDeleteByIdUsedAsTarget` | Lanza excepción si la regla se usa como `targetCode` |

### `RuleDefinitionControllerTest`

| Test | Descripción |
|---|---|
| `testDeleteRuleDefinition` | Borrado correcto, sin flash attribute de error |
| `testDeleteRuleDefinitionUsedInStrategy` | `IllegalArgumentException` capturada y añadida como flash attribute |

---

## Próximos pasos sugeridos

- Considerar mover la comprobación a la capa Application (`ManageRuleDefinitionService`) inyectando `StrategyRepository` para mayor coherencia con Clean Architecture.
- Añadir el mismo patrón de flash error al borrado de estrategias si aún no está implementado.
