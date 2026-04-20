# Fase 6 — Migración de datos y despliegue controlado de keywords prohibidas

## Resumen de la tarea
- Se implementó la fase 6 del plan `docs/gestion-keywords-prohibidas.md`.
- Se añadió un seed de arranque para cargar en BD los keywords históricos que estaban en fallback estático.
- Se eliminó el fallback hardcodeado del matcher para que la detección dependa solo de la configuración persistida.

## Código generado (si aplica)
- `src/main/java/com/market/analysis/infrastructure/migration/ProhibitedKeywordSeedRunner.java`
  - Runner idempotente de arranque que carga 16 keywords por defecto solo cuando la tabla está vacía.
- `src/main/java/com/market/analysis/domain/service/ProhibitedKeywordMatcher.java`
  - Eliminado fallback estático; ahora solo evalúa keywords activas configuradas.
- `src/test/java/com/market/analysis/unit/infrastructure/migration/ProhibitedKeywordSeedRunnerTest.java`
  - Pruebas de seed en tabla vacía y omisión cuando ya hay datos.
- `src/test/java/com/market/analysis/unit/domain/service/ProhibitedKeywordMatcherTest.java`
  - Ajuste de expectativa: sin keywords configuradas, el matcher devuelve `null`.

## Decisiones técnicas tomadas
- Se usó `CommandLineRunner` en infraestructura para mantener la migración de datos fuera del dominio y garantizar ejecución automática al arrancar.
- El seed se diseñó idempotente (solo tabla vacía) para no sobrescribir gestión manual desde UI.
- Se conservó la lista histórica exacta del fallback para mantener paridad funcional.

## Cobertura de tests y pruebas añadidas si faltan
- Validación base previa: `mvn test` en verde (`Tests run: 1053, Failures: 0, Errors: 0, Skipped: 0`).
- Pruebas añadidas/ajustadas:
  - `ProhibitedKeywordSeedRunnerTest`
  - `ProhibitedKeywordMatcherTest` (caso sin fallback).
- Validación focal tras cambios:
  - `mvn -Dtest=ProhibitedKeywordSeedRunnerTest,ProhibitedKeywordMatcherTest,ManageAnalyzeStockServiceTest test`

## Advertencias de SonarQube o arquitectura
- No se introduce lógica de negocio en vistas ni en infraestructura de persistencia.
- La detección de bloqueo sigue siendo determinista y desacoplada del frontend.

## Operación recomendada (fase 6)
1. Arrancar la aplicación en entorno sin datos de `prohibited_keywords`; el seed carga keywords por defecto automáticamente.
2. Revisar/ajustar keywords desde `/prohibited-tickers` (panel lateral) usando alta y borrado.
3. Mantener keywords específicas y evitar términos excesivamente genéricos para reducir falsos positivos.

## Próximos pasos sugeridos
1. Validar en staging/prod que la tabla no queda vacía tras despliegues.
2. Añadir monitoreo operativo para alertar si `prohibited_keywords` queda sin registros activos.
