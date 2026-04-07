# Plan de Implementación — Nuevos Indicadores Técnicos

**Indicadores a implementar:** EMA, MACD, RSI, Bollinger Bands, ATR  
**Basado en análisis:** `reglas.md`  
**Fecha del plan:** 2026-04-07

---

## Visión General

Todos los datos necesarios están disponibles en `HistoricalData` (300 velas OHLCV de Polygon). La implementación sigue siempre el mismo flujo de capas:

```
PolygonAdapter (infraestructura)
    → HistoricalData (modelo dominio)
        → StockHistoricalService.calculateIndicators() (cálculo, dominio)
            → TechnicalIndicators (carrier DTO dominio)
                → ManageAnalyzeStockService (propagación, aplicación)
                    → Stock (modelo dominio)
                        → RuleEvaluator (evaluación, dominio)
```

Ninguna fase rompe el código existente. Cada fase es independientemente compilable, testeable y desplegable.

---

## ⚠️ Restricción Crítica de Ordenación de Datos

> Polygon devuelve los datos con **`sort=desc`** → `closingPrices.get(0)` = precio **más reciente**.  
> Los algoritmos iterativos (EMA, RSI, ATR) necesitan procesar los datos **de más antiguo a más reciente**.  
> Solución estándar: invertir la lista al inicio del método de cálculo con `Collections.reverse()` sobre una copia.

---

## Fase 1 — Preparación del Modelo de Dominio y Persistencia

**Objetivo:** Añadir los campos necesarios a `TechnicalIndicators`, `Stock` y `StockEntity` antes de implementar ningún cálculo. Al finalizar esta fase el sistema compila y los tests existentes siguen pasando.

### 1.1 `TechnicalIndicators.java` — Nuevos campos

Añadir los campos `BigDecimal` para los 5 grupos de indicadores:

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

// MACD (derivado de EMA)
BigDecimal macdLine;       // EMA(12) - EMA(26)
BigDecimal macdSignal;     // EMA(9) del MACD line
BigDecimal macdHistogram;  // macdLine - macdSignal

// Bollinger Bands (período 20)
BigDecimal bbUpper20;      // SMA20 + 2*StdDev20
BigDecimal bbLower20;      // SMA20 - 2*StdDev20

// ATR
BigDecimal atr14;
```

### 1.2 `Stock.java` — Mismos campos

Idénticos campos `BigDecimal` con anotaciones Lombok existentes (`@Getter`, `@Setter`). Estos campos son los que lee `RuleEvaluator`.

### 1.3 `StockEntity.java` + migración de base de datos

Añadir columnas `DECIMAL(19,4) NULL` a la tabla `stocks` para todos los nuevos indicadores. Las columnas son **opcionales** (pueden ser `null` si no hay suficientes datos históricos).

```sql
ALTER TABLE stocks
  ADD COLUMN ema9         DECIMAL(19,4) NULL,
  ADD COLUMN ema12        DECIMAL(19,4) NULL,
  ADD COLUMN ema20        DECIMAL(19,4) NULL,
  ADD COLUMN ema26        DECIMAL(19,4) NULL,
  ADD COLUMN ema50        DECIMAL(19,4) NULL,
  ADD COLUMN ema200       DECIMAL(19,4) NULL,
  ADD COLUMN rsi14        DECIMAL(19,4) NULL,
  ADD COLUMN rsi30        DECIMAL(19,4) NULL,
  ADD COLUMN macd_line    DECIMAL(19,4) NULL,
  ADD COLUMN macd_signal  DECIMAL(19,4) NULL,
  ADD COLUMN macd_hist    DECIMAL(19,4) NULL,
  ADD COLUMN bb_upper20   DECIMAL(19,4) NULL,
  ADD COLUMN bb_lower20   DECIMAL(19,4) NULL,
  ADD COLUMN atr14        DECIMAL(19,4) NULL;
```

> Para H2 (dev), Spring Boot con `ddl-auto=update` o `create-drop` añade las columnas automáticamente al detectar los nuevos campos en `StockEntity`. Para MariaDB (prod), la migración SQL debe ejecutarse manualmente o gestionarse con Flyway/Liquibase.

### 1.4 `StockMapper.java` — Mapeo de los nuevos campos

Añadir en `toEntity()` y `toDomain()` los setters/getters para todos los nuevos campos, siguiendo exactamente el mismo patrón que los campos existentes (`sma20`, `sma50`, etc.).

### Criterio de completitud de la Fase 1
- [ ] `TechnicalIndicators` compila con los nuevos campos en `@Builder`
- [ ] `Stock` compila con los nuevos campos
- [ ] `StockEntity` tiene las columnas nuevas con `@Column`
- [ ] `StockMapper.toEntity()` y `StockMapper.toDomain()` mapean todos los campos nuevos
- [ ] `mvn compile` sin errores
- [ ] Todos los tests existentes siguen pasando (`mvn test`)

---

## Fase 2 — EMA (Exponential Moving Average)

**Objetivo:** Calcular y persistir EMA para 6 períodos (9, 12, 20, 26, 50, 200) y hacerlos disponibles en el `RuleEvaluator` mediante el código `EMA`.

### 2.1 `StockHistoricalService.java` — Método `calculateEma()`

```java
// Períodos a calcular en calculateIndicators():
BigDecimal ema9   = calculateEma(data.getClosingPrices(), 9);
BigDecimal ema12  = calculateEma(data.getClosingPrices(), 12);
BigDecimal ema20  = calculateEma(data.getClosingPrices(), 20);
BigDecimal ema26  = calculateEma(data.getClosingPrices(), 26);
BigDecimal ema50  = calculateEma(data.getClosingPrices(), 50);
BigDecimal ema200 = calculateEma(data.getClosingPrices(), 200);
```

**Algoritmo (método privado):**
```
1. Si prices == null || prices.size() < period → return null
2. Crear copia de la lista e INVERTIRLA (los datos vienen desc, necesitamos asc)
3. Seed: EMA_0 = SMA de los primeros N valores (usar calculateSma() ya existente)
4. Multiplicador k = 2.0 / (period + 1)
5. Iterar desde el índice N hasta el final:
   EMA_i = (precio_i - EMA_{i-1}) * k + EMA_{i-1}
6. Retornar el último EMA calculado con escala 4, RoundingMode.HALF_UP
```

### 2.2 `TechnicalIndicators.builder()` en `calculateIndicators()` — añadir los campos EMA

```java
return TechnicalIndicators.builder()
    // campos existentes...
    .ema9(ema9)
    .ema12(ema12)
    .ema20(ema20)
    .ema26(ema26)
    .ema50(ema50)
    .ema200(ema200)
    .build();
```

### 2.3 `ManageAnalyzeStockService.java` — propagación de EMA a `Stock`

En el bloque `getdataFromProvider()`, junto a los `setSma*()` existentes:
```java
stock.setEma9(technicalIndicators.getEma9());
stock.setEma12(technicalIndicators.getEma12());
// ... etc. para los 6 períodos
```

### 2.4 `RuleEvaluator.java` — case `"EMA"` en `getIndicatorValue()`

```java
case "EMA" -> getEmaValue(param, stock);
```

Método privado `getEmaValue(Double param, Stock stock)`:
```java
int period = param.intValue();
return switch (period) {
    case 9   -> stock.getEma9();
    case 12  -> stock.getEma12();
    case 20  -> stock.getEma20();
    case 26  -> stock.getEma26();
    case 50  -> stock.getEma50();
    case 200 -> stock.getEma200();
    default  -> null;
};
```

Y en `formatIndicatorName()`:
```java
case "EMA" -> String.format("EMA%d", param.intValue());
```

### Criterio de completitud de la Fase 2
- [ ] `calculateEma()` implementado y retorna `null` con datos insuficientes
- [ ] Los 6 períodos se calculan en `calculateIndicators()`
- [ ] Los valores se propagan correctamente a `Stock` via `ManageAnalyzeStockService`
- [ ] `RuleEvaluator` evalúa reglas con `EMA` para los 6 períodos soportados
- [ ] `mvn compile` y `mvn test` sin errores

---

## Fase 3 — RSI (Relative Strength Index)

**Objetivo:** Calcular RSI para períodos 14 y 30 y hacerlos disponibles como código `RSI`.

### 3.1 `StockHistoricalService.java` — Método `calculateRsi()`

```java
// En calculateIndicators():
BigDecimal rsi14 = calculateRsi(data.getClosingPrices(), 14);
BigDecimal rsi30 = calculateRsi(data.getClosingPrices(), 30);
```

**Algoritmo (método privado):**
```
1. Si prices == null || prices.size() < period + 1 → return null
2. Crear copia e INVERTIRLA (datos Polygon vienen desc)
3. Calcular los N cambios diarios: delta_i = price_i - price_{i-1}  (i = 1..N)
4. Separar en ganancias (delta > 0) y pérdidas (|delta| si delta < 0, 0 si delta >= 0)
5. Primera media de ganancias = promedio simple de los N deltas positivos
   Primera media de pérdidas = promedio simple de los N deltas negativos
6. CASO LÍMITE:
   - Si avgLoss == 0 → RSI = 100 (sin pérdidas en el período)
   - Si avgGain == 0 → RSI = 0   (sin ganancias en el período)
7. RS = avgGain / avgLoss
8. RSI = 100 - (100 / (1 + RS))
9. Retornar con escala 4, RoundingMode.HALF_UP
```

### 3.2 `TechnicalIndicators.builder()` — añadir campos RSI

```java
.rsi14(rsi14)
.rsi30(rsi30)
```

### 3.3 `ManageAnalyzeStockService.java` — propagación

```java
stock.setRsi14(technicalIndicators.getRsi14());
stock.setRsi30(technicalIndicators.getRsi30());
```

### 3.4 `RuleEvaluator.java` — case `"RSI"`

```java
case "RSI" -> getRsiValue(param, stock);
```

Método privado `getRsiValue(Double param, Stock stock)`:
```java
int period = param.intValue();
return switch (period) {
    case 14 -> stock.getRsi14();
    case 30 -> stock.getRsi30();
    default -> null;
};
```

Y en `formatIndicatorName()`:
```java
case "RSI" -> String.format("RSI%d", param.intValue());
```

### Criterio de completitud de la Fase 3
- [ ] `calculateRsi()` implementado con manejo de casos límite (avgLoss=0, avgGain=0)
- [ ] RSI(14) y RSI(30) calculados en `calculateIndicators()`
- [ ] Propagación correcta a `Stock`
- [ ] `RuleEvaluator` evalúa `RSI` con parámetros 14 y 30
- [ ] `mvn compile` y `mvn test` sin errores

---

## Fase 4 — MACD (Moving Average Convergence Divergence)

**Objetivo:** Calcular MACD Line, Signal Line e Histogram usando las EMAs ya calculadas en la Fase 2.

> **Dependencia:** Requiere que la **Fase 2** esté completada y que EMA(9), EMA(12) y EMA(26) estén disponibles.

### 4.1 `StockHistoricalService.java` — Método `calculateMacd()`

Los tres valores del MACD se calculan a partir de la serie completa de precios (no de los valores EMA ya escalados a `BigDecimal`), para evitar errores de redondeo acumulado.

```java
// En calculateIndicators():
BigDecimal[] macd = calculateMacd(data.getClosingPrices());
BigDecimal macdLine      = macd != null ? macd[0] : null;
BigDecimal macdSignal    = macd != null ? macd[1] : null;
BigDecimal macdHistogram = macd != null ? macd[2] : null;
```

**Algoritmo (método privado que retorna `BigDecimal[3]`):**
```
Parámetros fijos: fast=12, slow=26, signal=9
1. Si prices.size() < slow + signal → return null
2. Invertir lista (desc → asc)
3. Calcular serie completa de EMA(12) sobre todos los precios → List<Double> emaFast
4. Calcular serie completa de EMA(26) sobre todos los precios → List<Double> emaSlow
5. Calcular serie MACD = emaFast_i - emaSlow_i   (a partir del índice 25, cuando emaSlow existe)
6. Calcular EMA(9) sobre la serie MACD → serie Signal
7. Devolver el último valor de cada serie:
   result[0] = macdLine    (último MACD)
   result[1] = macdSignal  (último Signal)
   result[2] = macdLine - macdSignal  (histograma)
```

> Nota: Para este cálculo se necesitan versiones internas que operan sobre `List<Double>` (no sobre `BigDecimal`) para mayor eficiencia. Extraer un método privado `calculateEmaAsDoubles(List<Double> prices, int period)`.

### 4.2 `TechnicalIndicators.builder()` — añadir campos MACD

```java
.macdLine(macdLine)
.macdSignal(macdSignal)
.macdHistogram(macdHistogram)
```

### 4.3 `ManageAnalyzeStockService.java` — propagación

```java
stock.setMacdLine(technicalIndicators.getMacdLine());
stock.setMacdSignal(technicalIndicators.getMacdSignal());
stock.setMacdHistogram(technicalIndicators.getMacdHistogram());
```

### 4.4 `RuleEvaluator.java` — cases MACD (sin parámetro)

```java
case "MACD_LINE"  -> stock.getMacdLine();
case "MACD_SIGNAL"-> stock.getMacdSignal();
case "MACD_HIST"  -> stock.getMacdHistogram();
```

### Criterio de completitud de la Fase 4
- [ ] MACD Line, Signal y Histogram calculados correctamente
- [ ] Propagación a `Stock` correcta
- [ ] `RuleEvaluator` soporta los tres códigos sin parámetro
- [ ] `mvn compile` y `mvn test` sin errores

---

## Fase 5 — Bollinger Bands y ATR

**Objetivo:** Añadir los dos indicadores de volatilidad restantes.

### 5.1 Bollinger Bands — `StockHistoricalService.java`

```java
// En calculateIndicators():
BigDecimal bbUpper20 = calculateBollingerUpper(data.getClosingPrices(), 20, 2.0);
BigDecimal bbLower20 = calculateBollingerLower(data.getClosingPrices(), 20, 2.0);
```

**Algoritmo:**
```
1. Si prices.size() < period → return null
2. Tomar los primeros `period` valores (más recientes, índice 0..period-1)
3. Media = SMA(period) → usar calculateSma() ya existente
4. Desviación estándar:
   variance = Σ (price_i - media)² / period
   stdDev = sqrt(variance)
5. BB_UPPER = media + (k * stdDev)
   BB_LOWER = media - (k * stdDev)
   (k=2 por defecto, configurable via parámetro)
```

> No necesita invertir la lista porque se trabaja sobre los N valores más recientes, igual que SMA.

**Propagar en `TechnicalIndicators.builder()`:**
```java
.bbUpper20(bbUpper20)
.bbLower20(bbLower20)
```

**Propagación en `ManageAnalyzeStockService`:**
```java
stock.setBbUpper20(technicalIndicators.getBbUpper20());
stock.setBbLower20(technicalIndicators.getBbLower20());
```

**`RuleEvaluator` — cases BB:**
```java
case "BB_UPPER" -> getBbValue(param, stock, true);
case "BB_LOWER" -> getBbValue(param, stock, false);
```

Método privado `getBbValue(Double param, Stock stock, boolean upper)`:
```java
int period = param != null ? param.intValue() : 20;
return switch (period) {
    case 20 -> upper ? stock.getBbUpper20() : stock.getBbLower20();
    default -> null;
};
```

---

### 5.2 ATR — `StockHistoricalService.java`

```java
// En calculateIndicators():
BigDecimal atr14 = calculateAtr(data.getCandles(), 14);
```

> `HistoricalData` ya transporta `List<Candle>` con `highPrice`, `lowPrice`, `closePrice`. El método debe recibir `List<Candle>`, no `List<Double>`.

**Algoritmo:**
```
1. Si candles == null || candles.size() < period + 1 → return null
2. Invertir la lista de candles (datos vienen desc de Polygon)
3. Para cada vela i (desde i=1):
   TR_i = max(
     high_i - low_i,
     |high_i - close_{i-1}|,
     |low_i  - close_{i-1}|
   )
4. ATR inicial (seed) = media simple de los primeros `period` TR
5. Para el resto de velas: ATR_i = (ATR_{i-1} * (period-1) + TR_i) / period  (EMA suavizada de Wilder)
6. Retornar el último ATR calculado, escala 4, HALF_UP
```

**Propagar en `TechnicalIndicators.builder()`:**
```java
.atr14(atr14)
```

**Propagación en `ManageAnalyzeStockService`:**
```java
stock.setAtr14(technicalIndicators.getAtr14());
```

**`RuleEvaluator` — case ATR:**
```java
case "ATR" -> getAtrValue(param, stock);
```

Método privado `getAtrValue(Double param, Stock stock)`:
```java
int period = param != null ? param.intValue() : 14;
return switch (period) {
    case 14 -> stock.getAtr14();
    default -> null;
};
```

### Criterio de completitud de la Fase 5
- [ ] `calculateBollingerUpper()` y `calculateBollingerLower()` implementados
- [ ] `calculateAtr()` implementado usando `List<Candle>` con suavizado de Wilder
- [ ] Todos los nuevos campos propagados a `Stock`
- [ ] `RuleEvaluator` soporta `BB_UPPER`, `BB_LOWER`, `ATR`
- [ ] `mvn compile` y `mvn test` sin errores

---

## Fase 6 — Tests Unitarios

**Objetivo:** Garantizar cobertura ≥ 80% de todos los métodos de cálculo nuevos y de los nuevos cases en `RuleEvaluator`.

### 6.1 Ampliar `StockHistoricalServiceTest.java`

Añadir una clase anidada `@Nested` por indicador:

#### `EmaCalculationTests`
| Test | Descripción |
|------|-------------|
| `shouldCalculateEma9Correctly` | Con 50 precios constantes, EMA(9) = precio (convergencia) |
| `shouldReturnNullWhenInsufficientDataForEma` | Con 8 precios para EMA(9) → `null` |
| `shouldUseSmaAsEmaInitialSeed` | El primer EMA(N) coincide con SMA(N) de los primeros N valores |
| `shouldCalculateAllEmaPeriods` | Con 300 precios, todos los 6 períodos son no nulos |

#### `RsiCalculationTests`
| Test | Descripción |
|------|-------------|
| `shouldReturnRsi100WhenAllPricesRising` | N+1 precios siempre crecientes → RSI = 100 |
| `shouldReturnRsi0WhenAllPricesFalling` | N+1 precios siempre decrecientes → RSI = 0 |
| `shouldReturnNullWhenInsufficientDataForRsi` | Solo `period` precios (necesitan `period+1`) → `null` |
| `shouldCalculateRsi14WithKnownValues` | Verificar RSI(14) con una serie de precios con resultado esperado conocido |
| `shouldCalculateRsi30WithKnownValues` | Igual para período 30 |

#### `MacdCalculationTests`
| Test | Descripción |
|------|-------------|
| `shouldReturnNullMacdWhenInsufficientData` | Con menos de 35 precios (26+9) → todos nulos |
| `shouldCalculateMacdLineAsDifferenceOfEmas` | Verificar que `macdLine = EMA12 - EMA26` |
| `shouldCalculateMacdHistogramAsLineMinusSignal` | Verificar `histogram = line - signal` |
| `shouldCalculateAllMacdComponentsWithAdequateData` | Con 300 precios, los 3 componentes son no nulos |

#### `BollingerBandsCalculationTests`
| Test | Descripción |
|------|-------------|
| `shouldReturnNullBbWhenInsufficientData` | Con 19 precios para BB(20) → `null` |
| `shouldCalculateBbUpperAboveSma` | `BB_UPPER > SMA20` siempre (excepto stdDev=0) |
| `shouldCalculateBbLowerBelowSma` | `BB_LOWER < SMA20` siempre (excepto stdDev=0) |
| `shouldReturnSymmetricBandsAroundSmaWithConstantPrices` | Con precios constantes, `BB_UPPER = BB_LOWER = SMA20` |

#### `AtrCalculationTests`
| Test | Descripción |
|------|-------------|
| `shouldReturnNullAtrWhenInsufficientCandles` | Con 14 velas para ATR(14) (necesita 15) → `null` |
| `shouldCalculateAtrWithSimplePriceRange` | Velas con `high-low` fijo → ATR converge a ese rango |
| `shouldReturnNullAtrWithNullCandles` | `null` candles → `null` |
| `shouldCalculateAtr14WithAdequateData` | Con 100 velas, ATR(14) es no nulo y positivo |

---

### 6.2 Ampliar `RuleEvaluatorTest.java`

Añadir clases anidadas `@Nested` para cada nuevo indicador:

#### `EmaRuleTests`
- `shouldPassRuleWhenPriceAboveEma9`
- `shouldFailRuleWhenPriceBelowEma200`
- `shouldReturnFailedWhenEmaValueIsNull` (campo `ema9 = null` en Stock)
- `shouldFormatEmaNameCorrectlyInJustification` (verifica texto `"EMA9"` en justification)

#### `RsiRuleTests`
- `shouldPassRuleWhenRsi14BelowOversoldThreshold` (`RSI14 < 30`)
- `shouldPassRuleWhenRsi14AboveOverboughtThreshold` (`RSI14 > 70`)
- `shouldReturnFailedWhenRsiValueIsNull`
- `shouldFormatRsiNameCorrectlyInJustification`

#### `MacdRuleTests`
- `shouldPassRuleWhenMacdLineCrossesAboveSignal` (`MACD_LINE > MACD_SIGNAL`)
- `shouldPassRuleWhenMacdHistogramIsPositive` (`MACD_HIST > 0`)
- `shouldReturnFailedWhenMacdComponentIsNull`

#### `BollingerBandsRuleTests`
- `shouldPassRuleWhenPriceBelowBbLower` (precio en sobreventa de Bollinger)
- `shouldPassRuleWhenPriceAboveBbUpper` (precio en sobrecompra)
- `shouldReturnFailedWhenBbValueIsNull`

#### `AtrRuleTests`
- `shouldPassRuleWhenAtr14BelowThreshold` (`ATR14 < 2.0` para baja volatilidad)
- `shouldReturnFailedWhenAtrValueIsNull`
- `shouldFormatAtrNameInJustification`

---

### 6.3 Cobertura objetivo

| Clase | Métodos cubiertos | Cobertura objetivo |
|-------|------------------|--------------------|
| `StockHistoricalService` | `calculateEma`, `calculateRsi`, `calculateMacd`, `calculateBollingerUpper/Lower`, `calculateAtr` | ≥ 90% |
| `RuleEvaluator` | Todos los nuevos cases en `getIndicatorValue()` y `formatIndicatorName()` | ≥ 90% |

### Criterio de completitud de la Fase 6
- [ ] Todos los tests de `StockHistoricalServiceTest` pasan (incluidos los nuevos)
- [ ] Todos los tests de `RuleEvaluatorTest` pasan (incluidos los nuevos)
- [ ] `mvn test` sin errores en el módulo completo
- [ ] Cobertura de líneas ≥ 80% en `StockHistoricalService` y `RuleEvaluator`

---

## Resumen de Fases y Archivos Modificados

| Fase | Descripción | Archivos modificados |
|------|-------------|----------------------|
| **1** | Modelo de dominio y persistencia | `TechnicalIndicators.java`, `Stock.java`, `StockEntity.java`, `StockMapper.java` |
| **2** | EMA (6 períodos) | `StockHistoricalService.java`, `ManageAnalyzeStockService.java`, `RuleEvaluator.java` |
| **3** | RSI (14 y 30) | `StockHistoricalService.java`, `ManageAnalyzeStockService.java`, `RuleEvaluator.java` |
| **4** | MACD (Line, Signal, Hist) | `StockHistoricalService.java`, `ManageAnalyzeStockService.java`, `RuleEvaluator.java` |
| **5** | Bollinger Bands + ATR | `StockHistoricalService.java`, `ManageAnalyzeStockService.java`, `RuleEvaluator.java` |
| **6** | Tests unitarios | `StockHistoricalServiceTest.java`, `RuleEvaluatorTest.java` |

---

## Reglas de Dominio Declarables al Finalizar la Implementación

Una vez completadas las 6 fases, estas son las reglas de ejemplo que pueden registrarse en la BD (`rule_definition` + reglas en `strategy`):

```
# Tendencia
EMA(9) > EMA(26)            → Cruce alcista de medias rápidas
MACD_LINE > MACD_SIGNAL     → Cruce alcista MACD
PRICE > EMA(200)            → Precio por encima de la tendencia principal

# Momento
RSI(14) < 30                → Sobreventa RSI (posible entrada)
RSI(14) > 70                → Sobrecompra RSI (posible salida)
RSI(30) > 50                → Momentum positivo en largo plazo

# Volatilidad
PRICE < BB_LOWER(20)        → Precio bajo banda inferior Bollinger
PRICE > BB_UPPER(20)        → Precio sobre banda superior Bollinger
ATR(14) < 2.0               → Baja volatilidad (entrada conservadora)
ATR(14) > 5.0               → Alta volatilidad (gestión de riesgo)
```
