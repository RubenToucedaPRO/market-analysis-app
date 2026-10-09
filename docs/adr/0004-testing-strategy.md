# ADR 0004 — Estrategia de testing y calidad

- **Estado:** Aceptada (2026-10-13; suite ≈1165 tests, oct 2026).
- **Fecha:** 2026-10-13.

## Contexto
Con motor determinista + integraciones externas con cupo + UI server-side, se
necesita una pirámide que dé confianza sin depender de la red ni de claves.

## Decisión
- **Unitarios** (JUnit 5 + Mockito, sin `lenient` salvo motivo documentado):
  dominio puro, casos de uso con mocks, adapters con fixtures HTML
  (Finviz) y WebClient mockeado. Controladores con **MockMvc**.
- **Integración**: JPA contra BD real (p. ej. batch de velas), resto mockeado.
- **Puertas**: `mvn verify` debe pasar con **JaCoCo ≥80%** (check automático);
  CI (`testUnitarios-workflow.yml`: JDK 21 + `verify` + reporte de tests y
  cobertura en la PR; `docker.yml`: build+push GHCR).
- **SonarQube** Community local (`docker-compose.sonar.yml`): Quality Gate A,
  0 issues (63→0 en la tarea de oct 2026), exclusiones de cobertura reflejadas
  en el `pom` para que ambas herramientas midan lo mismo.
- Refactors de tests se verifican **caso a caso** (nº de ejecuciones
  antes/después) y ante resultados incoherentes se repite desde build limpio
  (`rm -rf target`).

## Consecuencias
- `main` siempre verde y desplegable; cada PR trae su evidencia de tests.
- Coste: mantener fixtures y el umbral 80% obliga a testar también el código
  nuevo de infraestructura, no solo dominio.
