# Task 2026-09-22: Aviso de reglas incompatibles en mapeo Finviz

## Resumen
Hallazgo del usuario probando: estrategia con precio >20000 y precio <40
generaba `sh_price_o20000,sh_price_u40` sin avisar; Finviz lo vacía con el AND
y el usuario no sabía por qué. El mapper traducía cada regla suelta sin mirar
si se contradicen. Ahora `FinvizFilterMapperImpl` detecta cotas incompatibles
sobre el mismo sujeto y añade warning (fluye solo a flash parcial + traza,
sin tocar Application ni Infrastructure). Los filtros se mantienen para que
Finviz devuelva vacío rápido (sin gastar cuota Finnhub); si Finviz devolviera
filas, la evaluación determinista las descartaría igual.

## Código generado
- `FinvizFilterMapperImpl`: `collectRangeBound` (solo reglas mapeadas con
  target estático CONSTANT/VALUE y operador >/</>/alias; SMA-cross no son
  rangos) + `detectIncompatibleRanges` (pares mismo sujeto, `lower >= upper`
  → warning `"Rules 'A' y 'B' are incompatible: no ticker can satisfy both."`)
  + record `RangeBound`. Dominio puro, sin dependencias nuevas.
- `FinvizFilterMapperTest`: 3 tests nuevos (incompatible avisa, compatible no
  avisa, distinto sujeto / mismo lado no avisa); fixture existente con rangos
  imposibles (PRICE>100/<80...) corregido a rangos coherentes (>100/<180...).
- El aviso no llegaba a la página (plantillas no pintaban `warnings`): añadidos
  `WebConstants.ATTR_SNAPSHOT_WARNINGS`, atributo en `loadLastSuggestionSnapshot`
  y bloque "Avisos" en `strategy-traceability.html` (+ claves i18n
  `traceability.warnings/no_warnings`); test de vista ampliado.
- Petición del usuario: avisos del mapper en español (antes en inglés).
- Segunda petición: si hay incompatibles ya no se llama a Finviz:
  `FinvizFilterMappingResult.incompatibleRanges` + early-return en
  `SuggestTickersService` (log `suggest_tickers_incompatible_filters`);
  test de servicio verifica `findTickers` nunca invocado.

## Decisiones técnicas tomadas
- Warning (no unmappable): cada regla sí mapea; es la combinación la imposible.
- Comparación en params crudos (misma escala por sujeto); solo desigualdades
  estrictas, que son las únicas mapeadas.
- No se elimina el filtro contradictorio: vacío rápido de Finviz + aviso
  explicativo guían al usuario a quitar un filtro.

## Cobertura de tests y pruebas añadidas si faltan
- `FinvizFilterMapperTest`: 11 run, 0 fallos (8 previos + 3 nuevos).
- Suite completa `mvn test`: 1066 run, 0 fallos, 0 errores.

## Advertencias de SonarQube o arquitectura
- Dominio puro intacto; métodos pequeños, anidamiento <4.
- Avisos del mapper en español (petición del usuario; antes en inglés, como
  `EMPTY_FILTERS_WARNING` que sigue en inglés: el flujo mezcla idiomas y queda
  pendiente uniformar en otra tarea).

## Próximos pasos sugeridos
1. Commit `fix:/test:/docs:`, push y PR a `main` tras validación.
2. Idea futura (no hacer): validación al crear la estrategia para impedir
   guardar rangos imposibles.
