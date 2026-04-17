# Eliminar modo de ejecución de Finviz

## Resumen
Se eliminó por completo la política de ejecución `FinvizExecutionMode` para que la sugerencia de tickers funcione siempre en modo tolerante. Cuando una regla no se puede mapear a Finviz, el caso de uso continúa con las reglas sí representables y devuelve las advertencias correspondientes.

## Cambios realizados
- Se eliminó el enum `FinvizExecutionMode`.
- Se quitaron los campos `executionMode` de los DTOs de request y response.
- Se simplificó `SuggestTickersService` para no bloquear nunca la ejecución por reglas no mapeables.
- Se actualizaron los tests unitarios y de contrato para reflejar el comportamiento siempre tolerante.

## Decisiones técnicas
- La política `STRICT` no aportaba valor funcional porque la UI no la exponía y el flujo actual no requiere un modo alternativo.
- Mantener una sola conducta reduce complejidad accidental y evita transportar estado innecesario entre capas.
- La trazabilidad se conserva mediante `warnings` y `unmappableRules`.

## Cobertura de tests
- Se actualizó la cobertura del caso de uso para verificar que las reglas no mapeables no bloquean la ejecución.
- Se ajustaron los tests de contrato de request y response para eliminar la dependencia del modo de ejecución.

## Riesgos o advertencias
- Si en el futuro se necesita un modo estricto real, habrá que reintroducirlo con una decisión visible en UI o API, no solo como un flag interno.

## Próximos pasos sugeridos
- Revisar si conviene renombrar mensajes y comentarios que todavía hablen de “modo” en documentación externa.
- Ejecutar la suite de tests del módulo de sugerencia de tickers para validar el refactor.