# Fase 5 backup_suggestions: pruebas y criterios de aceptación

## Resumen de la tarea
- Se completó la fase 5 de `docs/backup_suggestions.md` reforzando cobertura de pruebas para el flujo de snapshots de sugerencias.
- Se verificó explícitamente que el alta desde snapshot (`addFromLatestSnapshot`) no consume proveedores externos.
- Se añadió cobertura unitaria del repositorio SQL de snapshots para persistencia y lectura del último snapshot por estrategia.

## Código generado (si aplica)
- Nuevo test: `src/test/java/com/market/analysis/unit/infrastructure/persistence/repository/SqlSuggestionSnapshotRepositoryTest.java`
  - `shouldSaveSnapshot`
  - `shouldFindLatestSnapshotByStrategyId`
  - `shouldReturnEmptyWhenLatestSnapshotDoesNotExist`
- Ajuste de test existente:
  - `src/test/java/com/market/analysis/unit/application/usecase/AddSuggestedTickersToAnalysisServiceTest.java`
  - Se añadió verificación `verify(stockProviderPort, never()).getQuote(any())` en el caso de alta offline.

## Decisiones técnicas tomadas
- Se mantuvo validación en capa de Application/Infrastructure sin mover lógica a Thymeleaf.
- Se priorizaron tests unitarios con mocks para validar contratos de interacción de puertos y repositorios.

## Cobertura de tests y pruebas añadidas
- Persistencia y lectura de último snapshot por estrategia: cubierta en `SqlSuggestionSnapshotRepositoryTest`.
- Render en `detail` con fecha de sugerencia: ya cubierto por `StrategyControllerViewTest#shouldRenderTraceabilityBlockInDetail`.
- “Añadir sugeridos” sin invocar puertos externos: reforzado en `AddSuggestedTickersToAnalysisServiceTest#shouldAddAptoTickersFromLatestSnapshot`.
- Regresión APTO/NO_APTO y reglas no mapeables: mantenida en `SuggestTickersServiceTest`.

## Advertencias de SonarQube o arquitectura
- Sin incidencias adicionales detectadas en los cambios aplicados.

## Próximos pasos sugeridos
- Si se requiere mayor garantía de integración, añadir test de integración JPA/H2 para ordenación real `findTopByStrategyIdOrderBySuggestedAtDescIdDesc`.
