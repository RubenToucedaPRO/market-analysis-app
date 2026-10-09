# Task 2026-10-13 — ADRs (4) + Prompt Library

## Título
4 ADRs (`docs/adr/`) + Prompt Library (`docs/prompts/`, 9 patrones) +
enlaces desde README y actualización del plan.

## Resumen
- Rama `feature/adrs-prompt-library` (desde `main` post-#187, limpio).
- ADRs redactados desde el código real (verificado, no inventado):
  1. `0001-hexagonal-architecture.md` — capas, DIP, dominio sin Spring.
  2. `0002-rule-engine.md` — Rule/Strategy con weight+threshold, RuleEvaluator
     + RuleCapabilityCatalog, AND + umbral (PRs #158-#161).
  3. `0003-ai-boundaries.md` — motor determinista vs IA interpretativa,
     rotación OpenRouter, fallback, latencias asumidas.
  4. `0004-testing-strategy.md` — JUnit5/Mockito/MockMvc, JaCoCo ≥80%, CI,
     SonarQube A, verificación caso-a-caso y build limpio ante dudas.
- Prompt Library: índice + 9 patrones cortos con plantilla y ejemplo real
  del repo (workflow por rama, investigar-antes, lotes por regla, menú de
  validación, commits pequeños, build limpio, cuadrar tests, degradación
  amistosa, guard de alcance).
- `README.md`: trazabilidad enlaza `docs/adr/` y `docs/prompts/`.
- `docs/tfm-closure-plan.md`: revisión 13 Oct, fila 13 Oct hecha, cola
  renumerada (siguiente: Slides 14 Oct).

## Código generado
No aplica (solo docs). 4 ADRs + índice prompts + 9 patrones.

## Decisiones técnicas tomadas
- ADRs breves y veraces (paquetes, clases y PRs reales) en vez de
  arquitectura aspiracional: lo que el tribunal puede comprobar en el repo.
- Patrones con ejemplo real del repo, no genéricos: cada uno cita
  PR/doc donde se aplicó.

## Cobertura de tests y pruebas añadidas
- Sin cambios de código: no se añaden tests ni se ejecuta `mvn test`.
- Verificación runtime Docker omitida: solo `/docs` + 1 línea `README.md`
  (sin impacto en ejecución, excepción §3.7 — se indica aquí).

## Advertencias de SonarQube o arquitectura
- Ninguna: documentación.

## Próximos pasos sugeridos
1. Validar esta PR con menú (la tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED (14 Oct): **Slides Defensa** 12 + speaker notes.
