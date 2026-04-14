# Fix del selector de parámetro de sujeto en reglas

## Resumen
Se corrigió el comportamiento del fragmento de reglas para que el selector de parámetro de sujeto aparezca correctamente cuando el usuario elige un indicador que requiere parámetros.

El problema principal era doble:
- El `<select>` de parámetro se eliminaba del DOM mediante `th:if` en filas nuevas, así que JavaScript no podía mostrarlo después del cambio de indicador.
- La inicialización del script usaba un selector incorrecto para la fila existente y no sincronizaba de forma consistente los campos de sujeto y objetivo.

## Código generado o modificado
- [src/main/resources/templates/fragments/rule-row.html](../src/main/resources/templates/fragments/rule-row.html)
- [src/main/resources/static/js/strategy-manager.js](../src/main/resources/static/js/strategy-manager.js)
- [src/test/java/com/market/analysis/unit/presentation/controller/StrategyControllerViewTest.java](../src/test/java/com/market/analysis/unit/presentation/controller/StrategyControllerViewTest.java)

## Decisiones técnicas
- Se mantuvieron ambos controles de parámetro siempre presentes en el DOM y se controló su visibilidad con `style` y `disabled`.
- Se introdujeron variables seguras en Thymeleaf para evitar evaluación nula al iterar sobre `allowedParams`.
- Se refactorizó el JavaScript en helpers pequeños para reducir complejidad cognitiva y dejar la inicialización de sujeto y objetivo alineada.
- Se añadió una prueba de vista con `MockMvc` para verificar que el HTML renderizado contiene el selector de parámetro de sujeto.

## Cobertura de tests
- Se añadió cobertura específica para la vista de creación de estrategia.
- El test valida que la página renderizada incluye `subject-param-select-0` y `subject-param-input-0`.
- Resultado de validación: `2` tests ejecutados, `0` fallos.

## Advertencias de SonarQube o arquitectura
- Se evitó meter lógica de negocio en la vista; el cambio solo afecta presentación e interacción mínima.
- Se redujo la complejidad cognitiva del helper JavaScript principal separando responsabilidades.
- No se identificaron errores de compilación tras la validación.

## Próximos pasos sugeridos
- Revisar manualmente en navegador el alta de estrategia con indicadores de parámetro fijo y parámetro libre.
- Reutilizar la misma estrategia de null-safety en otros fragmentos Thymeleaf similares si aparecen.
