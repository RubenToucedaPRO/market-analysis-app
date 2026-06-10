# Mejora de cobertura: ManageAnalyzeStockService

## Resumen

Se amplía la cobertura de tests unitarios de `ManageAnalyzeStockService` añadiendo 12 nuevos casos de prueba que cubren caminos de ejecución previamente no testados.

El fichero afectado es:
`src/test/java/com/market/analysis/unit/application/usecase/ManageAnalyzeStockServiceTest.java`

## Casos añadidos

### Validación de estrategia en `getStockData`

| Método de test | Comportamiento cubierto |
|---|---|
| `getStockData_nullStrategyId_throwsIllegalArgumentException` | Lanza `IllegalArgumentException` con mensaje `"Strategy ID is required"` cuando `strategyId` es `null` |
| `getStockData_strategyNotFound_throwsIllegalArgumentException` | Lanza `IllegalArgumentException` con el ID en el mensaje cuando la estrategia no existe en el repositorio |

### `updateStockData` – ticker no encontrado

| Método de test | Comportamiento cubierto |
|---|---|
| `updateStockData_stockNotFound_throwsStockDataNotFoundException` | Lanza `StockDataNotFoundException` con el ID en el mensaje cuando el stock no existe |

### Métricas en caché (`applyCachedDailyMetrics`)

| Método de test | Comportamiento cubierto |
|---|---|
| `getDataFromProvider_stockExistsForToday_usesCachedMetricsAndSkipsHistoricalProvider` | Cuando `findByTickerAndLastUpdateBetween` devuelve un stock existente del día, no se llama al proveedor histórico; se usan las métricas en caché |

### Indicadores técnicos nulos (`enrichWithFreshHistoricalIndicators`)

| Método de test | Comportamiento cubierto |
|---|---|
| `enrichWithFreshHistoricalIndicators_nullIndicators_stockSavedWithoutIndicators` | Cuando `calculateIndicators` devuelve `null`, el stock se guarda igualmente sin indicadores |

### Persistencia de velas (`persistCandlesIfPresent`)

| Método de test | Comportamiento cubierto |
|---|---|
| `shouldNotPersistCandlesWhenHistoricalDataCandlesIsNull` | Cuando la lista de velas en `HistoricalData` es `null`, no se llama a `saveCandlesForTicker` |

## Decisiones técnicas

- Se mantiene `@MockitoSettings(strictness = Strictness.LENIENT)` ya que el `@BeforeEach` configura stubs que no todos los tests consumen (necesario por diseño del setup compartido).
- Se añaden aserciones sobre el mensaje de las excepciones (`assertThat(ex.getMessage()).contains(...)`) para garantizar un feedback claro al consumidor de la API.

## Cobertura antes y después

| Métrica | Antes | Después |
|---|---|---|
| Tests totales | 20 | 32 |
| Caminos sin cubrir | 6 | 0 |

## Próximos pasos sugeridos

- Considerar tests de integración para el flujo completo de `getStockData` con H2.
- Revisar cobertura de `EvaluateStrategyService` de forma similar.
