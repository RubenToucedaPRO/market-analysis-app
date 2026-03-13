# F1.2 – Añadir restricciones JPA y anotaciones de columna a CandleEntity

## Resumen de la tarea

Añadir anotaciones `@Column` con `name`, `nullable`, `precision` y `scale` a cada campo de `CandleEntity`, así como restricciones de unicidad e índice compuesto en la anotación `@Table`, definiendo con precisión el esquema físico de la tabla `candles`.

## Código generado

### `CandleEntity.java` (modificado)

```java
@Entity
@Table(
    name = "candles",
    uniqueConstraints = @UniqueConstraint(name = "uq_candles_ticker_datetime", columnNames = {"ticker", "date_time"}),
    indexes = @Index(name = "idx_candles_ticker_datetime", columnList = "ticker, date_time DESC")
)
@Getter
@Setter
public class CandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "date_time", nullable = false)
    private Instant dateTime;

    @Column(name = "open_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal closePrice;

    @Column(name = "volume", nullable = false)
    private Long volume;
}
```

## Decisiones técnicas

- **`ticker`**: `VARCHAR(20)`, `nullable = false`. Longitud 20 cubre los tickers más largos del mercado americano.
- **`date_time`**: nombre explícito para evitar conflictos con palabras reservadas en MariaDB. `nullable = false`.
- **`open_price`, `high_price`, `low_price`, `close_price`**: `DECIMAL(18,4)`, `nullable = false`. Precisión adecuada para datos OHLCV ajustados.
- **`volume`**: `BIGINT`, `nullable = false`. Coherente con el tipo `Long` y el truncado que ya hace el adapter.
- **Restricción única** `uq_candles_ticker_datetime` sobre `(ticker, date_time)` para evitar duplicados por ticker y timestamp.
- **Índice compuesto** `idx_candles_ticker_datetime` sobre `(ticker, date_time DESC)` para acelerar la lectura del histórico ordenado por fecha descendente.
- El nombre de la tabla se mantiene como `candles`.

## Cobertura de tests y pruebas añadidas

- Todos los tests existentes pasan sin cambios: 596 tests, 0 fallos.
- Las restricciones de esquema son validadas implícitamente por el contexto Spring/H2 en los tests de integración.
- No se requieren tests adicionales para esta tarea, ya que es una configuración de esquema JPA sin nueva lógica de negocio.

## Advertencias de SonarQube o arquitectura

- Ninguna. Los cambios son puramente declarativos en la capa de infraestructura.
- Sin lógica de negocio afectada.

## Próximos pasos sugeridos

- **F1.3**: Crear `JpaCandleRepository` con operaciones de búsqueda por ticker y borrado por ticker.
- **F1.4**: Implementar port de persistencia de velas y su adaptador en infraestructura.
- **F1.5**: Integrar la persistencia de velas en `PolygonAdapter` tras parsear la respuesta válida.
