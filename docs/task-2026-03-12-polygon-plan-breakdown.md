# Task: Refinamiento del plan de implementación para persistencia Polygon y chart

**Date:** 2026-03-12
**Status:** Completed

## Resumen de la tarea

Se ha refinado el documento principal de planificación [plan_regitro_tickers_polygon.md] para convertir las fases de implementación en pasos atómicos, supervisables y reutilizables en futuras peticiones de ejecución.

## Cambios realizados

### 1. Desglose detallado de la Fase 1

La fase de salvaguarda en base de datos se ha dividido en pasos identificables `F1.1` a `F1.10`, cubriendo:
- cierre del modelo de persistencia
- ajuste de entidad JPA
- creación de repositorio
- creación de componente de persistencia
- estrategia transaccional replace
- extensión del parseo en PolygonAdapter
- integración de persistencia en el flujo actual
- logging técnico
- tests unitarios
- tests de integración

### 2. Desglose detallado de la Fase 2

La fase de lectura para chart se ha dividido en pasos `F2.1` a `F2.10`, cubriendo:
- definición del contrato de lectura
- puerto de salida para velas
- caso de uso de lectura
- DTOs específicos de chart
- endpoint JSON
- decisión de librería de chart
- cambios de vista en `ticker-detail`
- JavaScript de carga y render
- ajuste visual y responsive
- tests y validación manual

### 3. Inclusión explícita de impacto en vistas

Cada paso indica de forma explícita si implica o no cambios en vistas, para que la implementación pueda pedirse por lotes sin ambigüedad.

## Decisiones técnicas reflejadas en el plan

- Mantener la fase 1 con impacto mínimo y concentrada en infraestructura.
- Posponer la exposición de lectura a dominio/aplicación hasta la fase 2.
- Separar el payload del chart del `StockDataDTO` actual.
- Dejar identificadores de pasos estables (`F1.x`, `F2.x`) para pedir su implementación individual.

## Cobertura de pruebas prevista

El documento refinado explicita dónde deben incorporarse:
- tests unitarios del adapter y persistencia
- tests de integración JPA/H2
- tests de aplicación y controlador para el endpoint de chart
- validaciones manuales de vista

## Cambios de código

- No se ha modificado código Java ni vistas.
- Solo se ha actualizado documentación de planificación.

## Próximos pasos sugeridos

1. Ejecutar `F1.1` y `F1.2` juntos si se quiere cerrar primero el modelo de persistencia.
2. Ejecutar `F1.3` a `F1.5` como bloque de infraestructura de guardado.
3. Ejecutar `F2.1` antes de tocar frontend para fijar el contrato del chart.