# Análisis de viabilidad — Rastreador activo de tickers con Finviz

**Fecha:** 2026-04-16  
**Rama de trabajo:** `copilot/featureanalisis-viabilidad-transformacion`  
**Base:** `main`

## Resumen de la tarea
Se realiza el análisis de viabilidad y el plan de aplicación por etapas numeradas para transformar el flujo actual (ingesta manual de tickers) en un rastreador activo que traduzca reglas internas (`RuleDefinition`) a filtros de Finviz, extraiga tickers y los inyecte en el análisis determinista ya existente.

## Código generado (si aplica)
No se generan cambios de código funcional en esta tarea. El entregable es de análisis técnico y planificación incremental.

## Análisis de viabilidad

### Viabilidad técnica: **Alta con restricciones controlables**
1. **Alineación arquitectónica**: Encaja en Arquitectura Hexagonal separando mapeo (Domain), orquestación (Application) y scraping (Infrastructure).
2. **Reutilización del núcleo actual**: El análisis determinista (R:R, validación de reglas, clasificación) ya existe y solo requiere un punto de entrada para tickers sugeridos.
3. **Riesgo principal**: Cobertura incompleta entre catálogo de reglas internas y filtros disponibles en Finviz gratuito.
4. **Riesgo secundario**: Fragilidad del scraping por cambios HTML o bloqueos por rate-limit/User-Agent.
5. **Mitigación**: Diseño tolerante a reglas no mapeables, observabilidad de filtros descartados, tests de contrato del mapper y adapter con HTML de referencia.

### Viabilidad funcional: **Media-Alta**
- El flujo aporta valor inmediato (descubrimiento de oportunidades) sin alterar decisiones deterministas.
- Requiere UX clara para distinguir: “ticker sugerido por screener” vs “ticker validado por estrategia”.

### Viabilidad operativa: **Media**
- Dependencia externa (Finviz) sin API oficial para esta modalidad.
- Necesario control de frecuencia y fallback cuando Finviz no responda o cambie estructura.

## Plan de implementación para desarrollo en paralelo (agentes IA)

Se redefine el plan en **frentes paralelos** para permitir ejecución simultánea con bajo acoplamiento y puntos de integración explícitos.

## Fase 0 — Contratos y baseline común (secuencial, corta)
**Objetivo**: fijar interfaces y convenciones mínimas para desbloquear trabajo paralelo seguro.

**Estado:** ✅ Implementada en esta rama con contratos base y tests mínimos de compilación/instanciación.

- Contratos creados:
  - `domain/model/FinvizFilterMappingResult`
  - `domain/port/out/FinvizScreenerPort`
  - `domain/port/in/SuggestTickersUseCase`
  - DTOs de soporte en `application/dto` para request/response, modo de ejecución y estado funcional.

- Definir contratos base: `FinvizFilterMappingResult`, `FinvizScreenerPort`, `SuggestTickersUseCase` (firma y DTOs).
- Acordar políticas globales:
  - regla no soportada => `UNMAPPABLE` con warning.
  - modo de ejecución por defecto => tolerante.
- Congelar nomenclatura de estados funcionales (`APTO`, `NO_APTO`) y campos de trazabilidad.

**Criterio de salida**: PR corto de contratos + tests mínimos de compilación/instanciación. A partir de aquí se habilita paralelismo.

## Fase 1 — Desarrollo paralelo por streams

### Stream A (Dominio) — Mapper de reglas a filtros Finviz
**Agente recomendado**: Agente A.

- Implementar `FinvizFilterMapper` en Domain.
- Construir matriz `indicator + operator (+param)` => `finvizCode`.
- Soportar salida estructurada:
  - `filters` concatenados,
  - `unmappableRules`,
  - `warnings`.
- Incluir tests unitarios exhaustivos de mapeo soportado/no soportado.

**Dependencias de entrada**: Fase 0.
**Artefactos de salida**: servicio de dominio + tests.

### Stream B (Infraestructura) — Adapter de scraping Finviz
**Agente recomendado**: Agente B.

- Implementar `JsoupFinvizAdapter` como `FinvizScreenerPort`.
- Incorporar `User-Agent` configurable, timeout, manejo de error de red.
- Añadir paginación (`&r=21`, `&r=41`, ...) y deduplicación.
- Tests con fixtures HTML y simulación de paginado/cambios de estructura.

**Dependencias de entrada**: Fase 0.
**Artefactos de salida**: adapter + tests de robustez.

### Stream C (Aplicación) — Orquestador y políticas de ejecución
**Agente recomendado**: Agente C.

- Implementar servicio de aplicación que:
  1. reciba estrategia,
  2. obtenga filtros desde mapper,
  3. consulte screener por puerto,
  4. ejecute pipeline determinista por ticker,
  5. clasifique `APTO` / `NO_APTO` y razones.
- Implementar política estricto/tolerante para filtros incompatibles.
- Tests de caso de uso con mocks para mapper, puerto y evaluador actual.

**Dependencias de entrada**: Fase 0.
**Dependencias blandas**: puede arrancar con stubs de Stream A y B.
**Artefactos de salida**: caso de uso orquestador + tests de integración de aplicación.

### Stream D (Presentation) — UI y feedback
**Agente recomendado**: Agente D.

- Añadir acción “Sugerir tickers desde mercado” en detalle de estrategia.
- Reusar `UiNotification` para éxito/parcial/error.
- Mostrar resultados y trazabilidad sin lógica de negocio en Thymeleaf.
- Añadir tests `MockMvc` de controlador y render mínimo de vista.

**Dependencias de entrada**: Fase 0.
**Dependencias blandas**: puede iniciar con DTOs mock de Stream C.
**Artefactos de salida**: endpoint/controlador/vista + tests web.

## Fase 2 — Integración cruzada (secuencial de convergencia)
**Objetivo**: unir streams sin regresiones.

- Rebase/merge ordenado de A + B + C + D.
- Sustituir stubs por implementaciones reales.
- Ejecutar batería completa: unit + integración + MockMvc.
- Revisar contract tests en fronteras:
  - Domain -> Application,
  - Application -> Port,
  - Infrastructure -> HTML parsing,
  - Presentation -> Application.

**Criterio de salida**: `mvn test` en verde + validación funcional end-to-end.

## Fase 3 — Hardening y operación
**Objetivo**: dejar lista la capacidad para uso real controlado.

- Logging estructurado de filtros aplicados/no mapeables y causa de descarte.
- Controles operativos: timeout, retry acotado, degradación con mensaje de usuario.
- Revisión SonarQube (hotspots y code smells relevantes).
- Documentación final de comportamiento y límites.

**Criterio de salida**: checklist de operación completado + documentación de límites de Finviz.

## Matriz rápida de paralelización

1. **Arranque común**: Fase 0 (1 PR).
2. **Paralelo real**: Streams A, B, C y D en ramas separadas (4 PRs).
3. **Convergencia**: Fase 2 (1 PR integradora).
4. **Estabilización**: Fase 3 (1 PR de hardening).

## Reglas de coordinación entre agentes

1. Cada stream mantiene su capa (sin invadir Domain/Application/Infrastructure/Presentation ajenas).
2. Cambios de contrato solo vía PR de Fase 0 o mini-PR de alineación aprobado por todos.
3. Ningún stream mergea sin tests propios en verde.
4. La decisión final de aptitud permanece determinista; Finviz solo propone candidatos.

## Ejecución operativa en paralelo (lista para lanzar)

### Preparación de ramas

1. Crear rama base de contratos: `feature/finviz-phase0-contracts`.
2. Tras merge de Fase 0, crear ramas de trabajo:
  - `feature/finviz-stream-a-mapper`
  - `feature/finviz-stream-b-adapter`
  - `feature/finviz-stream-c-orchestrator`
  - `feature/finviz-stream-d-ui`

### Prompt sugerido para Agente A (Dominio)

"Implementa Stream A en Arquitectura Hexagonal estricta:
0) Crea una rama `feature/finviz-stream-a-mapper` y su correspondiente PR.
1) crear `FinvizFilterMapper` en Domain,
2) mapear `indicator + operator (+param)` a código Finviz,
3) devolver `FinvizFilterMappingResult` con `filters`, `unmappableRules` y `warnings`,
4) no usar dependencias de infraestructura,
5) añadir tests unitarios completos para casos soportados/no soportados,
6) mantener compatibilidad con el evaluador determinista actual.
Entregable: PR pequeño, enfocado y con tests en verde." 

### Prompt sugerido para Agente B (Infraestructura)

"Implementa Stream B en Infrastructure:
0) Crea una rama `feature/finviz-stream-b-adapter` y su correspondiente PR.
1) crear `JsoupFinvizAdapter` como implementación de `FinvizScreenerPort`,
2) configurar `User-Agent`, timeout y gestión de errores de red,
3) soportar paginación con `&r=21`, `&r=41`, etc.,
4) deduplicar símbolos,
5) añadir tests con fixtures HTML y escenarios de cambios de estructura,
6) no introducir lógica de negocio fuera de Infrastructure.
Entregable: PR de adapter + tests de robustez." 

### Prompt sugerido para Agente C (Aplicación)

"Implementa Stream C en Application:
0) Crea una rama `feature/finviz-stream-c-orchestrator` y su correspondiente PR.
1) orquestar estrategia -> mapper -> screener -> pipeline determinista,
2) crear/usar `SuggestTickersUseCase` para clasificar `APTO`/`NO_APTO`,
3) incluir razones de descarte y trazabilidad,
4) soportar modo estricto y tolerante para incompatibilidades,
5) testear con mocks de mapper, puerto y evaluador interno,
6) no acoplar Application a parsing HTML ni detalles de infraestructura.
Entregable: PR del caso de uso + tests de servicio." 

### Prompt sugerido para Agente D (Presentation)

"Implementa Stream D en Presentation:
0) Crea una rama `feature/finviz-stream-d-ui` y su correspondiente PR.
1) añadir acción 'Sugerir tickers desde mercado' en detalle de estrategia,
2) conectar con caso de uso sin mover lógica de negocio a Thymeleaf,
3) usar `UiNotification` para éxito/parcial/error,
4) mostrar trazabilidad mínima (sugeridos, descartes, no mapeables),
5) añadir tests `MockMvc` y validación de render.
Entregable: PR web con controlador, vista y tests." 

### Orden recomendado de integración (merge)

1. Merge PR Fase 0 (contratos compartidos).
2. Merge Stream A (Domain mapper).
3. Merge Stream B (Infrastructure adapter).
4. Merge Stream C (Application orchestrator) con reemplazo de stubs.
5. Merge Stream D (Presentation) ajustando wiring final.
6. Abrir PR de convergencia (Fase 2) para validación end-to-end.
7. Abrir PR de hardening (Fase 3) con observabilidad y resiliencia.

### Criterios de aceptación por PR (checklist corto)

1. Respeta capa y dependencias de Arquitectura Hexagonal.
2. Tests de la capa modificada en verde.
3. Sin uso de lógica determinista fuera del núcleo existente.
4. Mensajería de errores controlada y sin filtrar detalles internos.
5. Documentación breve de decisiones en el propio PR.

## Dependencias, riesgos y controles
1. **Cobertura de mapeo**: priorizar indicadores ya presentes en `RuleCapabilityCatalog`.
2. **Cambios HTML de Finviz**: encapsular selectores en adapter + tests con fixtures.
3. **Rate limiting / bloqueos**: timeout, retries acotados y mensajes de degradación controlada.
4. **Consistencia determinista**: Finviz solo sugiere candidatos; la decisión final sigue en el evaluador interno.

## Decisiones técnicas tomadas
- Implementación por fases verticales para reducir riesgo y facilitar PRs pequeños.
- Separación estricta por capas para mantener Clean Architecture.
- Política explícita de reglas no mapeables para evitar falsas expectativas funcionales.

## Cobertura de tests y pruebas añadidas
- En esta tarea no se añade código productivo; no se crean tests nuevos.
- Validación ejecutada del estado base del repositorio: `mvn test` con **suite completa en verde** (snapshot de esta fecha: 2026-04-16).

## Advertencias de SonarQube o arquitectura
- No se detectan riesgos SonarQube por cambios de código, al tratarse de documentación.
- Riesgo arquitectónico a vigilar en implementación futura: evitar que scraping/HTML parsing contamine capa Application o Domain.

## Próximos pasos sugeridos
1. Abrir PR de **Fase 0** para fijar contratos compartidos (bloqueante corto).
2. Lanzar en paralelo 4 agentes con ramas separadas para Streams A, B, C y D.
3. Ejecutar PR de convergencia (Fase 2) con sustitución de stubs y validación end-to-end.
4. Cerrar con PR de hardening operacional (Fase 3) y revisión SonarQube.

## Referencia operativa

- Runbook de ejecución paralelo: `docs/task-2026-04-16-runbook-paralelizacion-agentes-finviz.md`.
