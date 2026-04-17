# Task — Fase 0 contratos Finviz

## Resumen de la tarea
Se implementa la fase 0 de `docs/rastreador-finviz.md` creando los contratos base para habilitar desarrollo paralelo: resultado de mapeo, puerto de screener y caso de uso de sugerencia de tickers con DTOs de entrada/salida.

## Código generado (si aplica)
- `src/main/java/com/market/analysis/domain/model/FinvizFilterMappingResult.java`
- `src/main/java/com/market/analysis/domain/port/out/FinvizScreenerPort.java`
- `src/main/java/com/market/analysis/domain/port/in/SuggestTickersUseCase.java`
- `src/main/java/com/market/analysis/application/dto/FinvizExecutionMode.java`
- `src/main/java/com/market/analysis/application/dto/TickerSuitabilityStatus.java`
- `src/main/java/com/market/analysis/application/dto/SuggestTickersRequestDTO.java`
- `src/main/java/com/market/analysis/application/dto/SuggestTickersResponseDTO.java`
- `src/main/java/com/market/analysis/application/dto/SuggestedTickerDTO.java`

## Decisiones técnicas tomadas
- Se mantiene arquitectura hexagonal: contratos de entrada/salida en `domain/port` y transporte en `application/dto`.
- Se fija modo por defecto `TOLERANT` para ejecución cuando existan reglas no mapeables.
- Se congela nomenclatura de estado funcional `APTO`/`NO_APTO` en `TickerSuitabilityStatus`.
- Se incluyen campos de trazabilidad (`warnings`, `unmappableRules`, `traceability`, `appliedFilters`) en los contratos.

## Cobertura de tests y pruebas añadidas
Se añaden tests mínimos de compilación/instanciación de contratos:
- `FinvizFilterMappingResultTest`
- `SuggestTickersRequestDTOTest`
- `SuggestTickersUseCaseContractTest`
- `FinvizScreenerPortContractTest`

Comando ejecutado:
- `mvn -Dtest=FinvizFilterMappingResultTest,SuggestTickersRequestDTOTest,SuggestTickersUseCaseContractTest,FinvizScreenerPortContractTest test`

## Advertencias de SonarQube o arquitectura
- No se introducen dependencias de infraestructura en Domain/Application.
- No se modifica la evaluación determinista existente.

## Próximos pasos sugeridos
1. Implementar Stream A (`FinvizFilterMapper`) sobre `FinvizFilterMappingResult`.
2. Implementar Stream B (`JsoupFinvizAdapter`) sobre `FinvizScreenerPort`.
3. Implementar Stream C (orquestación `SuggestTickersUseCase`) y conectar Stream D (UI).
