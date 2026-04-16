# Corrección de límite mínimo de datos en cálculo MACD

## Resumen de la tarea

Se corrigió un fallo en `StockHistoricalService.calculateMacd(...)` para el caso límite de **exactamente 35 precios** (mínimo considerado válido por el servicio). La implementación anterior podía construir una serie MACD vacía por desalineación de índices entre EMA rápida y EMA lenta.

## Código generado

### Cambios en producción
- `src/main/java/com/market/analysis/domain/service/StockHistoricalService.java`
  - Ajuste de alineación entre series EMA:
    - Antes: iteración desde un índice absoluto sobre `emaFast`.
    - Ahora: alineación por desplazamiento `slow - fast` contra el índice de `emaSlow`.
  - Protección adicional:
    - Si la serie de señal no puede calcularse, retorna array vacío para mantener contrato seguro.
  - Endurecimiento de método auxiliar:
    - `calculateEmaAsDoubles(...)` ahora devuelve lista vacía cuando no hay datos suficientes.

### Cambios en tests
- `src/test/java/com/market/analysis/unit/domain/service/StockHistoricalServiceTest.java`
  - Nuevo test: `calculateIndicators_shouldReturnNonNullMacdForExactly35Prices`.
  - Verifica explícitamente que con 35 precios `macdLine`, `macdSignal` y `macdHistogram` son no nulos.

## Decisiones técnicas tomadas

- Se mantuvo el diseño actual y contrato público del servicio (sin cambios de firma ni de API).
- Se aplicó una corrección mínima y localizada en la lógica de indexación de MACD.
- Se añadió una validación defensiva en el cálculo de EMA en serie para evitar comportamientos ambiguos en entradas insuficientes.

## Cobertura de tests y pruebas añadidas

- Test focalizado ejecutado:
  - `mvn -Dtest=StockHistoricalServiceTest test` ✅
- Resultado:
  - 54 tests ejecutados, 0 fallos, 0 errores.
- Se añadió cobertura específica para el borde exacto de 35 datos.

## Advertencias de SonarQube o arquitectura

- No se detectaron cambios que rompan arquitectura hexagonal ni Clean Architecture.
- No se añadió lógica de infraestructura en dominio.
- No se introdujeron patrones inseguros (sin `System.out`, sin `th:utext`, sin cambios de seguridad web).

## Próximos pasos sugeridos

1. Ejecutar `mvn test` completo en CI para validación integral tras merge.
2. Considerar añadir un test adicional para serie exactamente mínima en escenarios no monótonos (ruido de mercado) para robustez matemática.
