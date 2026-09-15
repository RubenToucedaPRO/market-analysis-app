# Architecture Walkthrough — Guía Personal

**Fecha**: 2026-09-16  
**Propósito**: Entender el flujo completo `analyze ticker` → `fetch data` → `calculate indicators` → `evaluate strategy` → `persist` → `IA` para no perderme en la arquitectura hexagonal.

---

## 1. Mapa Mental de Capas (Dependency Rule: **inward only**)

```
┌─────────────────────────────────────────────────────────────┐
│  PRESENTATION (Controller, DTOs, Thymeleaf)                 │
│  AnalyzeTickerController → ManageAnalyzeTickerUseCase       │
└──────────────────────────┬──────────────────────────────────┘
                           │ calls (interface)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  APPLICATION (Use Cases, Orchestration)                     │
│  AnalyzeAndPersistStockService                              │
│  - Coordina: StockProviderPort + HistoricalProviderPort     │
│  - Llama: StockHistoricalService (domain)                   │
│  - Llama: EvaluateStrategyService (domain)                  │
│  - Persiste via: StockDataRepository, StrategyEvaluationRepository, CandleHistoryRepository │
└──────────────────────────┬──────────────────────────────────┘
                           │ implements (ports)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  DOMAIN (Pure Java, Zero Framework)                         │
│  Entities: Rule, Strategy, Stock, StrategyObjective         │
│  Services: RuleEvaluator, RiskRewardCalculator, StockHistoricalService │
│  Value Objects: RuleResult, AnalysisResult, StrategyEvaluation │
│  Catalog: RuleCapabilityCatalog (single source of truth)    │
│  Ports (interfaces): StockProviderPort, HistoricalProviderPort, StockDataRepository, ... │
└──────────────────────────┬──────────────────────────────────┘
                           │ implements (adapters)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE (External Details)                          │
│  - PolygonAdapter  → HistoricalProviderPort  (RestTemplate) │
│  - FinnhubAdapter  → StockProviderPort     (RestClient)     │
│  - OpenrouterAdapter → ApiIPort              (OpenAI Client)│
│  - JPA Repositories → Repository Ports                       │
│  - JsoupFinvizAdapter → FinvizScreenerPort                   │
└─────────────────────────────────────────────────────────────┘
```

**Regla de oro**: El dominio **nunca** importa de infrastructure. Los adaptadores implementan puertos del dominio.

---

## 2. Flujo Principal: `analyzeAndPersist(ticker, strategy, origin)`

```
Usuario POST /analysis/getTickerData {tickers, strategyId}
         │
         ▼
AnalyzeTickerController.getTickerData()
         │
         ▼
ManageAnalyzeTickerUseCase.getStockData()  (interface)
         │
         ▼
AnalyzeAndPersistStockService.analyzeAndPersist()  @Transactional
         │
         ├─► getDataFromProvider(ticker)
         │      │
         │      ├─► stockProviderPort.getQuote(ticker)  → FinnhubAdapter → Finnhub API
         │      │      Returns: Stock (price, open, high, low, prevClose, volume)
         │      │
         │      ├─► Check cache: stockDataRepository.findByTickerAndLastUpdateBetween(today)
         │      │      │
         │      │      ├─► YES → applyCachedDailyMetrics() → copia SMA/EMA/RSI/MACD/BB/ATR/volume
         │      │      │
         │      │      └─► NO  → enrichWithFreshHistoricalIndicators()
         │      │                │
         │      │                ├─► historicalProviderPort.fetchHistoricalData(ticker) → PolygonAdapter
         │      │                │      │
         │      │                │      ├─► waitForRateLimit() (5 calls/min, 62s window)
         │      │                │      ├─► GET /v2/aggs/ticker/{t}/range/1/day/{from}/{to}?adjusted=true
         │      │                │      └─► parseApiResponse() → HistoricalData {prices[], volumes[], candles[]}
         │      │                │
         │      │                ├─► apiCallRateRepository.save(ticker, timestamp)
         │      │                ├─► candleHistoryRepository.saveCandlesForTicker(ticker, candles)
         │      │                │
         │      │                ├─► stockHistoricalService.calculateIndicators(historicalData, 20)
         │      │                │      │
         │      │                │      ├─► SMA 20/50/200
         │      │                │      ├─► EMA 9/12/20/26/50/200
         │      │                │      ├─► RSI 14/30
         │      │                │      ├─► MACD (12,26,9) → line, signal, histogram
         │      │                │      ├─► Bollinger Bands 20 (2σ) → upper, lower
         │      │                │      └─► ATR 14 (Wilder)
         │      │                │
         │      │                └─► applyTechnicalIndicators(stock, indicators)
         │      │
         │      └─► Returns enriched Stock
         │
         ├─► stock.setStrategyId(strategy.getId())
         ├─► stock.setOrigin(origin)
         ├─► savedStock = stockDataRepository.save(stock)
         │
         ├─► evaluateStrategyService.evaluateStrategy(strategy, savedStock)
         │      │
         │      ├─► strategy.validateConsistency() → cada rule.validate() → RuleCapabilityCatalog
         │      │
         │      ├─► For each rule: ruleEvaluator.evaluate(rule, stock)
         │      │      │
         │      │      ├─► resolveIndicator(subjectCode, subjectParam, stock) → RuleCapabilityCatalog
         │      │      ├─► resolveIndicator(targetCode, targetParam, stock) → RuleCapabilityCatalog
         │      │      ├─► evaluateOperator(operator, subject, target) → switch
         │      │      └─► buildJustification() → "PASSED: PRICE (150.00) > SMA20 (145.00)"
         │      │
         │      ├─► calculateMetrics() → {totalRules, passedRules, failedRules}
         │      ├─► determineOverallResult() → AND lógico (all passed)
         │      ├─► generateSummary()
         │      │
         │      ├─► If overallPassed → RiskRewardCalculator
         │      │      ├─► calculateTargetPrice(entry, objective, stock)
         │      │      ├─► calculateStopLossPrice(entry, objective, stock)
         │      │      ├─► calculateRiskRewardRatio(entry, target, stop)
         │      │      └─► calculatePositionSize(entry, stop, capitalToRisk)
         │      │
         │      └─► Returns StrategyEvaluation (compliant, complianceRate, summary, target, stop, R:R, shares)
         │
         ├─► strategyEvaluationRepository.save(evaluationResult, savedStock)
         ├─► savedStock.setStrategyEvaluation(evaluationResult)
         │
         └─► Returns savedStock → Controller → Redirect + Flash message
```

---

## 3. RuleEvaluator — Motor Determinista

```java
// Entrada: Rule + Stock
// Salida: RuleResult {rule, passed, justification}

evaluate(rule, stock):
  1. subjectValue = resolveIndicator(rule.subjectCode, rule.subjectParam, stock)
  2. targetValue  = resolveIndicator(rule.targetCode,  rule.targetParam,  stock)
  3. if null → FAILED "Missing data for X"
  4. passed = evaluateOperator(rule.operator, subjectValue, targetValue)
  5. justification = buildJustification(...)
  6. return RuleResult
```

**resolveIndicator delega a RuleCapabilityCatalog**:
```java
RuleCapability cap = catalog.getCapability(code).orElseThrow(...)
return cap.resolve(param, stock)  // lambda: (param, stock) -> stock.getSma20(), etc.
```

**Catálogo = Single Source of Truth**:
- `PRICE` → `stock.getCurrentPrice()`
- `SMA` (param 20/50/200) → `stock.getSma20/50/200()`
- `EMA` (9/12/20/26/50/200) → `stock.getEmaX()`
- `RSI` (14/30) → `stock.getRsi14/30()`
- `MACD_LINE/SIGNAL/HIST` → `stock.getMacdX()`
- `BB_UPPER/LOWER` (20) → `stock.getBbUpper20/Lower20()`
- `ATR` (14) → `stock.getAtr14()`
- `VOLUME/AVG_VOLUME` → `stock.getVolume()/getAverageVolume()`
- `CONSTANT/VALUE` → `BigDecimal.valueOf(param)`

**Operadores soportados**: `> >= < <= = == !=` (case-insensitive + aliases)

---

## 4. StockHistoricalService — Cálculo Indicadores

```java
calculateIndicators(HistoricalData data, volumePeriod):
  - Input: data.closingPrices[] (Polygon desc: most recent first), data.volumes[], data.candles[]
  - Output: TechnicalIndicators (all 18 indicators)
  
  SMA:  simple average of first N prices (desc = most recent N)
  EMA:  reverse → seed SMA → iterate multiplier 2/(N+1)
  RSI:  reverse → gains/losses over N periods → 100 - 100/(1+RS)
  MACD: EMA12 - EMA26 → signal = EMA9 of MACD → hist = line - signal
  BB:   SMA20 ± 2*StdDev20 (on most recent 20 prices, desc order)
  ATR:  reverse candles → TrueRange = max(H-L, |H-PC|, |L-PC|) → Wilder smoothing
  AvgVol: average of first N volumes
```

**Key insight**: Polygon devuelve **descendente** (más reciente primero). El servicio invierte internamente para cálculos que requieren orden cronológico (EMA, RSI, MACD, ATR), pero SMA/BB usan los primeros N del array desc (más recientes).

---

## 5. PolygonAdapter — Rate Limiting Manual

```java
// 5 calls/minute, ventana 62s
private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

waitForRateLimit():
  removeExpiredTimestamps()  // > 62s old
  while (size >= 5 && oldestCall != null):
    elapsed = now - oldestCall
    waitTime = 62000 - elapsed
    if (waitTime > 0) this.wait(waitTime + 200)
    removeExpiredTimestamps()
    refresh oldestCall

recordApiCall(): timestamps.addLast(now)
```

**Por qué RestTemplate y no WebClient**: Legacy, pero funciona. Candidato a migrar a WebClient reactivo (future work).

---

## 6. Dónde Me Perdo (y cómo navegar)

| Confusión | Resolución |
|-----------|------------|
| "¿Dónde se persiste el Stock?" | `AnalyzeAndPersistStockService` línea 79: `stockDataRepository.save(stock)` → `SqlStockDataRepository` → `JpaStockDataRepository.save(entity)` |
| "¿Cómo llega la Strategy al evaluador?" | Controller recibe `strategyId` → `ManageStrategyUseCase.getStrategyById()` → pasa `Strategy` domain a `analyzeAndPersist()` |
| "¿Dónde se calculan indicadores?" | `StockHistoricalService.calculateIndicators()` llamado desde `enrichWithFreshHistoricalIndicators()` |
| "¿RuleEvaluator vs EvaluateStrategyService?" | `RuleEvaluator` = 1 regla. `EvaluateStrategyService` = orquesta TODAS las reglas de una Strategy + métricas + R:R |
| "¿Puertos vs Adaptadores?" | Puerto = interfaz en `domain/port/out/`. Adaptador = implementación en `infrastructure/external/` o `persistence/` |

---

## 7. Puntos de Extensión (para scoring ponderado)

| Archivo | Qué tocar |
|---------|-----------|
| `Rule.java` | Añadir `weight` (int, default 1) |
| `Strategy.java` | Añadir `threshold` (int 0-100, default 100 = AND puro) |
| `EvaluateStrategyService.java` | Cambiar `determineOverallResult()` → weighted score |
| `AnalysisResult.java` | Añadir `score` field |
| `StrategyEvaluation.java` | Añadir `score` field |
| Tests | `RuleEvaluatorTest`, `EvaluateStrategyServiceTest` |

---

## 8. Comandos Útiles para Navegar

```bash
# Ver puertos (contratos)
find src/main/java -name "*Port.java" -path "*/domain/port/*" | head -20

# Ver adaptadores
find src/main/java -path "*/infrastructure/external/*" -name "*.java" | grep -v test

# Ver use cases
find src/main/java -path "*/application/usecase/*" -name "*.java" | grep -v test

# Ver servicios de dominio
find src/main/java -path "*/domain/service/*" -name "*.java" | grep -v test

# Ejecutar tests
./mvnw test -Dtest=RuleEvaluatorTest
./mvnw test -Dtest=EvaluateStrategyServiceTest
./mvnw verify  # full build + coverage + sonarqube
```

---

## 9. Mental Model Checklist (antes de tocar código)

- [ ] ¿Cambio en Domain? → No imports Spring/JPA, tests puros
- [ ] ¿Cambio en Application? → @Transactional si multiple writes, solo orquestación
- [ ] ¿Cambio en Infrastructure? → Implementa puerto, usa @Component, maneja errores externos
- [ ] ¿Nuevo indicador? → RuleCapabilityCatalog + StockHistoricalService + Stock fields + tests
- [ ] ¿Nueva regla? → Solo config (Rule entity), no código nuevo (Strategy pattern)

---

**Última actualización**: 2026-09-16 — Sesión arquitectura con OpenCode