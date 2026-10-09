# ADR 0002 — Motor de reglas declarativo con scoring ponderado

- **Estado:** Aceptada (2026-10-13; scoring 0-100 añadido sep 2026, PRs #158-#161).
- **Fecha:** 2026-10-13.

## Contexto
Las estrategias deben definirse desde la UI sin programar, persistirse como
configuración y evaluarse de forma determinista y explicable sobre snapshots
de mercado (OHLCV + indicadores SMA/EMA/RSI/MACD/Bollinger/ATR).

## Decisión
- `Rule` (dominio): condición técnica autocontenida que devuelve `RuleResult`
  (booleano + justificación). Inmutable, validada en constructor.
- `Strategy`: composición ordenada de reglas + `threshold` (0-100). Cada regla
  tiene `weight`; el score es el porcentaje ponderado de reglas cumplidas.
- `RuleEvaluator` + `RuleCapabilityCatalog`: la evaluación se guía por el
  catálogo de capacidades (qué indicador acepta qué parámetros y roles), no
  por `switch` dispersos; `EvaluateStrategyService` calcula score y R:R.
- `RuleDefinition` persistida (catálogo administrable desde la vista
  `/rule-definitions`); mapeadores DTO↔dominio↔JPA (`RuleDTOMapper`,
  `RuleMapper`).
- Criterio lógico AND entre reglas + umbral de score por estrategia.

## Consecuencias
- Añadir un indicador = entrada de catálogo + resolver, sin tocar el evaluador.
- Toda evaluación es reproducible y auditable (qué regla falló y por qué).
- Coste: el catálogo es la única fuente de verdad y debe mantenerse
  sincronizado con el evaluador (tests P0/P1/P2 lo vigilan).
