# Runbook operativo — Ejecución paralela de agentes IA (Finviz)

**Fecha:** 2026-04-16  
**Rama objetivo de integración:** `copilot/featureanalisis-viabilidad-transformacion`  
**Base:** `main`

## 1. Objetivo del Runbook

Estandarizar la ejecución paralela de agentes IA para implementar el rastreador activo con Finviz, minimizando conflictos de integración y preservando Arquitectura Hexagonal + Clean Architecture estricta.

## 2. Alcance

Este Runbook cubre:
- Planificación y arranque de trabajo paralelo.
- Cadencia diaria de sincronización.
- Reglas de merge por capas.
- Criterios de aceptación y bloqueo.
- Procedimiento de rollback funcional y técnico.

No cubre despliegue productivo; se centra en implementación y convergencia en repositorio.

## 3. Roles operativos

1. **Coordinador de integración (humano)**
   - Crea Fase 0 y gestiona ventanas de merge.
   - Decide bloqueos/desbloqueos.
   - Consolida PR de convergencia.

2. **Agente A (Domain)**
   - `FinvizFilterMapper` + tests unitarios.

3. **Agente B (Infrastructure)**
   - `JsoupFinvizAdapter` + tests con fixtures.

4. **Agente C (Application)**
   - `SuggestTickersUseCase` + políticas estricto/tolerante + tests.

5. **Agente D (Presentation)**
   - Acción UI, controlador, notificaciones + tests `MockMvc`.

## 4. Precondiciones

1. Compilación base estable en rama de trabajo.
2. Suite de tests verde en baseline (`mvn test`).
3. Contratos de Fase 0 definidos y aprobados.
4. Ramas creadas por stream:
   - `feature/finviz-phase0-contracts`
   - `feature/finviz-stream-a-mapper`
   - `feature/finviz-stream-b-adapter`
   - `feature/finviz-stream-c-orchestrator`
   - `feature/finviz-stream-d-ui`

## 5. Cadencia diaria recomendada

### 5.1 Ventana T0 (arranque, 15-20 min)

1. Confirmar estado de `main` y rama objetivo.
2. Confirmar contratos congelados (Fase 0).
3. Repartir prompts por stream (A/B/C/D).
4. Definir objetivo del día por PR (máximo 1 objetivo técnico por stream).

### 5.2 Ventana T1 (desarrollo paralelo, 2-4 horas)

1. Cada agente trabaja solo en su capa.
2. Se permiten stubs temporales únicamente en C y D.
3. Cada stream debe mantener tests locales en verde antes de pedir revisión.

### 5.3 Ventana T2 (sincronización intermedia, 15 min)

1. Coordinador recopila estado:
   - % avance por stream.
   - bloqueos técnicos.
   - cambios de contrato solicitados.
2. Si hay cambio de contrato, se abre mini-PR de alineación y se pausa merge cruzado.

### 5.4 Ventana T3 (cierre diario, 30-45 min)

1. Revisión rápida de PRs abiertos.
2. Merge de los PRs aptos según orden de integración.
3. Ejecución de `mvn test` tras cada merge relevante.
4. Registro diario de incidencias y riesgos para el siguiente ciclo.

## 6. Orden de integración (obligatorio)

1. PR Fase 0 (contratos compartidos).
2. Stream A (Domain mapper).
3. Stream B (Infrastructure adapter).
4. Stream C (Application orchestrator, reemplazo de stubs).
5. Stream D (Presentation + wiring final).
6. PR Fase 2 (convergencia end-to-end).
7. PR Fase 3 (hardening: observabilidad, resiliencia, Sonar).

## 7. Checklist de aceptación por PR

1. Respeta la capa asignada (sin contaminación entre Domain/Application/Infrastructure/Presentation).
2. Tests de la capa en verde.
3. Sin degradar determinismo del evaluador interno.
4. Errores controlados y mensajes de degradación claros.
5. Sin lógica de negocio en Thymeleaf.
6. Documentación técnica breve en el PR (qué, por qué, impacto).

## 8. Gestión de bloqueos

Un PR queda **bloqueado** si ocurre cualquiera de estos casos:
1. Modifica contratos sin aprobación explícita.
2. Introduce dependencias cruzadas contrarias a arquitectura.
3. Falla tests de su capa o rompe suite global.
4. Cambia lógica determinista para acomodar scraping/IA.

Resolución:
1. Etiquetar PR como `blocked`.
2. Abrir issue técnico de desbloqueo.
3. Corregir en el mismo stream o con mini-PR acordado.

## 9. Estrategia de rollback

### 9.1 Rollback funcional (preferido)

1. Desactivar entrypoint UI de sugerencias.
2. Mantener código mergeado detrás de feature toggle o flujo no expuesto.
3. Conservar contratos y tests no conflictivos.

### 9.2 Rollback técnico (si hay regresión severa)

1. Revertir PR más reciente que introduce la regresión.
2. Ejecutar `mvn test` completo.
3. Reabrir stream en nueva rama con alcance reducido.

## 10. Señales de salida (Definition of Done global)

1. Fases 0, 1, 2 y 3 completadas.
2. `mvn test` en verde tras convergencia.
3. Flujo UI ejecutable: sugerencia -> evaluación determinista -> resultado trazable.
4. Reglas no mapeables visibles para el usuario con degradación controlada.
5. Documentación final actualizada en `/docs`.

## 11. Plantilla de reporte diario

- Fecha:
- Estado Fase 0/1/2/3:
- Stream A:
- Stream B:
- Stream C:
- Stream D:
- Bloqueos abiertos:
- Riesgos nuevos:
- Decisiones tomadas:
- Próximo objetivo (24h):

## 12. Notas de arquitectura y calidad

- Mantener inyección por constructor.
- Mantener puertos en Application/Domain y adaptadores en Infrastructure.
- No usar `lenient` en Mockito salvo justificación explícita.
- Priorizar tests unitarios y `MockMvc` según capa.
- Finviz solo sugiere candidatos; la decisión final permanece en el evaluador determinista interno.
