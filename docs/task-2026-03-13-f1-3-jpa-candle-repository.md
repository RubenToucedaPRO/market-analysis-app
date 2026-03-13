# F1.3 – Crear el repositorio JPA de velas (JpaCandleRepository)

## Resumen de la tarea

Creación de `JpaCandleRepository`, la interfaz Spring Data JPA encargada del acceso a datos OHLCV históricos de la entidad `CandleEntity`. Esta capa habilita las operaciones de lectura y borrado necesarias para la estrategia de reemplazo transaccional por ticker que se implementará en F1.5.

---

## Código generado

### `JpaCandleRepository.java`

**Ubicación:** `src/main/java/com/market/analysis/infrastructure/persistence/repository/JpaCandleRepository.java`

```java
@Repository
public interface JpaCandleRepository extends JpaRepository<CandleEntity, Long> {

    List<CandleEntity> findByTickerOrderByDateTimeAsc(String ticker);

    void deleteByTicker(String ticker);

    boolean existsByTicker(String ticker);

}
```

---

## Operaciones declaradas

| Método | Propósito |
|---|---|
| `findByTickerOrderByDateTimeAsc(String ticker)` | Obtiene todas las velas de un ticker ordenadas por fecha ascendente para construir el dataset histórico del gráfico. |
| `deleteByTicker(String ticker)` | Elimina todas las velas existentes de un ticker antes del reemplazo transaccional (F1.5). |
| `existsByTicker(String ticker)` | Comprobación auxiliar para validaciones y tests. |

---

## Decisiones técnicas tomadas

- **Spring Data JPA derivation**: todos los métodos siguen la convención de nombre de Spring Data JPA y no requieren `@Query`. La implementación es generada automáticamente en tiempo de arranque.
- **Sin lógica de negocio**: la interfaz es puramente declarativa. La lógica transaccional de reemplazo se implementará en `SqlCandleHistoryRepository` (F1.4 / F1.5).
- **Índice existente**: el índice `idx_candles_ticker_datetime` definido en `CandleEntity` (F1.2) garantiza rendimiento eficiente en las consultas por ticker y fecha.
- **Convención de paquete**: sigue el patrón establecido por `JpaProhibitedTickerRepository`, `JpaCompanyProfileRepository`, etc.

---

## Cobertura de tests y pruebas

La interfaz JPA no requiere tests unitarios propios ya que:

- Spring Data JPA valida los métodos derivados en tiempo de arranque.
- Los métodos serán ejercidos indirectamente a través de los tests de `SqlCandleHistoryRepository` (F1.4), donde `JpaCandleRepository` se inyecta como mock.

Los tests de `CandleMapperTest` y `CandleTest` existentes continúan pasando sin cambios.

---

## Advertencias de SonarQube / arquitectura

- Ninguna: la interfaz es estrictamente declarativa y sigue los patrones establecidos en el proyecto.

---

## Próximos pasos sugeridos

- **F1.4**: Crear `CandleHistoryPort` (puerto de dominio de salida) y `SqlCandleHistoryRepository` que lo implemente usando `JpaCandleRepository`.
- **F1.5**: Implementar la estrategia transaccional de reemplazo por ticker en `SqlCandleHistoryRepository`.
- **F1.6**: Extender el parseo de `PolygonAdapter` para extraer velas OHLCV completas.
- **F1.7**: Integrar la persistencia histórica dentro del flujo de `fetchHistoricalData`.
