# Análisis de Datos de APIs y Nuevos Índices Técnicos — Documento de Tarea

**Fecha:** 2026-04-07  
**Tarea:** Análisis de datos obtenidos de Finnhub y Polygon.io para identificar nuevos índices técnicos implementables en el motor de reglas.

---

## Resumen de la Tarea

Se ha realizado un análisis exhaustivo de los datos disponibles a través de las APIs externas (Finnhub y Polygon.io) para identificar qué nuevos indicadores técnicos (RSI, EMA, MACD, Bollinger Bands, ATR, Stochastic, OBV, VWAP, etc.) pueden declararse como nuevas reglas en el sistema, dado que todos los datos necesarios para calcularlos ya están siendo obtenidos sin necesidad de llamadas API adicionales.

El resultado es el archivo `reglas.md` en la raíz del repositorio.

---

## Código Generado

No se generó código fuente. Esta tarea es de análisis y documentación.

---

## Hallazgos Principales

### Datos disponibles sin coste adicional de API

| Fuente | Datos disponibles |
|--------|-------------------|
| Finnhub `/quote` | Precio actual, apertura, máximo, mínimo, cierre anterior, cambio %, timestamp |
| Polygon.io `/v2/aggs` | 300 velas OHLCV diarias ajustadas (open, high, low, close, volume, timestamp) |

### Estado actual de los indicadores

El `RuleEvaluator` implementa: `PRICE`, `SMA` (20/50/200), `VOLUME`, `AVG_VOLUME`, `OPEN`, `HIGH`, `LOW`, `PREV_CLOSE`, `CONSTANT`/`VALUE`.

### Nuevos indicadores identificados (20 en total)

- **Alta prioridad (primera iteración):** RSI(14/30), EMA(9/12/26/50/200), MACD_LINE, MACD_SIGNAL, BB_UPPER/BB_LOWER, ATR(14)
- **Media prioridad (segunda iteración):** STOCH_K/D, WILLIAMS_R, CCI, OBV, VWAP
- **Baja prioridad:** ROC, PERCENT_CHANGE, GAP, GAP_PCT, INTRADAY_RANGE

---

## Decisiones Técnicas

1. **Sin nuevas llamadas API**: Todos los indicadores pueden calcularse con los datos ya obtenidos del endpoint `v2/aggs` de Polygon (300 velas OHLCV diarias).
2. **Separación limpia de capas**: Los cálculos deben añadirse exclusivamente en `StockHistoricalService` (dominio), propagándose a `TechnicalIndicators` → `Stock` → `RuleEvaluator`.
3. **Compatibilidad de esquema BD**: La tabla `rule_definition` ya soporta indicadores parametrizados (`requires_param = true`), sin necesidad de migraciones.
4. **Ordenación de datos**: `PolygonAdapter` usa `sort=desc`; los nuevos algoritmos deben considerar que `closingPrices.get(0)` es el dato más reciente.

---

## Cobertura de Tests

Esta tarea es exclusivamente de análisis y documentación. No se han añadido ni modificado tests.

En la implementación futura de cada indicador, se deberá cubrir en `StockHistoricalServiceTest`:
- Caso con datos insuficientes (→ `null`)
- Caso con valores conocidos matemáticamente verificables
- Caso límite (todos alcistas → RSI=100; todos bajistas → RSI=0)

---

## Advertencias de Arquitectura

- Los cálculos de indicadores NO deben añadirse en `PolygonAdapter` ni en capas de infraestructura.
- El `RuleEvaluator` NO debe contener lógica de cálculo, solo acceso a campos ya calculados.
- No usar `System.out.println`; usar SLF4J para cualquier log añadido.

---

## Próximos Pasos Sugeridos

1. Implementar RSI (14 y 30) en `StockHistoricalService` como primera iteración.
2. Implementar EMA para los períodos más usados (9, 12, 26, 50, 200).
3. Derivar MACD desde las EMA ya calculadas.
4. Añadir Bollinger Bands usando la SMA20 existente.
5. Implementar ATR usando las velas OHLCV ya persistidas.
6. Registrar los nuevos indicadores como `RuleDefinition` en la base de datos (seed SQL).
