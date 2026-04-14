# Corrección de valores de parámetros al editar estrategias

## Resumen
Se corrigió el problema por el que, al abrir una estrategia guardada en modo edición, los campos de parámetro de sujeto y valor de objetivo podían aparecer vacíos aunque los valores estuvieran persistidos.

La causa estaba en la rehidratación del `<select>` de parámetros permitidos: los valores numéricos guardados podían llegar como `50.0`, mientras que las opciones generadas en JavaScript se serializaban como `50`. Esa diferencia impedía que el select marcara la opción correcta.

## Código modificado
- [src/main/resources/static/js/strategy-manager.js](../src/main/resources/static/js/strategy-manager.js)
- [src/test/java/com/market/analysis/unit/presentation/controller/StrategyControllerViewTest.java](../src/test/java/com/market/analysis/unit/presentation/controller/StrategyControllerViewTest.java)

## Decisiones técnicas
- Se añadió normalización numérica antes de asignar el valor al select de parámetros permitidos.
- Se mantuvo el flujo actual de renderizado y la lógica de visibilidad en JavaScript.
- Se añadió una prueba de edición para verificar que el HTML renderiza los valores guardados.

## Cobertura de tests
- Se añadió un test de edición para la vista de estrategias.
- Se ejecutó la prueba de vista con éxito.
- Resultado de validación: `3` tests ejecutados, `0` fallos.

## Advertencias de SonarQube o arquitectura
- La corrección se limita a la capa de presentación y no altera la lógica de dominio.
- La normalización se hace en un helper pequeño para no inflar la complejidad del flujo principal.

## Próximos pasos sugeridos
- Revisar manualmente una estrategia que use parámetros con decimales para confirmar que el select marca el valor correcto.
- Si aparecen más casos con formato numérico similar, aplicar la misma normalización en otros flujos de edición.
