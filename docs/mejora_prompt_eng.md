# Mejora de Prompt Engineering (OpenRouter/OpenAI)

## Contexto actual (contrastado con código)

Tras revisar la implementación actual:

- La construcción del prompt está **acoplada** en `ManageAnalyzeStockService#getValorationIA`.
- El prompt se arma con `String` inline (texto fijo en inglés + “Remember answer in Spanish.”).
- Se envía un único `user message` a OpenRouter (`OpenrouterAdapter`), sin `system prompt` dedicado.
- No hay configuración explícita de parámetros de generación (`temperature`, `top_p`, `max_tokens`, `frequency_penalty`).
- No hay validación estructural de la respuesta ni estrategia de retry/fallback controlada.

Esto confirma que tu propuesta es correcta y necesaria; a continuación se refina para mantener arquitectura hexagonal y comportamiento determinista del dominio.

---

## Objetivo de mejora

Separar, estandarizar y robustecer la capa de análisis interpretativo por IA para que:

1. Sea mantenible y testeable.
2. No contamine casos de uso con texto de prompt.
3. Mantenga la regla del proyecto: la IA **no** afecta evaluación determinista ni decisiones de inversión.

---

## Plan por fases propuesto (refinado)

## Fase 1 — Refactor de construcción de prompts

### TAREA 1.1: Crear `PromptBuilder` (Domain Service)

- **Ubicación sugerida:** `src/main/java/com/market/analysis/domain/service/PromptBuilder.java`
- **Firma sugerida:**
  - `String buildAnalysisPrompt(Stock stock, StrategyEvaluation evaluation)`
- **Justificación del ajuste:**
  - En el código actual, `RiskReward` está embebido en `StrategyEvaluation` (campos como `riskRewardRatio`, `targetPrice`, `stopLossPrice`), por lo que no hace falta forzar un tercer objeto en la firma.
- **Contenido del template estructurado:**
  1. Contexto del análisis (interpretativo, no recomendación).
  2. Datos técnicos del ticker.
  3. Resultados de estrategia.
  4. Bloque de gestión de riesgo / R:R (si existe).
  5. Instrucciones de formato de salida con secciones fijas.

### TAREA 1.2: Template con ejemplos (few-shot)

- Incluir **2 ejemplos cortos** de respuesta bien formada (no demasiado largos para controlar tokens).
- Definir salida obligatoria por secciones, por ejemplo:
  - `Resumen técnico`
  - `Fortalezas`
  - `Riesgos`
  - `Conclusión interpretativa`
- **Refinamiento recomendado:** evitar pedir “chain-of-thought” explícito. En su lugar, pedir “justificación breve y verificable” por sección para mejorar trazabilidad sin exponer razonamientos internos extensos.

### TAREA 1.3: Validación de respuesta + resiliencia

- Crear validador de formato (p. ej. `PromptResponseValidator`) que compruebe presencia de secciones obligatorias.
- Si falla formato:
  1. Reintentar 1 vez con prompt más restrictivo (“devuelve exactamente estas secciones”).
  2. Si vuelve a fallar, usar fallback controlado:
     - Mensaje genérico neutral.
     - Log técnico con causa.
- Mantener comportamiento no bloqueante: la app guarda stock aunque la valoración IA sea nula o fallback.

---

## Fase 2 — Mejora de integración OpenRouter/OpenAI

### TAREA 2.1: Parámetros de generación configurables

Añadir propiedades en `config/application.properties` y bindearlas en el adaptador:

- `openrouter.model=google/gemma-4-31b-it:free` (o modelo objetivo)
- `openrouter.temperature=0.7` (rango típico: 0.0–2.0; menor = más determinista)
- `openrouter.max-tokens=500` (límite de longitud de salida para mantener concisión y coste)
- `openrouter.top-p=0.9` (rango: 0.0–1.0; controla muestreo por núcleo)
- `openrouter.frequency-penalty=0.5` (rango típico: -2.0–2.0; reduce repetición)

Objetivo: ajuste fino sin tocar código.

### TAREA 2.2: Sistema de prompt con `system` + `user`

En `OpenrouterAdapter`:

- `system prompt` para rol y límites:
  - Analista técnico interpretativo.
  - Prohibido dar asesoramiento financiero personalizado.
  - Responder en español claro y profesional.
- `user prompt` generado por `PromptBuilder` con datos estructurados.

Esto separa claramente:
- Política global de comportamiento (system).
- Contexto puntual del ticker (user).

---

## Fase 3 — Tests y calidad

1. **Unit tests de `PromptBuilder`**
   - Construcción con datos completos.
   - Construcción con campos opcionales nulos.
   - Formato esperado por secciones.

2. **Unit tests de validación/retry**
   - Respuesta válida a la primera.
   - Respuesta inválida + retry válido.
   - Respuesta inválida persistente + fallback.

3. **Actualizar tests de `ManageAnalyzeStockService`**
   - Verificar invocación al builder/validador.
   - Verificar persistencia de `valorationIA` en éxito y fallback.

4. **Tests de `OpenrouterAdapter` (configuración)**
   - Validar que usa propiedades de generación configurables.
   - Mantener cobertura de manejo de excepciones (`AIServiceException`).

---

## Fase 4 — Observabilidad y seguridad

- Logging estructurado sin exponer secretos (`OPENROUTER_API_KEY`).
- Métricas recomendadas:
  - ratio de respuestas válidas,
  - ratio de retry,
  - ratio de fallback.
- Limitar tamaño de prompt para evitar costes y respuestas truncadas.

---

## Riesgos técnicos detectados y mitigación

- **Riesgo:** Acoplar prompt al caso de uso otra vez.
  - **Mitigación:** centralizar en `PromptBuilder`.
- **Riesgo:** respuesta no parseable.
  - **Mitigación:** secciones obligatorias + retry + fallback.
- **Riesgo:** deriva a consejo de inversión.
  - **Mitigación:** restricciones explícitas en `system prompt`.

---

## Resultado esperado

Al finalizar estas fases, la integración IA quedará:

- Más mantenible (SRP / Clean Architecture).
- Más robusta ante respuestas inconsistentes.
- Más controlable vía configuración.
- Totalmente alineada con la regla del proyecto: la IA es **complemento interpretativo**, no motor de decisión.
