# Fase 3 backup_suggestions: alta de sugeridos desde snapshot persistido

## Resumen de la tarea
- Se implementó la Fase 3 de `docs/backup_suggestions.md` creando un caso de uso dedicado para añadir tickers sugeridos desde el último snapshot persistido.
- El flujo deja de depender de `POST /analysis/getTickerData` para esta acción y evita llamadas a proveedores externos.

## Código generado
- Nuevo puerto de entrada:
  - `AddSuggestedTickersToAnalysisUseCase`.
- Nuevo caso de uso en Application:
  - `AddSuggestedTickersToAnalysisService`.
  - Lee el último snapshot por estrategia, filtra tickers `APTO`, crea `Stock` y su `StrategyEvaluation` asociada.
- Wiring en infraestructura:
  - `BeanConfig` expone el bean del nuevo caso de uso.
- Presentación:
  - `StrategyController` añade `POST /strategies/{id}/add-suggested-tickers`.
  - El fragmento `strategy-traceability.html` actualiza el botón “Añadir sugeridos a análisis” al nuevo endpoint.

## Decisiones técnicas tomadas
- Se usa “último snapshot” por estrategia para minimizar cambios y cumplir el alcance de Fase 3 sin introducir identificadores adicionales.
- La creación offline persiste una evaluación básica consistente (`compliant=true`, `complianceRate=100`) y resumen desde trazabilidad disponible.
- No se realizaron integraciones con `StockProviderPort` ni `HistoricalProviderPort` en este flujo.

## Cobertura de tests y pruebas añadidas
- Nuevo test unitario:
  - `AddSuggestedTickersToAnalysisServiceTest`.
- Tests de controlador actualizados:
  - `StrategyControllerTest`.
  - `StrategyControllerViewTest`.

## Advertencias de SonarQube o arquitectura
- Se mantiene arquitectura hexagonal: endpoint → caso de uso (Application) → puertos de salida.
- No se movió lógica de negocio a Thymeleaf.

## Próximos pasos sugeridos
- Si se requiere mayor fidelidad de alta offline, mapear `deterministicMetrics` a campos concretos de `Stock`/`StrategyEvaluation`.
- En Fase 4, añadir marcadores de origen (`origin=SUGGESTION_SNAPSHOT`) y flujo de reconciliación opcional.
