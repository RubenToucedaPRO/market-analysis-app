# F1.10 – Prueba de integración JPA y validación en H2

## Resumen de la tarea

Implementación de F1.10 del plan de registro de tickers en Polygon: prueba de integración JPA para verificar que la estrategia transaccional de reemplazo por ticker funciona correctamente con persistencia real en H2.

---

## Archivos generados o modificados

| Archivo | Operación |
|---|---|
| `src/test/java/.../integration/infrastructure/persistence/repository/SqlCandleHistoryRepositoryIT.java` | Nuevo |
| `src/main/java/.../infrastructure/persistence/repository/JpaCandleRepository.java` | Modificado |

---

## Código generado

### `SqlCandleHistoryRepositoryIT.java`

Test de integración anotado con `@DataJpaTest` y `@Import` que levanta el contexto JPA real con H2 en memoria. Cubre los siguientes escenarios:

1. **Save 240 candles** – inserta 240 velas y verifica que todas quedan persistidas.
2. **Ordered ascending** – verifica que `findByTickerOrderByDateTimeAsc` retorna las velas ordenadas ascendentemente.
3. **Replace dataset** – llama dos veces para el mismo ticker; verifica que el segundo batch reemplaza completamente al primero.
4. **No previous candles remain** – tras el reemplazo, confirma que no queda ninguna vela del batch anterior.
5. **No duplicates when same batch saved twice** – llama dos veces con el mismo batch; confirma que no hay duplicados.
6. **Isolation between tickers** – sustituye el dataset de AAPL sin alterar las velas de MSFT.

### `JpaCandleRepository.java` – corrección de `deleteByTicker`

Se reemplazó el derived-delete por una JPQL bulk delete:

```java
@Modifying(clearAutomatically = true)
@Query("DELETE FROM CandleEntity c WHERE c.ticker = :ticker")
void deleteByTicker(@Param("ticker") String ticker);
```

**Motivo:** Hibernate aplica las operaciones de la primera sesión en orden INSERT → UPDATE → DELETE al hacer flush. Con el derived-delete (que internamente hace SELECT + EntityManager.remove()), el order de flush causaba una violación de la restricción única `uq_candles_ticker_datetime` cuando se llamaba `saveCandlesForTicker` dos veces dentro de la misma transacción exterior (comportamiento propio de `@DataJpaTest`). La JPQL bulk delete con `clearAutomatically = true` se ejecuta inmediatamente, limpia el primer nivel de caché y garantiza un delete-then-insert correcto en todos los contextos transaccionales.

---

## Decisiones técnicas

- **`@DataJpaTest` + `@Import`**: carga solo el slice JPA (sin HTTP ni otros beans) e importa manualmente `SqlCandleHistoryRepository` y `CandleMapper`. Esto mantiene el test ligero y aislado.
- **`@ActiveProfiles("test")`**: usa H2 en memoria con `ddl-auto=create-drop` definida en `application-test.properties`.
- **`@BeforeEach deleteAll()`**: limpia el estado antes de cada test para evitar interferencias entre tests dentro de la misma sesión H2.
- **`clearAutomatically = true`** en `@Modifying`: garantiza coherencia del primer nivel de caché tras un bulk delete JPQL.
- **Generación de 240 velas** con offset de un día por vela para respetar la restricción única `(ticker, date_time)`.

---

## Cobertura de tests añadida

| Escenario | Test |
|---|---|
| Persistencia de 240 velas | `saveCandlesForTicker_240Candles_allPersisted` |
| Orden ascendente por fecha | `saveCandlesForTicker_240Candles_orderedAscending` |
| Reemplazo de dataset completo | `saveCandlesForTicker_calledTwice_replacesDataset` |
| Ausencia de velas anteriores tras reemplazo | `saveCandlesForTicker_replace_noPreviousCandlesRemain` |
| Sin duplicados al guardar el mismo batch dos veces | `saveCandlesForTicker_sameBatchTwice_noDuplicates` |
| Aislamiento entre tickers | `saveCandlesForTicker_replaceTicker_doesNotAffectOtherTicker` |

---

## Advertencias de arquitectura / SonarQube

- No se ha introducido lógica de negocio en la capa de infraestructura.
- Se mantiene inyección por constructor en `SqlCandleHistoryRepository`.
- La corrección en `deleteByTicker` es un fix de correctness (no solo de tests): aplica a cualquier escenario donde `saveCandlesForTicker` se llame dentro de una transacción exterior.

---

## Próximos pasos sugeridos

- Iniciar Fase 2: lectura de velas para chart (F2.1 en adelante).
- Valorar añadir un test de integración que verifique el endpoint REST `/analysis/ticker/{stockId}/chart` una vez se implemente F2.3-F2.5.
