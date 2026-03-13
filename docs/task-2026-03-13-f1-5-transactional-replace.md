# F1.5 — Implementar la estrategia transaccional de reemplazo por ticker

## Resumen de la tarea

Implementación de la estrategia transaccional de reemplazo en `SqlCandleHistoryRepository`.
El método `saveCandlesForTicker` ahora ejecuta un ciclo atómico **delete-then-insert** dentro
de una única transacción JPA, garantizando que el histórico de cada ticker queda siempre
completo y sin duplicados tras cada fetch.

---

## Código generado / modificado

### `SqlCandleHistoryRepository.java`

Archivo afectado:
`src/main/java/com/market/analysis/infrastructure/persistence/repository/SqlCandleHistoryRepository.java`

Cambios:
- Añadida anotación `@Transactional` al método `saveCandlesForTicker`.
- Implementada la lógica de reemplazo:
  1. Guarda defensiva: si `candles` es `null` o vacía, retorno inmediato (no-op).
  2. `jpaCandleRepository.deleteByTicker(ticker)` — borra el histórico existente.
  3. Mapeo de dominio → entidad con `CandleMapper::toEntity`.
  4. `jpaCandleRepository.saveAll(entities)` — inserta el nuevo lote.
- Añadido logging con SLF4J (`log.debug` antes del ciclo, `log.info` al finalizar).
- Actualizado el Javadoc del método para reflejar el comportamiento real.

### `SqlCandleHistoryRepositoryTest.java`

Archivo afectado:
`src/test/java/com/market/analysis/unit/infrastructure/persistence/repository/SqlCandleHistoryRepositoryTest.java`

Los dos tests de stub anteriores han sido reemplazados por 5 tests orientados al comportamiento real:

| Test | Verificación |
|------|-------------|
| `saveCandlesForTicker_emptyList_noInteractions` | Lista vacía → sin interacciones JPA ni mapper |
| `saveCandlesForTicker_nullList_noInteractions` | Lista nula → sin interacciones JPA ni mapper |
| `saveCandlesForTicker_validList_deleteBeforeSave` | Delete ocurre antes de saveAll (InOrder) |
| `saveCandlesForTicker_validList_mapsAndSavesAllCandles` | Mapper invocado N veces; saveAll recibe las N entidades |
| `saveCandlesForTicker_validList_deletesExactlyOnceForTicker` | deleteByTicker invocado exactamente una vez con el ticker correcto |

---

## Decisiones técnicas

- **`@Transactional` en el método concreto**: La anotación se coloca en el método público de
  la clase, no en la clase entera, para limitar el alcance de la transacción. Al ser una clase
  `@Component` (no un proxy JDK), Spring crea un proxy CGLIB que intercepta la transacción
  correctamente.
- **Guarda ante lista vacía/nula**: Evita un delete accidental que dejaría el ticker sin
  histórico si el upstream devuelve una respuesta vacía por error transitorio.
- **`saveAll` en lote**: Más eficiente que `save` individual; JPA puede optimizarlo con
  batch inserts si `spring.jpa.properties.hibernate.jdbc.batch_size` está configurado.
- **Orden garantizado**: `deleteByTicker` antes de `saveAll` asegura que no puede haber
  duplicados aunque la constraint única ya lo protegería.
- **Logging**: `debug` al inicio (potencialmente verboso en producción), `info` al final
  para auditoría rápida de cuántas velas se han persistido.

---

## Cobertura de tests

| Escenario | Cubierto |
|-----------|---------|
| Lista nula → no-op | ✅ |
| Lista vacía → no-op | ✅ |
| Lista válida → delete antes de save | ✅ |
| Lista válida → mapper invocado por cada vela | ✅ |
| Lista válida → deleteByTicker exactamente una vez | ✅ |

Resultado: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` — BUILD SUCCESS.

---

## Advertencias de arquitectura

- No hay lógica de negocio en esta capa; se trata de infraestructura pura.
- El mapper se inyecta por constructor (Lombok `@RequiredArgsConstructor`) — sin field injection.
- La clase no implementa aún ningún puerto de dominio explícito; eso se definirá en F2 cuando
  sea necesario exponerla a través de un puerto de salida.

---

## Próximos pasos sugeridos

- **F1.6** — Extender el parseo de `PolygonAdapter` para extraer velas completas (OHLCV + timestamp).
- **F1.7** — Integrar la llamada a `saveCandlesForTicker` dentro del flujo `fetchHistoricalData`.
- **F1.8** — Añadir logging adicional de observabilidad en el adapter.
