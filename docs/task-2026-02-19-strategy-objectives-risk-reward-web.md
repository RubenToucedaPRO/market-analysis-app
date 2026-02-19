# Configuración de Objetivos de Estrategia y Visualización de Métricas de Riesgo

## Resumen de la tarea

Se actualizó el flujo web para permitir la configuración de objetivos de riesgo/recompensa en estrategias y la visualización de las métricas resultantes en el detalle del ticker.

## Cambios realizados

### 1. Nuevo DTO: `StrategyObjectiveDTO`

**Fichero:** `src/main/java/com/market/analysis/application/dto/StrategyObjectiveDTO.java`

Nuevo DTO de transporte con los campos:
- `targetType` (String): tipo de cálculo del objetivo (SMA, PERCENTAGE, FIXED_PRICE)
- `targetValue` (BigDecimal): valor del objetivo
- `stopLossType` (String): tipo de cálculo del stop-loss
- `stopLossValue` (BigDecimal): valor del stop-loss
- `capitalToRisk` (BigDecimal): capital a arriesgar en la operación
- `description` (String): descripción del objetivo

### 2. `StrategyDTO` actualizado

**Fichero:** `src/main/java/com/market/analysis/application/dto/StrategyDTO.java`

Se añadió el campo `objective` de tipo `StrategyObjectiveDTO`, que agrupa toda la configuración de gestión de riesgo de la estrategia.

### 3. `StockDataDTO` actualizado

**Fichero:** `src/main/java/com/market/analysis/application/dto/StockDataDTO.java`

Se añadieron cuatro nuevos campos derivados de la evaluación de la estrategia:
- `targetPrice` (BigDecimal): precio objetivo calculado
- `stopLossPrice` (BigDecimal): precio de stop-loss calculado
- `riskRewardRatio` (BigDecimal): ratio riesgo/recompensa
- `recommendedShares` (Integer): número de acciones sugeridas

### 4. `StrategyDTOMapper` actualizado

**Fichero:** `src/main/java/com/market/analysis/application/mapper/StrategyDTOMapper.java`

Se añadieron los métodos privados `toObjectiveDTO` y `toObjectiveDomain` para mapear bidireccionalmente entre `StrategyObjective` (dominio) y `StrategyObjectiveDTO` (presentación), incluyendo la conversión del enum `ObjectiveType`.

### 5. `StockDataDTOMapper` actualizado

**Fichero:** `src/main/java/com/market/analysis/application/mapper/StockDataDTOMapper.java`

Se añadió el mapeo de los nuevos campos de riesgo/recompensa desde `StrategyEvaluation` hacia `StockDataDTO`.

### 6. `StrategyController` actualizado

**Fichero:** `src/main/java/com/market/analysis/presentation/controller/StrategyController.java`

En el método `showCreateForm`, se inicializa el objeto `StrategyObjectiveDTO` vacío dentro del `StrategyDTO`, permitiendo que Thymeleaf bind correctamente los campos anidados del formulario (`objective.targetType`, etc.).

### 7. `strategies/create.html` actualizado

**Fichero:** `src/main/resources/templates/strategies/create.html`

Se añadió una nueva tarjeta "Gestión de Riesgo" con:
- `<select>` para `targetType` y `stopLossType` con opciones: SMA, PERCENTAGE, FIXED_PRICE
- `<input type="number">` para `targetValue`, `stopLossValue` y `capitalToRisk` con atributos `step="0.01"`, `min="0"` y `required`
- `<input type="text">` para `description` con atributo `required`
- Nombres de campo alineados con el DTO: `name="objective.targetType"`, etc.

### 8. `analysis/ticker-detail.html` actualizado

**Fichero:** `src/main/resources/templates/analysis/ticker-detail.html`

Se añadió una tarjeta "Plan de Riesgo / Recompensa" visible únicamente cuando `ticker.evaluationPassed` es `true`, mostrando:
- Precio Objetivo
- Stop Loss
- Acciones Sugeridas
- Ratio R/R con color dinámico:
  - `.text-success` si R:R ≥ 2.0
  - `.text-warning` si R:R ≥ 1.0 y < 2.0
  - `.text-danger` si R:R < 1.0

## Decisiones técnicas

- Los campos de tipo (`targetType`, `stopLossType`) se almacenan como `String` en el DTO para facilitar el binding de Thymeleaf y se convierten a enum `ObjectiveType` en el mapper, manteniendo el dominio limpio.
- La visibilidad del bloque de riesgo se controla con `th:if="${ticker.evaluationPassed}"` para evitar mostrar datos nulos.
- Se respeta la arquitectura hexagonal: la lógica de conversión reside en el mapper de la capa de aplicación, no en el controlador ni en la vista.

## Cobertura de tests añadida

**`StrategyDTOMapperTest`:**
- `testStrategyWithObjectiveToDTOConversion`: verifica la conversión de `StrategyObjective` a `StrategyObjectiveDTO`
- `testDTOWithObjectiveToStrategyDomainConversion`: verifica la conversión inversa de DTO a dominio
- `testStrategyWithNullObjectiveToDTO`: verifica manejo de `null` en el objetivo (toDTO)
- `testDTOWithNullObjectiveToDomain`: verifica manejo de `null` en el objetivo (toDomain)

**`StockDataDTOMapperTest`:**
- `testToDTOWithRiskRewardFields`: verifica el mapeo de los 4 nuevos campos desde `StrategyEvaluation`
- `testToDTOWithNullRiskRewardWhenNoEvaluation`: verifica que los campos son `null` cuando no hay evaluación

## Próximos pasos sugeridos

- Añadir validación de server-side (`@Valid` + `BindingResult`) en `StrategyController.saveStrategy` para reforzar las validaciones HTML5.
- Considerar internacionalizar los textos de opciones del `<select>` mediante `messages.properties`.
