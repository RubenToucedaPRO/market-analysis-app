# Implementación tarea 1.1 (Fase 1): PromptBuilder

## Resumen de la tarea

Se extrajo la construcción del prompt de IA desde `ManageAnalyzeStockService` hacia un servicio de dominio dedicado `PromptBuilder`, para desacoplar el caso de uso de texto inline de prompt.

## Código generado

- `src/main/java/com/market/analysis/domain/service/PromptBuilder.java`
- Integración en:
  - `src/main/java/com/market/analysis/application/usecase/ManageAnalyzeStockService.java`
  - `src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java`
- Tests:
  - `src/test/java/com/market/analysis/unit/domain/service/PromptBuilderTest.java`
  - `src/test/java/com/market/analysis/unit/application/usecase/ManageAnalyzeStockServiceTest.java`

## Decisiones técnicas tomadas

- `PromptBuilder` recibe `Stock` y `StrategyEvaluation` (`buildAnalysisPrompt(Stock, StrategyEvaluation)`).
- Se añadieron placeholders `N/A` para campos nulos/ausentes y evitar fallos por `NullPointerException`.
- Se mantiene la restricción de salida en español y en una sola frase, sin afectar lógica determinista de evaluación.

## Cobertura de tests y pruebas añadidas

- Nuevos tests unitarios de `PromptBuilder`:
  - Construcción con datos completos.
  - Construcción con datos opcionales ausentes.
- Ajuste de tests de `ManageAnalyzeStockService` para validar la invocación a `PromptBuilder`.
- Ejecución validada:
  - `mvn -Dtest=ManageAnalyzeStockServiceTest,PromptBuilderTest test` ✅

## Advertencias de SonarQube o arquitectura

- Sin cambios de arquitectura fuera del alcance.
- Se mantiene separación de responsabilidades (SRP) y Clean Architecture.

## Próximos pasos sugeridos

- Implementar tarea 1.2 (template con ejemplos/few-shot).
- Implementar tarea 1.3 (validador de formato + retry/fallback).
