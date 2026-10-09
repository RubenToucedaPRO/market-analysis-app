# ADR 0003 — IA solo interpretativa, nunca decisoria

- **Estado:** Aceptada (2026-10-13; vigente desde la integración OpenRouter).
- **Fecha:** 2026-10-13.

## Contexto
Los LLMs (modelos gratuitos vía OpenRouter, con 429 y respuestas vacías
frecuentes) son útiles para redactar síntesis, pero son no-deterministas y
pueden fallar. Si participaran en la evaluación, los resultados dejarían de
ser reproducibles y el TFM perdería su base cuantitativa.

## Decisión
Frontera estricta, verificable en código:
- El motor (`Rule` → `RuleEvaluator` → `EvaluateStrategyService`) y las
  métricas (R:R, score, cumplimiento) **nunca** llaman a la IA.
- **Éxito**: la IA (`OpenrouterAdapter`, modelo principal
  `poolside/laguna-s-2.1:free` + 4 reservas con rotación ante 429) redacta texto
  a partir de métricas ya calculadas (`PromptBuilder` + `PromptResponseValidator`
  con reintento).
- **Fallo total** (los 5 modelos fallan): se persiste el fallback amistoso
  ("…Reintenta más tarde"), con métricas (`ai_valoration_metrics`) y aviso en
  la vista; la evaluación numérica queda intacta.
- Regla de trabajo: jamás se modifica la lógica de evaluación para contentar
  a la IA (norma de `AGENTS.md`).

## Consecuencias
- Resultados deterministas y defendibles aunque la IA falle o cambie de modelo.
- La calidad del texto depende de cupos gratuitos (latencias altas asumidas
  por diseño en proyecto educativo; ver README § APIs Externas).
- El prompt engineering (`docs/mejora_prompt_eng.md`) evoluciona sin riesgo
  sobre el núcleo.
