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

## Plan de implementación por fases (numeradas)

### Fase 1 — Diccionario de mapeo (Dominio)
**Objetivo**: traducir reglas internas a filtros Finviz.

- Crear `FinvizFilterMapper` (servicio de dominio puro o componente de dominio) con tabla de correspondencias `indicator + operator (+param)` → `finvizCode`.
- Definir estrategia de incompatibilidades:
  - regla no soportada ⇒ se reporta como `UNMAPPABLE` (no rompe ejecución).
  - combinación inválida ⇒ se omite y se notifica en resultado de traducción.
- Entregable: método `map(List<Rule>) -> FinvizFilterMappingResult` con:
  - `filters` concatenados (`ta_sma20_pa,...`)
  - `unmappableRules`
  - `warnings`

**Criterio de salida**: 100% de tests unitarios del mapper para casos soportados/no soportados.

### Fase 2 — Puerto y construcción de URL (Aplicación)
**Objetivo**: definir contrato de consulta desacoplado.

- Crear puerto `FinvizScreenerPort` (Application/Domain port out).
- Crear caso de uso/servicio de aplicación que:
  1. recibe estrategia,
  2. usa `FinvizFilterMapper`,
  3. construye URL base `https://finviz.com/screener.ashx?v=111&f=` + filtros.
- Añadir política para filtros no combinables:
  - modo estricto (falla controlada),
  - modo tolerante (continúa con subset válido).

**Criterio de salida**: tests unitarios de construcción de URL y política de tolerancia.

### Fase 3 — Adaptador de scraping (Infraestructura)
**Objetivo**: obtener tickers desde Finviz.

- Implementar `JsoupFinvizAdapter` como implementación de `FinvizScreenerPort`.
- Requisitos técnicos:
  - `User-Agent` configurable,
  - timeout y manejo de errores de red,
  - paginación (`&r=21`, `&r=41`, ...),
  - deduplicación de símbolos.
- Devolver `List<String>` de tickers limpios.

**Criterio de salida**: tests del adapter con fixtures HTML y paginación simulada.

### Fase 4 — Orquestación en caso de uso
**Objetivo**: integrar sugerencias de mercado con análisis determinista existente.

- Crear `SuggestTickersUseCase` en Application.
- Flujo:
  1. traducir estrategia → filtros,
  2. consultar screener,
  3. por cada ticker, ejecutar pipeline actual de análisis,
  4. marcar estado final (`APTO` / `NO_APTO`) usando reglas y R:R.
- Registrar razones de descarte para trazabilidad.

**Criterio de salida**: test de servicio con mocks del puerto y validación de clasificación final.

### Fase 5 — UI y feedback de usuario (Presentation)
**Objetivo**: exponer la funcionalidad al usuario final.

- En detalle de estrategia, añadir acción: **“Sugerir tickers desde mercado”**.
- Reusar `UiNotification` para feedback:
  - éxito: cantidad de sugeridos y estrategia aplicada,
  - parcial: sugeridos + no mapeables,
  - error: fallo de consulta externa.
- Mostrar resultados en la vista de análisis sin introducir lógica de negocio en Thymeleaf.

**Criterio de salida**: prueba de controlador (MockMvc) y verificación manual del flujo UI.

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
- Validación ejecutada del estado base del repositorio: `mvn test` (986 tests en verde).

## Advertencias de SonarQube o arquitectura
- No se detectan riesgos SonarQube por cambios de código, al tratarse de documentación.
- Riesgo arquitectónico a vigilar en implementación futura: evitar que scraping/HTML parsing contamine capa Application o Domain.

## Próximos pasos sugeridos
1. Abrir PR de implementación de **Fase 1** exclusivamente (mapper + tests).
2. Definir matriz inicial de equivalencias `RuleCapabilityCatalog` ↔ filtros Finviz.
3. Acordar política oficial de tratamiento de reglas no soportadas (estricto vs tolerante).
4. Ejecutar implementación incremental fase a fase con validación por PR corto.
