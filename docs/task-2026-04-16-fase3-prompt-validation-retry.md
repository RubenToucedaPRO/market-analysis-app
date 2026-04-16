# Fase 3 - Validación de respuesta IA + retry/fallback

## Resumen de la tarea

Se implementó la fase 3 del plan de mejora de prompt engineering para robustecer la calidad de salida IA:

- Validación estructural por secciones obligatorias.
- Reintento automático con prompt más estricto cuando el formato inicial es inválido.
- Fallback neutral cuando persiste respuesta inválida (o ante error de integración).

## Código generado

- `src/main/java/com/market/analysis/domain/service/PromptResponseValidator.java`
- `src/main/java/com/market/analysis/application/usecase/ManageAnalyzeStockService.java`
- `src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java`
- `src/test/java/com/market/analysis/unit/domain/service/PromptResponseValidatorTest.java`
- `src/test/java/com/market/analysis/unit/application/usecase/ManageAnalyzeStockServiceTest.java`

## Decisiones técnicas tomadas

- Se mantuvo el flujo IA en la capa Application (`ManageAnalyzeStockService`) y la lógica de validación en Domain Service (`PromptResponseValidator`), respetando arquitectura limpia.
- La validación exige estas secciones: `Resumen técnico`, `Fortalezas`, `Riesgos`, `Conclusión interpretativa`.
- El caso de uso ahora es no bloqueante para IA: ante formato inválido persistente o excepción, se guarda fallback neutral.

## Cobertura de tests y pruebas añadidas

- Nuevos tests unitarios para `PromptResponseValidator`:
  - respuesta válida con secciones obligatorias,
  - respuesta inválida,
  - construcción de prompt de retry estricto,
  - null safety.
- Tests de `ManageAnalyzeStockService` ampliados para:
  - respuesta válida a la primera,
  - respuesta inválida + retry válido,
  - respuesta inválida persistente + fallback.
- Ejecución validada:
  - `mvn -Dtest=PromptResponseValidatorTest,ManageAnalyzeStockServiceTest,PromptBuilderTest,OpenrouterAdapterTest test`

## Advertencias de SonarQube o arquitectura

- Sin cambios en la lógica determinista de evaluación de estrategias.
- La IA sigue siendo complemento interpretativo y no condiciona decisiones de dominio.

## Próximos pasos sugeridos

- Añadir test más específico de `OpenrouterAdapter` para cubrir explícitamente `system + user prompt` cuando se implemente la tarea 2.2.
