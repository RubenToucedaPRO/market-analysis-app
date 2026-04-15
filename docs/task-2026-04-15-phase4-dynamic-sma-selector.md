# Fase 4: Selector Dinámico de SMA en Formulario reutilizando `ruleDefinitions`

## Resumen

Implementación de la Fase 4 del documento `riskreward.md`: selector dinámico de períodos SMA en el formulario de gestión de riesgo, reutilizando la infraestructura existente de `globalThis.ruleDefinitions` que ya alimenta los selectores de reglas.

## Tareas completadas

1. **Nuevo fichero `risk-management.js`**: JavaScript dedicado a la lógica de toggle entre `<select>` (SMA) e `<input>` (PERCENTAGE/FIXED_PRICE) para los campos de valor objetivo y stop-loss.
2. **Modificación de `risk-management-fields.html`**: Añadidos `<select>` ocultos (`objectiveTargetSmaSelect`, `objectiveStopLossSmaSelect`) junto a los `<input>` existentes.
3. **Importación en `create.html`**: Incluido `risk-management.js` después de `strategy-manager.js`.

## Decisiones técnicas

- **Separación de ficheros JS**: Se creó `risk-management.js` en lugar de extender `strategy-manager.js` para mantener responsabilidad única (SRP) y evitar acoplar la lógica de reglas con la de gestión de riesgo.
- **Sin opciones hardcodeadas**: El `<select>` de SMA se puebla dinámicamente desde `globalThis.ruleDefinitions` buscando la entrada con `code === "SMA"` y extrayendo `allowedParams`. Si se añade una nueva SMA al catálogo (`RuleCapabilityCatalog`), las opciones se actualizan automáticamente.
- **Sincronización select→input en submit**: Antes del envío del formulario, el valor seleccionado en el `<select>` se copia al `<input>` oculto (`name="objective.targetValue"` / `name="objective.stopLossValue"`) para que el binding de Thymeleaf funcione sin modificar el backend.
- **Placeholders dinámicos**: Al cambiar el tipo, el placeholder del `<input>` se actualiza: "ej., 5.00 (%)" para PERCENTAGE, "ej., 150.00 ($)" para FIXED_PRICE.
- **Soporte de edición**: Al cargar la página, `DOMContentLoaded` aplica la lógica según el tipo actualmente seleccionado, pre-seleccionando el período SMA correcto si aplica.

## Archivos afectados

| Archivo | Cambio |
|---|---|
| `src/main/resources/static/js/risk-management.js` | Nuevo fichero con lógica de toggle SMA/input |
| `src/main/resources/templates/fragments/risk-management-fields.html` | Añadidos `<select>` ocultos para SMA |
| `src/main/resources/templates/strategies/create.html` | Import de `risk-management.js` |
| `docs/task-2026-04-15-phase4-dynamic-sma-selector.md` | Este documento |

## Cobertura de tests

- No se requieren tests unitarios Java adicionales: los cambios son exclusivamente de frontend (HTML + JS).
- La lógica del catálogo (`RuleCapabilityCatalog`) y la validación de períodos SMA (`StrategyObjective.validate()`) ya están cubiertas por tests existentes.
- El build y todos los tests existentes pasan correctamente.

## Advertencias

- Ninguna advertencia de SonarQube ni arquitectura. Los cambios no tocan lógica de negocio ni capas de dominio.

## Próximos pasos sugeridos

- **Fase 5**: Validaciones avanzadas de coherencia (warnings para stop-loss > 20%, target > 100%, períodos SMA iguales, ratio < 1.0).
- Considerar añadir tests E2E (Selenium/Cypress) para validar el comportamiento dinámico del selector en el navegador.
