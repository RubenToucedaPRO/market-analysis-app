# Fase 1 — Preparación del Modelo de Dominio y Persistencia

## Resumen

Implementación de la **Fase 1** del plan de nuevos indicadores técnicos (EMA, MACD, RSI, Bollinger Bands, ATR) definido en `implementacion_reglas.md`. Esta fase prepara el modelo de dominio y la capa de persistencia para alojar los nuevos indicadores **sin implementar aún los cálculos** (Fase 2). Al finalizar, el sistema compila y todos los tests existentes siguen pasando.

**Fecha:** 2026-04-07  
**Rama:** `copilot/sub-pr-75`  
**Basado en:** `implementacion_reglas.md` §Fase 1

---

## Objetivo

Añadir los 14 campos de nuevos indicadores a:
1. `TechnicalIndicators.java` — modelo carrier del dominio
2. `Stock.java` — modelo de dominio evaluado por `RuleEvaluator`
3. `StockEntity.java` — entidad JPA (columnas `DECIMAL(19,4) NULL`)
4. `StockMapper.java` — mapeo bidireccional `toEntity` / `toDomain`
5. `ManageAnalyzeStockService.java` — propagación `TechnicalIndicators → Stock`

---

## Campos Añadidos

| Campo | Grupo | Descripción |
|-------|-------|-------------|
| `ema9`, `ema12`, `ema20`, `ema26`, `ema50`, `ema200` | EMA | Medias móviles exponenciales |
| `rsi14`, `rsi30` | RSI | Índice de fuerza relativa |
| `macdLine`, `macdSignal`, `macdHistogram` | MACD | Convergencia/divergencia de medias |
| `bbUpper20`, `bbLower20` | Bollinger Bands | Bandas de volatilidad período 20 |
| `atr14` | ATR | Rango verdadero medio período 14 |

Todos los campos son `BigDecimal` nullable (pueden ser `null` si no hay suficientes datos históricos para el cálculo).

---

## Código Generado

### `TechnicalIndicators.java`

```java
// EMA
BigDecimal ema9;
BigDecimal ema12;
BigDecimal ema20;
BigDecimal ema26;
BigDecimal ema50;
BigDecimal ema200;

// RSI
BigDecimal rsi14;
BigDecimal rsi30;

// MACD (derived from EMA)
BigDecimal macdLine;      // EMA(12) - EMA(26)
BigDecimal macdSignal;    // EMA(9) of MACD line
BigDecimal macdHistogram; // macdLine - macdSignal

// Bollinger Bands (period 20)
BigDecimal bbUpper20;     // SMA20 + 2*StdDev20
BigDecimal bbLower20;     // SMA20 - 2*StdDev20

// ATR
BigDecimal atr14;
```

### `Stock.java`

Idénticos campos con anotaciones Lombok existentes (`@Getter`, `@Setter`, `@Builder`). Estos campos son los que leerá `RuleEvaluator` en las fases siguientes.

### `StockEntity.java`

Columnas JPA añadidas con `@Column(precision = 19, scale = 4)` nullable. Las columnas de MACD y Bollinger Bands usan `name` explícito para seguir la convención `snake_case` de la BD:

```java
@Column(name = "macd_line", precision = 19, scale = 4)
@Column(name = "macd_signal", precision = 19, scale = 4)
@Column(name = "macd_hist", precision = 19, scale = 4)
@Column(name = "bb_upper20", precision = 19, scale = 4)
@Column(name = "bb_lower20", precision = 19, scale = 4)
```

Para H2 (dev), `ddl-auto=update` añade las columnas automáticamente. Para MariaDB (prod), aplicar la migración SQL del `implementacion_reglas.md` §1.3.

### `StockMapper.java`

Añadidos en `toEntity()` y `toDomain()` los setters/getters para todos los nuevos campos, siguiendo el mismo patrón que los campos existentes (`sma20`, `sma50`, `sma200`).

### `ManageAnalyzeStockService.java`

Propagación de los nuevos indicadores de `TechnicalIndicators` a `Stock` en el método `getdataFromProvider()`, siguiendo el mismo patrón que SMA y volume:

```java
stock.setEma9(technicalIndicators.getEma9());
// ... (todos los campos)
stock.setAtr14(technicalIndicators.getAtr14());
```

---

## Decisiones Técnicas

1. **Campos `null` por defecto**: Los nuevos campos en `TechnicalIndicators` son `null` hasta que la Fase 2 implemente los cálculos en `StockHistoricalService`. Esto es correcto: `RuleEvaluator` debe manejar `null` de forma segura en los `case` que añada la Fase 3.

2. **`@Column(precision = 19, scale = 4)`**: Consistente con la precisión necesaria para indicadores financieros y con el `SMA_SCALE = 4` usado en `StockHistoricalService`.

3. **Sin cambios en `RuleEvaluator`**: Los nuevos indicadores no son evaluables aún (sin cálculo). Los `case` en `getIndicatorValue()` se añadirán en la Fase 3 del plan.

4. **Sin ruptura de arquitectura**: La propagación sigue el flujo `PolygonAdapter → HistoricalData → StockHistoricalService → TechnicalIndicators → ManageAnalyzeStockService → Stock`, consistente con la arquitectura hexagonal.

---

## Cobertura de Tests

### Tests añadidos

**`StockMapperTest`** (+3 tests):
- `testToEntityWithNewIndicatorFields` — verifica que todos los campos nuevos se mapean correctamente de `Stock` a `StockEntity`
- `testToDomainWithNewIndicatorFields` — verifica el mapeo inverso `StockEntity → Stock`
- `testToDomainWithNullNewIndicatorFields` — verifica que los campos son `null` cuando no están establecidos

**`StockHistoricalServiceTest`** (+2 tests, nuevo `@Nested` "Phase 1 — New Indicator Fields"):
- `testNewIndicatorFieldsAreNullBeforePhase2` — documenta que los nuevos campos son `null` (cálculo no implementado aún)
- `testTechnicalIndicatorsBuilderSupportsNewFields` — verifica que el builder de `TechnicalIndicators` soporta todos los campos nuevos

### Resultado final
```
Tests run: 639, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```
(+5 tests respecto a los 634 tests previos)

---

## Advertencias de Arquitectura

- ⚠️ Los campos del `RuleEvaluator` para los nuevos indicadores (`RSI`, `EMA`, `MACD_LINE`, etc.) se añadirán en la **Fase 3** para evitar `NullPointerException` mientras el cálculo no esté implementado.
- ⚠️ En MariaDB (prod) se requiere ejecutar la migración SQL manual o con Flyway/Liquibase (ver `implementacion_reglas.md` §1.3).

---

## Próximos Pasos

- **Fase 2**: Implementar cálculos en `StockHistoricalService.calculateIndicators()` — `calculateEma()`, `calculateRsi()`, `calculateMacd()`, `calculateBollingerBands()`, `calculateAtr()` con inversión de lista (`Collections.reverse()` sobre copia) para respetar el orden cronológico de datos Polygon (`sort=desc`).
- **Fase 3**: Ampliar `RuleEvaluator.getIndicatorValue()` con los nuevos `case`.
- **Fase 4**: Tests unitarios de cálculo con valores conocidos para cada indicador.
