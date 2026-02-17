# 🚀 Roadmap de Implementación de Features - market-analysis-app

**Fecha:** 17 de Febrero de 2026  
**Objetivo:** Completar funcionalidad prometida para TFM  
**Metodología:** Desarrollo asistido por IA (Copilot, ChatGPT)  
**Tiempo estimado total:** 2-3 semanas

---

## 📋 Índice de Features Priorizadas

Las features están ordenadas por **prioridad e impacto** para el TFM:

1. **[CRÍTICO]** Cálculo de Risk:Reward (R:R) Real
2. **[ALTO]** Gráficos Interactivos con Chart.js
3. **[ALTO]** Tests de Integración con APIs Reales
4. **[ALTO]** Tracking Temporal de Evaluaciones
5. **[MEDIO]** Calendario de Ganancias (Earnings Calendar)
6. **[MEDIO]** Backtesting Básico sobre 90 días
7. **[MEDIO]** Comparación Side-by-Side de Estrategias
8. **MEDIO]** Mejora de Prompt Engineering (OpenAI)
9. **[BAJO]** Sistema de Alertas por Email
10. **[BAJO]** Exportación de Reportes (PDF/CSV)

---

## 🎯 Feature 1: Cálculo de Risk:Reward (R:R) Real

**Prioridad:** CRÍTICA  
**Impacto:** Alto - Feature prometida en README no implementada  
**Tiempo estimado:** 3-4 días  
**Complejidad:** Media-Alta

### Descripción
Implementar cálculo automático de la relación Riesgo/Beneficio basado en:
- Detección de niveles de soporte y resistencia
- Cálculo de stop-loss sugerido
- Cálculo de take-profit objetivo
- Relación R:R = (Target - Entry) / (Entry - Stop)

### Paso 1: Implementación del Dominio (Domain Layer)

**Objetivo:** Crear modelos y lógica de negocio pura para R:R

**Prompt para IA:**
```
Necesito implementar el cálculo de Risk:Reward en mi aplicación de análisis técnico.

CONTEXTO:
- Arquitectura hexagonal con Clean Architecture
- Domain layer debe ser puro (sin dependencias de frameworks)
- Ubicación: src/main/java/com/market/analysis/domain/

TAREA 1.1: Crear Value Object RiskRewardRatio
- Paquete: domain/model/
- Campos: BigDecimal entryPrice, BigDecimal stopLoss, BigDecimal takeProfit, BigDecimal ratio
- Método calculateRatio(): (takeProfit - entryPrice) / (entryPrice - stopLoss)
- Validaciones: stopLoss < entryPrice < takeProfit
- Inmutable con builder pattern
- Método isAcceptable(BigDecimal minRatio): verifica si R:R >= minRatio

TAREA 1.2: Crear SupportResistanceLevel Value Object
- Paquete: domain/model/
- Campos: BigDecimal level, String type (SUPPORT/RESISTANCE), int strength
- Método distanceToPrice(BigDecimal currentPrice): calcula distancia porcentual

TAREA 1.3: Crear RiskRewardCalculator (Domain Service)
- Paquete: domain/service/
- Método calculateRiskReward(Stock stock, List<Candle> historicalData): RiskRewardRatio
- Lógica:
  * Detectar soporte más cercano (mínimo de últimos 20 días)
  * Detectar resistencia más cercana (máximo de últimos 20 días)
  * Entry = precio actual
  * Stop = soporte - (2% del precio actual)
  * Target = resistencia
  * Calcular ratio

TAREA 1.4: Añadir campo riskReward a AnalysisResult
- Modificar: domain/model/AnalysisResult.java
- Añadir: private RiskRewardRatio riskReward
- Actualizar builder

Usa Java 21, Lombok, y sigue el estilo del código existente.
```

### Paso 2: Implementación Application e Infrastructure

**Objetivo:** Integrar R:R en casos de uso y persistencia

**Prompt para IA:**
```
Ahora necesito integrar el cálculo de Risk:Reward en las capas Application e Infrastructure.

CONTEXTO:
- Domain ya implementado con RiskRewardRatio y RiskRewardCalculator
- Uso caso principal: ManageAnalyzeStockService
- Persistencia: JPA con MariaDB/H2

TAREA 2.1: Actualizar ManageAnalyzeStockService
- Archivo: application/usecase/ManageAnalyzeStockService.java
- Inyectar RiskRewardCalculator en constructor
- En método analyzeStock(), después de evaluar estrategia:
  * Obtener historical data del stock
  * Calcular RiskReward: riskRewardCalculator.calculate(stock, historicalData)
  * Añadir al AnalysisResult
- Manejar excepción si cálculo falla (datos insuficientes)

TAREA 2.2: Crear RiskRewardDTO
- Paquete: application/dto/
- Campos: BigDecimal entryPrice, stopLoss, takeProfit, ratio, boolean acceptable
- Mapper de RiskRewardRatio → RiskRewardDTO

TAREA 2.3: Actualizar StockDataDTO
- Archivo: application/dto/StockDataDTO.java
- Añadir campo: RiskRewardDTO riskReward
- Actualizar mapper StockDataDTOMapper

TAREA 2.4: Persistencia - Crear RiskRewardEntity (embeddable)
- Paquete: infrastructure/persistence/entity/
- Anotación @Embeddable
- Campos: BigDecimal entryPrice, stopLoss, takeProfit, ratio
- Columnas: rr_entry_price, rr_stop_loss, rr_take_profit, rr_ratio

TAREA 2.5: Actualizar StockEntity
- Archivo: infrastructure/persistence/entity/StockEntity.java
- Añadir campo: @Embedded private RiskRewardEntity riskReward
- Actualizar mapper

TAREA 2.6: Crear tests unitarios
- RiskRewardCalculatorTest: validar lógica de cálculo
- ManageAnalyzeStockServiceTest: verificar integración
- Mocks para dependencias

Usa Spring Boot 3.5.x, JPA, Lombok. Sigue convenciones del proyecto.
```

### Paso 3: Desarrollo de la Vista (Presentation Layer)

**Objetivo:** Mostrar R:R en la interfaz de usuario

**Prompt para IA:**
```
Necesito mostrar el Risk:Reward calculado en la vista de análisis de ticker.

CONTEXTO:
- Frontend: Thymeleaf + Bootstrap 5 + HTMX
- Vista principal: templates/analysis/ticker-detail.html
- Estilo: Cards con badges para métricas

TAREA 3.1: Actualizar AnalyzeTickerController
- Archivo: presentation/controller/AnalyzeTickerController.java
- Método showTickerDetail() ya retorna StockDataDTO
- Verificar que riskReward está incluido en el modelo

TAREA 3.2: Crear fragment para Risk:Reward Card
- Archivo nuevo: templates/fragments/risk-reward-card.html
- Estructura:
  * Card header: "Risk:Reward Ratio" con icono 📊
  * Badge con ratio (verde si >= 2.0, amarillo si >= 1.5, rojo si < 1.5)
  * Mostrar Entry, Stop Loss, Take Profit con formato moneda
  * Gráfico visual simple (barra de progreso)
  * Recomendación textual basada en ratio

TAREA 3.3: Integrar fragment en ticker-detail.html
- Archivo: templates/analysis/ticker-detail.html
- Añadir después de la sección de Strategy Evaluation
- th:replace="fragments/risk-reward-card :: riskRewardCard(${stock})"
- Mostrar solo si riskReward != null

TAREA 3.4: Añadir estilos CSS
- Archivo: static/css/custom.css (o crear si no existe)
- Clases para badges de R:R
- Estilos para barra de progreso visual
- Responsive design para móviles

TAREA 3.5: Añadir interactividad con HTMX (opcional)
- Botón para recalcular R:R con parámetros personalizados
- Endpoint hx-post="/api/risk-reward/recalculate"
- Actualización parcial del card sin recargar página

Usa Thymeleaf 3.x, Bootstrap 5, iconos Bootstrap Icons. Mantén consistencia con vistas existentes.
```

### Paso 4: Testing y Validación

**Prompt para IA:**
```
Crear suite de tests completa para la feature Risk:Reward.

TAREA 4.1: Tests de Dominio
- RiskRewardCalculatorTest: casos de borde, datos insuficientes, validaciones
- RiskRewardRatioTest: validar construcción, cálculos, método isAcceptable

TAREA 4.2: Tests de Integración
- ManageAnalyzeStockServiceIntegrationTest con @SpringBootTest
- Mock de APIs externas pero DB real (H2)
- Verificar persistencia de R:R

TAREA 4.3: Tests de Controller
- AnalyzeTickerControllerTest con MockMvc
- Verificar que vista incluye datos de R:R
- Test de endpoint recalcular (si implementado)

TAREA 4.4: Tests End-to-End (manual)
- Checklist de validación:
  * Analizar ticker conocido (ej: AAPL)
  * Verificar R:R en vista
  * Validar cálculo manual vs automático
  * Probar con diferentes condiciones de mercado
```

---

## 📈 Feature 2: Gráficos Interactivos con Chart.js

**Prioridad:** ALTA  
**Impacto:** Alto - Mejora significativa de UX  
**Tiempo estimado:** 2-3 días  
**Complejidad:** Media

### Descripción
Integrar Chart.js para visualizar:
- Precio histórico con SMAs superpuestas
- Volumen en gráfico de barras
- Indicadores técnicos
- Señales de estrategia marcadas en el gráfico

### Paso 1: Implementación del Dominio

**Prompt para IA:**
```
Preparar datos del dominio para visualización en gráficos.

CONTEXTO:
- Ya existe HistoricalData y Candle en domain/model/
- Necesito estructurar datos para Chart.js

TAREA 1.1: Crear ChartDataPoint Value Object
- Paquete: domain/model/
- Campos: LocalDate date, BigDecimal value, String label
- Para series temporales genéricas

TAREA 1.2: Crear PriceChartData
- Paquete: domain/model/
- Campos: List<ChartDataPoint> prices, sma20, sma50, sma200, volume
- Método fromHistoricalData(HistoricalData data): convierte Candles a ChartDataPoints

TAREA 1.3: Actualizar Stock domain model
- Añadir método getPriceChartData(): PriceChartData
- Basado en historical data cargada
```

### Paso 2: Application e Infrastructure

**Prompt para IA:**
```
Implementar endpoint API REST para datos de gráficos.

TAREA 2.1: Crear ChartDataDTO
- Paquete: application/dto/
- Estructura compatible con Chart.js:
  * labels: List<String> (fechas)
  * datasets: List<DatasetDTO>
    - label: String (nombre serie)
    - data: List<BigDecimal> (valores)
    - borderColor, backgroundColor, type (line/bar)

TAREA 2.2: Crear ChartDataMapper
- Convierte PriceChartData → ChartDataDTO
- Formato fechas: "dd/MM/yyyy"
- Colores por defecto para cada serie

TAREA 2.3: Crear ChartDataController (REST)
- Paquete: presentation/controller/
- Endpoint GET /api/chart-data/{ticker}
- Retorna ChartDataDTO como JSON
- Manejo de errores 404 si ticker no existe

TAREA 2.4: Añadir CORS si es necesario
- Configurar en SecurityConfig
- Solo para endpoints /api/chart-data/*
```

### Paso 3: Desarrollo de Vista

**Prompt para IA:**
```
Integrar Chart.js en la vista de análisis de ticker.

TAREA 3.1: Añadir Chart.js CDN
- Archivo: templates/analysis/ticker-detail.html
- En <head>: <script src="https://cdn.jsdelivr.net/npm/chart.js@4"></script>

TAREA 3.2: Crear container para gráfico
- Añadir en ticker-detail.html después de company info:
  <div class="card mb-4">
    <div class="card-header">Análisis Técnico</div>
    <div class="card-body">
      <canvas id="priceChart" height="300"></canvas>
    </div>
  </div>

TAREA 3.3: Crear chart-loader.js
- Ubicación: static/js/chart-loader.js
- Función loadPriceChart(ticker):
  * Fetch /api/chart-data/{ticker}
  * Configurar Chart.js con datos recibidos
  * Múltiples datasets: precio, SMA20, SMA50, SMA200, volumen
  * Eje Y dual: precio (left), volumen (right)
  * Tooltips personalizados
  * Responsive: true

TAREA 3.4: Inicializar gráfico en página
- Añadir al final de ticker-detail.html:
  <script th:inline="javascript">
    document.addEventListener('DOMContentLoaded', function() {
      const ticker = /*[[${stock.ticker}]]*/ 'AAPL';
      loadPriceChart(ticker);
    });
  </script>

TAREA 3.5: Estilos y responsividad
- Asegurar que canvas se adapta a pantalla
- Loading spinner mientras carga datos
- Mensaje de error si falla carga
```

### Paso 4: Features Avanzadas del Gráfico

**Prompt para IA:**
```
Añadir funcionalidades avanzadas al gráfico.

TAREA 4.1: Marcadores de señales de estrategia
- Puntos en el gráfico donde strategy evaluó "PASSED"
- Iconos distintos para señales de compra/venta
- Tooltip con justificación de la señal

TAREA 4.2: Selector de rango temporal
- Botones: 1M, 3M, 6M, 1Y, ALL
- Filtrar datos sin recargar página
- Actualizar gráfico con animación

TAREA 4.3: Toggle de indicadores
- Checkboxes para mostrar/ocultar SMA20, SMA50, SMA200
- Toggle para volumen
- Persistir preferencias en localStorage

TAREA 4.4: Exportar gráfico como imagen
- Botón "Descargar gráfico"
- Usa chart.toBase64Image()
- Download como PNG con nombre ticker_fecha.png
```

---

## 🧪 Feature 3: Tests de Integración con APIs Reales

**Prioridad:** ALTA  
**Impacto:** Crítico - Garantiza calidad  
**Tiempo estimado:** 2 días  
**Complejidad:** Media

### Descripción
Implementar suite completa de tests de integración que validan:
- Llamadas reales a APIs externas (con rate limiting)
- Persistencia en base de datos
- Flujos end-to-end completos

### Paso 1: Configuración de Tests de Integración

**Prompt para IA:**
```
Configurar infraestructura para tests de integración.

TAREA 1.1: Crear perfil application-integration-test.yml
- Ubicación: src/test/resources/
- Configuración:
  * Base de datos H2 en modo test
  * Logs en nivel DEBUG
  * Timeouts reducidos para APIs
  * Rate limiting deshabilitado (para tests)

TAREA 1.2: Crear clase base IntegrationTestBase
- Paquete: src/test/java/com/market/analysis/
- Anotaciones:
  @SpringBootTest(webEnvironment = RANDOM_PORT)
  @ActiveProfiles("integration-test")
  @Transactional
  @Rollback
- Propiedades comunes para tests
- Setup de TestRestTemplate

TAREA 1.3: Configurar TestContainers (opcional)
- Añadir dependency testcontainers + testcontainers-mariadb
- Configurar container MariaDB para tests más realistas
- Inicialización en @BeforeAll
```

### Paso 2: Tests de Adaptadores Externos

**Prompt para IA:**
```
Crear tests de integración para adaptadores de APIs externas.

TAREA 2.1: FinnhubAdapterIntegrationTest
- Ubicación: infrastructure/external/finnhub/
- Tests:
  * testFetchQuote_RealAPI_Success(): ticker = "AAPL"
  * testFetchCompanyProfile_RealAPI_Success()
  * testFetchQuote_InvalidTicker_ThrowsException()
  * testRateLimiting_MultipleRequests()
- Usar @Tag("integration") para separar de unit tests
- Sleep entre requests para respetar rate limits

TAREA 2.2: PolygonAdapterIntegrationTest
- Tests:
  * testFetchHistoricalData_RealAPI_Returns300Candles()
  * testFetchTechnicalIndicators_ValidTicker_Success()
  * testFetchHistoricalData_InvalidDateRange_Handled()
- Verificar que datos retornados son consistentes

TAREA 2.3: OpenrouterAdapterIntegrationTest
- Tests:
  * testGetValoration_RealAPI_ReturnsText()
  * testGetValoration_EmptyInput_HandlesGracefully()
  * testResponseTime_UnderThreshold(): validar < 5 segundos
- Mock parcial: usa datos reales pero limita tokens
```

### Paso 3: Tests End-to-End de Casos de Uso

**Prompt para IA:**
```
Tests de integración para casos de uso completos.

TAREA 3.1: ManageAnalyzeStockServiceIntegrationTest
- Test completo del flujo:
  1. Llamar analyzeStock("AAPL")
  2. Verificar llamadas a APIs reales
  3. Validar persistencia en DB
  4. Verificar cálculo de R:R
  5. Validar generación de análisis IA
- Asserts:
  * Stock guardado en DB con ID
  * Historical data cargada (>= 200 candles)
  * SMAs calculadas correctamente
  * Strategy evaluada con resultados
  * RiskReward calculado (si hay datos suficientes)
  * Valoración IA generada (length > 0)

TAREA 3.2: StrategyEvaluationFlowIntegrationTest
- Flujo completo de evaluación de estrategia:
  1. Crear estrategia en DB
  2. Analizar múltiples tickers con esa estrategia
  3. Verificar evaluaciones persistidas
  4. Consultar histórico de evaluaciones
- Validar consistencia de datos

TAREA 3.3: ProhibitedTickerFlowIntegrationTest
- Test de filtrado:
  1. Añadir ticker a lista negra
  2. Intentar analizar ese ticker
  3. Verificar excepción lanzada
  4. Remover de lista negra
  5. Verificar que ahora se puede analizar
```

### Paso 4: Tests de Controladores Web

**Prompt para IA:**
```
Tests de integración de controladores con MockMvc.

TAREA 4.1: AnalyzeTickerControllerIntegrationTest
- Usar @WebMvcTest + @MockBean para servicios
- Tests:
  * testShowAnalysisPage_GET_ReturnsView()
  * testAnalyzeTicker_POST_ValidTicker_RedirectsToDetail()
  * testShowTickerDetail_GET_ExistingTicker_DisplaysData()
  * testAnalyzeTicker_POST_ProhibitedTicker_ShowsError()
- Verificar que vistas renderizan correctamente
- Validar bindings de modelo

TAREA 4.2: StrategyControllerIntegrationTest
- Tests CRUD completos:
  * testCreateStrategy_POST_Success()
  * testListStrategies_GET_DisplaysAll()
  * testUpdateStrategy_PUT_Success()
  * testDeleteStrategy_DELETE_RemovesFromDB()
- Verificar navegación entre vistas

TAREA 4.3: Configurar Maven Surefire
- Separar tests unitarios vs integración
- Profile "integration-test":
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
      <includes>
        <include>**/*IntegrationTest.java</include>
      </includes>
    </configuration>
  </plugin>
- Comando: mvn verify -P integration-test
```

---

## 📊 Feature 4: Tracking Temporal de Evaluaciones

**Prioridad:** ALTA  
**Impacto:** Alto - Permite análisis histórico  
**Tiempo estimado:** 2-3 días  
**Complejidad:** Media

### Descripción
Persistir historial de evaluaciones de estrategias para:
- Ver evolución temporal de un ticker
- Comparar performance de estrategias a lo largo del tiempo
- Detectar cambios de tendencia

### Paso 1: Implementación del Dominio

**Prompt para IA:**
```
Extender dominio para soportar tracking temporal.

TAREA 1.1: Modificar StrategyEvaluation
- Archivo: domain/model/StrategyEvaluation.java
- Añadir campo: boolean isLatest (marca evaluación más reciente)
- Método markAsHistorical(): pone isLatest = false

TAREA 1.2: Crear HistoricalEvaluationSummary Value Object
- Paquete: domain/model/
- Campos:
  * String ticker
  * String strategyName
  * List<EvaluationDataPoint> timeline
  * TrendAnalysis trend (NEW_VALUE_OBJECT)
- Método analyzePerformanceOverTime(): calcula tendencias

TAREA 1.3: Crear TrendAnalysis Value Object
- Campos:
  * String direction: IMPROVING, DECLINING, STABLE
  * BigDecimal changePercentage
  * int consecutivePasses, consecutiveFails
  * String analysis: texto descriptivo de la tendencia

TAREA 1.4: Crear puerto de salida HistoricalEvaluationRepository
- Paquete: domain/port/out/
- Métodos:
  * findByTickerAndStrategy(ticker, strategyId, limit, order): List<StrategyEvaluation>
  * findLatestByTicker(ticker): Optional<StrategyEvaluation>
  * countEvaluationsByTickerInDateRange(ticker, start, end): long
```

### Paso 2: Application e Infrastructure

**Prompt para IA:**
```
Implementar tracking en capa de aplicación y persistencia.

TAREA 2.1: Actualizar ManageAnalyzeStockService
- Al guardar nueva evaluación:
  1. Marcar evaluaciones anteriores del mismo ticker+strategy como isLatest=false
  2. Guardar nueva evaluación con isLatest=true
  3. Mantener histórico completo (no borrar)

TAREA 2.2: Crear ManageHistoricalEvaluationService (nuevo caso de uso)
- Paquete: application/usecase/
- Métodos:
  * getEvaluationHistory(ticker, strategyId, limit): HistoricalEvaluationSummary
  * compareStrategiesOverTime(ticker, List<strategyIds>, dateRange): ComparisonResult
  * detectTrendChanges(ticker, strategyId): List<TrendChangeAlert>

TAREA 2.3: Actualizar StrategyEvaluationEntity
- Archivo: infrastructure/persistence/entity/StrategyEvaluationEntity.java
- Añadir campo: boolean isLatest (columna is_latest)
- Índice en (ticker, strategy_id, is_latest) para queries rápidas

TAREA 2.4: Implementar HistoricalEvaluationRepositoryImpl
- Paquete: infrastructure/persistence/repository/
- Implementa puerto del dominio
- Queries JPA con ordenamiento por evaluatedAt DESC
- Paginación para grandes volúmenes

TAREA 2.5: Crear DTOs para histórico
- HistoricalEvaluationSummaryDTO
- EvaluationTimelineDTO
- TrendAnalysisDTO
```

### Paso 3: Desarrollo de Vista

**Prompt para IA:**
```
Crear vista para visualizar tracking temporal.

TAREA 3.1: Crear HistoricalEvaluationController
- Endpoint GET /analysis/history/{ticker}
- Retorna vista con histórico de evaluaciones
- Query params: strategyId (opcional), days (default 90)

TAREA 3.2: Crear vista history.html
- Ubicación: templates/analysis/history.html
- Estructura:
  * Header con nombre del ticker y selector de estrategia
  * Timeline visual (línea de tiempo con puntos)
  * Cada punto: fecha, resultado (PASS/FAIL), métricas clave
  * Sección de análisis de tendencia
  * Tabla detallada debajo del timeline

TAREA 3.3: Integrar gráfico de evolución
- Usar Chart.js para línea temporal
- Eje X: fechas
- Eje Y: compliance rate (% de reglas cumplidas)
- Líneas múltiples si se comparan varias estrategias
- Marcadores para cambios de tendencia

TAREA 3.4: Añadir link desde ticker-detail
- En ticker-detail.html añadir botón:
  "Ver historial de evaluaciones" → /analysis/history/{ticker}
- Badge con número de evaluaciones previas

TAREA 3.5: Filtros interactivos
- Selector de rango de fechas (date picker)
- Dropdown de estrategias disponibles
- Checkbox "Solo mostrar cambios significativos"
- Actualización con HTMX sin reload
```

### Paso 4: Análisis Automático de Tendencias

**Prompt para IA:**
```
Implementar análisis inteligente de tendencias.

TAREA 4.1: Crear TrendAnalyzer (Domain Service)
- Paquete: domain/service/
- Método analyzeTrend(List<StrategyEvaluation> history): TrendAnalysis
- Algoritmo:
  * Contar passes/fails consecutivos
  * Calcular moving average de compliance rate
  * Detectar crossovers (de failing a passing o viceversa)
  * Clasificar tendencia: IMPROVING, DECLINING, STABLE
  * Calcular confidence score (basado en cantidad de datos)

TAREA 4.2: Integrar análisis en ManageHistoricalEvaluationService
- Aplicar TrendAnalyzer a datos históricos
- Generar alertas si hay cambio significativo
- Incluir análisis en HistoricalEvaluationSummary

TAREA 4.3: Mejorar visualización de tendencias
- Iconos visuales: ↗️ ↘️ ➡️
- Colores: verde (improving), rojo (declining), gris (stable)
- Panel destacado si hay alerta de cambio de tendencia
- Recomendación textual basada en análisis

TAREA 4.4: Tests
- TrendAnalyzerTest con casos variados:
  * Tendencia clara al alza
  * Tendencia clara a la baja
  * Datos insuficientes (< 5 evaluaciones)
  * Volatilidad alta (sin tendencia clara)
```

---

## 📅 Feature 5: Calendario de Ganancias (Earnings Calendar)

**Prioridad:** MEDIA  
**Impacto:** Medio - Funcionalidad prometida  
**Tiempo estimado:** 1-2 días  
**Complejidad:** Baja-Media

### Descripción
Integrar datos de calendario de ganancias desde Finnhub para mostrar:
- Próximas fechas de earnings
- Earnings pasadas con surprises
- Alertas antes de eventos importantes

### Paso 1: Implementación del Dominio

**Prompt para IA:**
```
Añadir modelo de earnings calendar al dominio.

TAREA 1.1: Crear EarningsEvent Value Object
- Paquete: domain/model/
- Campos:
  * LocalDate date
  * String ticker
  * BigDecimal epsEstimate (estimación)
  * BigDecimal epsActual (si ya ocurrió)
  * BigDecimal epsSurprise (actual - estimate)
  * BigDecimal revenueEstimate
  * BigDecimal revenueActual
  * String fiscalPeriod (Q1 2024, etc)
  * boolean isUpcoming
- Métodos:
  * hasBeatEstimates(): verifica si actual > estimate
  * getSurprisePercentage(): (actual - estimate) / estimate * 100
  * getDaysUntilEvent(): días hasta earnings

TAREA 1.2: Crear EarningsCalendar Value Object
- Campos:
  * String ticker
  * List<EarningsEvent> upcomingEvents
  * List<EarningsEvent> historicalEvents
  * EarningsEvent nextEvent
- Método hasUpcomingEarnings(): boolean

TAREA 1.3: Extender puerto FinnhubPort
- Añadir método: fetchEarningsCalendar(ticker, from, to): List<EarningsEvent>
```

### Paso 2: Application e Infrastructure

**Prompt para IA:**
```
Integrar earnings calendar en la aplicación.

TAREA 2.1: Implementar fetchEarningsCalendar en FinnhubAdapter
- Archivo: infrastructure/external/finnhub/FinnhubAdapter.java
- Endpoint Finnhub: /calendar/earnings?symbol={ticker}&from={from}&to={to}
- Mapear respuesta JSON a EarningsEvent
- Manejar errores y rate limiting
- Cache de 1 hora (earnings no cambian frecuentemente)

TAREA 2.2: Crear EarningsCalendarService (Application)
- Paquete: application/usecase/
- Método getEarningsCalendar(ticker): EarningsCalendar
- Obtiene próximos 90 días y últimos 365 días
- Ordena eventos por fecha
- Marca isUpcoming según fecha actual

TAREA 2.3: Persistir earnings en BD (opcional)
- EarningsEventEntity con relación a StockEntity
- Repository para consultas históricas
- Job programado para actualizar diariamente

TAREA 2.4: Crear EarningsCalendarDTO
- Paquete: application/dto/
- Mapper de EarningsCalendar → DTO
- Incluye análisis de surprise promedio
```

### Paso 3: Desarrollo de Vista

**Prompt para IA:**
```
Mostrar earnings calendar en la interfaz.

TAREA 3.1: Crear fragment earnings-calendar.html
- Ubicación: templates/fragments/
- Secciones:
  * Próximo evento (destacado con countdown)
  * Timeline de eventos futuros (próximos 3 meses)
  * Histórico con surprises (últimos 4 quarters)
  * Gráfico de tendencia de EPS

TAREA 3.2: Integrar en ticker-detail.html
- Añadir card "Calendario de Ganancias" después de análisis técnico
- Mostrar solo si hay earnings disponibles
- Badge "Próximo earnings en X días" si está cerca (< 30 días)

TAREA 3.3: Crear vista dedicada earnings-calendar.html
- Endpoint: /analysis/earnings/{ticker}
- Calendario visual estilo Google Calendar
- Filtros por trimestre/año
- Comparación con earnings de competidores (futuro)

TAREA 3.4: Alertas visuales
- Badge rojo si earnings < 7 días (alta volatilidad esperada)
- Tooltip con disclaimers sobre trading en earnings
- Link a estrategia de earnings (si implementada)
```

### Paso 4: Integración con Análisis

**Prompt para IA:**
```
Usar earnings calendar en el análisis de ticker.

TAREA 4.1: Actualizar ManageAnalyzeStockService
- Incluir earnings calendar en análisis completo
- Añadir campo earningsCalendar a StockDataDTO

TAREA 4.2: Análisis de impacto de earnings
- Si earnings < 30 días:
  * Añadir warning en análisis
  * Ajustar nivel de riesgo
  * Sugerir esperar post-earnings para trading

TAREA 4.3: Integración con OpenAI
- Incluir info de próximo earnings en prompt
- Pedir análisis de riesgo pre-earnings
- Generar recomendación específica
```

---

## 🔄 Feature 6: Backtesting Básico sobre 90 días

**Prioridad:** MEDIA  
**Impacto:** Alto - Validación de estrategias  
**Tiempo estimado:** 3-4 días  
**Complejidad:** Alta

### Descripción
Implementar backtesting básico que evalúa estrategia sobre:
- Últimos 90 días de datos históricos
- Simulación de señales de entrada/salida
- Cálculo de métricas: win rate, profit/loss, max drawdown

### Paso 1: Implementación del Dominio

**Prompt para IA:**
```
Crear dominio para backtesting.

TAREA 1.1: Crear Trade Value Object
- Paquete: domain/model/
- Campos:
  * LocalDate entryDate, exitDate
  * BigDecimal entryPrice, exitPrice
  * int quantity
  * BigDecimal profitLoss
  * BigDecimal profitLossPercentage
  * String outcome: WIN/LOSS/BREAKEVEN
  * String exitReason: STOP_LOSS, TAKE_PROFIT, STRATEGY_EXIT, TIME_BASED

TAREA 1.2: Crear BacktestResult Value Object
- Campos:
  * String ticker, strategyName
  * LocalDate startDate, endDate
  * List<Trade> trades
  * BacktestMetrics metrics
  * BigDecimal totalProfitLoss
  * int totalTrades, winningTrades, losingTrades
- Métodos:
  * getWinRate(): winningTrades / totalTrades
  * getAverageProfitLoss()
  * getMaxDrawdown()

TAREA 1.3: Crear BacktestMetrics Value Object
- Campos:
  * BigDecimal winRate
  * BigDecimal avgWin, avgLoss
  * BigDecimal profitFactor (total wins / total losses)
  * BigDecimal maxDrawdown (peak to trough)
  * int consecutiveWins, consecutiveLosses
  * BigDecimal sharpeRatio (si es posible calcular)

TAREA 1.4: Crear BacktestEngine (Domain Service)
- Método runBacktest(Strategy, HistoricalData, BacktestConfig): BacktestResult
- Algoritmo:
  * Iterar día por día en historical data
  * Evaluar estrategia en cada día
  * Si PASS → simular entrada (si no hay posición abierta)
  * Monitorear posición abierta:
    - Salir si stop loss alcanzado
    - Salir si take profit alcanzado
    - Salir si estrategia da señal FAIL
  * Registrar cada trade en resultados
  * Calcular métricas al final
```

### Paso 2: Application e Infrastructure

**Prompt para IA:**
```
Implementar backtesting en application layer.

TAREA 2.1: Crear BacktestConfig
- Paquete: application/dto/
- Campos configurables:
  * BigDecimal initialCapital (default 10000)
  * BigDecimal positionSize (% de capital por trade, default 0.1)
  * BigDecimal stopLossPercentage (default 0.02 = 2%)
  * BigDecimal takeProfitPercentage (default 0.06 = 6%)
  * int maxHoldingDays (default 30)
  * boolean compoundReturns

TAREA 2.2: Crear RunBacktestService
- Paquete: application/usecase/
- Método runBacktest(ticker, strategyId, config): BacktestResult
- Flujo:
  1. Cargar strategy desde BD
  2. Obtener historical data (90 días + buffer para SMAs)
  3. Validar datos suficientes
  4. Ejecutar BacktestEngine
  5. Persistir resultado (opcional)
  6. Retornar DTO

TAREA 2.3: Persistir resultados de backtest
- BacktestResultEntity
- Relación con Strategy y Ticker
- Timestamp de ejecución
- JSON con trades detallados

TAREA 2.4: Crear DTOs
- BacktestResultDTO con métricas
- TradeDTO para cada operación
- BacktestConfigDTO para configuración
```

### Paso 3: Desarrollo de Vista

**Prompt para IA:**
```
Crear interfaz para backtesting.

TAREA 3.1: Crear BacktestController
- Endpoint GET /backtest/configure?ticker={ticker}&strategy={id}
  → Muestra formulario de configuración
- Endpoint POST /backtest/run
  → Ejecuta backtest y redirige a resultados
- Endpoint GET /backtest/results/{id}
  → Muestra resultados del backtest

TAREA 3.2: Vista backtest-configure.html
- Formulario con campos de BacktestConfig
- Sliders para stop loss % y take profit %
- Date range picker para período (default 90 días)
- Botón "Ejecutar Backtest"
- Loading spinner al submit

TAREA 3.3: Vista backtest-results.html
- Card con resumen de métricas:
  * Win Rate (con gauge visual)
  * Total P/L (verde/rojo según positivo/negativo)
  * Profit Factor
  * Max Drawdown
  * Sharpe Ratio
- Tabla de trades:
  * Fecha entrada/salida
  * Precio entrada/salida
  * P/L
  * Razón de salida
  * Sorting por columnas
- Gráfico de equity curve (Chart.js)
- Gráfico de distribución de P/L por trade

TAREA 3.4: Integrar desde strategy-detail.html
- Botón "Ejecutar Backtest" junto a detalles de estrategia
- Link a histórico de backtests previos
```

### Paso 4: Visualizaciones Avanzadas

**Prompt para IA:**
```
Mejorar visualización de resultados de backtest.

TAREA 4.1: Equity Curve Chart
- Gráfico línea mostrando evolución de capital
- Eje X: fechas de trades
- Eje Y: capital acumulado
- Línea horizontal en capital inicial
- Áreas sombreadas para drawdowns

TAREA 4.2: Distribución de Returns
- Histograma de P/L por trade
- Media y mediana marcadas
- Muestra asimetría (skewness)

TAREA 4.3: Calendario de trades
- Heatmap mostrando días con trades
- Verde para wins, rojo para losses
- Identificar patrones de timing

TAREA 4.4: Comparación de estrategias
- Vista para comparar backtests de múltiples estrategias
- Tabla comparativa de métricas
- Gráficos superpuestos de equity curves
```

---

## 🔀 Feature 7: Comparación Side-by-Side de Estrategias

**Prioridad:** MEDIA  
**Impacto:** Medio - Mejora usabilidad  
**Tiempo estimado:** 2 días  
**Complejidad:** Media

### Paso 1: Implementación del Dominio

**Prompt para IA:**
```
Extender dominio para comparación de estrategias.

TAREA 1.1: Crear StrategyComparison Value Object
- Paquete: domain/model/
- Campos:
  * String ticker
  * List<Strategy> strategies
  * Map<Long, StrategyEvaluation> evaluations (strategyId → evaluation)
  * ComparisonMetrics metrics

TAREA 1.2: Crear ComparisonMetrics Value Object
- Campos por estrategia:
  * boolean passed
  * BigDecimal complianceRate
  * RiskRewardRatio riskReward
  * int rulesCount
  * String recommendation
- Método getBestStrategy(): Strategy (basado en métricas)
```

### Paso 2: Application e Infrastructure

**Prompt para IA:**
```
Implementar servicio de comparación.

TAREA 2.1: Crear CompareStrategiesService
- Paquete: application/usecase/
- Método compareStrategies(ticker, List<strategyIds>): StrategyComparison
- Ejecuta evaluación de cada estrategia en paralelo
- Agrega resultados en objeto de comparación

TAREA 2.2: Optimización de consultas
- Evaluar todas las estrategias en una sola carga de datos
- Evitar N+1 queries
- Cache de historical data compartido
```

### Paso 3: Desarrollo de Vista

**Prompt para IA:**
```
Crear vista de comparación.

TAREA 3.1: Crear ComparisonController
- Endpoint GET /strategies/compare?ticker={ticker}&ids={id1,id2,...}
- Validar máximo 5 estrategias simultáneas

TAREA 3.2: Vista compare-strategies.html
- Tabla comparativa con columnas por estrategia
- Filas:
  * Nombre estrategia
  * Resultado general (badge PASS/FAIL)
  * Compliance rate (barra progreso)
  * R:R ratio
  * Número de reglas
  * Link a detalle
- Highlight de mejor estrategia
- Botón para añadir/quitar estrategias

TAREA 3.3: Selector de estrategias
- Dropdown múltiple para elegir estrategias
- Autocompletado con búsqueda
- Actualización dinámica de tabla
```

---

## 🤖 Feature 8: Mejora de Prompt Engineering (OpenAI)

**Prioridad:** MEDIA  
**Impacto:** Medio - Mejora calidad de análisis IA  
**Tiempo estimado:** 1 día  
**Complejidad:** Baja

### Paso 1: Mejorar Prompts

**Prompt para IA:**
```
Refactorizar construcción de prompts para OpenAI.

TAREA 1.1: Crear PromptBuilder (Domain Service)
- Paquete: domain/service/
- Método buildAnalysisPrompt(Stock, StrategyEvaluation, RiskReward): String
- Template estructurado:
  * Contexto del análisis
  * Datos técnicos del ticker
  * Resultados de estrategia
  * R:R calculado
  * Instrucciones específicas de formato de salida

TAREA 1.2: Prompt template con ejemplos (few-shot)
- Incluir 2-3 ejemplos de análisis bien hechos
- Chain-of-thought: pedir razonamiento paso a paso
- Formato estructurado de respuesta (sections)

TAREA 1.3: Validación de respuestas
- Verificar que respuesta contiene secciones esperadas
- Retry con prompt ajustado si falla
- Fallback a respuesta genérica si persiste error
```

### Paso 2: Configuración Avanzada

**Prompt para IA:**
```
Mejorar configuración de OpenAI integration.

TAREA 2.1: Parámetros de generación
- Temperature: 0.7 (balance creatividad/precisión)
- Max tokens: 500 (respuestas concisas)
- Top P: 0.9
- Frequency penalty: 0.5 (reducir repetición)

TAREA 2.2: Sistema de prompts
- System prompt definiendo rol del asistente
- Restricciones: no dar consejos de inversión
- Estilo: profesional pero accesible
```

---

## 📧 Feature 9: Sistema de Alertas por Email

**Prioridad:** BAJA  
**Impacto:** Medio - Engagement de usuarios  
**Tiempo estimado:** 2 días  
**Complejidad:** Media

### Paso 1: Configuración de Email

**Prompt para IA:**
```
Configurar envío de emails con Spring Boot.

TAREA 1.1: Añadir dependencia spring-boot-starter-mail
- Actualizar pom.xml

TAREA 1.2: Configuración SMTP
- application.yml con propiedades de mail
- Usar variables de entorno para credenciales
- Soporte para Gmail, SendGrid, etc.

TAREA 1.3: Crear EmailService
- Métodos para diferentes tipos de email
- Templates HTML con Thymeleaf
- Manejo de errores de envío
```

### Paso 2: Implementación de Alertas

**Prompt para IA:**
```
Implementar alertas basadas en eventos.

TAREA 2.1: Definir tipos de alertas
- Nueva señal de estrategia (PASS)
- Cambio de tendencia detectado
- Próximo earnings (7 días)
- Cambio significativo en R:R

TAREA 2.2: Sistema de suscripciones
- Usuario define alertas que quiere recibir
- Persistir preferencias en BD
- Gestión de suscripciones en UI
```

---

## 📄 Feature 10: Exportación de Reportes (PDF/CSV)

**Prioridad:** BAJA  
**Impacto:** Bajo - Nice to have  
**Tiempo estimado:** 1-2 días  
**Complejidad:** Baja-Media

### Paso 1: Exportación CSV

**Prompt para IA:**
```
Implementar exportación a CSV.

TAREA 1.1: Crear ExportService
- Método exportStrategyEvaluations(ticker, format): File
- Formato CSV con todas las métricas
- Headers descriptivos

TAREA 1.2: Endpoint de descarga
- GET /export/evaluations/{ticker}?format=csv
- Content-Type: text/csv
- Nombre archivo: ticker_estrategia_fecha.csv
```

### Paso 2: Exportación PDF

**Prompt para IA:**
```
Implementar reportes PDF.

TAREA 2.1: Añadir dependencia iText o similar
- pom.xml

TAREA 2.2: Template de reporte PDF
- Logo y header profesional
- Secciones: resumen, gráficos, métricas
- Footer con disclaimers

TAREA 2.3: Generación de PDF
- Incluir gráficos como imágenes
- Tabla de trades
- Análisis y recomendaciones
```

---

## 🎯 Resumen de Prioridades para el TFM

### Críticas (Semana 1)
1. Cálculo Risk:Reward Real
2. Gráficos Interactivos
3. Tests de Integración

### Altas (Semana 2)
4. Tracking Temporal de Evaluaciones
5. Calendario de Ganancias
6. Backtesting Básico

### Medias (Semana 3 - si hay tiempo)
7. Comparación de Estrategias
8. Mejora Prompt Engineering

### Bajas (Post-TFM - opcional)
9. Sistema de Alertas
10. Exportación de Reportes

---

## 📝 Notas de Implementación

### Uso de IA para Desarrollo

Para cada tarea, usar herramientas de IA:
- **GitHub Copilot:** Para generar código boilerplate y tests
- **ChatGPT/Claude:** Para planificar arquitectura y resolver problemas
- **Copilot Chat:** Para refactoring y optimización

### Documentación del Proceso

Documenta cada feature implementada:
- Screenshots del antes/después
- Decisiones técnicas tomadas
- Prompts efectivos que usaste
- Dificultades encontradas y soluciones

### Testing Continuo

Después de cada paso:
- Ejecutar tests unitarios: `mvn test`
- Ejecutar tests de integración: `mvn verify -P integration-test`
- Validar manualmente en navegador
- Commit incremental con mensaje descriptivo

---

## 🚀 Estrategia de Implementación

### Enfoque Ágil

1. **Implementar feature completa** (las 3-4 pasos)
2. **Validar y testear** exhaustivamente
3. **Documentar** con capturas y decisiones
4. **Commit y push**
5. **Pasar a siguiente feature**

### Evitar Scope Creep

- Implementar versión básica primero
- Iterar y mejorar después
- No añadir features no planificadas
- Priorizar funcionalidad sobre perfección

### Mantenimiento de Calidad

- Cobertura de tests > 80%
- SonarQube sin issues críticos
- Documentación actualizada
- README reflejando funcionalidad real

---

**Autor:** Análisis de features priorizadas  
**Fecha:** 17 de Febrero de 2026  
**Revisión:** 1.0
