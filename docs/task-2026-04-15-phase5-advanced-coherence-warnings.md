# Phase 5: Validaciones Avanzadas de Coherencia

## Resumen

Implementación de la Fase 5 del documento `riskreward.md`: validaciones avanzadas de coherencia que detectan y advierten (sin bloquear) sobre configuraciones de estrategia técnicamente válidas pero lógicamente improbables o riesgosas.

## Tareas Implementadas

### 1. Warning: stop-loss porcentaje > 20%
- Se advierte cuando el porcentaje de stop-loss supera el umbral orientativo de 20%.
- Implementado como constante `STOP_LOSS_WARNING_THRESHOLD` en `StrategyObjective`.

### 2. Warning: target porcentaje > 100%
- Se advierte cuando el porcentaje de target supera el umbral orientativo de 100%.
- Implementado como constante `TARGET_WARNING_THRESHOLD` en `StrategyObjective`.

### 3. Warning: misma SMA para target y stop-loss
- Se advierte cuando ambos (target y stop-loss) son de tipo SMA y usan el mismo periodo.
- Esta configuración es improbable y no generaría un plan de riesgo válido.

### 4. Warning: ratio R:R < 1.0
- Se registra un warning en logs cuando el ratio riesgo/recompensa calculado es menor a 1.0.
- Implementado en `EvaluateStrategyService` tras calcular el ratio.

## Decisiones Técnicas

- **Warnings, no excepciones**: Todas las validaciones son informativas (no bloquean). Esto permite flexibilidad para traders con perfiles de riesgo distintos.
- **Umbrales configurables**: Los umbrales (20%, 100%, 1.0) están definidos como constantes para facilitar su futura configuración.
- **`collectWarnings()` en `StrategyObjective`**: Método puro que devuelve lista inmutable de warnings. No modifica estado.
- **SLF4J en `EvaluateStrategyService`**: Se añade logger para registrar warnings. SLF4J es una API de logging aceptable en servicios de dominio.
- **Campo `riskWarnings` en `StrategyEvaluation`**: Campo transiente (no persistido) que transporta warnings hasta la capa de presentación.
- **Propagación a UI**: Se añade `riskWarnings` a `StockDataDTO` y se mapea desde `StrategyEvaluation`. Se muestra en las vistas `ticker-detail.html` y `analysis.html`.

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `StrategyObjective.java` | Método `collectWarnings()` con umbrales como constantes |
| `EvaluateStrategyService.java` | Logger SLF4J, integración de warnings, detección de R:R < 1.0 |
| `StrategyEvaluation.java` | Campo `riskWarnings` (transiente, `@Builder.Default`) |
| `StockDataDTO.java` | Campo `riskWarnings` |
| `StockDataDTOMapper.java` | Mapeo de `riskWarnings` |
| `ticker-detail.html` | Sección "Avisos de Configuración" |
| `analysis.html` | Indicador de warnings en la vista de lista |

## Tests Añadidos

### StrategyObjectiveTest (12 tests nuevos)
- Warning para stop-loss > 20%
- No warning para stop-loss = 20% exacto
- No warning para stop-loss tipo no PERCENTAGE
- Warning para target > 100%
- No warning para target = 100% exacto
- No warning para target tipo no PERCENTAGE
- Warning para misma SMA en target y stop-loss
- No warning para SMAs con periodos diferentes
- No warning para same value con tipos distintos
- Múltiples warnings simultáneos
- No warnings para estrategia válida normal
- Lista inmutable retornada

### EvaluateStrategyServiceTest (5 tests nuevos)
- Warning en riskWarnings para stop-loss alto
- Warning en riskWarnings para target alto
- Warning cuando R:R < 1.0
- Vacío cuando sin condiciones de warning
- Warnings recopilados incluso cuando estrategia no cumple reglas

## Cobertura

- 17 tests nuevos añadidos
- 1003 tests totales pasan correctamente
- Cobertura cubre todos los caminos: umbrales exactos, por encima, tipos mixtos, múltiples warnings

## Próximos Pasos Sugeridos

- Externalizar umbrales a configuración (`application.yml` o base de datos)
- Considerar persistir warnings para histórico de auditoría
- Añadir i18n para los mensajes de warning
