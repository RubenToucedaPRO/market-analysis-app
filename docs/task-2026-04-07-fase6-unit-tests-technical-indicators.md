# Fase 6 — Tests Unitarios de Indicadores Técnicos

**Fecha:** 2026-04-07  
**Rama:** `copilot/sub-pr-75`  
**PR asociado:** Sub-PR de #75

---

## Resumen

Implementación de la Fase 6 del plan de indicadores técnicos: ampliación de la cobertura de tests unitarios para todos los indicadores implementados en las Fases 2–5 (EMA, RSI, MACD, Bollinger Bands, ATR).

Las Fases 2–5 ya habían incluido tests básicos para cada indicador. Esta fase añade los tests específicos restantes descritos en `implementacion_reglas.md`:

- Verificación de convergencia de EMA con precios constantes (`shouldCalculateEma9Correctly`)
- Verificación de la semilla SMA en EMA inicial (`shouldUseSmaAsEmaInitialSeed`)
- Verificación de que MACD_LINE = EMA12 − EMA26 para precios constantes (`shouldCalculateMacdLineAsDifferenceOfEmas`)
- Verificación de que BB_UPPER > SMA20 cuando stdDev > 0 (`shouldCalculateBbUpperAboveSma`)
- Verificación de que BB_LOWER < SMA20 cuando stdDev > 0 (`shouldCalculateBbLowerBelowSma`)
- Regla de Bollinger cuando precio < BB_LOWER (sobreventa) (`shouldPassRuleWhenPriceBelowBbLower`)
- Regla de Bollinger cuando precio > BB_UPPER (sobrecompra) (`shouldPassRuleWhenPriceAboveBbUpper`)
- Fallo cuando el valor BB es nulo (`shouldReturnFailedWhenBbValueIsNull`)
- Verificación del nombre ATR en la justificación (`shouldFormatAtrNameInJustification`)

---

## Archivos Modificados

| Archivo | Tipo de cambio |
|---------|---------------|
| `src/test/java/com/market/analysis/unit/domain/service/StockHistoricalServiceTest.java` | +5 tests nuevos en `EmaCalculationTests`, `MacdCalculationTests` y `BollingerAtrCalculationTests` |
| `src/test/java/com/market/analysis/unit/domain/service/RuleEvaluatorTest.java` | +4 tests nuevos en `BollingerAtrRuleTests` |

---

## Tests Añadidos

### `StockHistoricalServiceTest`

#### En `EmaCalculationTests`

```java
@Test
@DisplayName("EMA9 should equal the constant price when all 50 prices are identical (convergence)")
void shouldCalculateEma9Correctly()
```
- Con 50 precios constantes de 100.0, EMA(9) converge a 100.0000.

```java
@Test
@DisplayName("EMA(N) seed should equal SMA(N) of the first N values when only N prices are available")
void shouldUseSmaAsEmaInitialSeed()
```
- Con exactamente 9 precios y período 9, no hay iteración adicional: EMA(9) = semilla = media aritmética de todos los precios.

#### En `MacdCalculationTests`

```java
@Test
@DisplayName("MACD_LINE should equal EMA12 minus EMA26 for constant-price series")
void shouldCalculateMacdLineAsDifferenceOfEmas()
```
- Con precios constantes, EMA12 = EMA26 = precio constante, por lo tanto MACD_LINE = 0 = EMA12 − EMA26.

#### En `BollingerAtrCalculationTests`

```java
@Test
@DisplayName("BB_UPPER should be strictly above SMA20 when prices vary (stdDev > 0)")
void shouldCalculateBbUpperAboveSma()
```
- Con precios variables (stdDev > 0), BB_UPPER > SMA20.

```java
@Test
@DisplayName("BB_LOWER should be strictly below SMA20 when prices vary (stdDev > 0)")
void shouldCalculateBbLowerBelowSma()
```
- Con precios variables (stdDev > 0), BB_LOWER < SMA20.

---

### `RuleEvaluatorTest`

#### En `BollingerAtrRuleTests`

```java
@Test
@DisplayName("Should pass rule when price is below BB_LOWER (oversold signal)")
void shouldPassRuleWhenPriceBelowBbLower()
```
- Precio (85) < BB_LOWER (90): regla `PRICE < BB_LOWER(20)` pasa.

```java
@Test
@DisplayName("Should pass rule when price is above BB_UPPER (overbought signal)")
void shouldPassRuleWhenPriceAboveBbUpper()
```
- Precio (115) > BB_UPPER (110): regla `PRICE > BB_UPPER(20)` pasa.

```java
@Test
@DisplayName("Should fail rule when BB value is null (no indicator data available)")
void shouldReturnFailedWhenBbValueIsNull()
```
- Sin datos BB en el `Stock`, la regla falla con justificación "Missing".

```java
@Test
@DisplayName("Justification should include ATR code when evaluating ATR rule")
void shouldFormatAtrNameInJustification()
```
- La justificación de una regla ATR contiene la cadena "ATR".

---

## Decisiones Técnicas

1. **Precios constantes para MACD**: Se eligieron precios constantes para `shouldCalculateMacdLineAsDifferenceOfEmas` ya que garantizan que todos los valores EMA convergen al precio constante, haciendo verificable que la diferencia es 0. Esta es la forma más directa de validar la relación EMA12 − EMA26 = MACD_LINE con la implementación interna.

2. **Precios alternantes para Bollinger**: Se usan precios que varían cíclicamente (`100 + (i % 5) * 2.0`) para garantizar stdDev > 0 en los tests de BB_UPPER > SMA20 y BB_LOWER < SMA20.

3. **Stocks independientes en RuleEvaluatorTest**: Para los tests de precio fuera de bandas, se crean instancias `Stock` específicas (precio=85 para sobreventa, precio=115 para sobrecompra) en lugar de reusar el `stockWithBbAtr` del `@BeforeEach`.

---

## Cobertura de Tests

| Clase | Tests antes de Fase 6 | Tests añadidos | Tests totales |
|-------|----------------------|----------------|---------------|
| `StockHistoricalService` (indicadores) | 38 | +5 | 43 |
| `RuleEvaluator` (indicadores) | 41 | +4 | 45 |
| **Total general** | 698 | +9 | **707** |

### Resultado `mvn test`

```
Tests run: 707, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Criterio de Completitud de la Fase 6

- [x] Todos los tests de `StockHistoricalServiceTest` pasan (incluidos los nuevos)
- [x] Todos los tests de `RuleEvaluatorTest` pasan (incluidos los nuevos)
- [x] `mvn test` sin errores en el módulo completo (707 tests, 0 fallos)
- [x] Cobertura de líneas ≥ 80% en `StockHistoricalService` y `RuleEvaluator`

---

## Próximos Pasos Sugeridos

- Registrar reglas de ejemplo en la base de datos usando los indicadores ahora cubiertos:
  - `EMA(9) > EMA(26)` — cruce alcista de medias rápidas
  - `MACD_LINE > MACD_SIGNAL` — cruce alcista MACD
  - `RSI(14) < 30` — sobreventa RSI
  - `PRICE < BB_LOWER(20)` — precio bajo banda inferior Bollinger
  - `ATR(14) < 2.0` — baja volatilidad
- Considerar tests de integración end-to-end que validen el flujo completo desde `PolygonAdapter` hasta `RuleEvaluator`.
