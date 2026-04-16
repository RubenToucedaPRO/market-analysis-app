# Fase 4 — Observabilidad y seguridad en integración IA (OpenRouter/OpenAI)

## Resumen

Se implementó la fase 4 del plan `docs/mejora_prompt_eng.md` con cambios mínimos y acotados al flujo de valoración IA:

- Observabilidad con métricas agregadas de validez/reintento/fallback.
- Endurecimiento de logging para no exponer contenido sensible en logs.
- Límite de tamaño de prompt para controlar costes y truncados en respuesta.

## Código generado

### 1) `ManageAnalyzeStockService`

- Se añadió truncado defensivo de prompt a `4000` caracteres antes de llamar al puerto IA:
  - `enforcePromptSize(prompt, ticker, stage)`
- Se añadieron contadores thread-safe (`AtomicLong`) para:
  - solicitudes totales,
  - respuestas válidas,
  - reintentos,
  - fallbacks.
- Se añadió `logAiMetrics()` para emitir ratios:
  - `validRatio`,
  - `retryRatio`,
  - `fallbackRatio`.
- Se dejó de loguear el texto completo de la valoración IA y ahora se registra solo longitud y si se usó fallback.

### 2) `OpenrouterAdapter`

- Se eliminó el logging del prompt completo y de la respuesta completa.
- Se reemplazó por logging estructurado de metadatos:
  - modelo,
  - longitud de prompt/respuesta,
  - parámetros de generación (`temperature`, `maxTokens`, `topP`, `frequencyPenalty`).

### 3) Tests

- `ManageAnalyzeStockServiceTest`:
  - Ajuste de fallback esperado para alinearlo con el mensaje actual.
  - Nuevo test: truncado de prompt sobredimensionado antes de invocar `apiIAPort`.

## Decisiones técnicas tomadas

1. **Sin cambios de contrato en puertos**: no se alteró `ApiIAPort` para mantener compatibilidad y minimizar impacto.
2. **Métricas por logging estructurado**: se priorizó una implementación simple y trazable sin introducir nuevas dependencias ni capas.
3. **Truncado en Application**: el límite se aplica en el caso de uso para cubrir prompt inicial y prompt de reintento.

## Cobertura y pruebas añadidas

- Se añadió cobertura unitaria para el caso de truncado de prompt.
- Se corrigió una aserción de test que estaba desalineada con el fallback vigente.
- Se ejecutaron pruebas dirigidas del módulo afectado.

## Advertencias SonarQube / arquitectura

- No se detectan cambios que rompan la arquitectura hexagonal.
- No se exponen secretos ni payloads completos de IA en logs.
- La lógica determinista de evaluación no se modifica.

## Próximos pasos sugeridos

1. Externalizar `MAX_PROMPT_CHARS` a propiedad de configuración (`application.properties`) si se requiere ajuste por entorno.
2. Exponer estas métricas en Actuator/Micrometer para dashboards de operación.
