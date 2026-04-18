# Task — Stream C Application Orchestrator

## Resumen de la tarea
Se implementa el caso de uso de Application para sugerencia de tickers (`SuggestTickersUseCase`) orquestando estrategia → mapper Finviz → screener → clasificación determinista (`APTO`/`NO_APTO`) con trazabilidad, razones de descarte y política `STRICT`/`TOLERANT`.

## Código generado (si aplica)
- `src/main/java/com/market/analysis/application/usecase/SuggestTickersService.java`
- `src/main/java/com/market/analysis/application/usecase/DeterministicTickerEvaluator.java`
- `src/main/java/com/market/analysis/application/usecase/DeterministicTickerEvaluation.java`
- `src/main/java/com/market/analysis/domain/service/FinvizFilterMapper.java`
- `src/test/java/com/market/analysis/unit/application/usecase/SuggestTickersServiceTest.java`

## Decisiones técnicas tomadas
- Se mantiene desacoplamiento hexagonal: Application depende de contratos (`FinvizFilterMapper`, `FinvizScreenerPort`, `StrategyRepository`, `DeterministicTickerEvaluator`) sin parsing HTML ni detalles de infraestructura.
- Se añade política explícita:
  - `STRICT`: bloquea ejecución si hay reglas no mapeables.
  - `TOLERANT`: continúa con mapeo parcial.
- La clasificación por ticker usa estado funcional fijo `APTO`/`NO_APTO` y conserva trazabilidad por candidato.

## Cobertura de tests y pruebas añadidas si faltan
Tests añadidos en `SuggestTickersServiceTest` cubriendo:
- Orquestación completa en modo tolerante.
- Bloqueo en modo estricto por incompatibilidades.
- Valores por defecto (`TOLERANT`, `maxCandidates=25`).
- Validación de entrada (strategyId obligatorio).

Comando de validación ejecutado:
- `mvn -Dtest=SuggestTickersServiceTest,SuggestTickersUseCaseContractTest test` (incluye test de contrato ya existente en el repositorio)

## Advertencias de SonarQube o arquitectura
- No se detecta acoplamiento de Application con adapter HTML/JSoup.
- No se modifica la lógica determinista existente; solo se orquesta vía contrato interno.

## Próximos pasos sugeridos
1. Conectar implementación concreta de `FinvizFilterMapper` (Stream A).
2. Conectar implementación concreta de `FinvizScreenerPort` (Stream B).
3. Añadir wiring en configuración/Presentation al integrar Stream D.
