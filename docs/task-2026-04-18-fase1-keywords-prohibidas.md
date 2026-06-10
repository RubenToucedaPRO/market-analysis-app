# Fase 1 — Modelado de dominio y persistencia de keywords prohibidas

## Resumen de la tarea
- Se implementa la Fase 1 de `docs/gestion-keywords-prohibidas.md`.
- Se añade el modelo de dominio para keywords prohibidas y su persistencia JPA/SQL desacoplada por puerto.
- Se incorpora normalización de keyword (trim + mayúsculas) en operaciones de persistencia (`save`, `exists`, `delete`) para mantener consistencia.

## Código generado (si aplica)
- `ProhibitedKeyword` (dominio).
- `ProhibitedKeywordRepository` (puerto de salida).
- `ProhibitedKeywordEntity` + `JpaProhibitedKeywordRepository` + `SqlProhibitedKeywordRepository`.
- `ProhibitedKeywordMapper`.
- Tests unitarios de mapper y repositorio SQL.

## Decisiones técnicas tomadas
- Se mantiene patrón ya usado en `ProhibitedTicker`: puerto de dominio + adapter SQL + repositorio JPA + mapper.
- Se crea tabla lógica `prohibited_keywords` mediante entidad JPA, con unicidad en `keyword`.
- Se aplica normalización consistente a `Locale.ROOT` en repositorio para garantizar búsquedas y borrados coherentes con persistencia.

## Cobertura de tests y pruebas añadidas si faltan
- Nuevos tests:
  - `ProhibitedKeywordMapperTest`
  - `SqlProhibitedKeywordRepositoryTest`
- Validación ejecutada:
  - `mvn -Dtest=ProhibitedKeywordMapperTest,SqlProhibitedKeywordRepositoryTest test` ✅

## Advertencias de SonarQube o arquitectura
- La normalización se concentra en el adapter de persistencia para mantener consistencia inmediata en Fase 1; en Fase 2 puede extraerse al caso de uso si se desea centralizar en Application.

## Próximos pasos sugeridos
1. Implementar Fase 2 (`ManageProhibitedKeywordUseCase` + servicio Application + DTO/mapper).
2. Integrar matcher de keywords en flujo de análisis (Fase 3).
3. Exponer gestión en UI lateral de `/prohibited-tickers` (Fase 4).
