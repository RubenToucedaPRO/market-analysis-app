# Simplificación del fragmento `rule-row`

## Resumen
Se simplificó la plantilla Thymeleaf del fragmento de reglas para quitar la lógica de decisión sobre parámetros permitidos y delegar la visibilidad del selector o input al JavaScript.

La vista ahora solo renderiza una estructura estable:
- contenedor de parámetro de sujeto
- `select` de parámetro de sujeto
- `input` numérico de parámetro de sujeto
- contenedor de parámetro de objetivo
- `select` de parámetro de objetivo
- `input` numérico de parámetro de objetivo

El script `strategy-manager.js` se encarga de:
- mostrar u ocultar el contenedor correcto
- poblar el `select` con los valores permitidos
- preservar el valor actual al alternar entre select e input

## Código modificado
- [src/main/resources/templates/fragments/rule-row.html](../src/main/resources/templates/fragments/rule-row.html)
- [src/main/resources/static/js/strategy-manager.js](../src/main/resources/static/js/strategy-manager.js)

## Decisiones técnicas
- Se eliminó la resolución server-side de `selectedDef` y `selectedAllowedParams` para reducir complejidad en Thymeleaf.
- Se dejó una estructura HTML estable para que el script gestione toda la interacción dinámica.
- Se preservó el valor actual del parámetro al cambiar entre control fijo y control libre.

## Cobertura de tests
- Se validó el fragmento con el test de vista existente para la pantalla de creación de estrategias.
- Se ejecutó también el test unitario del controlador de estrategias.
- Resultado de validación: `12` tests ejecutados, `0` fallos, más una prueba de vista específica con `2` tests adicionales en ejecución separada, también sin fallos.

## Advertencias de SonarQube o arquitectura
- Se redujo la carga de lógica condicional en Thymeleaf.
- La lógica de UI sigue en la capa de presentación y no invade dominio ni aplicación.
- El helper JavaScript se mantiene pequeño y con responsabilidades separadas.

## Próximos pasos sugeridos
- Revisar manualmente el alta y edición de estrategias con indicadores de parámetro fijo y libre.
- Si se quiere una UI más robusta sin depender tanto de JS, evaluar un fragmento reutilizable para el campo de parámetro.
