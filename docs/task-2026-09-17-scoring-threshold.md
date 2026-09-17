# Task 2026-09-17 — Scoring (b): threshold en Strategy + score ponderado

## Resumen de la tarea

Segunda parte del scoring 0-100 (opción B del plan TFM). `Strategy` gana un
`threshold` (0-100, por defecto 100) y `EvaluateStrategyService` calcula un
score ponderado por peso de regla: `overallPassed = score >= threshold`.
Con el valor por defecto (100) el comportamiento es idéntico al anterior
(todas las reglas deben pasar), así que no se tocó ningún test existente.

Alcance acordado con menú: **solo comportamiento, sin cambio de BD**.
El score vive en `AnalysisResult.calculatedMetrics` (`score`, `passedWeight`,
`totalWeight`); persistirlo (`StrategyEvaluation.score` + columna) y mostrarlo
queda para la parte (c) vistas.

## Código generado

- `domain/model/Strategy.java`: campo `threshold` (`Integer` en builder,
  `null` → 100), validación 0-100 en `validateConsistency()`.
- `domain/exception/DomainErrorCodes.java`: `STRATEGY_THRESHOLD_INVALID`.
- `messages.properties`: `validation.strategy_threshold_invalid=Strategy
  threshold must be between 0 and 100`.
- `domain/service/EvaluateStrategyService.java`:
  - `calculateMetrics` añade `score` (100 · Σ pesos pasados / Σ todos,
    escala 2, `HALF_UP`), `passedWeight`, `totalWeight`.
  - `determineOverallResult(strategy, score)` → `score >= threshold`.
  - Resumen, `complianceRate` (sin ponderar, intacto) y gate del plan de
    riesgo intactos. Sin cambios de firmas públicas.

## Decisiones técnicas tomadas

- `threshold` como `int` interno con normalización `null` → 100 en el
  constructor (mismo estilo que `rules` null → lista vacía).
- Rango 0-100 inclusivo: 0 = siempre pasa, 100 = AND clásico.
- El score NO se expone en `StrategyEvaluation` todavía (decisión por menú):
  añadirlo pedía entity + mapper + columna, territorio de (c).
- Tests existentes intactos a propósito: con threshold 100 por defecto,
  toda la suite anterior debe seguir verde sin modificaciones (lo hace).

## Cobertura de tests y pruebas añadidas

- Suite completa: `Tests run: 1045, Failures: 0, Errors: 0` (1036 + 9 nuevos).
- `StrategyTest` +5: default 100, threshold explícito, -1 y 101 rechazados
  (código `validation.strategy_threshold_invalid`), límites 0/100 válidos.
- `EvaluateStrategyServiceTest` → nuevo `@Nested ThresholdScoringTests` +4:
  pesos 3(pasa)+1(falla) con threshold 75 → compliant + plan de riesgo
  calculado + `complianceRate` sigue 50.00; con 76 → no compliant + sin plan;
  redondeo 2/3 → 66.67 (66 pasa, 67 falla).
- Sin `lenient` nuevo (los stubs existentes ya lo eran y se reutilizan).

## Advertencias de SonarQube o arquitectura

- Dominio puro (sin imports de infra en `Strategy` / servicio).
- Complejidad trivial; sin cambios en vistas ni DTOs.
- `messages.properties` es el único fichero de mensajes (sin variantes por
  idioma): la clave nueva va ahí.

## Próximos pasos sugeridos

- (c) vistas: columna `strategies.threshold` + `strategy_evaluations.score`,
  campo threshold en `StrategyDTO` + formulario, mostrar score en detalle
  (recordar ALTER manual en Docker local: el contenedor ignora `./config`).
- (d) tests de scoring de extremo a extremo + `JaCoCo verify`.

## Verificación runtime

- `docker compose down && docker compose up --build -d` → app Up,
  `market-analysis-mysql` Healthy, `GET /` → 200.
- `GET /strategies/49` (con reglas de peso 1) → 200: lectura con el nuevo
  mapeo OK; al guardar reevalúa igual que antes (threshold implícito 100).

## Checklist de pruebas web

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Ve a `/strategies` → abre "Precio superior a 20" y pulsa Guardar sin
   cambiar nada → caja VERDE de éxito (comportamiento idéntico: todo vale 1
   y el threshold implícito es 100).
2. Abre otra estrategia cualquiera y haz lo mismo → también VERDE
   (ninguna evaluación existente cambió de resultado).

### B. Ideas futuras (NO hacer ahora, son las partes c/d)

- Campo threshold editable en el formulario + score visible en el detalle.
- Tests de extremo a extremo del scoring + verificación JaCoCo.
