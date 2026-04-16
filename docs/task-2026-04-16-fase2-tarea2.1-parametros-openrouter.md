# Fase 2 — Tarea 2.1: Parámetros de generación configurables (OpenRouter)

## Resumen

Se implementó la parametrización de generación para OpenRouter desde configuración externa, evitando valores hardcodeados en `OpenrouterAdapter`.

## Cambios aplicados

- `config/application.properties`
  - Añadidas propiedades:
    - `openrouter.model`
    - `openrouter.temperature`
    - `openrouter.max-tokens`
    - `openrouter.top-p`
    - `openrouter.frequency-penalty`
- `OpenrouterAdapter`
  - Inyección por constructor de las nuevas propiedades.
  - Uso de los parámetros en `ChatCompletionCreateParams` (`model`, `temperature`, `maxTokens`, `topP`, `frequencyPenalty`).

## Decisiones técnicas

- Se mantiene la arquitectura hexagonal: el cambio queda encapsulado en infraestructura (`OpenrouterAdapter`) y configuración (`application.properties`).
- No se altera la lógica determinista del dominio ni del caso de uso; solo la capa interpretativa IA.
- Se usan valores por defecto coherentes con el plan de mejora para permitir ajuste sin tocar código.

## Tests y validación

- Se actualizaron tests unitarios de `OpenrouterAdapterTest` para el nuevo constructor con parámetros configurables.
- Se ajustaron aserciones de `PromptBuilderTest` al formato de prompt vigente para mantener la suite consistente.
- Validación prevista con Maven (`mvn test`).

## Advertencias Sonar/arquitectura

- Sin nuevos acoplamientos de negocio a infraestructura.
- Sin uso de `lenient` en Mockito.

## Próximos pasos sugeridos

- Implementar la Tarea 2.2 (separación `system` + `user` prompt en el adaptador).
- Añadir test unitario específico que verifique la serialización de parámetros de generación en la request al cliente IA.
