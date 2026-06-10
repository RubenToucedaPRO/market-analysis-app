# Fase 3 — Integración de keywords prohibidas en el flujo de análisis

## Resumen de la tarea
- Se implementa la Fase 3 definida en `docs/gestion-keywords-prohibidas.md`.
- Se extrae la lógica hardcodeada de bloqueo por keyword fuera de `CompanyProfile`.
- Se integra un matcher de dominio en `ManageAnalyzeStockService` para evaluar prohibiciones usando keywords persistidas y mantener compatibilidad funcional.

## Código generado (si aplica)
- Nuevo servicio de dominio: `ProhibitedKeywordMatcher`.
- `ManageAnalyzeStockService` ahora resuelve el motivo de prohibición mediante:
  - keywords configuradas en `ProhibitedKeywordRepository`,
  - fallback temporal a lista estática cuando no hay keywords activas en BD.
- `BeanConfig` actualizado para inyectar `ProhibitedKeywordRepository` y `ProhibitedKeywordMatcher` en el caso de uso de análisis.
- `CompanyProfile` queda desacoplado de la lista estática y conserva solo validación/obsolescencia de perfil.
- Tests:
  - Nuevo `ProhibitedKeywordMatcherTest`.
  - Ajustes en `ManageAnalyzeStockServiceTest`.
  - Ajustes en `CompanyProfileTest` al retirar lógica de prohibición del modelo.

## Decisiones técnicas tomadas
- Se mantiene Clean Architecture: la orquestación queda en Application y la regla de matching en Domain Service.
- El matcher acepta la lista de keywords desde repositorio para no acoplar el modelo `CompanyProfile` a infraestructura ni a configuración estática.
- Se implementa fallback transitorio a keywords estáticas cuando la tabla está vacía, preservando comportamiento previo durante la migración.

## Cobertura de tests y pruebas añadidas si faltan
- Validación dirigida ejecutada:
  - `mvn -Dtest=ManageAnalyzeStockServiceTest,CompanyProfileTest,ProhibitedKeywordMatcherTest test` ✅
- Resultado: `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`.

## Advertencias de SonarQube o arquitectura
- `ManageAnalyzeStockServiceTest` mantiene configuración histórica `@MockitoSettings(strictness = Strictness.LENIENT)` previa a esta tarea.
- El fallback estático es temporal y debe eliminarse al completar la fase final de migración de datos (Fase 6).

## Próximos pasos sugeridos
1. Implementar Fase 4 para gestionar/visualizar keywords en `/prohibited-tickers`.
2. Completar Fase 5 con cobertura de controlador/repositorio para flujo UI de keywords.
3. Ejecutar Fase 6 para seed inicial y retirada del fallback estático.
