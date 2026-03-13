# Plan de registro de tickers Polygon en BD

## 1. Objetivo
Diseñar una estrategia para guardar en base de datos los datos históricos devueltos por Polygon en el flujo de fetchHistoricalData, con el menor impacto posible sobre el código actual, para habilitar después la vista de gráfico (chart) por ticker.

Condiciones de diseño:
- Mantener arquitectura hexagonal y Clean Architecture.
- No afectar la evaluación determinista de reglas ni los cálculos de indicadores.
- Mantener la integración IA solo como capa interpretativa.

## 2. Estado actual del flujo

### Flujo funcional hoy
1. AnalyzeTickerController recibe tickers y strategyId.
2. ManageAnalyzeStockService.getStockData orquesta la carga por ticker.
3. Si no hay snapshot diario reutilizable, llama a HistoricalProviderPort.fetchHistoricalData.
4. PolygonAdapter llama a Polygon, parsea JSON y construye HistoricalData.
5. StockHistoricalService calcula SMA y volumen promedio con closingPrices y volumes.
6. Se guarda Stock y StrategyEvaluation.

### Qué se guarda hoy en BD
- Snapshot de stock (precio actual, SMA, volumen, etc.).
- Evaluación de estrategia.
- Perfil de compañía.
- No se guarda explícitamente la serie OHLCV histórica de Polygon para pintar gráfico.

### Activos ya existentes en el proyecto
- Existe modelo de dominio Candle.
- Existe entidad CandleEntity y mapper CandleMapper.
- No existe repositorio JPA/SQL operativo para velas.
- StockEntity no tiene relación activa con lista de velas.

## 3. Análisis del JSON real historico-polygon.json

Muestra analizada: ticker BNAI.

### Metadatos principales
- queryCount: 240
- resultsCount: 240
- adjusted: true
- status: DELAYED
- results length: 240

### Calidad y consistencia de datos
- missing_o: 0
- missing_h: 0
- missing_l: 0
- missing_c: 0
- missing_v: 0
- missing_t: 0
- distinct_timestamps: 240
- Orden temporal: descendente (más reciente primero)
- Duplicados de timestamp: no detectados

### Rango temporal y valores
- max_date (UTC): 2026-03-11T04:00:00Z
- min_date (UTC): 2025-03-27T04:00:00Z
- max_c: 63
- min_c: 1.2
- max_v: 150430012
- min_v: 3561.5

### Precisión decimal observada
- open: hasta 3 decimales
- high: hasta 4 decimales
- low: hasta 4 decimales
- close: hasta 3 decimales
- volume: viene con decimales en algunos puntos (actualmente se trunca a Long en el adapter)

## 4. Brecha funcional a resolver
Para pintar chart posterior se necesita almacenar por vela, como mínimo:
- ticker
- fecha/hora de vela
- open, high, low, close
- volume

Actualmente fetchHistoricalData solo expone cierre y volumen para indicadores. El OHLC y timestamp existen en el JSON, pero no se persisten.

## 5. Estrategia recomendada (mínimo impacto)

## Decisión base
Aplicar un enfoque en infraestructura, sin tocar la lógica determinista de dominio:
- Mantener HistoricalData para cálculo de indicadores tal como está.
- Añadir persistencia de velas dentro del flujo de PolygonAdapter tras parsear respuesta válida.
- Guardar serie por ticker y timestamp para reutilizarla en la vista chart.

Esto minimiza cambios en casos de uso y evita ampliar de forma invasiva puertos de dominio en esta primera etapa.

## Modelo de persistencia propuesto
Aprovechar CandleEntity existente, evolucionándola para soportar consulta por ticker:
- ticker: String
- date_time: Instant
- open_price: BigDecimal
- high_price: BigDecimal
- low_price: BigDecimal
- close_price: BigDecimal
- volume: Long
- opcional fase 2: trades_count (n), vwap (vw)

Índices y restricciones:
- unique (ticker, date_time)
- index (ticker, date_time desc)

Nota: La relación a StockEntity no debe ser el eje de consulta histórica, porque el gráfico se necesita por ticker y no por snapshot puntual de strategy.

## Política de guardado recomendada
Para simplicidad y consistencia entre H2 y MariaDB:
- Estrategia Replace transaccional por ticker:
1. Parsear respuesta completa en memoria.
2. Validar dataset.
3. En transacción: borrar velas existentes de ticker.
4. Insertar lote nuevo (saveAll).

Ventajas:
- Evita complejidad de upsert cross-database.
- Refleja correctamente ajustes históricos cuando Polygon recalcula.
- Con 240 registros por ticker, coste asumible.

## 6. Pasos de implementación propuestos

Los pasos se desglosan con identificadores para poder pedirme luego la ejecución exacta de cada uno, por ejemplo:
- implementar F1.2
- implementar F1.5 y sus tests
- implementar F2.4 sin tocar aún la vista

Cada paso incluye objetivo, impacto técnico y si implica cambios en vistas.

### Fase 1: Salvaguarda en BD (sin gráfico todavía)

#### F1.1. Añadir campo ticker a CandleEntity y actualizar CandleMapper
Objetivo:
- Añadir `ticker` como campo persistido en `CandleEntity` para que la consulta histórica funcione por ticker de forma directa.
- Eliminar la relación `@ManyToOne StockEntity stock` de `CandleEntity` — es el eje equivocado para un histórico de velas y no existe ningún código que la use actualmente.
- Actualizar `CandleMapper` para incluir `ticker` en los dos sentidos del mapeo.

Decisiones ya tomadas tras análisis del código:
- La relación con `StockEntity` se elimina por completo: no existe código activo que la use, el campo `stock` nunca es asignado por el mapper, y la FK `stocks_id` no aporta ningún valor al flujo de histórico.
- El campo `ticker` se añade como `String` no nulable en la entidad.
- `CandleMapper.toEntity()` pasará a asignar `entity.setTicker(candle.getTicker())`.
- `CandleMapper.toDomain()` pasará a asignar `.ticker(entity.getTicker())` en el builder.

Trabajo concreto:
- En `CandleEntity`: eliminar el bloque `@ManyToOne`/`@JoinColumn` y el campo `stock`. Añadir `private String ticker`.
- En `CandleMapper.toEntity()`: añadir `entity.setTicker(candle.getTicker())` junto al resto de campos.
- En `CandleMapper.toDomain()`: añadir `.ticker(entity.getTicker())` en el builder.

Archivos afectados:
- `CandleEntity.java`
- `CandleMapper.java`

Cambios en vistas:
- Ninguno.

Resultado esperado:
- `CandleEntity` tiene ticker + dateTime + OHLCV, sin FK a stocks.
- El mapper mapea los 7 campos en ambas direcciones sin campos nulos ni omitidos.

#### F1.2. Añadir restricciones JPA y anotaciones de columna a CandleEntity
Objetivo:
- Definir con precisión el esquema físico de la tabla `candles` mediante anotaciones JPA, sin depender de valores por defecto de Hibernate.

Decisiones ya tomadas:
- `ticker`: columna `VARCHAR(20)`, `nullable = false`.
- `date_time`: columna `TIMESTAMP`, `nullable = false` (nombre explícito para evitar conflictos con palabras reservadas en MariaDB).
- `open_price`, `high_price`, `low_price`, `close_price`: `DECIMAL(18,4)`, `nullable = false`.
- `volume`: `BIGINT`, `nullable = false` — coherente con el tipo `Long` actual y el truncado que ya hace el adapter.
- Restricción única sobre `(ticker, date_time)`: se declara con `@Table(uniqueConstraints = ...)` en la propia entidad.
- Índice compuesto `idx_candles_ticker_datetime` sobre `(ticker, date_time DESC)` para acelerar la lectura del histórico ordenado: se declara con `@Table(indexes = ...)`.
- El nombre físico de la tabla queda como `candles`, sin cambio.

Trabajo concreto:
- En `CandleEntity`: añadir `@Column` con `name`, `nullable`, `precision` y `scale` a cada campo.
- En la anotación `@Table`: añadir `uniqueConstraints` y `indexes` para reflejar las restricciones anteriores.

Archivos afectados:
- `CandleEntity.java`

Cambios en vistas:
- Ninguno.

Resultado esperado:
- DDL generado por `ddl-auto=update` producirá la columna `ticker`, la constraint unique y el índice automáticamente en H2 y MariaDB.

#### F1.3. Crear el repositorio JPA de velas
Objetivo:
- Incorporar acceso JPA dedicado al histórico.

Trabajo previsto:
- Crear JpaCandleRepository.
- Añadir operaciones mínimas:
  - búsqueda por ticker ordenada por fecha
  - borrado por ticker
  - comprobaciones auxiliares si hicieran falta para tests o validaciones

Archivos previsibles:
- JpaCandleRepository

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Capa JPA preparada para lectura y reemplazo del dataset histórico.

#### F1.4. Crear SqlCandleHistoryRepository
Objetivo:
- Encapsular la lógica de guardado masivo fuera de PolygonAdapter para no mezclar HTTP con detalles JPA.

Decisiones ya tomadas:
- Nombre del componente: `SqlCandleHistoryRepository`, en paquete `infrastructure.persistence.repository`, coherente con el patrón `Sql*Repository` del proyecto.
- En fase 1 no se abre un puerto de dominio adicional: el componente es un `@Component` de infraestructura que PolyonAdapter inyectará directamente. El puerto se introduce en F2.2 cuando la capa de aplicación necesite leer velas.
- Método público: `saveCandlesForTicker(String ticker, List<Candle> candles)` — la lógica transaccional de reemplazo se implementa en F1.5 dentro de este mismo componente.
- El mapper `CandleMapper` lo inyecta el propio `SqlCandleHistoryRepository`; no se modifica el mapper en este paso (ya siendo actualizado en F1.1).

Trabajo concreto:
- Crear clase `SqlCandleHistoryRepository` anotada con `@Component`.
- Inyectar por constructor: `JpaCandleRepository` (creado en F1.3) y `CandleMapper`.
- Declarar método público `saveCandlesForTicker(String ticker, List<Candle> candles)` con cuerpo vacío de momento (se implementa en F1.5).

Archivos afectados:
- `SqlCandleHistoryRepository.java` (nuevo)

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Clase creada, compilable y con su test de estructura básica listo para F1.5.

#### F1.5. Implementar la estrategia transaccional de reemplazo por ticker
Objetivo:
- Garantizar consistencia al refrescar el histórico.

Trabajo previsto:
- Implementar transacción única:
  1. validar lista de velas
  2. borrar histórico existente del ticker
  3. insertar el lote nuevo
- Proteger el flujo ante listas vacías o nulas.
- Definir comportamiento en caso de error parcial.

Archivos previsibles:
- componente creado en F1.4
- repositorio JPA si necesita operación adicional

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Cada fetch deja un dataset completo y sin duplicados para el ticker.

#### F1.6. Extender el parseo de PolygonAdapter para extraer velas completas
Objetivo:
- Aprovechar el mismo JSON que hoy se usa para indicadores y extraer además OHLCV + timestamp.

Trabajo previsto:
- Separar el parseo en dos salidas lógicas desde la misma respuesta:
  - HistoricalData para indicadores
  - lista de Candle para persistencia
- Convertir t a Instant.
- Convertir o/h/l/c a BigDecimal o tipo equivalente robusto.
- Convertir volume siguiendo la política actual del sistema.

Detalle importante:
- No romper el contrato actual de fetchHistoricalData.
- No alterar el orden de closingPrices/volumes que usa StockHistoricalService.

Archivos previsibles:
- PolygonAdapter
- opcionalmente clase auxiliar privada o DTO interno de parseo si se decide limpiar el adapter

Cambios en vistas:
- Ninguno.

Resultado esperado:
- El adapter seguirá devolviendo HistoricalData y además generará una colección de velas persistibles.

#### F1.7. Integrar persistencia histórica dentro del flujo de fetchHistoricalData
Objetivo:
- Guardar velas únicamente cuando la respuesta Polygon haya sido parseada con éxito.

Trabajo previsto:
- Invocar el componente de persistencia desde PolygonAdapter después del parseo válido.
- Asegurar que no se guarda nada si el JSON es inválido o la llamada falla.
- Mantener intacto el comportamiento de excepciones existentes.

Archivos previsibles:
- PolygonAdapter
- componente de persistencia creado en F1.4

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Cada ejecución satisfactoria de fetchHistoricalData deja persistido el histórico correspondiente.

#### F1.8. Añadir logging técnico y observabilidad de la salvaguarda
Objetivo:
- Poder auditar qué histórico se guarda y detectar incidencias rápidamente.

Trabajo previsto:
- Registrar al menos:
  - ticker
  - número de velas persistidas
  - fecha mínima y máxima persistida
  - si el replace fue satisfactorio
- Evitar logs excesivos por vela individual.

Archivos previsibles:
- PolygonAdapter
- componente de persistencia

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Trazabilidad suficiente para verificar el comportamiento en dev y prod.

#### F1.9. Añadir pruebas unitarias de parseo y persistencia
Objetivo:
- Cubrir la nueva lógica sin debilitar la cobertura actual.

Trabajo previsto:
- Ampliar PolygonAdapterTest para cubrir:
  - parseo OHLCV completo
  - llamada a persistencia cuando la respuesta es válida
  - no persistencia ante JSON inválido o error HTTP
- Añadir tests del componente de persistencia:
  - replace por ticker
  - manejo de lista vacía

Archivos previsibles:
- PolygonAdapterTest
- nuevos tests del repositorio/servicio de velas

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Cobertura de la nueva responsabilidad de salvaguarda.

#### F1.10. Añadir prueba de integración JPA y validación manual en H2/MariaDB
Objetivo:
- Verificar que el diseño funciona igual en persistencia real.

Trabajo previsto:
- Test de integración con H2 para:
  - guardar 240 velas
  - reemplazar dataset del mismo ticker
  - verificar ausencia de duplicados
- Validación manual en entorno local o docker si aplica.

Archivos previsibles:
- tests de integración de persistencia

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Fase 1 cerrada con persistencia fiable y lista para explotación posterior.

### Fase 2: Lectura para chart

#### F2.1. Definir el contrato de lectura para el gráfico
Objetivo:
- Fijar el contrato completo (URL, payload JSON, casos de error) antes de tocar backend o frontend.

Decisiones ya tomadas:
- La URL del endpoint será `GET /analysis/ticker/{stockId}/chart` — reutiliza el `stockId` ya disponible en la vista `ticker-detail`, sin añadir un nuevo parámetro de contexto.
- El controlador resolverá internamente el ticker a partir del `stockId` consultando el repositorio de stocks existente.
- El payload JSON devuelve un array plano de velas ordenadas ascendente, cada elemento con: `timestamp` (epoch ms), `open`, `high`, `low`, `close`, `volume`.
- No se incluyen metadatos de ticker ni de rango en el payload inicial — el frontend ya tiene el contexto de la página.
- Si el stock no existe: `404 Not Found`.
- Si el stock existe pero no hay velas persistidas: `200 OK` con array vacío `[]` — la UI mostrará el estado “sin datos”.
- El endpoint es solo de lectura y no dispara ningún fetch a Polygon.

Trabajo concreto (solo documentación y contratos, sin código):
- Registrar las decisiones anteriores en este paso para que F2.2–F2.5 las implementen directamente sin nueva deliberación.

Archivos afectados:
- Ninguno en este paso.

Cambios en vistas:
- Ninguno.

Resultado esperado:
- Contrato cerrado: URL, payload, códigos HTTP y relación con `stockId` están fijados antes de escribir código.

#### F2.2. Crear el puerto de salida y lectura desde infraestructura
Objetivo:
- Hacer accesible el histórico persistido desde la capa de aplicación mediante un puerto de dominio.

Decisiones ya tomadas:
- Nombre del puerto: `CandleHistoryPort` en `domain/port/out/` — coherente con el sufijo `Port` usado en el proyecto (`HistoricalProviderPort`, `StockDataRepository`).
- Un único método: `List<Candle> findByTickerOrderByDateTimeAsc(String ticker)`.
- No se añaden variantes con límite ni rango temporal en esta fase — el endpoint devuelve el histórico completo disponible (máx. 240 velas).
- La implementación en infraestructura la proporciona `SqlCandleHistoryRepository` (ya creado en F1.4), que implementará esta interfaz además de su responsabilidad de escritura.
- `JpaCandleRepository` ya tiene el método de consulta necesario (creado en F1.3); no requiere métodos extra en este paso.

Trabajo concreto:
- Crear interfaz `CandleHistoryPort` en `domain/port/out/` con el método definido arriba.
- Añadir `implements CandleHistoryPort` a `SqlCandleHistoryRepository`.
- Implementar el método: delegar en `JpaCandleRepository.findByTickerOrderByDateTimeAsc(ticker)` y mapear entidades a dominio con `CandleMapper`.
- Registrar `SqlCandleHistoryRepository` en `BeanConfig` como implementación del puerto.

Archivos afectados:
- `CandleHistoryPort.java` (nuevo)
- `SqlCandleHistoryRepository.java` (añadir implements + método)
- `BeanConfig.java`

Cambios en vistas:
- Ninguno.

Resultado esperado:
- La capa de aplicación puede inyectar `CandleHistoryPort` sin depender de JPA.

#### F2.3. Crear el caso de uso de lectura para chart
Objetivo:
- Exponer una operación de aplicación dedicada al gráfico, desacoplada del flujo de análisis.

Decisiones ya tomadas:
- Se crea una interfaz de puerto de entrada nueva: `GetTickerChartUseCase` en `domain/port/in/` — no se toca `ManageAnalyzeTickerUseCase` para no contaminar el flujo actual.
- Método: `List<CandleChartDTO> getChartData(Long stockId)`.
- El servicio de aplicación que lo implementa se llama `TickerChartService` en `application/service/`.
- La resolución de `stockId` a `ticker` se hace dentro de `TickerChartService`: consulta el repositorio de stocks existente (`StockDataRepository`) para obtener el ticker, luego llama a `CandleHistoryPort`.
- Si el stock no existe, el servicio lanza `StockNotFoundException` (ya existente en el proyecto) — el controlador la mapeará a `404`.
- El DTO de salida se define en F2.4; en este paso se usa el tipo ya como referencia.

Trabajo concreto:
- Crear interfaz `GetTickerChartUseCase` en `domain/port/in/` con método `getChartData(Long stockId)`.
- Crear `TickerChartService` en `application/service/` implementando `GetTickerChartUseCase`.
- Inyectar por constructor: `StockDataRepository` y `CandleHistoryPort`.
- Implementar: buscar stock por id → obtener ticker → llamar `CandleHistoryPort.findByTickerOrderByDateTimeAsc(ticker)` → mapear a `List<CandleChartDTO>`.
- Registrar `TickerChartService` en `BeanConfig`.

Archivos afectados:
- `GetTickerChartUseCase.java` (nuevo)
- `TickerChartService.java` (nuevo)
- `BeanConfig.java`

Cambios en vistas:
- Ninguno.

Resultado esperado:
- La capa presentation puede llamar a `GetTickerChartUseCase.getChartData(stockId)` y recibir los datos listos para serializar.

#### F2.4. Crear CandleChartDTO
Objetivo:
- Definir el DTO que serializa cada vela en el payload JSON del endpoint de chart.

Decisiones ya tomadas:
- Nombre: `CandleChartDTO` en `application/dto/`.
- Campos: `long timestamp` (epoch ms), `BigDecimal open`, `BigDecimal high`, `BigDecimal low`, `BigDecimal close`, `Long volume`.
- No se crea un DTO envolvente con metadata — el endpoint devuelve `List<CandleChartDTO>` directamente, el frontend ya tiene el contexto del ticker desde la página.
- No se crea un mapper dedicado: la conversión `Candle → CandleChartDTO` la hace `TickerChartService` con una llamada directa al constructor/builder.

Trabajo concreto:
- Crear clase `CandleChartDTO` con los campos indicados y anotada con `@Getter @Builder` (Lombok).
- En `TickerChartService`: añadir la conversión `Candle → CandleChartDTO` dentro del método `getChartData`.

Archivos afectados:
- `CandleChartDTO.java` (nuevo)
- `TickerChartService.java` (añadir conversión)

Cambios en vistas:
- Ninguno.

Resultado esperado:
- DTO estático y simple listo para serializar como JSON por Jackson.

#### F2.5. Exponer endpoint JSON para el chart
Objetivo:
- Habilitar la carga asíncrona del histórico desde la vista detalle.

Decisiones ya tomadas:
- El endpoint se añade al controlador existente `AnalyzeTickerController` — no se crea un controlador nuevo para evitar dispersión en el namespace `/analysis`.
- Anotación: `@GetMapping("/ticker/{id}/chart")` + `@ResponseBody` — devuelve `ResponseEntity<List<CandleChartDTO>>`.
- Parámetro: `@PathVariable Long id` (el `stockId`).
- Inyectar `GetTickerChartUseCase` en el controlador por constructor.
- Si `StockNotFoundException`: devolver `ResponseEntity.notFound().build()` (`404`).
- Si la lista está vacía: devolver `ResponseEntity.ok(List.of())` (`200` con `[]`).
- El endpoint es `@ResponseBody`, no devuelve vista Thymeleaf.

Trabajo concreto:
- Añadir `GetTickerChartUseCase` como dependencia inyectada en `AnalyzeTickerController`.
- Añadir método `getChartData(@PathVariable Long id)` con la lógica de gestión de errores definida.

Archivos afectados:
- `AnalyzeTickerController.java`

Cambios en vistas:
- Ninguno.

Resultado esperado:
- `GET /analysis/ticker/{id}/chart` devuelve JSON consumible por JavaScript.

#### F2.6. Integrar ApexCharts en la vista de detalle
Objetivo:
- Añadir la librería de chart en la página de detalle como primer paso de integración visual.

Decisiones ya tomadas:
- Librería elegida: **ApexCharts** — soporta candlestick nativo, API sencilla para JSON OHLCV, responsive por defecto, sin paso de build, bundle < 200 KB minificado.
- Descartada Lightweight Charts: menos documentación en contexto server-side Thymeleaf y requiere más transformación manual del payload.
- Carga vía CDN: `https://cdn.jsdelivr.net/npm/apexcharts` — se añade un `<script>` al final del `<body>` de `ticker-detail.html` únicamente, no en el layout global para no cargar la librería en todas las páginas.

Trabajo concreto:
- Añadir en `ticker-detail.html`, justo antes del cierre del `</body>` (o en el bloque `scripts` de Thymeleaf si el layout lo soporta):
  ```html
  <script src="https://cdn.jsdelivr.net/npm/apexcharts"></script>
  ```
- No inicializar el chart todavía — eso se hace en F2.7 y F2.8.

Archivos afectados:
- `analysis/ticker-detail.html`

Cambios en vistas:
- Sí. Primera modificación real de la vista detalle.

Resultado esperado:
- ApexCharts disponible en el contexto JS de la página de detalle, listo para instanciar el gráfico en el siguiente paso.

#### F2.7. Modificar la vista ticker-detail para incluir contenedor y estados UI
Objetivo:
- Preparar la página de detalle para mostrar el gráfico sin romper el diseño actual.

Trabajo previsto:
- Añadir una nueva card o sección visual en ticker-detail.html.
- Añadir contenedor del gráfico.
- Añadir estados de:
  - cargando
  - sin datos
  - error de carga
- Ajustar distribución responsive para desktop y mobile.

Archivos previsibles:
- analysis/ticker-detail.html
- estilos CSS si fueran necesarios

Cambios en vistas:
- Sí.
- Este paso es exclusivamente de UI estructural.

Resultado esperado:
- La vista tendrá espacio y estructura para el chart aunque aún no esté conectado del todo.

#### F2.8. Implementar JavaScript de carga y transformación de datos
Objetivo:
- Consumir el endpoint y transformar el payload al formato de la librería elegida.

Trabajo previsto:
- Hacer fetch del endpoint de chart desde la vista de detalle.
- Convertir la respuesta al formato requerido por la librería.
- Renderizar la serie OHLC.
- Si se decide, añadir segunda serie o panel auxiliar para volumen.

Archivos previsibles:
- ticker-detail.html o JS estático dedicado

Cambios en vistas:
- Sí.
- Se añade comportamiento cliente, no solo markup.

Resultado esperado:
- Chart funcional en la vista de detalle.

#### F2.9. Afinar experiencia visual y comportamiento de la vista
Objetivo:
- Integrar el chart en la UI existente con calidad suficiente.

Trabajo previsto:
- Ajustar alturas, márgenes y comportamiento responsive.
- Verificar convivencia con cards actuales de indicadores, evaluación y plan de ejecución.
- Revisar si conviene pestaña, acordeón o bloque independiente para no sobrecargar la pantalla.

Archivos previsibles:
- ticker-detail.html
- CSS si procede

Cambios en vistas:
- Sí.
- Paso centrado en acabado visual.

Resultado esperado:
- Vista detalle coherente y usable con el chart integrado.

#### F2.10. Añadir pruebas y validación manual del chart
Objetivo:
- Cerrar la fase 2 con garantías funcionales.

Trabajo previsto:
- Tests del caso de uso y endpoint JSON.
- Validación manual de:
  - carga del gráfico
  - orden correcto de velas
  - comportamiento con ticker sin histórico
  - comportamiento tras refrescar datos del ticker

Archivos previsibles:
- tests de aplicación/controlador

Cambios en vistas:
- No obligatorios en este paso, salvo ajustes menores detectados en validación.

Resultado esperado:
- Fase 2 finalizada con lectura de histórico y visualización operativa.

### Fase 3: Optimización opcional
1. Añadir fields opcionales n y vw si se van a usar en analítica visual avanzada.
2. Definir política de retención (por ejemplo 300-400 velas por ticker).
3. Evaluar cache de lectura en endpoint de chart.
4. Evaluar separación de serie de volumen, overlays de SMA y zoom temporal.

## 7. Impacto estimado por capa

### Domain
- Impacto mínimo o nulo en fase 1 (recomendado).
- Sin cambio en reglas ni evaluación.

### Application
- Sin cambios obligatorios en fase 1.

### Infrastructure
- Impacto principal:
  - entidad/repo/adaptador de velas
  - integración de persistencia en PolygonAdapter

### Presentation
- Sin cambios en fase 1.
- Cambios en fase 2 para endpoint y chart.

## 8. Riesgos y mitigaciones

Riesgo: Borrado e inserción incompletos por error intermedio.
Mitigación: transacción única por ticker.

Riesgo: Duplicados de velas.
Mitigación: unique (ticker, date_time).

Riesgo: Datos históricos desactualizados.
Mitigación: refresh completo por ticker cada vez que se haga fetch.

Riesgo: Impacto en rendimiento al escalar.
Mitigación: índice compuesto y batch saveAll.

## 9. Plan de pruebas

### Unit tests
- PolygonAdapter:
  - parseo OHLCV + timestamp para persistencia
  - no persistir ante JSON inválido
- Repositorio de velas:
  - replace por ticker
  - unicidad ticker/date_time

### Integración
- Persistencia H2:
  - alta de ticker y guardado de 240 velas
  - nueva carga reemplaza dataset anterior

### Regresión funcional
- Confirmar que SMA20/SMA50/SMA200 y volumen siguen idénticos en el flujo actual.
- Confirmar que evaluación de estrategia no cambia.

## 10. Criterios de aceptación de la fase de salvaguarda
- Al ejecutar análisis de un ticker, quedan persistidas sus velas históricas en BD.
- No hay duplicados por ticker y fecha.
- El flujo actual de análisis y evaluación sigue funcionando sin cambios de comportamiento.
- Existe consulta por ticker ordenada para alimentar chart en siguiente fase.

## 11. Orden recomendado de ejecución real
1. Ejecutar `F1.1` y `F1.2` para cerrar modelo y entidad.
2. Ejecutar `F1.3`, `F1.4` y `F1.5` para dejar lista la persistencia transaccional.
3. Ejecutar `F1.6`, `F1.7` y `F1.8` para integrar guardado y observabilidad en PolygonAdapter.
4. Ejecutar `F1.9` y `F1.10` para cerrar pruebas y validación de la fase 1.
5. Ejecutar `F2.1` para fijar el contrato de lectura antes de tocar backend o frontend.
6. Ejecutar `F2.2`, `F2.3`, `F2.4` y `F2.5` para habilitar el endpoint JSON de chart.
7. Ejecutar `F2.6`, `F2.7`, `F2.8` y `F2.9` para integrar la visualización en la vista detalle.
8. Ejecutar `F2.10` para pruebas finales y validación manual de la fase 2.

## 12. Conclusión
El JSON actual de Polygon tiene calidad suficiente para persistencia OHLCV completa y soporte de gráfico. La opción de menor impacto es guardar velas en infraestructura durante fetchHistoricalData, sin alterar la lógica determinista actual de análisis. Esto deja preparada la base para la vista chart en un segundo paso controlado.