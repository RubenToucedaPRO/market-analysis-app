# RequiresParam siempre no editable en formulario RuleDefinition

## Resumen
`requiresParam` deriva del `RuleCapabilityCatalog` y nunca debe editarse a mano. Se deja el checkbox siempre `disabled` en `create.html` (antes solo en creación: `th:disabled="${!isEdit}"`, editable en edición). El backend ya autocorrige desde el catálogo, así que el campo es solo visual en ambos modos.

## Código
- `src/main/resources/templates/rule-definitions/create.html:103-109`: `th:disabled="${!isEdit}"` → `disabled` fijo.
- Sin cambios de backend/JS: `ManageRuleDefinitionService.alignRequiresParamFromCatalog()` cubre create/update aunque el campo no se envíe.

## Decisiones
- `disabled` fijo en vez de `th:disabled="true"` para HTML estático simple; sin lógica en vista.
- Se mantiene `th:field` para pintado inicial + sync visual vía `rule-definition-form.js`.

## Tests
- `ManageRuleDefinitionServiceP0Test, P2Test, RuleDefinitionControllerTest`: 27 tests, 0 fallos, BUILD SUCCESS.
- Sin tests nuevos: cambio solo de vista; la autocorrección ya está cubierta.

## SonarQube
- Sin lógica en Thymeleaf, sin SpEL, a11y mejorada (campo no editable coherente con ayuda "Se establece automáticamente...").

## Próximos pasos
- Verificación manual create + edit.
