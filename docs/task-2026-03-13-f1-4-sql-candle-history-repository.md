# Task: F1.4 – Crear SqlCandleHistoryRepository

**Fecha:** 2026-03-13  
**Estado:** Completado

---

## Resumen de la tarea

Implementación del paso F1.4 del plan de persistencia de velas históricas Polygon.  
Se ha creado `SqlCandleHistoryRepository`, el componente de infraestructura encargado de encapsular las operaciones de guardado masivo de velas OHLCV, separando los detalles JPA del adaptador HTTP `PolygonAdapter`.

---

## Código generado

### `SqlCandleHistoryRepository.java`
Ubicación: `src/main/java/com/market/analysis/infrastructure/persistence/repository/`

- Anotado con `@Component` para integración con el contenedor Spring.
- Inyección por constructor de `JpaCandleRepository` y `CandleMapper`, usando `@RequiredArgsConstructor` de Lombok.
- Método público `saveCandlesForTicker(String ticker, List<Candle> candles)` declarado con cuerpo vacío (stub). La lógica transaccional de reemplazo (delete-then-insert) se implementará en F1.5.

---

## Decisiones técnicas tomadas

| Decisión | Justificación |
|---|---|
| Nombre `SqlCandleHistoryRepository` | Coherente con el patrón `Sql*Repository` del proyecto |
| Cuerpo vacío en F1.4 | Según el plan, F1.5 implementa la lógica transaccional |
| No se abre puerto de dominio | El puerto `CandleHistoryPort` se introduce en F2.2 |
| Inyección por constructor | Buenas prácticas del proyecto; evita `@Autowired` en campo |

---

## Archivos afectados

- `SqlCandleHistoryRepository.java` (nuevo)
- `SqlCandleHistoryRepositoryTest.java` (nuevo)

---

## Cobertura de tests

Se ha creado `SqlCandleHistoryRepositoryTest` en el paquete `unit/infrastructure/persistence/repository/`.

### Tests incluidos

| Test | Descripción |
|---|---|
| `saveCandlesForTicker_stubbed_noInteractions` | Verifica que el método no interactúa con ninguna dependencia (stub F1.4) |
| `saveCandlesForTicker_emptyList_noError` | Verifica que la llamada con lista vacía no lanza excepción |

---

## Advertencias de arquitectura

- Ninguna. El componente no contiene lógica de negocio y no altera el dominio.
- SonarQube: El método stub puede generar un aviso de método vacío; está justificado por el plan de implementación incremental.

---

## Próximos pasos sugeridos

1. **F1.5**: Implementar la lógica transaccional de reemplazo en `saveCandlesForTicker` (delete-then-insert por ticker).
2. **F1.6**: Extender el parseo de `PolygonAdapter` para extraer velas OHLCV completas.
3. **F1.7**: Integrar `SqlCandleHistoryRepository` en `PolygonAdapter` para persistir las velas tras cada fetch.
