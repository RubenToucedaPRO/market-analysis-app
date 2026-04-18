# Stream B — Adapter de scraping Finviz con Jsoup

**Fecha:** 2026-04-17  
**Rama:** `copilot/sub-pr-109-again`

## Resumen de la tarea
Se implementa `JsoupFinvizAdapter` en Infrastructure como implementación de `FinvizScreenerPort`, añadiendo configuración de `User-Agent`, timeout, paginación por `r=1,21,41...`, deduplicación de símbolos y tolerancia a errores de red/cambios de estructura HTML.

## Código generado
- `src/main/java/com/market/analysis/infrastructure/external/finviz/JsoupFinvizAdapter.java`
- `src/test/java/com/market/analysis/unit/infrastructure/external/finviz/JsoupFinvizAdapterTest.java`
- `src/test/resources/fixtures/finviz/screener-page-1.html`
- `src/test/resources/fixtures/finviz/screener-page-2.html`
- `src/test/resources/fixtures/finviz/screener-structure-changed.html`
- `config/application.properties` (propiedades `finviz.*`)
- `pom.xml` (dependencia `org.jsoup:jsoup:1.18.3`)

## Decisiones técnicas tomadas
1. El scraping queda encapsulado en Infrastructure mediante un adapter de puerto saliente, sin mover lógica de negocio fuera del dominio/aplicación.
2. Se usa `LinkedHashSet` para deduplicar y preservar orden de aparición.
3. La paginación avanza en bloques de 20 (`r=1,21,41...`) y se detiene por ausencia de resultados o de siguiente página.
4. Los errores de red se gestionan de forma tolerante: se devuelven resultados ya recopilados y se evita propagar fallos de parsing/red al dominio.

## Cobertura de tests y pruebas añadidas
- Test unitario del adapter con fixtures HTML:
  - paginación + deduplicación,
  - error de red en páginas posteriores,
  - cambio de estructura HTML (sin símbolos detectables),
  - `maxResults <= 0`.
- Ejecución validada:
  - `mvn -Dtest=JsoupFinvizAdapterTest,FinvizScreenerPortContractTest test`
  - `mvn test`

## Advertencias SonarQube / arquitectura
- Sin lógica de negocio añadida fuera de Infrastructure.
- Constructor con menos de 7 parámetros.
- Recursos de test cerrados con `try-with-resources`.

## Próximos pasos sugeridos
1. Integrar el puerto en Stream C (orquestador `SuggestTickersUseCase`).
2. Ajustar observabilidad (métricas/log estructurado) en fase de hardening.
