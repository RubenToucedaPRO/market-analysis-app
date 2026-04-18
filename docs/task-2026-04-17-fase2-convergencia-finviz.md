# Fase 2 Finviz — Convergencia e integración cruzada

**Fecha:** 2026-04-17  
**Rama:** `copilot/featureanalisis-viabilidad-transformacion`  
**Referencia:** `docs/rastreador-finviz.md` (Fase 2)

## Resumen de la tarea
Se completó la Fase 2 de convergencia para Finviz uniendo piezas de Domain, Application e Infrastructure sin introducir acoplamientos indebidos. El objetivo fue reemplazar puntos no integrados por wiring real y validar el flujo completo en test suite.

## Código generado
Archivos modificados/añadidos:

1. `src/main/java/com/market/analysis/domain/service/FinvizFilterMapperImpl.java`
   - Implementa `FinvizFilterMapper`.
   - Añade `map(Strategy)` como contrato principal para Application.
   - Mantiene mapeo por reglas para compatibilidad y tests.

2. `src/main/java/com/market/analysis/application/usecase/DefaultDeterministicTickerEvaluator.java` (nuevo)
   - Implementación real de `DeterministicTickerEvaluator`.
   - Orquesta cotización + histórico + cálculo de indicadores + evaluación determinista.
   - Devuelve trazabilidad controlada para casos de degradación.

3. `src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java`
   - Registro de beans de convergencia:
     - `FinvizFilterMapper`
     - `DeterministicTickerEvaluator`
     - `SuggestTickersUseCase` (`SuggestTickersService`)
   - Cierra el wiring Domain -> Application -> Port -> Infrastructure.

4. Tests:
   - `src/test/java/com/market/analysis/unit/application/usecase/DefaultDeterministicTickerEvaluatorTest.java` (nuevo)
   - `src/test/java/com/market/analysis/unit/domain/service/FinvizFilterMapperTest.java` (ajustes de contrato)

5. Documentación de estado:
   - `docs/rastreador-finviz.md` (Fase 2 marcada como implementada).

## Decisiones técnicas tomadas
1. **Convergencia sin HTML parsing en Application**: la capa Application solo consume puertos (`StockProviderPort`, `HistoricalProviderPort`, `FinvizScreenerPort`) y servicios de dominio.
2. **Evaluación determinista preservada**: la aptitud `APTO/NO_APTO` se deriva de `EvaluateStrategyService`, manteniendo el evaluador interno como fuente de verdad.
3. **Degradación controlada con trazabilidad**: cuando falta quote/histórico/indicadores, el evaluador devuelve `NO_APTO` con mensaje explícito en `traceability`.
4. **Cambios mínimos en contratos existentes**: se amplía `FinvizFilterMapperImpl` para implementar interfaz sin romper tests existentes de mapeo por reglas.

## Cobertura de tests y pruebas añadidas
- Tests dirigidos ejecutados:
  - `mvn -Dtest=FinvizFilterMapperTest,DefaultDeterministicTickerEvaluatorTest,SuggestTickersServiceTest test`
  - Resultado: **15 tests**, 0 fallos.
- Suite completa ejecutada:
  - `mvn test`
  - Resultado snapshot: **1029 tests**, 0 fallos.

## Advertencias de SonarQube o arquitectura
- Sin dependencias de infraestructura introducidas en Domain.
- Application no acopla parsing HTML ni detalles de Jsoup.
- Riesgo operativo mantenido: disponibilidad de proveedores externos (quote/histórico) gestionado con degradación funcional.

## Próximos pasos sugeridos
1. En Fase 3, añadir retry acotado configurable para fallos transitorios de proveedores.
2. Añadir métricas/observabilidad por causa de descarte en sugerencias (`NO_APTO` por falta de datos vs reglas no cumplidas).
3. Añadir test de integración Spring para validar wiring de beans Finviz en contexto real.
