# Task 2026-09-17 — Scoring (c): vistas (threshold, pesos y score)

## Resumen de la tarea

Tercera parte del scoring 0-100 (opción B del plan TFM). El scoring ya es
visible y editable desde la web:
- Formulario de estrategia: campo numérico `threshold` (0-100, por defecto
  100) + campo `weight` (≥1, por defecto 1) por regla.
- Detalle de estrategia: muestra threshold y peso de cada regla.
- Vistas de análisis (`analysis.html`, `ticker-detail.html`): muestran el
  `score` ponderado junto al `complianceRate` (solo si no es nulo: las
  evaluaciones históricas anteriores al scoring no tienen).
- Persistencia: columnas `strategies.threshold` (DEFAULT 100) y
  `strategy_evaluations.score` (nullable), con `script-bd.sql` actualizado.

## Código generado

- `StrategyEntity.threshold` + `StrategyMapper` (ambos sentidos; `null`→100).
- `StrategyEvaluation.score` (dominio) + `StrategyEvaluationEntity.score`
  (nullable, precision 5 scale 2) + `StrategyEvaluationMapper` (3 métodos).
- `EvaluateStrategyService`: `.score(score)` en el `StrategyEvaluation`.
- `StrategyDTO.threshold` + `StrategyDTOMapper`; `RuleDTO.weight` +
  `RuleDTOMapper` (con guard `null`→1, ver Decisiones).
- `StockDataDTO.score` + `StockDataDTOMapper` (alimenta las vistas ticker).
- `StrategyController.showCreateForm`: threshold 100 por defecto.
- Plantillas: `strategies/create.html` (input threshold),
  `fragments/rule-row.html` (input peso, clonado por `strategy-manager.js`
  para filas nuevas), `strategies/detail.html` (threshold + badge de peso),
  `analysis/analysis.html` y `analysis/ticker-detail.html` (score con
  `th:if` anti-nulos).
- `messages.properties`: `strategy.form.threshold`,
  `strategy.detail.threshold`, `strategy.detail.rule_weight`, `rule.weight`,
  `ticker.detail.score`, `analysis.score`.

## Decisiones técnicas tomadas

- **Botón eliminar sin efecto en filas nuevas (hallazgo reportado)**: los
  listeners de `data-action="remove-rule"` se ataban una sola vez al cargar
  la página, así que las filas añadidas con "Añadir Regla" nunca tenían
  handler (bug preexistente, no de esta tarea). Fix en
  `static/js/strategy-manager.js`: listener delegado en `#rules-container`
  que cubre filas presentes y futuras. `node --check` OK; suite 1048/1048.

- **NPE por unboxing (hallazgo)**: `RuleDTOMapper.toDomain` pasaba
  `Integer` nulo al builder de `Rule` (`int` primitivo con
  `@Builder.Default`). Guard `null`→1 en el mapper, igual que ya hacía el
  `RuleMapper` de infraestructura en la parte (a). 3 tests fallaban; tras el
  fix, 1048/1048.
- `score` nullable (no DEFAULT): las evaluaciones históricas no tienen score
  y las vistas lo ocultan con `th:if`. `threshold` sí con DEFAULT 100 porque
  equivale al AND clásico.
- Sin cambios de firmas públicas ni de lógica de evaluación; el controlador
  no necesitó cambios (binding `@ModelAttribute` automático).

## Cobertura de tests y pruebas añadidas

- Suite completa: `Tests run: 1048, Failures: 0, Errors: 0` (1045 + 3 nuevos).
- `RuleDTOMapperTest`: peso en ambos sentidos + default 1 con peso nulo.
- `StrategyDTOMapperTest`: threshold en ambos sentidos + default 100 nulo.
- `StrategyMapperTest`: threshold en ambos sentidos + `null`→100 (legacy).
- `StrategyEvaluationMapperTest`: score en ambos sentidos.
- `StockDataDTOMapperTest`: score mapeado.
- `EvaluateStrategyServiceTest`: el caso threshold-75 ahora afirma
  `score == 75.00`.
- Sin `lenient` nuevo.

## Advertencias de SonarQube o arquitectura

- DTOs solo transporte; validación sigue en dominio (`validateConsistency`).
- `th:text` en todo lo nuevo; expresiones SpEL cortas; i18n sin hardcode.
- Sin lógica de negocio en plantillas (solo `th:if` de presentación).

## Próximos pasos sugeridos

- (d) tests de scoring de extremo a extremo + `JaCoCo verify`.
- Railway (Día 3): la BD se crea desde `script-bd.sql` (ya lleva las columnas).

## Verificación runtime

- `docker compose down && docker compose up --build -d` → app Up,
  `market-analysis-mysql` Healthy, `GET /` → 200.
- Como el contenedor ignora `./config` (hallazgo parte a), columnas creadas
  a mano en el MySQL local:
  `ALTER TABLE strategies ADD COLUMN threshold INT DEFAULT 100;`
  `ALTER TABLE strategy_evaluations ADD COLUMN score DECIMAL(5,2) NULL;`
- `GET /strategies/49` → 200 (muestra threshold 100% y pesos 1).

## Checklist de pruebas web

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Ve a `/strategies/49` → bajo la descripción ves "Umbral de Aprobación:
   100%" y en cada regla un badge "Peso: 1".
2. Pulsa "Sugerir tickers desde mercado" (o abre cualquier ticker evaluado
   en `/analysis`) → junto a "Tasa de Cumplimiento" ves "Puntuación
   Ponderada" con el mismo valor (todos los pesos valen 1).
3. Ve a `/strategies/new` → el formulario trae "Umbral de Aprobación
   (0-100)" con 100 y cada regla un campo "Peso de la Regla" con 1; pulsa
   "Añadir Regla Condicional" → la fila nueva también trae peso 1.
4. En ese mismo formulario, pulsa la papelera de una fila → la fila
   desaparece (incluidas las recién añadidas); si borras todas, aparece una
   fila vacía nueva automáticamente.

### B. Ideas futuras (NO hacer ahora, es la parte d)

- Tests de extremo a extremo del scoring + verificación JaCoCo.
