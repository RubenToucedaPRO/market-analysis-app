# Task: Implementación Fase 0 — Inventario y catálogo canónico

Fecha: 2026-04-16

## Resumen de la tarea

Se completó el ajuste pendiente de Fase 0 para dejar explícita la distinción entre capacidades soportadas por el evaluador y capacidades configurables por usuario:

- Se mantuvo el catálogo canónico completo (`getSupportedCodes`) para compatibilidad y evaluación.
- Se añadió un subconjunto explícito de códigos configurables para UI/autoría (`getUserConfigurableCodes`).
- Se clasificó `VALUE` como alias interno/derivado (no configurable), manteniendo compatibilidad en evaluación.
- La carga de capacidades para crear definiciones (`ManageRuleDefinitionService#getCatalogCapabilities`) ahora usa solo códigos configurables.

## Código generado

- `RuleCapabilityCatalog`:
  - `INTERNAL_OR_DERIVED_CODES`
  - `USER_CONFIGURABLE_CODES`
  - `isUserConfigurable(String)`
  - `getUserConfigurableCodes()`
- `ManageRuleDefinitionService`:
  - `getCatalogCapabilities()` actualizado para usar `getUserConfigurableCodes()`
- Tests:
  - `RuleCapabilityCatalogTest`: valida exclusión de `VALUE` en códigos configurables.
  - `ManageRuleDefinitionServiceP2Test`: valida que `getCatalogCapabilities()` no expone `VALUE`.

## Decisiones técnicas

- Se evitó romper compatibilidad histórica manteniendo `VALUE` en el catálogo soportado por el evaluador.
- Se aplicó separación explícita “soportado vs configurable” en dominio para centralizar la decisión de Fase 0.
- Se limitó el cambio a superficies mínimas (catálogo + servicio de exposición a UI).

## Cobertura de tests

Se añadieron pruebas unitarias focalizadas sobre:

- nueva API de códigos configurables del catálogo,
- exclusión de alias internos al exponer capacidades para alta de definiciones.

## Advertencias SonarQube / arquitectura

- Sin impactos de seguridad ni cambios en lógica determinista de evaluación.
- Se preserva Clean Architecture: la política de capacidad configurable permanece en dominio y se consume desde Application.

## Próximos pasos sugeridos

- Si se identifican más códigos internos/derivados, incorporarlos en `INTERNAL_OR_DERIVED_CODES`.
- Evaluar si la edición de definiciones existentes con códigos internos debe restringirse explícitamente en una fase posterior.
