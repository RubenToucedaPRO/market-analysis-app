# Formateo decimal en vista de detalle de estrategia

## Resumen de la tarea
Se ajustó la vista de detalle de estrategia para que todos los valores numéricos mostrados en gestión de riesgo y parámetros de reglas se presenten con un máximo de dos decimales y sin parte decimal cuando el resultado sea `.00`.

## Código generado
Archivo modificado:
- src/main/resources/templates/strategies/detail.html

Campos actualizados con formato decimal:
- `strategy.objective.targetValue`
- `strategy.objective.stopLossValue`
- `strategy.objective.capitalToRisk`
- `rule.subjectParam`
- `rule.targetParam`

Implementación aplicada en Thymeleaf:
- Redondeo a 2 decimales con `setScale(2, RoundingMode.HALF_UP)`.
- Eliminación de ceros decimales no significativos con `stripTrailingZeros()`.
- Renderizado final con `toPlainString()`.

## Decisiones técnicas tomadas
- Se resolvió en capa de presentación (Thymeleaf) para no alterar DTOs, dominio ni casos de uso.
- Se mantuvo la vista en modo solo lectura, sin introducir inputs ni lógica de edición.
- Se evitó JavaScript para formato numérico con el fin de garantizar consistencia server-side en render inicial.

## Cobertura de tests y pruebas añadidas
- No se añadieron tests unitarios en esta tarea por tratarse de formato visual en plantilla Thymeleaf.
- Validación recomendada manual con datos de ejemplo:
  - `10` -> `10`
  - `10.2` -> `10.2`
  - `10.236` -> `10.24`
  - `10.00` -> `10`

## Advertencias de SonarQube o arquitectura
- Sin cambios de arquitectura ni de lógica de negocio.
- No se detectan riesgos de seguridad ni incumplimientos de separación de responsabilidades.

## Próximos pasos sugeridos
1. Unificar este formato decimal en otras vistas de detalle/listado para consistencia visual.
2. Considerar helper/fragmento reutilizable para expresiones de formateo y reducir duplicación.
3. Añadir test de integración web (MockMvc) que verifique el render del formato en HTML.
