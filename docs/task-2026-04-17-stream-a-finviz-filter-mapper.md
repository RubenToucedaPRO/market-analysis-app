# Task — Stream A FinvizFilterMapper

## Resumen de la tarea
Se implementa `FinvizFilterMapper` en Domain para traducir reglas deterministas soportadas a filtros Finviz, devolviendo trazabilidad de reglas no mapeables mediante `FinvizFilterMappingResult`.

## Código generado (si aplica)
- `src/main/java/com/market/analysis/domain/service/FinvizFilterMapper.java`
- `src/test/java/com/market/analysis/unit/domain/service/FinvizFilterMapperTest.java`

## Decisiones técnicas tomadas
- El mapper se implementa como servicio de dominio puro, sin dependencias de infraestructura.
- Se define una matriz explícita `indicator + operator (+param) -> finvizCode` con combinaciones soportadas para `PRICE/SMA`, `SMA/SMA` y `VOLUME/AVG_VOLUME`.
- Se respeta compatibilidad con el evaluador actual aceptando alias de operador (`GREATER_THAN`, `LESS_THAN`) y dejando no soportadas las combinaciones sin equivalencia exacta en Finviz.
- Se agregan `unmappableRules` y `warnings` por cada regla no convertible, sin bloquear reglas sí mapeables.

## Cobertura de tests y pruebas añadidas
Tests unitarios añadidos en `FinvizFilterMapperTest` para:
- mapeos soportados,
- alias de operadores del evaluador,
- casos no soportados con `unmappableRules` y `warnings`,
- deduplicación de filtros,
- entradas nulas/vacías,
- reglas nulas dentro de la lista.

Comando ejecutado:
- `mvn -Dtest=FinvizFilterMapperTest,FinvizFilterMappingResultTest test`

## Advertencias de SonarQube o arquitectura
- No se introducen acoplamientos entre Domain e Infrastructure.
- No se modifica la lógica del evaluador determinista (`RuleEvaluator`).

## Próximos pasos sugeridos
1. Conectar `FinvizFilterMapper` en el caso de uso `SuggestTickersUseCase` (Stream C).
2. Ajustar/expandir matriz de mapeo según filtros finales soportados por el adapter de Finviz (Stream B).
