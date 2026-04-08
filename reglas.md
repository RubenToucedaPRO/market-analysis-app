# Análisis de Datos de APIs y Nuevos Índices Técnicos

## 1. Datos Disponibles en las APIs

### 1.1 Finnhub — Cotización en Tiempo Real (`/quote`)

La clase `QuoteData` expone los siguientes campos por cada ticker:

| Campo | Descripción | Uso actual en el sistema |
|-------|-------------|--------------------------|
| `c`   | Precio actual (Current price) | ✅ `PRICE` en `RuleEvaluator` |
| `d`   | Cambio absoluto respecto al cierre anterior | ❌ Sin indicador declarado |
| `dp`  | Cambio porcentual respecto al cierre anterior | ❌ Sin indicador declarado |
| `h`   | Máximo intradiario | ✅ `HIGH` en `RuleEvaluator` |
| `l`   | Mínimo intradiario | ✅ `LOW` en `RuleEvaluator` |
| `o`   | Apertura del día | ✅ `OPEN` en `RuleEvaluator` |
| `pc`  | Cierre previo (Previous close) | ✅ `PREV_CLOSE` en `RuleEvaluator` |
| `t`   | Timestamp Unix de la cotización | ❌ Sin indicador declarado |

### 1.2 Polygon.io — Datos Históricos OHLCV (`/v2/aggs/ticker/{ticker}/range/1/day`)

El sistema solicita hasta **300 velas diarias** (configurable mediante `SIZE_HISTORICAL`), ordenadas de más reciente a más antiguo (`sort=desc`). Cada vela `Candle` contiene:

| Campo | Descripción |
|-------|-------------|
| `ticker` | Símbolo del activo |
| `dateTime` | Marca de tiempo (epoch ms) |
| `openPrice` | Precio de apertura |
| `highPrice` | Precio máximo de la sesión |
| `lowPrice` | Precio mínimo de la sesión |
| `closePrice` | Precio de cierre |
| `volume` | Volumen de contratos negociados |

Además, `HistoricalData` consolida:
- `closingPrices` — Lista de 300 precios de cierre (para cálculos de SMA, RSI, EMA, etc.)
- `volumes` — Lista de 300 volúmenes diarios

---

## 2. Indicadores Actualmente Implementados

### En `StockHistoricalService` (cálculo)

| Indicador | Períodos | Datos necesarios |
|-----------|----------|-----------------|
| SMA | 20, 50, 200 | `closingPrices` |
| Volumen actual | — | `volumes[0]` |
| Volumen medio | 20 sesiones | `volumes` |

### En `RuleEvaluator` (evaluación de reglas)

| Código | Descripción | Parámetro |
|--------|-------------|-----------|
| `PRICE` | Precio actual | — |
| `SMA` | Media móvil simple | 20, 50, 200 |
| `VOLUME` | Volumen de la última sesión | — |
| `AVG_VOLUME` | Volumen medio (período configurable) | — |
| `OPEN` | Precio de apertura intradiario | — |
| `HIGH` | Máximo intradiario | — |
| `LOW` | Mínimo intradiario | — |
| `PREV_CLOSE` | Cierre anterior | — |
| `CONSTANT` / `VALUE` | Valor numérico fijo para comparación | cualquier `Double` |

---

## 3. Nuevos Índices que Pueden Añadirse

Los siguientes indicadores son **completamente calculables** con los datos ya disponibles, sin necesitar nuevas llamadas a API. Se agrupan por la fuente de datos que requieren.

---

### 3.1 Indicadores basados en precios de cierre (`closingPrices`)

#### RSI — Relative Strength Index

**Descripción:** Oscilador de momento que mide la velocidad y magnitud de los cambios de precio, acotado entre 0 y 100. Valores > 70 indican sobrecompra; valores < 30 indican sobreventa.

**Fórmula:**
```
RS  = Media de ganancias de N períodos / Media de pérdidas de N períodos
RSI = 100 - (100 / (1 + RS))

Casos límite:
  Si no hay pérdidas en el período → RSI = 100 (mercado solo alcista)
  Si no hay ganancias en el período → RSI = 0  (mercado solo bajista)
  (Evitan división por cero en el cálculo de RS)
```

**Datos necesarios:** `closingPrices` (mínimo N+1 valores; con 300 hay margen suficiente).

**Períodos recomendados:** 14 (estándar), 30 (tendencias largas).

**Código propuesto:** `RSI` con parámetro `14` o `30`.

**Ejemplo de regla:** `RSI(14) < 30` → activo en zona de sobreventa (posible entrada).

**Cambios en el código:**
- `StockHistoricalService.calculateIndicators()`: añadir `calculateRsi(closingPrices, 14)` y `calculateRsi(closingPrices, 30)`.
- `TechnicalIndicators`: añadir campos `rsi14` y `rsi30`.
- `Stock`: añadir campos `rsi14` y `rsi30`.
- `RuleEvaluator.getIndicatorValue()`: añadir case `"RSI"` que devuelva el campo según el parámetro.

---

#### EMA — Exponential Moving Average

**Descripción:** Media móvil que pondera exponencialmente los datos más recientes, reaccionando más rápido que la SMA a los cambios de precio.

**Fórmula:**
```
Inicialización (seed): EMA_inicial = SMA(N) calculada sobre los primeros N cierres
Actualización diaria:  k = 2 / (N + 1)
                       EMA_hoy = (Precio_hoy - EMA_ayer) * k + EMA_ayer
```

**Datos necesarios:** `closingPrices`.

**Períodos recomendados:** 9, 12, 20, 26, 50, 200.

**Código propuesto:** `EMA` con parámetro del período (ej. `12`, `26`).

**Ejemplo de regla:** `EMA(9) > EMA(21)` → señal alcista de cruce de medias rápidas.

**Cambios en el código:**
- `StockHistoricalService`: añadir `calculateEma(closingPrices, period)`.
- `TechnicalIndicators`: campos `ema9`, `ema12`, `ema20`, `ema26`, `ema50`, `ema200`.
- `Stock`: mismos campos o un mapa `ema`.
- `RuleEvaluator`: case `"EMA"` con dispatch por parámetro.

---

#### MACD — Moving Average Convergence Divergence

**Descripción:** Diferencia entre EMA(12) y EMA(26). La línea de señal es EMA(9) del propio MACD. El histograma es MACD - Señal. Indica cambios en dirección, momento y duración de una tendencia.

**Fórmula:**
```
MACD      = EMA(12) - EMA(26)
Señal     = EMA(9 periodos de MACD)
Histograma = MACD - Señal
```

**Datos necesarios:** `closingPrices` (depende de EMA).

**Código propuesto:** `MACD_LINE`, `MACD_SIGNAL`, `MACD_HIST` (sin parámetro, configuración fija 12/26/9 estándar).

**Ejemplo de regla:** `MACD_LINE > MACD_SIGNAL` → cruce alcista del MACD (señal de compra).

---

#### ROC — Rate of Change

**Descripción:** Porcentaje de cambio del precio de cierre en N períodos. Mide la velocidad del movimiento del precio.

**Fórmula:**
```
ROC = ((Precio_hoy - Precio_{hoy-N}) / Precio_{hoy-N}) * 100
```

**Datos necesarios:** `closingPrices` (mínimo N+1 valores).

**Período recomendado:** 10, 20.

**Código propuesto:** `ROC` con parámetro del período.

**Ejemplo de regla:** `ROC(10) > 5` → el precio ha subido más de un 5% en 10 días.

---

### 3.2 Indicadores basados en OHLCV (`candles`)

#### ATR — Average True Range

**Descripción:** Medida de volatilidad que promedia el rango verdadero durante N períodos. No indica dirección, solo magnitud del movimiento.

**Fórmula:**
```
TR_i = max(High_i - Low_i, |High_i - Close_{i-1}|, |Low_i - Close_{i-1}|)
ATR  = Media de TR para N períodos (normalmente EMA del TR)
```

**Datos necesarios:** `highPrice`, `lowPrice`, `closePrice` de cada `Candle`.

**Período recomendado:** 14.

**Código propuesto:** `ATR` con parámetro `14`.

**Ejemplo de regla:** `ATR(14) < 2.0` → baja volatilidad, activo estable (entrada conservadora).

---

#### Bollinger Bands

**Descripción:** Bandas alrededor de una SMA definidas como SMA ± (k · Desviación Estándar). Miden la volatilidad relativa y zonas de sobrecompra/sobreventa dinámicas.

**Fórmula:**
```
BB_MID   = SMA(N)
BB_UPPER = SMA(N) + (k * StdDev(N))
BB_LOWER = SMA(N) - (k * StdDev(N))
```
Parámetros estándar: N=20, k=2.

**Datos necesarios:** `closingPrices`.

**Código propuesto:** `BB_UPPER`, `BB_LOWER`, `BB_MID` (con parámetro del período, p.ej. `20`).

**Ejemplo de regla:** `PRICE < BB_LOWER(20)` → precio por debajo de la banda inferior (potencial rebote).

---

#### Stochastic Oscillator (%K y %D)

**Descripción:** Compara el cierre actual con el rango de precios de N períodos. %D es la media móvil de %K. Valores > 80 sobrecompra; < 20 sobreventa.

**Fórmula:**
```
%K = ((Close - LowestLow_N) / (HighestHigh_N - LowestLow_N)) * 100
%D = SMA(3 de %K)
```

**Datos necesarios:** `highPrice`, `lowPrice`, `closePrice` de cada `Candle`.

**Período recomendado:** 14.

**Código propuesto:** `STOCH_K`, `STOCH_D` con parámetro del período.

**Ejemplo de regla:** `STOCH_K(14) < 20` → zona de sobreventa estocástica.

---

#### Williams %R

**Descripción:** Variante invertida del Estocástico. Rango de -100 a 0. Valores > -20 sobrecompra; valores < -80 sobreventa.

**Fórmula:**
```
%R = ((HighestHigh_N - Close) / (HighestHigh_N - LowestLow_N)) * -100
```

**Datos necesarios:** `highPrice`, `lowPrice`, `closePrice` de cada `Candle`.

**Código propuesto:** `WILLIAMS_R` con parámetro del período `14`.

**Ejemplo de regla:** `WILLIAMS_R(14) < -80` → condición de sobreventa.

---

#### OBV — On-Balance Volume

**Descripción:** Acumula volumen de manera positiva o negativa según si el precio cerró por encima o por debajo del día anterior. Detecta acumulación/distribución de activos.

**Fórmula:**
```
Si Close_hoy > Close_ayer: OBV = OBV_ayer + Volume_hoy
Si Close_hoy < Close_ayer: OBV = OBV_ayer - Volume_hoy
Si Close_hoy = Close_ayer: OBV = OBV_ayer
```

**Datos necesarios:** `closePrice`, `volume` de cada `Candle`.

**Código propuesto:** `OBV` (sin parámetro).

**Ejemplo de regla:** `OBV > CONSTANT(0)` → presión compradora neta.

---

#### CCI — Commodity Channel Index

**Descripción:** Mide la desviación del precio típico respecto a su media en N períodos. Valores > +100 sobrecompra; < -100 sobreventa.

**Fórmula:**
```
TP       = (High + Low + Close) / 3
CCI      = (TP - SMA_TP_N) / (0.015 * MeanDeviation_N)
```

**Datos necesarios:** `highPrice`, `lowPrice`, `closePrice` de cada `Candle`.

**Período recomendado:** 20.

**Código propuesto:** `CCI` con parámetro `20`.

**Ejemplo de regla:** `CCI(20) > 100` → activo en sobrecompra por CCI.

---

#### VWAP — Volume Weighted Average Price

**Descripción:** Precio promedio ponderado por volumen desde el inicio del período. Referencia clave para institucionales.

**Fórmula:**
```
VWAP = Σ(TP_i * Volume_i) / Σ(Volume_i)
donde TP_i = (High_i + Low_i + Close_i) / 3
```

**Datos necesarios:** `highPrice`, `lowPrice`, `closePrice`, `volume` de cada `Candle`.

**Código propuesto:** `VWAP` (sin parámetro, calculado sobre toda la serie disponible o sobre el período especificado).

**Ejemplo de regla:** `PRICE > VWAP` → precio por encima del VWAP, presión alcista institucional.

---

### 3.3 Indicadores derivados de datos intradiarios (Finnhub)

#### PERCENT_CHANGE — Cambio Porcentual Intradiario

**Descripción:** Porcentaje de cambio del precio actual respecto al cierre anterior. Campo `dp` ya disponible en `QuoteData`.

**Datos necesarios:** `dp` de `QuoteData` (ya en `Stock` implícitamente a través de `currentPrice` y `previousClose`).

**Código propuesto:** `PERCENT_CHANGE` (sin parámetro).

**Ejemplo de regla:** `PERCENT_CHANGE > 2.0` → activo con momentum intradiario positivo > 2%.

> **Nota:** Este campo ya llega de Finnhub pero no está mapeado a `Stock` ni disponible en `RuleEvaluator`. Requiere añadir el campo `percentChange` a `Stock` y mapearlo en `FinnhubMapper`.

---

#### GAP — Apertura Gap

**Descripción:** Diferencia entre el precio de apertura y el cierre anterior. Detecta aperturas con hueco (gap up/down).

**Fórmula:**
```
GAP = OpenPrice - PreviousClose
GAP% = ((OpenPrice - PreviousClose) / PreviousClose) * 100
```

**Datos necesarios:** `openPrice`, `previousClose` (ambos ya en `Stock`).

**Código propuesto:** `GAP` y `GAP_PCT` (sin parámetro), calculados en el momento de evaluación.

**Ejemplo de regla:** `GAP_PCT > 1.5` → gap de apertura al alza superior al 1.5%.

---

#### INTRADAY_RANGE — Rango Intradiario

**Descripción:** Amplitud de la vela del día (High - Low). Útil para filtrar días de alta volatilidad.

**Fórmula:**
```
INTRADAY_RANGE = HighOfDay - LowOfDay
```

**Datos necesarios:** `highOfDay`, `lowOfDay` (ya en `Stock`).

**Código propuesto:** `INTRADAY_RANGE` (sin parámetro).

**Ejemplo de regla:** `INTRADAY_RANGE > 3.0` → sesión con movimiento amplio (posible ruptura o volatilidad).

---

## 4. Resumen de Nuevos Índices Propuestos

| Código | Categoría | Período/Parámetro | Datos necesarios | Prioridad |
|--------|-----------|-------------------|-----------------|-----------|
| `RSI` | Oscilador momento | 14, 30 | `closingPrices` | 🔴 Alta |
| `EMA` | Tendencia | 9, 12, 20, 26, 50, 200 | `closingPrices` | 🔴 Alta |
| `MACD_LINE` | Tendencia/Momento | — (12/26/9) | `closingPrices` | 🔴 Alta |
| `MACD_SIGNAL` | Tendencia/Momento | — | `closingPrices` | 🔴 Alta |
| `MACD_HIST` | Tendencia/Momento | — | `closingPrices` | 🟡 Media |
| `BB_UPPER` | Volatilidad | 20 | `closingPrices` | 🔴 Alta |
| `BB_LOWER` | Volatilidad | 20 | `closingPrices` | 🔴 Alta |
| `BB_MID` | Volatilidad | 20 | `closingPrices` | 🟡 Media |
| `ATR` | Volatilidad | 14 | OHLCV `candles` | 🔴 Alta |
| `STOCH_K` | Oscilador | 14 | OHLCV `candles` | 🟡 Media |
| `STOCH_D` | Oscilador | 14 | OHLCV `candles` | 🟡 Media |
| `WILLIAMS_R` | Oscilador | 14 | OHLCV `candles` | 🟡 Media |
| `CCI` | Oscilador | 20 | OHLCV `candles` | 🟡 Media |
| `OBV` | Volumen | — | `candles` (close + vol) | 🟡 Media |
| `VWAP` | Volumen/Precio | — | OHLCV + vol | 🟡 Media |
| `ROC` | Momento | 10, 20 | `closingPrices` | 🟢 Baja |
| `PERCENT_CHANGE` | Intradiario | — | `dp` Finnhub | 🟢 Baja |
| `GAP` | Intradiario | — | `open`, `prevClose` | 🟢 Baja |
| `GAP_PCT` | Intradiario | — | `open`, `prevClose` | 🟢 Baja |
| `INTRADAY_RANGE` | Intradiario | — | `high`, `low` | 🟢 Baja |

---

## 5. Impacto en la Arquitectura (Cambios Necesarios)

Para cada nuevo indicador, los cambios se limitan a **tres clases** sin romper la arquitectura hexagonal:

### Capa Domain

**`TechnicalIndicators.java`** — añadir campos `BigDecimal` para cada nuevo indicador calculado:
```java
BigDecimal rsi14;
BigDecimal rsi30;
BigDecimal ema9;
BigDecimal ema12;
BigDecimal ema26;
BigDecimal macdLine;
BigDecimal macdSignal;
BigDecimal macdHist;
BigDecimal bbUpper20;
BigDecimal bbLower20;
BigDecimal atr14;
// ...etc.
```

**`StockHistoricalService.java`** — añadir métodos de cálculo privados:
```java
private BigDecimal calculateRsi(List<Double> prices, int period) { ... }
private BigDecimal calculateEma(List<Double> prices, int period) { ... }
private BigDecimal calculateAtr(List<Candle> candles, int period) { ... }
// ...etc.
```

**`Stock.java`** — añadir los mismos campos para que el `RuleEvaluator` pueda acceder a ellos en tiempo de evaluación.

### Capa Domain Service

**`RuleEvaluator.java`** — ampliar el `switch` en `getIndicatorValue()`:
```java
case "RSI"           -> getRsiValue(param, stock);
case "EMA"           -> getEmaValue(param, stock);
case "MACD_LINE"     -> stock.getMacdLine();
case "MACD_SIGNAL"   -> stock.getMacdSignal();
case "BB_UPPER"      -> getBbUpperValue(param, stock);
case "BB_LOWER"      -> getBbLowerValue(param, stock);
case "ATR"           -> getAtrValue(param, stock);
case "PERCENT_CHANGE"-> stock.getPercentChange();
case "GAP"           -> calculateGap(stock);
// ...etc.
```

### Capa Application

**`ManageAnalyzeStockService.java`** — sin cambios directos; los nuevos indicadores se propagan automáticamente a través de `TechnicalIndicators` → `Stock`.

### Capa Infrastructure

**`FinnhubMapper.java`** — mapear el campo `dp` de `QuoteData` al nuevo campo `percentChange` de `Stock` (solo para `PERCENT_CHANGE`).

> ⚠️ **ADVERTENCIA CRÍTICA — Ordenación de datos Polygon:**  
> `PolygonAdapter` solicita los datos con `sort=desc`, por lo que `closingPrices.get(0)` y `candles.get(0)` representan el dato **más reciente**.  
> Todos los algoritmos de cálculo (RSI, EMA, ATR, Stochastic, etc.) deben **invertir la lista** antes de iterar, o recorrer el array en orden inverso, para procesar los datos cronológicamente de más antiguo a más reciente.  
> Un error en este punto produce resultados matemáticamente incorrectos en **todos** los indicadores. Verificar con un test unitario usando valores conocidos ordenados cronológicamente.

---

## 6. Criterios de Priorización

### Implementar en primera iteración (RSI, EMA, MACD, Bollinger Bands, ATR)

Estos cinco indicadores cubren los cuatro pilares del análisis técnico clásico:
1. **Tendencia**: EMA, MACD
2. **Momento**: RSI
3. **Volatilidad**: Bollinger Bands, ATR
4. **Confirmación de señales**: combinación de todos

### Implementar en segunda iteración (Stochastic, CCI, OBV, VWAP, Williams %R)

Añaden profundidad analítica especialmente útil para confirmación de señales y análisis de volumen.

### Implementar cuando sea necesario (ROC, GAP, PERCENT_CHANGE, INTRADAY_RANGE)

Indicadores simples derivados de datos ya disponibles, de menor complejidad de cálculo.

---

## 7. Consideraciones Técnicas

- **Datos suficientes**: Con 300 velas diarias disponibles hay margen para calcular cualquiera de los indicadores propuestos, incluyendo RSI(30), SMA(200), ATR(14), etc.
- **Ordenación de datos**: `PolygonAdapter` solicita los datos con `sort=desc` (más reciente primero). Los algoritmos de cálculo deben invertir la lista o iterar en orden inverso donde sea necesario.
- **Precisión**: Usar `BigDecimal` con `RoundingMode.HALF_UP` y escala 4 (consistente con la implementación actual de SMA en `StockHistoricalService`).
- **Desacoplamiento**: Los cálculos deben vivir exclusivamente en `StockHistoricalService` (dominio), sin lógica de cálculo en adapters ni en el `RuleEvaluator`.
- **Parametrización en base de datos**: La tabla `rule_definition` ya soporta indicadores con `requires_param = true`, por lo que los nuevos indicadores parametrizados (RSI, EMA, ATR...) se pueden registrar directamente en la BD sin cambios de esquema.
- **Tests unitarios**: Cada método de cálculo añadido a `StockHistoricalService` debe tener test en `StockHistoricalServiceTest` con casos conocidos (ej. RSI = 100 con N períodos siempre alcistas).
