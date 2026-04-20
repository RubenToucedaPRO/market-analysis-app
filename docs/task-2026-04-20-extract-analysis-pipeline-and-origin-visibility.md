# Task: Extracción de pipeline de análisis determinista y visibilidad por origin

## Resumen de la tarea
Se refactorizó la lógica de análisis de ticker para extraerla de ManageAnalyzeStockService hacia un pipeline reutilizable de Application. Este pipeline ahora puede ser invocado tanto desde el flujo de análisis como desde el flujo de estrategia.

Además, se introdujo un nuevo origin de stock para distinguir altas creadas desde estrategia y se aplicó un filtro de visibilidad para que esos registros no aparezcan en la vista de análisis.

## Código generado
- Nuevo servicio de aplicación:
  - src/main/java/com/market/analysis/application/usecase/StockDeterministicAnalysisPipeline.java
- Modificaciones en orquestación de análisis:
  - src/main/java/com/market/analysis/application/usecase/ManageAnalyzeStockService.java
- Reutilización del pipeline desde estrategia:
  - src/main/java/com/market/analysis/application/usecase/AddSuggestedTickersToAnalysisService.java
- Nuevo origen en dominio:
  - src/main/java/com/market/analysis/domain/model/StockOrigin.java
- Nuevo contrato de lectura para visibilidad en análisis:
  - src/main/java/com/market/analysis/domain/port/out/StockDataRepository.java
- Implementación de filtrado en persistencia:
  - src/main/java/com/market/analysis/infrastructure/persistence/repository/JpaStockDataRepository.java
  - src/main/java/com/market/analysis/infrastructure/persistence/repository/SqlStockDataRepository.java
- Inyección/configuración de beans:
  - src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java

## Decisiones técnicas tomadas
1. Se creó StockDeterministicAnalysisPipeline para encapsular quote, históricos, indicadores, cache diario, persistencia de candles y evaluación de estrategia en un único flujo determinista reutilizable.
2. ManageAnalyzeStockService quedó como orquestador fino y delega en el pipeline tanto la validación de perfiles/prohibidos como el procesamiento determinista.
3. AddSuggestedTickersToAnalysisService ahora utiliza el mismo pipeline para altas desde estrategia, evitando la evaluación simplificada offline previa.
4. Se añadió StockOrigin.STRATEGY_SUGGESTION para identificar altas de estrategia.
5. Se introdujo un método de repositorio findAllStocksVisibleInAnalysis que excluye STRATEGY_SUGGESTION y mantiene compatibilidad con registros legacy de origin nulo.

## Cobertura de tests y pruebas añadidas
Se actualizaron tests unitarios y se añadieron pruebas del pipeline extraído:

- Modificado:
  - src/test/java/com/market/analysis/unit/application/usecase/ManageAnalyzeStockServiceTest.java
  - src/test/java/com/market/analysis/unit/application/usecase/AddSuggestedTickersToAnalysisServiceTest.java
- Nuevo:
  - src/test/java/com/market/analysis/unit/application/usecase/StockDeterministicAnalysisPipelineTest.java

Resultado de ejecución de tests objetivo:
- 22 tests pasados
- 0 fallos

## Advertencias de SonarQube o arquitectura
- Se ajustaron bucles en AddSuggestedTickersToAnalysisService para evitar múltiples continue en el mismo loop.
- Se eliminaron imports no usados detectados por análisis estático.
- Se mantiene IA solo para interpretación (sin impacto en evaluación determinista), conforme a arquitectura definida en README y AGENTS.

## Próximos pasos sugeridos
1. Exponer métricas de pipeline por origin (EXTERNAL_PROVIDER vs STRATEGY_SUGGESTION) para observabilidad operativa.
2. Añadir test de integración de repositorio para validar explícitamente el filtro de visibilidad en análisis.
3. Revisar si refreshFromSuggestionSnapshot debe redirigir a estrategias en lugar de análisis cuando el origin sea STRATEGY_SUGGESTION.
