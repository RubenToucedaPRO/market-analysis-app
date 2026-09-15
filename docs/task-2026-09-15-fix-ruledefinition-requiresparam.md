# Fix creación de RuleDefinition con requiresParam=true tras extracción JS

## Resumen de la tarea
En `src/main/resources/templates/rule-definitions/create.html` no se podía crear una regla cuyo indicador requiere parámetro (ej. SMA, EMA, RSI) desde el commit `8a5487d` que extrajo el JS inline a `/js/rule-definition-form.js`.

Causa raíz: en ese refactor el checkbox pasó de `th:onclick="return false"` (se envía) a `th:disabled="${!isEdit}"` (no se envía por spec HTML). El JS solo hacía `checkbox.checked = ...`, sin re-habilitar antes del submit. El DTO llegaba siempre con `requiresParam=false` y `ManageRuleDefinitionService.validateAgainstCatalog()` lo rechazaba con `validation.rd_param_conflict`.

Fix elegido por el usuario: el backend autocorrige el flag desde el catálogo canónico (fuente de verdad), en lugar de rechazar. El checkbox deshabilitado queda como ayuda visual.

## Código generado

### 1. `src/main/java/com/market/analysis/application/usecase/ManageRuleDefinitionService.java`
- `createRuleDefinition()` y `updateRuleDefinition()` ahora llaman a:
```java
validateCodeSupported(ruleDefinitionDto);
alignRequiresParamFromCatalog(ruleDefinitionDto);
```
- Nuevo:
```java
private void validateCodeSupported(RuleDefinitionDTO dto) {
    String code = dto.getCode();
    if (!RuleCapabilityCatalog.isSupported(code)) {
        log.warn("Rejected rule definition with unsupported code='{}'. Supported: {}",
                code, RuleCapabilityCatalog.getSupportedCodes());
        throw new DomainValidationException(
                DomainErrorCodes.RD_UNSUPPORTED_CODE, code);
    }
}

private void alignRequiresParamFromCatalog(RuleDefinitionDTO dto) {
    String code = dto.getCode();
    boolean catalogRequiresParam = RuleCapabilityCatalog.getCapability(code)
            .map(RuleCapability::isRequiresParam)
            .orElse(false);
    if (dto.isRequiresParam() != catalogRequiresParam) {
        log.info("Autocorrecting rule definition code='{}': requiresParam={} -> {} (catalog value)",
                code, dto.isRequiresParam(), catalogRequiresParam);
        dto.setRequiresParam(catalogRequiresParam);
    }
}
```
- Se elimina el `throw RD_PARAM_CONFLICT`. La constante `DomainErrorCodes.RD_PARAM_CONFLICT` y `messages.properties: validation.rd_param_conflict` se conservan por compatibilidad, pero ya no se lanzan.

### 2. `src/main/resources/static/js/rule-definition-form.js`
- Sync inicial en `DOMContentLoaded` para reflejar valor preseleccionado tras error de validación:
```js
document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-action="sync-requires-param"]').forEach(function (el) {
    syncRequiresParam(el);
    el.addEventListener('change', function () {
      syncRequiresParam(el);
    });
  });
});
```
- Comentario de cabecera: checkbox deshabilitado es solo visual; backend alinea.

### 3. `src/test/java/com/market/analysis/unit/application/usecase/ManageRuleDefinitionServiceP0Test.java`
- `testCreateRejectsInconsistentRequiresParam` → `testCreateAutocorrectsInconsistentRequiresParam` (SMA false → true, verifica `save` llamado).
- `testCreateRejectsInconsistentRequiresParamForNoParamIndicator` → `testCreateAutocorrects...` (PRICE true → false).
- `testUpdateRejectsInconsistentRequiresParam` → `testUpdateAutocorrects...` (EMA false → true).

## Decisiones técnicas
- **Backend fuente de verdad (Clean Architecture):** `RuleCapabilityCatalog` manda; el form no es confiable porque el campo va `disabled` en creación. Respeta SRP (métodos `validateCodeSupported` + `alignRequiresParamFromCatalog` separados) y DIP (casos de uso contra puertos).
- **No se revierte a `onclick="return false"`:** `disabled` es mejor UX/a11y y ahora es inocuo porque el backend corrige.
- **No se añade hidden ni re-enable en submit:** innecesario con autocorrección; se evita lógica duplicada en frontend (AGENTS.md: lógica compleja en Application, DTO solo transporte).
- **Se conserva `RD_PARAM_CONFLICT` sin uso:** evita romper docs/i18n externos; Sonar no lo marca como bug al ser constante pública.

## Cobertura de tests y pruebas
- `mvn -Dtest='ManageRuleDefinitionServiceP0Test,ManageRuleDefinitionServiceP2Test,RuleDefinitionControllerTest' test`: 27 tests, 0 fallos.
- `mvn test` completo: **1016 tests, 0 fallos, BUILD SUCCESS**.
- Cobertura: comportamiento nuevo cubierto por los 3 tests de autocorrección + tests existentes de códigos válidos (SMA true, no-param loop) y rechazo de `VWAP`/`STOCH` desconocidos.
- Sin `lenient` en Mockito; stubs con `any()` solo donde la mutación del DTO lo requiere.

## Advertencias SonarQube / arquitectura
- Sin lógica de negocio en Thymeleaf/JS (JS solo visual).
- Constructor injection intacto (`@RequiredArgsConstructor`).
- Sin `System.out`, `try-with-resources` no aplica, SLF4J usado.
- Complejidad cognitiva < 15, sin nuevos parámetros, clase < 1000 líneas.
- `th:disabled` + `th:text` correctos; CSRF/`th:action` sin cambios.

## Próximos pasos sugeridos
- Verificación manual: `/rule-definitions/new` → SMA → Guardar → debe crear con `requires_param=true` y flash `ruledefinition.created`.
- Opcional: limpiar `RD_PARAM_CONFLICT` de `DomainErrorCodes`/`messages.properties`/docs cuando se confirme que ninguna rama lo usa.
- Opcional: test de integración MockMvc posteando `code=SMA` sin `requiresParam` y asertando creación 3xx.
