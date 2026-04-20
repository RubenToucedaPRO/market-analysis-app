# Fase 2 — Caso de uso de gestión de keywords prohibidas

## Resumen de la tarea
- Se implementa la Fase 2 definida en `docs/gestion-keywords-prohibidas.md`.
- Se añade el caso de uso de gestión de keywords prohibidas en capa Application/Domain (`ManageProhibitedKeywordUseCase` + `ManageProhibitedKeywordService`).
- Se incorpora DTO y mapper de Application para keywords, manteniendo separación entre dominio y transporte.

## Código generado (si aplica)
- `ManageProhibitedKeywordUseCase` en `domain.port.in`.
- `ManageProhibitedKeywordService` en `application.usecase`.
- `ProhibitedKeywordDTO` y `ProhibitedKeywordDTOMapper`.
- Wiring en `BeanConfig` para exponer el nuevo caso de uso como bean.
- Tests unitarios:
  - `ManageProhibitedKeywordServiceTest`
  - `ProhibitedKeywordDTOMapperTest`

## Decisiones técnicas tomadas
- Se sigue el patrón ya existente en el proyecto (`UseCase` de dominio + servicio de aplicación + puerto de salida).
- La validación funcional se centra en Application:
  - keyword no vacía,
  - sin duplicados tras normalización (`trim` + mayúsculas con `Locale.ROOT`),
  - longitud máxima de 100 caracteres.
- Al crear una keyword se fuerza `active=true` para mantener semántica de alta inicial activa.

## Cobertura de tests y pruebas añadidas si faltan
- Pruebas añadidas para mapper y servicio, cubriendo:
  - mapeo DTO↔dominio,
  - normalización,
  - validaciones de vacío/longitud,
  - rechazo de duplicados,
  - alta y borrado.
- Validación ejecutada:
  - `mvn -Dtest=ManageProhibitedKeywordServiceTest,ProhibitedKeywordDTOMapperTest test` ✅

## Advertencias de SonarQube o arquitectura
- Se mantiene Clean Architecture: el dominio no depende de infraestructura y la lógica de validación del caso de uso reside en Application.
- La normalización existe también en el adapter SQL de Fase 1 para consistencia defensiva; puede unificarse en fases posteriores si se decide centralizar completamente.

## Próximos pasos sugeridos
1. Ejecutar Fase 3 para integrar matcher de keywords en flujo de análisis.
2. Exponer gestión en la vista `/prohibited-tickers` (Fase 4).
3. Añadir pruebas de controlador y flujo integrado cuando se complete la UI.
