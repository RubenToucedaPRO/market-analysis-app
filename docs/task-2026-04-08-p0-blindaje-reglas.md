# P0: Blindaje mínimo para cortar fallos en runtime

Fecha: 2026-04-08
Tarea referenciada: Backlog P0 de `docs/definicion_reglas.md`

---

## 1. Título

Implementación del P0 de mejora en la definición de reglas: catálogo canónico, validación de definiciones y endurecimiento de la validación de estrategias.

---

## 2. Resumen de la tarea

Las cuatro tareas del P0 definidas en `docs/definicion_reglas.md` han sido implementadas:

1. **Catálogo canónico** (`RuleCapabilityCatalog` + `RuleCapability`): fuente de verdad única en el dominio con todos los códigos, restricciones de parámetros y operadores aceptados por `RuleEvaluator`.
2. **Validación en creación y edición de `RuleDefinition`**: `ManageRuleDefinitionService` rechaza códigos no soportados y marcas `requiresParam` inconsistentes con el catálogo.
3. **Validación en creación de estrategia**: `Rule.validate()` (llamado desde `Strategy.validateConsistency()`) valida `subjectCode`, `targetCode`, parámetros y operador antes de guardar.
4. **Tests de regresión P0**: tres nuevas clases de test con cobertura exhaustiva de casos válidos e inválidos.

---

## 3. Código generado

### `domain/model/RuleCapability.java`

Value Object que describe si un indicador requiere parámetro y qué valores son aceptados. Tres factory methods: `noParam()`, `withAllowedParams(Set<Double>)`, `anyParam()`.

### `domain/model/RuleCapabilityCatalog.java`

Catálogo estático (final, sin instancia) que registra las 18 capacidades soportadas por `RuleEvaluator`. Expone:

- `isSupported(String code)` — comprobación de código (insensible a mayúsculas)
- `getCapability(String code)` — devuelve `Optional<RuleCapability>`
- `isOperatorSupported(String operator)` — valida operadores de comparación
- `getSupportedCodes()` — conjunto completo de códigos soportados

### `domain/model/Rule.java` (modificado)

Se añade el método `validate()` que usa `RuleCapabilityCatalog` para comprobar:

- `subjectCode` soportado
- `subjectParam` aceptado por la capacidad del sujeto
- `targetCode` soportado
- `targetParam` aceptado por la capacidad del objetivo
- `operator` soportado

### `domain/model/Strategy.java` (modificado)

`validateConsistency()` llama ahora a `rule.validate()` para cada regla de la estrategia.

### `application/usecase/ManageRuleDefinitionService.java` (modificado)

Método privado `validateAgainstCatalog(RuleDefinitionDTO)` que:

1. Comprueba que el `code` exista en el catálogo.
2. Comprueba que `requiresParam` sea coherente con la capacidad catalogada.

Se invoca en `createRuleDefinition` (antes del chequeo de duplicado) y en `updateRuleDefinition`.

---

## 4. Decisiones técnicas

| Decisión | Motivo |
|---|---|
| Catálogo en capa de dominio (no infraestructura) | Sigue Clean Architecture. El dominio tiene derecho a conocer sus propias capacidades. |
| `RuleCapabilityCatalog` como clase final con métodos estáticos | Es un catálogo sin estado, no necesita instanciarse. Evita inyección innecesaria. |
| Validación en `Rule.validate()` en lugar de en el servicio | Centraliza la invariante en la entidad de dominio que la posee, siguiendo SRP. |
| `Strategy.validateConsistency()` llama `rule.validate()` | Composición jerárquica: la estrategia delega la validación de sus reglas. |
| No se modificó `RuleEvaluator` | El objetivo del P0 es bloquear entradas inválidas antes de llegar al evaluador, no refactorizar el evaluador (eso es P1/Fase 3). |

---

## 5. Cobertura de tests añadida

### `RuleCapabilityCatalogTest` (63 tests)

- Verifica que los 18 códigos soportados son reconocidos.
- Verifica que códigos externos (MACD, VWAP, STOCH...) son rechazados.
- Verifica insensibilidad a mayúsculas.
- Verifica parámetros válidos e inválidos por indicador (SMA, EMA, RSI, BB, ATR, CONSTANT, indicadores sin parámetro).
- Verifica operadores válidos e inválidos.

### `RuleValidateTest` (17 tests)

- Casos válidos: PRICE vs CONSTANT, SMA50 vs SMA200, RSI14 < 30, MACD_LINE >= MACD_SIGNAL, etc.
- Casos inválidos por código no soportado (sujeto y objetivo).
- Casos inválidos por parámetro fuera de rango (SMA 100, EMA sin parámetro, RSI 7).
- Caso inválido por parámetro en indicador sin parámetro (PRICE con param).
- Casos inválidos por operador no soportado.

### `ManageRuleDefinitionServiceP0Test` (7 tests)

- Rechaza código no soportado en creación.
- Rechaza `requiresParam` inconsistente con el catálogo en creación.
- Acepta todos los códigos sin parámetro válidos.
- Acepta SMA con `requiresParam=true`.
- Rechaza código no soportado en actualización.
- Rechaza `requiresParam` inconsistente en actualización.

### `ManageStrategyServiceP0Test` (9 tests)

- Rechaza estrategia con `subjectCode` no soportado.
- Rechaza estrategia con `targetCode` no soportado.
- Rechaza SMA con período 100 (no permitido).
- Rechaza EMA sin parámetro.
- Rechaza CONSTANT sin valor.
- Rechaza PRICE con parámetro inesperado.
- Rechaza operador `CROSS_ABOVE`.
- Acepta PRICE > SMA200.
- Acepta RSI14 < CONSTANT 30.

---

## 6. Advertencias y consideraciones

- El P0 **no migra datos existentes** en base de datos. Si hay registros `RuleDefinition` con códigos obsoletos (`MACD` sin sufijo, etc.), seguirán siendo leídos pero no podrán ser editados hasta que se corrijan. La migración es parte del P2 (tarea 10).
- Las reglas persistidas en estrategias existentes no se revalidan retroactivamente. La validación aplica solo a nuevas creaciones/actualizaciones.
- El catálogo es solo lectura y está en memoria. Cambios en `RuleEvaluator` deben reflejarse manualmente en `RuleCapabilityCatalog`.

---

## 7. Próximos pasos sugeridos

- **P1, tarea 5**: introducir `RuleCapability` como Value Object completo con operadores permitidos por indicador y roles sujeto/objetivo.
- **P1, tarea 6**: refactorizar `RuleEvaluator` para consumir el catálogo de capacidades en lugar de `switch` con literales.
- **P2, tarea 10**: script de migración para limpiar `RuleDefinition` existentes incompatibles con el catálogo.
