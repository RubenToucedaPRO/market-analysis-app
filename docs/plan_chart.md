# Plan de Implementación: Gráfico de Velas en Ticker Detail

**Fecha:** 2026-03-16  
**Proyecto:** market-analysis-app  
**Objetivo:** Añadir un gráfico de velas (candlestick) en miniatura en `ticker-detail.html` y una vista de gráfico detallado con SMA20, SMA50 y SMA200.

---

## 1. Contexto y Estado Actual

### ¿Qué existe hoy?

| Componente | Estado |
|---|---|
| **Entidad `CandleEntity`** | ✅ Implementada (`candles` table, OHLCV + ticker + dateTime) |
| **`JpaCandleRepository`** | ✅ Implementado (incluye `findByTickerOrderByDateTimeAsc`) |
| **`CandleHistoryRepository` (port out)** | ✅ Implementado — pero solo escritura (`saveCandlesForTicker`, `deleteCandlesByTicker`) |
| **`SqlCandleHistoryRepository`** | ✅ Implementado — solo escritura |
| **`PolygonAdapter`** | ✅ Fetches 300 velas OHLCV y las persiste |
| **`StockHistoricalService`** | ✅ Calcula SMA20, SMA50, SMA200 (escalares, no series) |
| **`ticker-detail.html`** | ✅ Muestra SMA20/50/200 como texto; **no tiene gráfico** |
| **Librería de gráficos** | ❌ No existe ninguna integrada |
| **Endpoint REST de velas** | ❌ No existe |
| **Vista de gráfico detallado** | ❌ No existe |

### Flujo de datos actual (escritura)

```
PolygonAdapter → HistoricalData (List<Candle>)
    └─ ManageAnalyzeStockService → CandleHistoryRepository → JpaCandleRepository (DB)
```

### Flujo de datos necesario (lectura, nuevo)

```
Browser → GET /analysis/ticker/{id}/candles (JSON)
    └─ AnalyzeTickerController → ManageAnalyzeTickerUseCase.findCandlesByStockId(id)
        └─ ManageAnalyzeStockService → CandleHistoryRepository.findCandlesByTicker(ticker)
            └─ SqlCandleHistoryRepository → JpaCandleRepository.findByTickerOrderByDateTimeAsc(ticker)
```

---

## 2. Arquitectura de la Solución

Se respeta estrictamente la **Arquitectura Hexagonal** con **Clean Architecture**:

- **Domain (port out):** Se añade contrato de lectura en `CandleHistoryRepository`.
- **Domain (port in):** Se añade método de consulta en `ManageAnalyzeTickerUseCase`.
- **Application:** Se añade `CandleDTO` + implementación del caso de uso.
- **Infrastructure:** Se implementa el método de lectura en `SqlCandleHistoryRepository`.
- **Presentation:** Nuevo endpoint REST + nuevo endpoint de vista + JavaScript.
- **Frontend:** Plantilla Thymeleaf con miniatura + nueva plantilla de gráfico detallado.

---

## 3. Librería de Gráficos

### Decisión: **TradingView Lightweight Charts v4.x** (CDN)

| Criterio | TradingView Lightweight Charts | Chart.js + financial plugin |
|---|---|---|
| Propósito | Específico para charts financieros | Genérico |
| Tamaño | ~40 KB minified | ~70 KB + plugin |
| Candlestick nativo | ✅ Sí | ⚠️ Solo con plugin |
| Licencia | Apache 2.0 | MIT |
| API líneas SMA | ✅ `addLineSeries()` nativo | ⚠️ Dataset adicional |
| Vanilla JS | ✅ Sin dependencias | ✅ Sin dependencias |

**URL CDN:**
```html
<script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>
```

> **Nota SonarQube:** No se cargará vía `th:utext`. La URL del CDN se hardcodeará en el atributo `src` del `<script>`, lo cual es correcto y seguro.

---

## 4. Estructura de DTOs del Gráfico

### `CandleDTO` (Application Layer)

```java
// Ubicación: com/market/analysis/application/dto/CandleDTO.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CandleDTO {
    private long time;         // epoch seconds (para TradingView)
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
}
```

### `CandleChartDTO` (Application Layer) — payload completo para el gráfico

```java
// Ubicación: com/market/analysis/application/dto/CandleChartDTO.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CandleChartDTO {
    private String ticker;
    private List<CandleDTO> candles;
    private BigDecimal sma20;     // valor escalar actual
    private BigDecimal sma50;     // valor escalar actual
    private BigDecimal sma200;    // valor escalar actual
}
```

> **Decisión de diseño:** Las SMAs se exponen como valores escalares (ya calculados y persistidos en `Stock`) en lugar de series completas. El JavaScript calculará las series SMA sobre las velas recibidas, evitando sobrecarga en el servidor y respetando SRP.

---

## 5. Plan de Implementación por Fases

### Fase 1 — Exposición de Lectura en Backend

#### F2.1 — Extender `CandleHistoryRepository` (port out) con método de lectura

**Archivo:** `domain/port/out/CandleHistoryRepository.java`

```java
List<Candle> findCandlesByTicker(String ticker);
```

- Impacto en vistas: ❌  
- Tests: Tests unitarios existentes no afectados. Añadir test en `SqlCandleHistoryRepositoryTest`.

---

#### F2.2 — Implementar `findCandlesByTicker` en `SqlCandleHistoryRepository`

**Archivo:** `infrastructure/persistence/repository/SqlCandleHistoryRepository.java`

```java
@Override
public List<Candle> findCandlesByTicker(String ticker) {
    // org.springframework.util.Assert (misma librería usada en saveCandlesForTicker)
    Assert.hasText(ticker, "ticker must not be null or blank");
    return jpaCandleRepository.findByTickerOrderByDateTimeAsc(ticker)
        .stream()
        .map(candleMapper::toDomain)
        .toList();
}
```

- Impacto en vistas: ❌  
- Tests: `SqlCandleHistoryRepositoryTest` + `SqlCandleHistoryRepositoryIT`.  
- `JpaCandleRepository.findByTickerOrderByDateTimeAsc` ya existe — **sin cambios en JPA**.

---

#### F2.3 — Crear `CandleDTO` en capa Application

**Archivo:** `application/dto/CandleDTO.java`

- Impacto en vistas: ❌  
- Tests: No requiere tests propios (DTO puro).

---

#### F2.4 — Crear `CandleChartDTO` en capa Application

**Archivo:** `application/dto/CandleChartDTO.java`

- Impacto en vistas: ❌  
- Tests: No requiere tests propios (DTO puro).

---

#### F2.5 — Extender `ManageAnalyzeTickerUseCase` (port in) con `findCandlesByStockId`

**Archivo:** `domain/port/in/ManageAnalyzeTickerUseCase.java`

```java
CandleChartDTO findCandlesByStockId(Long id);
```

- Impacto en vistas: ❌  
- Tests: Actualizar mocks en tests existentes que implementen el puerto.

---

#### F2.6 — Implementar `findCandlesByStockId` en `ManageAnalyzeStockService`

**Archivo:** `application/usecase/ManageAnalyzeStockService.java`

```java
@Override
public CandleChartDTO findCandlesByStockId(Long id) {
    Stock stock = stockDataRepository.findById(id)
        .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + id));
    List<Candle> candles = candleHistoryRepository.findCandlesByTicker(stock.getTicker());
    List<CandleDTO> candleDTOs = candles.stream()
        .map(c -> CandleDTO.builder()
            .time(c.getDateTime().getEpochSecond())
            .open(c.getOpenPrice())
            .high(c.getHighPrice())
            .low(c.getLowPrice())
            .close(c.getClosePrice())
            .volume(c.getVolume())
            .build())
        .toList();
    return CandleChartDTO.builder()
        .ticker(stock.getTicker())
        .candles(candleDTOs)
        .sma20(stock.getSma20())
        .sma50(stock.getSma50())
        .sma200(stock.getSma200())
        .build();
}
```

- Impacto en vistas: ❌  
- Tests: `ManageAnalyzeStockServiceTest` — añadir caso de prueba para `findCandlesByStockId`.

---

### Fase 2 — Capa de Presentación (Endpoints)

#### F2.7 — Nuevo endpoint REST JSON de velas

**Archivo:** `presentation/controller/AnalyzeTickerController.java`

```java
@GetMapping(value = "/ticker/{id}/candles", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
public CandleChartDTO getCandlesJson(@PathVariable Long id) {
    return manageAnalyzeTickerUseCase.findCandlesByStockId(id);
}
```

- Ruta: `GET /analysis/ticker/{id}/candles`
- Respuesta: JSON con velas y SMAs escalares
- Seguridad: Solo lectura, no modifica estado → no requiere CSRF token.
- Impacto en vistas: ❌  
- Tests: `AnalyzeTickerControllerTest` con `MockMvc`.

---

#### F2.8 — Nuevo endpoint de vista para el gráfico detallado

**Archivo:** `presentation/controller/AnalyzeTickerController.java`

```java
@GetMapping("/ticker/{id}/chart")
public String getTickerChart(@PathVariable Long id, Model model) {
    StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
    model.addAttribute("ticker", ticker);
    return "analysis/ticker-chart";
}
```

- Ruta: `GET /analysis/ticker/{id}/chart`
- Impacto en vistas: ✅ (crea nueva plantilla)
- Tests: `AnalyzeTickerControllerTest` — verificar que renderiza la vista correcta.

---

#### F2.9 — Tests del controlador (F2.7 y F2.8)

**Archivo:** `test/.../presentation/controller/AnalyzeTickerControllerTest.java`

Casos a cubrir:
- `GET /analysis/ticker/{id}/candles` → HTTP 200 + JSON con `ticker`, `candles[]`, `sma20`, `sma50`, `sma200`.
- `GET /analysis/ticker/{id}/candles` con ID inexistente → HTTP 404.
- `GET /analysis/ticker/{id}/chart` → HTTP 200 + modelo contiene `ticker`.

---

### Fase 3 — JavaScript del Gráfico

#### F2.10 — Crear `candle-chart.js` (gráfico detallado)

**Archivo:** `static/js/candle-chart.js`

Responsabilidades:
1. Al cargar la página `ticker-chart.html`, realizar `fetch('/analysis/ticker/{id}/candles')`.
2. Crear un gráfico de velas con TradingView Lightweight Charts.
3. Añadir series de línea para SMA20, SMA50 y SMA200 calculadas dinámicamente.
4. Gestionar el caso de pocos datos (< 20 velas → omitir SMA20, etc.).
5. Hacer el gráfico responsive con `ResizeObserver`.

```javascript
// Cálculo de SMA sobre array de closePrice
function calculateSma(data, period) {
    return data.map((_, i) => {
        if (i < period - 1) return null;
        const slice = data.slice(i - period + 1, i + 1);
        const avg = slice.reduce((s, c) => s + c.close, 0) / period;
        return { time: data[i].time, value: avg };
    }).filter(Boolean);
}
```

- Colores SMA: SMA20 = azul (`#2196F3`), SMA50 = naranja (`#FF9800`), SMA200 = rojo (`#F44336`).
- Estilo de gráfico: fondo oscuro o claro según Bootstrap theme.

---

#### F2.11 — Crear `mini-chart.js` (miniatura en ticker-detail)

**Archivo:** `static/js/mini-chart.js`

Responsabilidades:
1. Al cargar `ticker-detail.html`, realizar `fetch('/analysis/ticker/{id}/candles')`.
2. Renderizar un gráfico de velas compacto (últimas 60 velas) sin ejes ni etiquetas.
3. Al hacer click, navegar a `/analysis/ticker/{id}/chart`.
4. Si no hay datos, mostrar un placeholder con texto informativo.

---

### Fase 4 — Plantillas Thymeleaf

#### F2.12 — Crear `analysis/ticker-chart.html` (gráfico detallado)

**Archivo:** `templates/analysis/ticker-chart.html`

Estructura de la página:
```
┌─────────────────────────────────────────────────────┐
│  Navbar (fragment)                                   │
├─────────────────────────────────────────────────────┤
│  Header: Logo + TICKER + precio actual               │
├─────────────────────────────────────────────────────┤
│  Leyenda: ■ Velas  ─ SMA20  ─ SMA50  ─ SMA200      │
├─────────────────────────────────────────────────────┤
│                                                      │
│         div#chart-container (100% ancho)             │
│         TradingView Lightweight Charts               │
│                                                      │
├─────────────────────────────────────────────────────┤
│  Botón "← Volver al detalle"                        │
└─────────────────────────────────────────────────────┘
```

Directrices:
- Usar `th:text` (no `th:utext`) siempre.
- Incluir `th:fragment` de navbar.
- Pasar `ticker.id` al JS vía atributo `data-ticker-id` en el div del gráfico.
- No incluir lógica de negocio en la plantilla.
- Breadcrumb: Análisis → Detalle → Gráfico.

---

#### F2.13 — Actualizar `ticker-detail.html` con miniatura de gráfico

**Archivo:** `templates/analysis/ticker-detail.html`

Añadir una nueva sección (card Bootstrap) entre el header y la sección de indicadores:

```html
<!-- Gráfico de velas en miniatura -->
<div class="card mb-3">
  <div class="card-body p-2">
    <div id="mini-chart"
         th:attr="data-ticker-id=${ticker.id}"
         style="width:100%; height:200px; cursor:pointer;">
    </div>
    <p class="text-muted text-end small mt-1 mb-0">
      <a th:href="@{/analysis/ticker/{id}/chart(id=${ticker.id})}">
        <i class="bi bi-arrows-fullscreen"></i> Ver gráfico detallado
      </a>
    </p>
  </div>
</div>
```

> **Nota:** La navegación al gráfico detallado se implementa en `mini-chart.js` mediante un event listener sobre `#mini-chart`, leyendo el atributo `data-ticker-id`. No se usa JS inline en el HTML para respetar la separación de responsabilidades y las reglas de SonarQube.

---

### Fase 5 — Tests, Seguridad y Documentación

#### F2.14 — Tests unitarios del dominio / aplicación

| Clase de test | Caso de prueba |
|---|---|
| `SqlCandleHistoryRepositoryTest` | `findCandlesByTicker` devuelve lista vacía si no hay datos |
| `SqlCandleHistoryRepositoryTest` | `findCandlesByTicker` devuelve velas ordenadas por fecha |
| `SqlCandleHistoryRepositoryTest` | `findCandlesByTicker` lanza `IllegalArgumentException` si ticker es blank |
| `SqlCandleHistoryRepositoryIT` | Test de integración H2: guardar + leer velas |
| `ManageAnalyzeStockServiceTest` | `findCandlesByStockId` retorna `CandleChartDTO` con SMA escalares |
| `ManageAnalyzeStockServiceTest` | `findCandlesByStockId` lanza `StockDataNotFoundException` si ID no existe |

---

#### F2.15 — Tests del controlador

| Clase de test | Caso de prueba |
|---|---|
| `AnalyzeTickerControllerTest` | `GET /ticker/{id}/candles` → 200 + JSON válido |
| `AnalyzeTickerControllerTest` | `GET /ticker/{id}/candles` con ID inválido → 404 |
| `AnalyzeTickerControllerTest` | `GET /ticker/{id}/chart` → 200 + vista `analysis/ticker-chart` |
| `AnalyzeTickerControllerTest` | `GET /ticker/{id}/chart` con ID inválido → 404 |

---

#### F2.16 — Documentación de tarea en `/docs`

Crear `docs/task-2026-03-16-chart-candlestick-implementation.md` con:
- Resumen de la tarea
- Decisiones técnicas (librería elegida, DTOs, endpoints)
- Cobertura de tests
- Advertencias SonarQube
- Próximos pasos

---

## 6. Tabla Resumen de Cambios por Fichero

| Fase | Fichero | Tipo de cambio |
|---|---|---|
| F2.1 | `domain/port/out/CandleHistoryRepository.java` | Añadir método `findCandlesByTicker` |
| F2.2 | `infrastructure/persistence/repository/SqlCandleHistoryRepository.java` | Implementar `findCandlesByTicker` |
| F2.3 | `application/dto/CandleDTO.java` | Nuevo fichero |
| F2.4 | `application/dto/CandleChartDTO.java` | Nuevo fichero |
| F2.5 | `domain/port/in/ManageAnalyzeTickerUseCase.java` | Añadir método `findCandlesByStockId` |
| F2.6 | `application/usecase/ManageAnalyzeStockService.java` | Implementar `findCandlesByStockId` |
| F2.7 | `presentation/controller/AnalyzeTickerController.java` | Añadir endpoint JSON `/candles` |
| F2.8 | `presentation/controller/AnalyzeTickerController.java` | Añadir endpoint vista `/chart` |
| F2.9 | `test/.../AnalyzeTickerControllerTest.java` | Tests MockMvc nuevos endpoints |
| F2.10 | `static/js/candle-chart.js` | Nuevo fichero JS |
| F2.11 | `static/js/mini-chart.js` | Nuevo fichero JS |
| F2.12 | `templates/analysis/ticker-chart.html` | Nueva plantilla Thymeleaf |
| F2.13 | `templates/analysis/ticker-detail.html` | Añadir sección mini chart |
| F2.14 | `test/.../SqlCandleHistoryRepositoryTest.java` | Tests del método de lectura |
| F2.15 | `test/.../ManageAnalyzeStockServiceTest.java` | Tests del caso de uso |
| F2.16 | `docs/task-2026-03-16-chart-candlestick-implementation.md` | Documentación de tarea |

---

## 7. Consideraciones Arquitectónicas y de Calidad

### Respeto a AGENTS.md

| Regla | Cómo se cumple |
|---|---|
| Arquitectura Hexagonal | Dominio puro, Application solo orquesta, Infrastructure implementa |
| No `th:utext` | Todas las expresiones usan `th:text` o son literales en atributos `src` |
| CSRF habilitado | Solo el endpoint GET `/candles` (no modifica estado), no requiere CSRF |
| Constructor injection | Todos los beans con `@RequiredArgsConstructor` |
| SLF4J logging | `log.debug/info` en `SqlCandleHistoryRepository` y `ManageAnalyzeStockService` |
| Cobertura ≥ 80% | Tests cubriendo todos los nuevos métodos públicos |
| No IA en lógica | SMA se calcula en JS (cliente) sobre datos crudos; no IA involucrada |
| `try-with-resources` | No aplica (no se abren recursos manuales) |
| Máx. 7 params constructor | `ManageAnalyzeStockService` ya tiene muchos parámetros; no se añaden más |

### Seguridad

- El endpoint `GET /analysis/ticker/{id}/candles` devuelve solo datos de lectura.
- No se expone información sensible; los datos son OHLCV públicos.
- Validación del `id` realizada por Spring MVC y el port de dominio.

### Rendimiento

- La consulta de velas usa índice `idx_candles_ticker_datetime` ya existente.
- Para la miniatura, se pueden limitar las velas a las últimas 90 (parámetro en JS).
- No se añaden joins ni queries N+1.

### Responsive / UX

- `#chart-container`: `width: 100%; height: 500px` en vista detallada.
- `#mini-chart`: `width: 100%; height: 200px` en `ticker-detail.html`.
- `ResizeObserver` para adaptar el gráfico al cambio de tamaño de ventana.

---

## 8. Orden de Ejecución Recomendado

```
F2.1 → F2.2 → F2.3 → F2.4 → F2.5 → F2.6   ← Backend (sin vistas)
    → F2.7 → F2.8                             ← Endpoints (sin JS ni HTML)
    → F2.9                                    ← Tests controlador
    → F2.10 → F2.11                           ← JavaScript
    → F2.12 → F2.13                           ← Plantillas Thymeleaf
    → F2.14 → F2.15                           ← Tests dominio/aplicación
    → F2.16                                   ← Documentación
```

Cada paso `F2.x` puede pedirse individualmente en una sesión de agente separada, en el orden indicado.

---

## 9. Criterios de Aceptación

- [ ] Al acceder a `/analysis/ticker/{id}`, se muestra un gráfico de velas en miniatura con las últimas velas disponibles.
- [ ] Al hacer click en el gráfico miniatura se navega a `/analysis/ticker/{id}/chart`.
- [ ] En la vista del gráfico detallado se muestran todas las velas del ticker.
- [ ] Si existen datos suficientes, se superponen las líneas SMA20 (azul), SMA50 (naranja) y SMA200 (rojo).
- [ ] Si no hay suficientes velas para una SMA, esa línea no se muestra.
- [ ] Si no hay velas en base de datos, se muestra un mensaje informativo en lugar del gráfico.
- [ ] El endpoint `GET /analysis/ticker/{id}/candles` devuelve JSON válido con `ticker`, `candles`, `sma20`, `sma50`, `sma200`.
- [ ] Los tests unitarios e integración pasan con cobertura ≥ 80% en las clases nuevas.
- [ ] No hay alertas nuevas de SonarQube relacionadas con este cambio.
