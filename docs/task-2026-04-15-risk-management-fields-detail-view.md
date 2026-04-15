# Añadir campos de gestión de riesgo en vista de detalle

## Resumen de la tarea
Se incorporó en la vista de detalle de estrategia una nueva tarjeta de "Gestión de Riesgo" con los mismos campos funcionales existentes en la vista de creación, pero en formato no editable.

## Código generado
Archivo modificado:
- src/main/resources/templates/strategies/detail.html

Cambios principales:
- Nueva tarjeta visual "Gestión de Riesgo" entre "Detalles de la Estrategia" y "Reglas de Ejecución".
- Estado vacío cuando no existe objetivo de riesgo (`strategy.objective == null`).
- Renderizado en solo lectura de:
  - Tipo de Objetivo (`strategy.objective.targetType`)
  - Valor Objetivo (`strategy.objective.targetValue`)
  - Tipo de Stop Loss (`strategy.objective.stopLossType`)
  - Valor Stop Loss (`strategy.objective.stopLossValue`)
  - Capital a Arriesgar (`strategy.objective.capitalToRisk`)
  - Descripción del Objetivo (`strategy.objective.description`)

## Decisiones técnicas tomadas
- Se mantuvo la lógica de negocio fuera de la vista, mostrando únicamente datos ya preparados por el controlador y DTO.
- Se usó estructura de tarjeta consistente con el resto de la página para conservar uniformidad visual.
- Se evitó cualquier control de edición (`input`, `select`) para cumplir el requisito de solo lectura.

## Cobertura de tests y pruebas añadidas
- No se añadieron tests unitarios en esta tarea al tratarse de un cambio de presentación (Thymeleaf HTML).
- Se recomienda validación manual de rendering en:
  - Estrategias con `objective` informado.
  - Estrategias sin `objective` (estado vacío).

## Advertencias de SonarQube o arquitectura
- Sin incidencias de arquitectura detectadas en este cambio.
- No se introdujo lógica de dominio en la vista.

## Próximos pasos sugeridos
1. Formatear valores monetarios/decimales de riesgo con patrón común de la aplicación (si aplica).
2. Añadir test de integración web (MockMvc) que verifique presencia de la tarjeta y campos en la vista de detalle.
3. Evaluar extracción a fragmento reutilizable si se requiere el mismo bloque en otras vistas.
