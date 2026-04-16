# Implementación tarea 1.2 (Fase 1): Template con ejemplos (few-shot)

## Resumen de la tarea

Se actualizó `PromptBuilder` para incorporar un template de salida por secciones obligatorias y dos ejemplos cortos (few-shot), manteniendo la naturaleza interpretativa de la IA.

## Código generado

- `src/main/java/com/market/analysis/domain/service/PromptBuilder.java`
- `src/test/java/com/market/analysis/unit/domain/service/PromptBuilderTest.java`

## Decisiones técnicas tomadas

- Se definieron secciones fijas para la salida: `Resumen técnico`, `Fortalezas`, `Riesgos` y `Conclusión interpretativa`.
- Se añadieron 2 ejemplos breves de respuesta para guiar formato y tono sin aumentar demasiado el prompt.
- Se pidió justificación “breve y verificable” para evitar instrucciones de razonamiento extenso (chain-of-thought).

## Cobertura de tests y pruebas añadidas

- Se reforzó `PromptBuilderTest` para verificar:
  - Presencia de secciones obligatorias.
  - Presencia de los dos ejemplos few-shot.
  - Conservación de placeholders `N/A` y de la directiva de justificación breve/verificable.

## Advertencias de SonarQube o arquitectura

- No se alteró lógica determinista de evaluación.
- Se mantiene SRP: `ManageAnalyzeStockService` delega la construcción textual a `PromptBuilder`.

## Próximos pasos sugeridos

- Implementar tarea 1.3 (validación de formato + retry/fallback controlado).
