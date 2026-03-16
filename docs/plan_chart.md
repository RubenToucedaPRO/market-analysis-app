# Plan de Implementación: Vista de Gráfico de Velas (Candlestick Chart)

**Fecha:** 2026-03-16  
**Autor:** Copilot Agent  
**Repositorio:** market-analysis-app  
**Stack:** Spring Boot 3.5.10 · Java 21 · Thymeleaf · Bootstrap 5 · H2/MariaDB

---

## Contexto y Análisis del Estado Actual

### Estado actual relevante

| Artefacto | Estado | Notas |
|---|---|---|
| `Candle` (domain model) | ✅ Existe | OHLCV + ticker + dateTime |
| `CandleEntity` (JPA) | ✅ Existe | FK `stocks_id`, sin `ticker` directo |
| `CandleMapper` | ✅ Existe | Domain ↔ Entity |
| `PolygonAdapter` | ⚠️ Parcial | Solo extrae `c` y `v`; ignora `o`, `h`, `l`, `t` |
| `HistoricalData` | ⚠️ Parcial | Solo `List<Double> closingPrices` y `List<Long> volumes` |
| `JpaCandleRepository` | ❌ No existe | Sin interfaz Spring Data JPA para velas |
| `CandleHistoryPort` | ❌ No existe | Puerto de persistencia de velas |
| `CandleChartRepository` | ❌ No existe | Puerto de consulta para el gráfico |
| `GetCandleChartUseCase` | ❌ No existe | Puerto de entrada del caso de uso |
| `GetCandleChartService` | ❌ No existe | Servicio de aplicación aislado |
| `CandleChartPointDTO` | ❌ No existe | Payload optimizado para el frontend |
| `CandleChartController` | ❌ No existe | Endpoint REST JSON |
| Mini-gráfico (UI) | ❌ No existe | Integrar en `ticker-detail.html` |
| Vista completa del gráfico | ❌ No existe | Nueva plantilla `ticker-chart.html` |

### Decisión arquitectónica: vínculo candle → ticker

`CandleEntity` tiene FK `stocks_id → StockEntity`. Para consultar velas por ticker sin romper la normalización se usará una `@Query` JPQL con JOIN. Se descarta añadir `ticker` directamente a `CandleEntity` para no duplicar información y mantener la integridad referencial.

### Decisión arquitectónica: librería de gráficos

Se elige **TradingView Lightweight Charts** (MIT, ~45 KB min+gzip) servida vía CDN, sin dependencias de npm ni de bundler. Razones:
- Compatible con Thymeleaf (HTML puro + `<script>`).
- Soporta candlestick nativo con OHLCV.
- Soporte de zonas SMA como series de líneas adicionales.
- No requiere Backend-for-Frontend ni configuración de bundler.
- Alternativa evaluada y descartada: Chart.js (no tiene candlestick nativo; requiere plugin de terceros con licencia más compleja).

### Decisión arquitectónica: endpoint REST separado

El gráfico se alimenta de un endpoint REST `GET /api/chart/{stockId}` independiente del flujo MVC de análisis. Esto permite:
- Carga asíncrona (AJAX/fetch) sin re-render de la página.
- Cabeceras de caché específicas.
- Evolución futura (WebSocket, streaming) sin afectar el flujo de análisis.

---

## Fase 1 — Dominio: Puertos y Modelos

**Objetivo:** Definir los contratos del dominio antes de cualquier implementación.  
**Regla:** Ninguna dependencia hacia infraestructura o presentación.

### 1.1 Enriquecer `HistoricalData` con velas completas

Actualmente `HistoricalData` solo transporta precios de cierre y volúmenes. El adaptador de Polygon ya tiene toda la información necesaria (`o`, `h`, `l`, `c`, `v`, `t`) pero la descarta. Se propone añadir `List<Candle> candles` al modelo de dominio `HistoricalData`:

```java
// domain/model/HistoricalData.java  (cambio mínimo)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoricalData {
    private String ticker;
    private List<Double> closingPrices;   // se mantiene para compatibilidad con indicadores
    private List<Long> volumes;           // se mantiene para compatibilidad con indicadores
    private List<Candle> candles;         // ← NUEVO: velas OHLCV completas en orden DESC
    private Instant lastUpdate;
}
```

**Importante:** El orden de las listas debe mantenerse coherente: `closingPrices[i]` y `volumes[i]` y `candles[i]` corresponden al mismo período temporal. Polygon devuelve datos en orden descendente (`sort=desc`); las listas en `HistoricalData` se mantienen en ese mismo orden DESC para no romper el cálculo de indicadores existente (que espera el precio más reciente primero). La conversión a orden ascendente para el gráfico se realiza en la **capa de repositorio** (`Collections.reverse` tras la consulta paginada), no en el servicio ni en el frontend. Así, el contrato del puerto `CandleChartRepository` garantiza siempre datos en orden cronológico ascendente.

### 1.2 Puerto de salida: `CandleHistoryPort`

Puerto de escritura de velas. Lo implementará la capa de persistencia.

```java
// domain/port/out/CandleHistoryPort.java
public interface CandleHistoryPort {

    /**
     * Persiste la lista de velas asociadas a un stock.
     * Si ya existen velas para la misma fecha y stock, se deben ignorar (idempotente).
     *
     * @param candles   lista de velas en cualquier orden
     * @param stockId   identificador del stock al que pertenecen
     */
    void saveAll(List<Candle> candles, Long stockId);
}
```

### 1.3 Puerto de salida: `CandleChartRepository`

Puerto de lectura para el caso de uso del gráfico. Separado de `CandleHistoryPort` para respetar la separación lectura/escritura (CQRS lite).

```java
// domain/port/out/CandleChartRepository.java
public interface CandleChartRepository {

    /**
     * Devuelve las últimas {@code limit} velas del stock, ordenadas
     * ascendentemente por timestamp (la más antigua primero).
     *
     * @param stockId   identificador del stock
     * @param limit     número máximo de velas a devolver (≤ 365)
     * @return lista vacía si no hay datos
     */
    List<Candle> findByStockIdOrderByDateTimeAsc(Long stockId, int limit);
}
```

### 1.4 Puerto de entrada: `GetCandleChartUseCase`

Caso de uso aislado de recuperación del gráfico.

```java
// domain/port/in/GetCandleChartUseCase.java
public interface GetCandleChartUseCase {

    /**
     * Obtiene el payload de velas optimizado para renderizar el gráfico.
     *
     * @param stockId   identificador del stock
     * @param days      número de días a mostrar (1–365, default 90)
     * @return respuesta con metadatos y lista de puntos del gráfico
     * @throws StockDataNotFoundException si el stock no existe
     */
    CandleChartResponseDTO getChartData(Long stockId, int days);
}
```

**Convención de nombres:** coherente con `ManageAnalyzeTickerUseCase` y `ManageStrategyUseCase` ya existentes.

### Tests de la Fase 1

- [ ] `GetCandleChartUseCaseContractTest`: verifica que el puerto de entrada tiene la firma correcta y que `CandleChartResponseDTO` es serializable.
- No requiere mocks complejos: solo validaciones de contrato.

---

## Fase 2 — Infraestructura: Persistencia de Velas

**Objetivo:** Implementar el repositorio JPA y el adaptador de escritura.

### 2.1 Añadir ticker implícito en consultas JPA

`CandleEntity` usa FK `stocks_id`. Las consultas JPQL harán JOIN con `StockEntity` para filtrar por `stockId`. No se altera el esquema de base de datos.

### 2.2 `JpaCandleRepository`

```java
// infrastructure/persistence/repository/JpaCandleRepository.java
@Repository
public interface JpaCandleRepository extends JpaRepository<CandleEntity, Long> {

    /**
     * Obtiene las últimas N velas de un stock, ordenadas por fecha ascendente.
     * Se usa JPQL con Pageable para limitar el resultado.
     */
    @Query("""
        SELECT c FROM CandleEntity c
        WHERE c.stock.id = :stockId
        ORDER BY c.dateTime DESC
        """)
    List<CandleEntity> findTopByStockIdOrderByDateTimeDesc(
            @Param("stockId") Long stockId,
            Pageable pageable);

    /**
     * Verifica si ya existe una vela para ese stock y fecha exacta (idempotencia).
     */
    boolean existsByStockIdAndDateTime(Long stockId, Instant dateTime);

    /**
     * Elimina todas las velas de un stock (útil cuando se borra el stock).
     */
    void deleteByStockId(Long stockId);
}
```

### 2.3 `SqlCandleChartRepository`

Implementa `CandleChartRepository`. Aplica el límite, invierte el orden (DESC en DB → ASC para gráfico).

```java
// infrastructure/persistence/repository/SqlCandleChartRepository.java
@Component
@RequiredArgsConstructor
public class SqlCandleChartRepository implements CandleChartRepository {

    private final JpaCandleRepository jpaCandleRepository;
    private final CandleMapper candleMapper;

    private static final int MAX_CANDLES = 365;

    @Override
    public List<Candle> findByStockIdOrderByDateTimeAsc(Long stockId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_CANDLES);
        Pageable pageable = PageRequest.of(0, safeLimit);

        List<CandleEntity> entities =
                jpaCandleRepository.findTopByStockIdOrderByDateTimeDesc(stockId, pageable);

        // Invertir: de DESC (más reciente primero) a ASC (cronológico) para el gráfico
        List<CandleEntity> ascending = new ArrayList<>(entities);
        Collections.reverse(ascending);

        return ascending.stream()
                .map(e -> candleMapper.toDomain(e))
                .filter(Objects::nonNull)
                .toList();
    }
}
```

### 2.4 `SqlCandleHistoryRepository`

Implementa `CandleHistoryPort`. Persiste velas en bloque, idempotente por fecha.

```java
// infrastructure/persistence/repository/SqlCandleHistoryRepository.java
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlCandleHistoryRepository implements CandleHistoryPort {

    private final JpaCandleRepository jpaCandleRepository;
    private final JpaStockDataRepository jpaStockDataRepository;
    private final CandleMapper candleMapper;

    @Override
    public void saveAll(List<Candle> candles, Long stockId) {
        if (candles == null || candles.isEmpty()) {
            log.debug("No candles to persist for stockId={}", stockId);
            return;
        }

        StockEntity stock = jpaStockDataRepository.findById(stockId)
                .orElseThrow(() -> new StockDataNotFoundException("Stock not found: " + stockId));

        int saved = 0;
        for (Candle candle : candles) {
            if (candle.getDateTime() == null) continue;
            // Idempotencia: skip si ya existe la misma fecha para este stock
            if (jpaCandleRepository.existsByStockIdAndDateTime(stockId, candle.getDateTime())) {
                continue;
            }
            CandleEntity entity = candleMapper.toEntity(candle);
            entity.setStock(stock);
            jpaCandleRepository.save(entity);
            saved++;
        }
        log.info("Persisted {} new candles for stockId={}", saved, stockId);
    }
}
```

### 2.5 Actualización de `PolygonAdapter`

El adaptador debe parsear todos los campos OHLCV del response de Polygon. La API devuelve resultados con los campos: `t` (timestamp epoch ms), `o`, `h`, `l`, `c`, `v`.

```java
// Método privado de mapeo actualizado en PolygonAdapter
private HistoricalData mapToHistoricalData(String ticker, String jsonBody) {
    List<Double> prices = new ArrayList<>();
    List<Long> volumes = new ArrayList<>();
    List<Candle> candles = new ArrayList<>();   // ← NUEVO
    try {
        JsonNode root = objectMapper.readTree(jsonBody);
        JsonNode resultsNode = root.path("results");

        if (resultsNode.isArray()) {
            for (JsonNode node : resultsNode) {
                double close = node.path("c").asDouble();
                long volume  = node.path("v").asLong();
                prices.add(close);
                volumes.add(volume);

                // ← NUEVO: parsear OHLCV completo + timestamp
                long timestampMs = node.path("t").asLong();
                if (timestampMs > 0) {
                    Instant dateTime = Instant.ofEpochMilli(timestampMs);
                    Candle candle = Candle.builder()
                            .ticker(ticker)
                            .dateTime(dateTime)                              // epoch ms UTC
                            .openPrice(BigDecimal.valueOf(node.path("o").asDouble()))
                            .highPrice(BigDecimal.valueOf(node.path("h").asDouble()))
                            .lowPrice(BigDecimal.valueOf(node.path("l").asDouble()))
                            .closePrice(BigDecimal.valueOf(close))
                            .volume(volume)
                            .build();
                    candles.add(candle);
                }
            }
        }
    } catch (Exception e) {
        throw new PolygonException("Error mapping historical data for " + ticker, e);
    }

    return HistoricalData.builder()
            .ticker(ticker)
            .closingPrices(prices)
            .volumes(volumes)
            .candles(candles)           // ← NUEVO
            .lastUpdate(Instant.now())
            .build();
}
```

**Nota temporal:** El campo `t` en Polygon es epoch **milisegundos** UTC. Se mantiene este formato en toda la cadena hasta el frontend, que también usa epoch ms.

### 2.6 Actualización de `ManageAnalyzeStockService`

Añadir `CandleHistoryPort` a la lista de dependencias y llamarlo tras calcular indicadores:

```java
// En getdataFromProvider(), tras:
//   apiCallRateRepository.save(ticker, historicalData.getLastUpdate());
// Añadir:

if (historicalData.getCandles() != null && !historicalData.getCandles().isEmpty()) {
    // La persistencia de candles es un efecto secundario, no bloquea el flujo
    candleHistoryPort.saveAll(historicalData.getCandles(), savedStock.getId());
}
```

**Importante:** La persistencia de velas debe ocurrir **después** de persistir el `Stock` (para tener el `stockId`). El conteo de parámetros del constructor de `ManageAnalyzeStockService` subirá de 11 a 12; se debe verificar el límite SonarQube S107 (máx 7). Si se supera, refactorizar usando un objeto `ManageAnalyzeStockService.Dependencies` record para agrupar dependencias.

### Tests de la Fase 2

- [ ] `SqlCandleChartRepositoryTest`: verifica límite, orden ASC, lista vacía.
- [ ] `SqlCandleHistoryRepositoryTest`: verifica idempotencia, skip en duplicados, log de conteo.
- [ ] `PolygonAdapterCandleParsingTest`: verifica que `t`, `o`, `h`, `l`, `c`, `v` se parsean correctamente; mock de RestTemplate con JSON de ejemplo de Polygon.

---

## Fase 3 — Aplicación: Caso de Uso Aislado

**Objetivo:** Implementar el caso de uso `GetCandleChartUseCase` sin acoplar lógica del gráfico al flujo de análisis existente.

### 3.1 DTO de payload optimizado

El payload debe ser mínimo y directo al formato esperado por TradingView Lightweight Charts.

```java
// application/dto/CandleChartPointDTO.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CandleChartPointDTO {
    /** Timestamp en epoch milisegundos UTC */
    private long time;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
}
```

```java
// application/dto/CandleChartResponseDTO.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CandleChartResponseDTO {
    private String ticker;
    private Long stockId;
    /** Número de días solicitados */
    private int requestedDays;
    /** Número de puntos realmente devueltos */
    private int pointCount;
    /** Si la lista está vacía, indica el motivo */
    private String emptyReason;
    /** Lista ordenada ascendentemente por time */
    private List<CandleChartPointDTO> candles;
}
```

### 3.2 `GetCandleChartService`

```java
// application/usecase/GetCandleChartService.java
@RequiredArgsConstructor
@Slf4j
public class GetCandleChartService implements GetCandleChartUseCase {

    private static final int DEFAULT_DAYS = 90;
    private static final int MAX_DAYS = 365;

    private final StockDataRepository stockDataRepository;
    private final CandleChartRepository candleChartRepository;

    @Override
    public CandleChartResponseDTO getChartData(Long stockId, int days) {
        // 1. Validar parámetros
        int safeDays = (days <= 0 || days > MAX_DAYS) ? DEFAULT_DAYS : days;

        // 2. Verificar existencia del stock
        Stock stock = stockDataRepository.findById(stockId)
                .orElseThrow(() -> new StockDataNotFoundException("Stock not found: " + stockId));

        // 3. Obtener velas (ya ordenadas ASC por el repositorio)
        List<Candle> candles = candleChartRepository.findByStockIdOrderByDateTimeAsc(stockId, safeDays);

        // 4. Verificar consistencia: no debe haber timestamps duplicados
        boolean hasDuplicates = candles.stream()
                .map(Candle::getDateTime)
                .distinct()
                .count() < candles.size();
        if (hasDuplicates) {
            log.warn("Duplicate timestamps detected for stockId={}. Chart may be inconsistent.", stockId);
        }

        // 5. Mapear a DTO
        List<CandleChartPointDTO> points = candles.stream()
                .map(c -> CandleChartPointDTO.builder()
                        .time(c.getDateTime().toEpochMilli())   // epoch ms UTC
                        .open(c.getOpenPrice())
                        .high(c.getHighPrice())
                        .low(c.getLowPrice())
                        .close(c.getClosePrice())
                        .volume(c.getVolume())
                        .build())
                .toList();

        // 6. Estado vacío explícito
        String emptyReason = null;
        if (points.isEmpty()) {
            emptyReason = "No hay datos de velas disponibles. Actualiza el ticker para cargar el histórico.";
            log.info("No candle data available for stockId={}", stockId);
        }

        return CandleChartResponseDTO.builder()
                .ticker(stock.getTicker())
                .stockId(stockId)
                .requestedDays(safeDays)
                .pointCount(points.size())
                .emptyReason(emptyReason)
                .candles(points)
                .build();
    }
}
```

### 3.3 Registro del bean en configuración Spring

Añadir `GetCandleChartService` al `@Configuration` de la capa de aplicación (si existe), similar al patrón de `ManageAnalyzeStockService`. Si se usa auto-detección de componentes, anotar con `@Service`.

### Tests de la Fase 3

- [ ] `GetCandleChartServiceTest`:
  - `getChartData_conDatosValidos_devuelveListaOrdenadaAsc()`
  - `getChartData_stockNoExiste_lanzaStockDataNotFoundException()`
  - `getChartData_sinVelas_devuelveListaVaciaConEmptyReason()`
  - `getChartData_diasFueraDeRango_usaDefault()`
  - `getChartData_conTimestampsDuplicados_logWarn()`

---

## Fase 4 — Presentación: Endpoint REST del Gráfico

**Objetivo:** Exponer los datos de velas como JSON para consumo asíncrono del frontend.

### 4.1 `CandleChartController`

```java
// presentation/controller/CandleChartController.java
@RestController
@RequestMapping("/api/chart")
@RequiredArgsConstructor
@Slf4j
public class CandleChartController {

    private static final int DEFAULT_DAYS = 90;
    private static final int MAX_DAYS = 365;
    private static final int CACHE_SECONDS = 300; // 5 minutos

    private final GetCandleChartUseCase getCandleChartUseCase;

    /**
     * Devuelve las velas OHLCV para el gráfico del stock indicado.
     * Las velas están ordenadas ascendentemente por timestamp.
     *
     * Ejemplo: GET /api/chart/42?days=90
     *
     * @param stockId  ID del stock persistido
     * @param days     número de días a incluir (1-365, default 90)
     */
    @GetMapping("/{stockId}")
    public ResponseEntity<CandleChartResponseDTO> getChartData(
            @PathVariable Long stockId,
            @RequestParam(defaultValue = "90") int days) {

        int safeDays = Math.min(Math.max(days, 1), MAX_DAYS);
        log.debug("Chart data requested for stockId={}, days={}", stockId, safeDays);

        CandleChartResponseDTO response = getCandleChartUseCase.getChartData(stockId, safeDays);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_SECONDS, TimeUnit.SECONDS).mustRevalidate())
                .eTag(String.valueOf(response.getPointCount()))
                .body(response);
    }
}
```

**Cabeceras de caché:**
- `Cache-Control: max-age=300, must-revalidate` — datos de mercado pueden cambiar; 5 minutos es un compromiso razonable para datos históricos diarios.
- `ETag` basado en el número de puntos — permite al cliente detectar si hay nuevos datos sin descargar el payload completo.

### 4.2 Manejo de errores

Reutilizar el `@ControllerAdvice` existente (en `presentation/exception/`) para manejar:
- `StockDataNotFoundException` → 404 con mensaje JSON claro.
- `IllegalArgumentException` → 400.
- `Exception` genérica → 500 sin exponer stack trace.

Si no existe un `@ControllerAdvice` con soporte JSON (puede estar orientado a HTML), crear `CandleChartExceptionHandler` limitado al `CandleChartController` con `@ControllerAdvice(assignableTypes = CandleChartController.class)`.

Formato de error estándar:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Stock not found: 99",
  "timestamp": "2026-03-16T11:00:00Z"
}
```

### Tests de la Fase 4

- [ ] `CandleChartControllerTest` (MockMvc):
  - `GET /api/chart/1` → 200 con JSON válido y cabeceras de caché.
  - `GET /api/chart/1?days=30` → 200 con `requestedDays=30`.
  - `GET /api/chart/999` → 404 cuando el stock no existe.
  - `GET /api/chart/1?days=0` → usa default 90.
  - `GET /api/chart/1?days=999` → usa max 365.
  - Verificar que `candles` está en orden ascendente por `time`.

---

## Fase 5 — Frontend: Vistas del Gráfico

**Objetivo:** Integrar el gráfico en la UI con separación clara entre carga de datos y renderizado.

### 5.1 Librería: TradingView Lightweight Charts

**CDN (sin npm, sin bundler):**
```html
<script src="https://unpkg.com/lightweight-charts@4.2.0/dist/lightweight-charts.standalone.production.js"></script>
```

Se recomienda fijar la versión (`@4.2.0`) en el tag para evitar cambios silenciosos. Verificar con `gh-advisory-database` antes de incluir.

### 5.2 Mini-gráfico en `ticker-detail.html`

Añadir una card con el contenedor del gráfico mini (altura ~200px) antes de los indicadores técnicos. Incluir enlace a la vista completa.

**Estructura HTML a añadir en `ticker-detail.html`:**

```html
<!-- Mini Chart Card -->
<div class="card border-0 shadow-sm mb-4">
    <div class="card-header bg-white border-0 py-3 d-flex justify-content-between align-items-center">
        <h6 class="mb-0 fw-bold">
            <i class="bi bi-bar-chart-line me-2"></i>Histórico de Precios (90 días)
        </h6>
        <a th:href="@{/analysis/ticker/{id}/chart(id=${ticker.id})}"
           class="btn btn-sm btn-outline-primary">
            <i class="bi bi-fullscreen me-1"></i>Ver detalle
        </a>
    </div>
    <div class="card-body p-0">
        <!-- Estado vacío: visible hasta que JS cargue los datos -->
        <div id="chart-empty-state" class="text-center py-5 text-muted" style="display:none">
            <i class="bi bi-bar-chart-line display-4"></i>
            <p class="mt-2" id="chart-empty-message">Sin datos de velas disponibles.</p>
        </div>
        <!-- Estado de carga -->
        <div id="chart-loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
        </div>
        <!-- Contenedor del gráfico -->
        <div id="mini-chart-container"
             th:data-stock-id="${ticker.id}"
             style="height: 200px; display: none;"></div>
    </div>
</div>
```

### 5.3 Nueva vista completa: `analysis/ticker-chart.html`

Ruta: `GET /analysis/ticker/{id}/chart`  
Plantilla nueva Thymeleaf. Contiene:
- Navbar reutilizable (`th:replace`).
- Cabecera con ticker, nombre y precio.
- Gráfico de velas a pantalla completa (altura ~500px).
- Selector de rango temporal: 30d / 90d / 180d / 1y (botones que recargan el gráfico JS).
- Indicadores SMA 20/50/200 como series de líneas superpuestas.
- Volumen en subgráfico inferior.
- Botón "Volver al detalle".
- Estado vacío con mensaje y botón de actualización.

**Estructura de la plantilla:**

```html
<!-- analysis/ticker-chart.html (esqueleto) -->
<!doctype html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
  <title th:text="${ticker.ticker} + ' - Gráfico | AlphaSeeker'">Chart</title>
  <!-- Bootstrap, Bootstrap Icons, styles.css -->
</head>
<body>
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <main class="container-fluid my-3">
    <!-- Cabecera: ticker, precio, variación -->
    <!-- Botones de rango: 30d / 90d / 180d / 1y -->
    <!-- Contenedor del gráfico principal -->
    <div id="chart-container"
         th:data-stock-id="${ticker.id}"
         th:data-sma20="${ticker.sma20}"
         th:data-sma50="${ticker.sma50}"
         th:data-sma200="${ticker.sma200}"
         style="height: 500px; position: relative;">
      <!-- spinner de carga y estado vacío (idéntico al mini) -->
    </div>
    <!-- Subgráfico de volumen -->
    <div id="volume-chart-container" style="height: 120px;"></div>
    <!-- Botón volver -->
  </main>
  <script src="lightweight-charts CDN"></script>
  <script th:src="@{/js/candle-chart.js}"></script>
</body>
</html>
```

### 5.4 Módulo JavaScript: `static/js/candle-chart.js`

**Separación de responsabilidades JS:**

| Función | Responsabilidad |
|---|---|
| `loadChartData(stockId, days)` | Solo fetch de datos (`/api/chart/{stockId}?days={days}`) |
| `renderCandleChart(container, data)` | Solo renderizado; no hace fetch |
| `renderVolumeChart(container, data)` | Solo renderizado del subgráfico de volumen |
| `renderSmaLines(chart, smaValues)` | Añade series de líneas SMA |
| `showLoading() / hideLoading()` | Control de estado visual |
| `showEmptyState(message)` | Estado sin datos |
| `initChart(stockId, days)` | Orquestador: llama a load + render |

**Esquema del módulo:**

```javascript
// static/js/candle-chart.js

const ChartModule = (() => {
    'use strict';

    const API_BASE = '/api/chart';

    async function loadChartData(stockId, days) {
        const response = await fetch(`${API_BASE}/${stockId}?days=${days}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to load chart data`);
        }
        return response.json();
    }

    function renderCandleChart(container, candles) {
        // TradingView Lightweight Charts API
        const chart = LightweightCharts.createChart(container, {
            width: container.clientWidth,
            height: container.clientHeight,
            layout: { background: { color: '#ffffff' }, textColor: '#333' },
            grid: { vertLines: { color: '#f0f0f0' }, horzLines: { color: '#f0f0f0' } },
            timeScale: { timeVisible: true, secondsVisible: false, borderColor: '#ddd' },
        });

        const candleSeries = chart.addCandlestickSeries({
            upColor: '#26a69a',
            downColor: '#ef5350',
            borderVisible: false,
            wickUpColor: '#26a69a',
            wickDownColor: '#ef5350',
        });

        // Convertir epoch ms → segundos (Lightweight Charts usa segundos UTC)
        const chartData = candles.map(c => ({
            time: Math.floor(c.time / 1000),
            open: parseFloat(c.open),
            high: parseFloat(c.high),
            low: parseFloat(c.low),
            close: parseFloat(c.close),
        }));

        candleSeries.setData(chartData);
        chart.timeScale().fitContent();

        // Hacer el gráfico responsive
        new ResizeObserver(() => {
            chart.applyOptions({ width: container.clientWidth });
        }).observe(container);

        return { chart, candleSeries };
    }

    function renderSmaLines(chart, lastClose, smaValues) {
        // smaValues: { sma20, sma50, sma200 } (valores escalares del último cálculo)
        // Se renderizan como líneas horizontales de referencia si no se dispone
        // de series completas de SMA. En futuras fases, calcular en frontend.
        const colors = { sma20: '#1e88e5', sma50: '#fb8c00', sma200: '#8e24aa' };
        Object.entries(smaValues).forEach(([key, value]) => {
            if (!value) return;
            const line = chart.addLineSeries({ color: colors[key], lineWidth: 1,
                                               title: key.toUpperCase() });
            // Placeholder: misma y-value en todo el rango (a reemplazar por serie real)
            line.setData([{ time: Math.floor(Date.now()/1000) - 86400, value: parseFloat(value) }]);
        });
    }

    function showLoading(loadingEl, containerEl) {
        if (loadingEl) loadingEl.style.display = 'block';
        if (containerEl) containerEl.style.display = 'none';
    }

    function hideLoading(loadingEl) {
        if (loadingEl) loadingEl.style.display = 'none';
    }

    function showEmptyState(emptyEl, messageEl, message) {
        if (emptyEl) emptyEl.style.display = 'block';
        if (messageEl && message) messageEl.textContent = message;
    }

    async function initChart(containerId, stockId, days) {
        const container = document.getElementById(containerId);
        const loadingEl = document.getElementById('chart-loading');
        const emptyEl = document.getElementById('chart-empty-state');
        const emptyMsg = document.getElementById('chart-empty-message');

        if (!container || !stockId) return;

        showLoading(loadingEl, container);

        try {
            const data = await loadChartData(stockId, days);

            hideLoading(loadingEl);

            if (!data.candles || data.candles.length === 0) {
                showEmptyState(emptyEl, emptyMsg, data.emptyReason || 'Sin datos disponibles.');
                return;
            }

            container.style.display = 'block';
            const { chart } = renderCandleChart(container, data.candles);

            // SMA lines (desde data-attributes de Thymeleaf)
            const smaValues = {
                sma20: container.dataset.sma20,
                sma50: container.dataset.sma50,
                sma200: container.dataset.sma200,
            };
            renderSmaLines(chart, null, smaValues);

        } catch (err) {
            hideLoading(loadingEl);
            showEmptyState(emptyEl, emptyMsg, 'Error al cargar el gráfico. Inténtalo de nuevo.');
            console.error('Chart error:', err);
        }
    }

    // API pública del módulo
    return { initChart, loadChartData, renderCandleChart };
})();

// Auto-init en páginas que tengan el contenedor
document.addEventListener('DOMContentLoaded', () => {
    const miniContainer = document.getElementById('mini-chart-container');
    if (miniContainer) {
        const stockId = miniContainer.dataset.stockId;
        ChartModule.initChart('mini-chart-container', stockId, 90);
    }

    const fullContainer = document.getElementById('chart-container');
    if (fullContainer) {
        const stockId = fullContainer.dataset.stockId;
        const defaultDays = 90;
        ChartModule.initChart('chart-container', stockId, defaultDays);

        // Botones de rango (solo en vista completa)
        document.querySelectorAll('[data-chart-days]').forEach(btn => {
            btn.addEventListener('click', () => {
                const days = parseInt(btn.dataset.chartDays, 10);
                fullContainer.innerHTML = '';
                ChartModule.initChart('chart-container', stockId, days);
                // Actualizar estado activo de botones
                document.querySelectorAll('[data-chart-days]').forEach(b =>
                    b.classList.remove('active'));
                btn.classList.add('active');
            });
        });
    }
});
```

### 5.5 Ruta MVC para la vista completa

Añadir en `AnalyzeTickerController`:

```java
// GET /analysis/ticker/{id}/chart
@GetMapping("/ticker/{id}/chart")
public String getTickerChart(@PathVariable Long id, Model model) {
    StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
    model.addAttribute("ticker", ticker);
    return "analysis/ticker-chart";
}
```

### Estados visuales del gráfico

| Estado | Componente visible | Trigger |
|---|---|---|
| Cargando | Spinner Bootstrap | Inmediatamente al cargar la página |
| Con datos | Gráfico de velas | Respuesta del API con `candles.length > 0` |
| Sin datos | Card con mensaje + botón Actualizar | `pointCount == 0` o `emptyReason != null` |
| Error de red | Card con mensaje de error | `fetch()` lanza excepción |

### Tests de la Fase 5

- [ ] `CandleChartControllerMvcTest`: verifica que `GET /analysis/ticker/{id}/chart` devuelve 200 y la vista correcta.
- [ ] Test visual manual (screenshot): verificar que mini chart aparece en ticker-detail.html y que la vista completa se muestra correctamente.

---

## Fase 6 — Verificación y Cierre

### 6.1 Checklist de verificación arquitectónica

- [ ] Ninguna dependencia de infraestructura en el dominio (`domain/` no importa nada de `infrastructure/`).
- [ ] `GetCandleChartService` solo depende de puertos (`StockDataRepository`, `CandleChartRepository`), nunca de JPA directamente.
- [ ] `CandleChartController` solo llama al caso de uso `GetCandleChartUseCase`, nunca al repositorio.
- [ ] El ordenamiento ASC es responsabilidad del repositorio, no del servicio ni del controlador.
- [ ] El payload JSON no expone entidades JPA (`CandleEntity`), solo DTOs.
- [ ] No hay lógica de negocio en Thymeleaf ni en `candle-chart.js`.
- [ ] Los timestamps en el payload JSON son siempre **epoch milliseconds UTC**.

### 6.2 Checklist de rendimiento

- [ ] La consulta `JpaCandleRepository` usa `Pageable` con `LIMIT` (evita cargar todo el historial).
- [ ] El endpoint REST tiene `Cache-Control: max-age=300`.
- [ ] El frontend no re-dibuja el gráfico salvo cambio explícito de rango.
- [ ] Para un stock con 300 velas, el payload JSON es ~30 KB (BigDecimal como string). Evaluar si comprimir con `Content-Encoding: gzip` (Spring Boot lo hace automáticamente si `server.compression.enabled=true`).

### 6.3 Checklist de consistencia de datos

- [ ] Verificar que no hay timestamps duplicados en la respuesta (validado en `GetCandleChartService` con log de warning).
- [ ] Verificar que todos los campos OHLCV son no nulos antes de construir el DTO.
- [ ] Verificar que `low ≤ close ≤ high` y `low ≤ open ≤ high` en el servicio (log de warning si se viola).

### 6.4 Extensibilidad futura (no implementar en esta fase)

| Extensión | Preparación actual |
|---|---|
| Rangos 1m, 5m (intradía) | `CandleChartRepository` acepta `int limit`; la fuente de datos solo necesita cambiar en el adaptador. |
| Zoom / selección de fechas | El frontend puede pasar `?startDate=&endDate=` en futuras versiones del endpoint. |
| Series de SMA calculadas en frontend | `CandleChartPointDTO` devuelve suficiente información para calcular SMAs en JS. |
| WebSocket para precios en tiempo real | `CandleChartController` es `@RestController` independiente; se puede añadir `@MessageMapping` sin afectar la vista. |
| Tests de integración con H2 | `SqlCandleChartRepository` y `SqlCandleHistoryRepository` son candidatos directos. |

### 6.5 Documentación a generar

Al completar cada fase, crear el correspondiente `task-<fecha>-<slug>.md` en `/docs/` siguiendo el patrón existente.

---

## Resumen de Archivos a Crear/Modificar

### Nuevos archivos

| Capa | Archivo | Descripción |
|---|---|---|
| Domain | `domain/port/in/GetCandleChartUseCase.java` | Puerto de entrada del caso de uso |
| Domain | `domain/port/out/CandleHistoryPort.java` | Puerto de escritura de velas |
| Domain | `domain/port/out/CandleChartRepository.java` | Puerto de consulta de velas |
| Application | `application/usecase/GetCandleChartService.java` | Servicio de aplicación |
| Application | `application/dto/CandleChartPointDTO.java` | DTO de un punto del gráfico |
| Application | `application/dto/CandleChartResponseDTO.java` | DTO de respuesta completa |
| Infrastructure | `infrastructure/persistence/repository/JpaCandleRepository.java` | Spring Data JPA |
| Infrastructure | `infrastructure/persistence/repository/SqlCandleChartRepository.java` | Implementa `CandleChartRepository` |
| Infrastructure | `infrastructure/persistence/repository/SqlCandleHistoryRepository.java` | Implementa `CandleHistoryPort` |
| Presentation | `presentation/controller/CandleChartController.java` | REST endpoint |
| Templates | `resources/templates/analysis/ticker-chart.html` | Vista completa del gráfico |
| Static | `resources/static/js/candle-chart.js` | Módulo JS de renderizado |
| Tests | `unit/application/usecase/GetCandleChartServiceTest.java` | Tests del servicio |
| Tests | `unit/presentation/controller/CandleChartControllerTest.java` | Tests del endpoint |
| Tests | `unit/infrastructure/repository/SqlCandleChartRepositoryTest.java` | Tests del repositorio |

### Archivos a modificar

| Archivo | Cambio |
|---|---|
| `domain/model/HistoricalData.java` | Añadir `List<Candle> candles` |
| `infrastructure/external/polygon/PolygonAdapter.java` | Parsear `t`, `o`, `h`, `l` del response |
| `application/usecase/ManageAnalyzeStockService.java` | Añadir `CandleHistoryPort`; persistir velas |
| `presentation/controller/AnalyzeTickerController.java` | Añadir ruta `/ticker/{id}/chart` |
| `resources/templates/analysis/ticker-detail.html` | Añadir card con mini-gráfico |
| Spring `@Configuration` de Application | Registrar `GetCandleChartService` como bean |

---

## Decisiones Arquitectónicas Clave Documentadas

1. **`CandleHistoryPort` vs. `CandleChartRepository` separados:** El principio de segregación de interfaces (ISP) justifica tener puertos distintos para escritura y lectura de velas. El caso de uso del gráfico no debe saber cómo se persisten las velas, y el flujo de análisis no debe saber cómo se consultan para renderizar.

2. **Orden temporal:** Polygon devuelve datos en DESC. La inversión a ASC se hace **en el repositorio** (`Collections.reverse` tras la consulta paginada), no en el servicio ni en el frontend. Esto garantiza que el contrato del puerto de salida siempre devuelva datos cronológicos.

3. **Formato de tiempo: epoch ms en JSON, epoch s en Lightweight Charts:** La API REST devuelve `time` en epoch ms (coherente con `Instant.toEpochMilli()` de Java). El módulo JS convierte a segundos (`Math.floor(c.time / 1000)`) antes de pasarlos a la librería, que usa epoch seconds. Esto evita perder información de sub-segundos en el futuro y mantiene el contrato JSON independiente de la librería de frontend.

4. **Sin ticker en `CandleEntity`:** Mantener la normalización relacional; las consultas usan JOIN con `StockEntity`. Si en el futuro el rendimiento es un problema, se puede añadir un índice compuesto `(stocks_id, date_time)`.

5. **Estado vacío explícito en el payload:** El campo `emptyReason` en `CandleChartResponseDTO` permite al frontend mostrar un mensaje descriptivo sin lógica adicional. El frontend no necesita inferir si los datos están vacíos; el backend lo dice explícitamente.

6. **SonarQube S107 (max 7 parámetros):** `ManageAnalyzeStockService` ya tiene 11 dependencias. Añadir `CandleHistoryPort` lo llevaría a 12. Se recomienda refactorizar agrupando dependencias en un record `ManageAnalyzeStockDependencies` antes o durante esta implementación.
