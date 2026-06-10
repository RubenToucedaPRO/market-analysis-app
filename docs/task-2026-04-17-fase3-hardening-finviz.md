# Fase 3 Finviz — Hardening y operación

**Fecha:** 2026-04-17  
**Rama:** `copilot/sub-pr-109-one-more-time`

## Resumen de la tarea
Se completa la fase 3 del rastreador Finviz reforzando resiliencia operativa, trazabilidad por logs y degradación controlada hacia la UI.

## Código generado (si aplica)
- `JsoupFinvizAdapter`: retry acotado configurable (`finviz.max-retries`) y logs estructurados por intento.
- `SuggestTickersService`: logs estructurados de mapeo/candidatos/descartes y degradación controlada cuando falla el screener.
- `StrategyController`: notificación de resultado parcial también cuando el caso de uso devuelve warnings operativos.
- `application.properties`: nueva propiedad `finviz.max-retries`.

## Decisiones técnicas tomadas
1. Retry simple y acotado sin backoff para minimizar cambios y mantener comportamiento predecible.
2. Degradación en Application (no en Presentation) para preservar Clean Architecture y centralizar política funcional.
3. Mensajería al usuario reutilizando `UiNotification.warning` sin introducir nuevos flujos UI.

## Cobertura de tests y pruebas añadidas si faltan
- Nuevos tests unitarios:
  - Reintento exitoso tras fallo transitorio en `JsoupFinvizAdapterTest`.
  - Degradación de `SuggestTickersService` cuando Finviz falla.
  - Notificación parcial por warnings operativos en `StrategyControllerTest`.
- Validación ejecutada:
  - Baseline previo: `mvn test` (1029 tests, verde).
  - Cambios de fase 3: tests objetivo del área modificada en verde.

## Advertencias de SonarQube o arquitectura
- Sin nuevos hotspots de seguridad en los cambios aplicados.
- Se mantiene separación hexagonal: scraping y resiliencia de red en Infrastructure; política de degradación en Application; feedback al usuario en Presentation.

## Próximos pasos sugeridos
1. Monitorizar en entorno real la frecuencia de warnings de degradación para ajustar `FINVIZ_MAX_RETRIES`.
2. Si aumenta inestabilidad de Finviz, añadir backoff incremental y métrica de latencia/errores por página.
