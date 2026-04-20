# Fase 2 backup_suggestions: contrato mínimo por ticker para alta offline

## Resumen de la tarea
- Se implementó la Fase 2 de `docs/backup_suggestions.md` definiendo y persistiendo un contrato mínimo por ticker sugerido.
- El snapshot por ticker ahora puede almacenar datos de preparación para alta offline en análisis.

## Código generado
- Se amplió el contrato de ticker sugerido:
  - `SuggestedTickerDTO`: `strategyId`, `suggestedAt`, `deterministicMetrics`.
  - `SuggestedTickerSnapshot`: `strategyId`, `suggestedAt`, `deterministicMetrics`.
- Persistencia:
  - `SuggestedTickerSnapshotEntity`: nuevas columnas `strategy_id`, `suggested_at`, `deterministic_metrics`.
  - `SuggestionSnapshotMapper`: mapeo completo ida/vuelta de los nuevos campos.
- Orquestación:
  - `SuggestTickersService` persiste los nuevos campos por ticker **APTO** (para `NO_APTO` se mantienen nulos), conservando trazabilidad y estado.

## Decisiones técnicas tomadas
- Se mantuvo la estructura de snapshot ya existente para minimizar impacto y evitar crear un subsistema paralelo.
- `deterministicMetrics` se definió como lista opcional serializable (si no hay métricas calculadas en esta fase, se persiste vacío).
- El comportamiento funcional previo de clasificación APTO/NO_APTO no se modifica.

## Cobertura de tests y pruebas añadidas
- Tests actualizados:
  - `SuggestTickersServiceTest` (verifica persistencia de campos offline por ticker APTO y lectura del snapshot).
- Tests nuevos:
  - `SuggestionSnapshotMapperTest` (mapeo dominio↔entidad de campos Fase 2).
- Ejecución validada:
  - `mvn -Dtest=SuggestTickersServiceTest,SuggestionSnapshotMapperTest test` ✅

## Advertencias de SonarQube o arquitectura
- Se mantiene arquitectura hexagonal: dominio/aplicación desacoplados de JPA mediante mapper y repositorio.
- No se movió lógica de negocio a la capa de presentación.

## Próximos pasos sugeridos
- Fase 3: implementar caso de uso específico para alta en análisis desde snapshot sin llamadas a APIs externas.
- Definir mapeo de `deterministicMetrics` a campos concretos de `StockData` si se requiere enriquecer la creación offline.
