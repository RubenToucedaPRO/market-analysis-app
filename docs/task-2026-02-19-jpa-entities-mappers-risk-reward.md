# Task: Update JPA Entities and Mappers for Risk-Reward Support

**Date:** 2026-02-19  
**Slug:** jpa-entities-mappers-risk-reward

---

## Resumen de la tarea

Actualización de las entidades JPA y los mappers de infraestructura para soportar la nueva configuración de objetivos de estrategia (`StrategyObjective`) y los campos de evaluación de riesgo/recompensa en los resultados de evaluación de estrategias.

---

## Cambios implementados

### 1. Nueva clase `StrategyObjectiveEntity` (`@Embeddable`)

Creada en `infrastructure/persistence/entity/StrategyObjectiveEntity.java`.  
Contiene los campos:

| Campo              | Tipo       | Columna                        | Restricciones                          |
|--------------------|------------|--------------------------------|----------------------------------------|
| `targetType`       | `String`   | `objective_target_type`        | nullable = true                        |
| `targetValue`      | `BigDecimal` | `objective_target_value`     | nullable = true, precision=19, scale=4 |
| `stopLossType`     | `String`   | `objective_stop_loss_type`     | nullable = true                        |
| `stopLossValue`    | `BigDecimal` | `objective_stop_loss_value`  | nullable = true, precision=19, scale=4 |
| `capitalToRisk`    | `BigDecimal` | `objective_capital_to_risk`  | nullable = true, precision=19, scale=4 |
| `description`      | `String`   | `objective_description`        | nullable = true, length=500            |

Los campos `targetType` y `stopLossType` se persisten como `String` (nombre del enum `ObjectiveType`) para desacoplar la persistencia del dominio.

### 2. `StrategyEntity` — campo `@Embedded` añadido

```java
@Embedded
private StrategyObjectiveEntity objective;
```

### 3. `StrategyEvaluationEntity` — nuevos campos de riesgo/recompensa

| Campo              | Tipo       | Columna              | Restricciones                          |
|--------------------|------------|----------------------|----------------------------------------|
| `targetPrice`      | `BigDecimal` | `target_price`     | nullable = true, precision=19, scale=4 |
| `stopLossPrice`    | `BigDecimal` | `stop_loss_price`  | nullable = true, precision=19, scale=4 |
| `riskRewardRatio`  | `BigDecimal` | `risk_reward_ratio`| nullable = true, precision=19, scale=4 |
| `recommendedShares`| `Integer`  | `recommended_shares` | nullable = true                        |

### 4. `StrategyMapper` — métodos de mapeo actualizados

- `toDomain(StrategyEntity)`: construye `StrategyObjective` a partir de `StrategyObjectiveEntity`, convirtiendo el tipo de cadena al enum `ObjectiveType`. Devuelve `null` si el embeddable es `null`.
- `toEntity(Strategy)`: crea `StrategyObjectiveEntity` desde `StrategyObjective`, serializando el enum como cadena. Devuelve `null` si el objetivo es `null`.
- Métodos privados añadidos: `toObjectiveDomain` y `toObjectiveEntity`.

### 5. `StrategyEvaluationMapper` — campos de riesgo/recompensa añadidos

- `toEntity(StrategyEvaluation, StockEntity)`: ahora mapea `targetPrice`, `stopLossPrice`, `riskRewardRatio`, `recommendedShares`.
- `toEntity(StrategyEvaluation)`: ídem.
- `toDomain(StrategyEvaluationEntity)`: ahora mapea los cuatro nuevos campos desde la entidad al modelo de dominio.

---

## Decisiones técnicas

- **`nullable = true` en todos los campos nuevos**: garantiza compatibilidad con registros existentes en la base de datos al usar `ddl-auto=update`.
- **Enum serializado como String**: evita problemas de migración si el enum cambia de ordinal y mantiene legibilidad en base de datos.
- **Sin lógica de negocio en las entidades**: las entidades son puros contenedores de datos para Hibernate.
- **Conversión de `ObjectiveType` en el mapper**: si el valor almacenado es `null`, el mapper devuelve `null` para el campo correspondiente sin lanzar excepción.

---

## Cobertura de tests

### Tests añadidos en `StrategyMapperTest`

- `testToEntityWithObjective`: verifica el mapeo de `StrategyObjective` → `StrategyObjectiveEntity`.
- `testToDomainWithObjective`: verifica el mapeo de `StrategyObjectiveEntity` → `StrategyObjective`.
- `testToDomainWithNullObjective`: verifica que un objetivo nulo en la entidad produce `null` en el dominio.
- `testToEntityWithNullObjective`: verifica que un objetivo nulo en el dominio produce `null` en la entidad.

### Tests añadidos en `StrategyEvaluationMapperTest`

- `shouldMapRiskRewardFieldsFromDomainToEntity`: verifica mapeo de los cuatro campos de riesgo/recompensa de dominio a entidad.
- `shouldMapRiskRewardFieldsFromEntityToDomain`: verifica mapeo inverso.
- `shouldHandleNullRiskRewardFields`: verifica que los campos nulos se mapean correctamente.

### Tests añadidos en `StrategyEvaluationEntityTest`

- `shouldGetAndSetRiskRewardFields`: verifica getters/setters de los cuatro nuevos campos.
- `shouldAllowNullValuesForRiskRewardFields`: verifica que los campos son nulos por defecto.

---

## Próximos pasos sugeridos

- Verificar que la aplicación arranca correctamente con `ddl-auto=update` y los nuevos campos se crean en la base de datos.
- Integrar la persistencia del `StrategyObjective` en los casos de uso de creación/actualización de estrategias.
- Integrar los campos `targetPrice`, `stopLossPrice`, `riskRewardRatio` y `recommendedShares` en el flujo de evaluación de estrategias.
