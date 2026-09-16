# Fix: ids no numéricos no llegan al detalle de análisis

- Fecha: 2026-09-16
- Slug: `analysis-ticker-numeric-id`
- Alcance: `AnalyzeTickerController` + test de enrutado (recuperado del stash, sin cambios de comportamiento feliz)
- Base: `main` tras PR #148 (mismo patrón que el fix de `StrategyController`)

## Resumen

El mismo defecto latente corregido en `StrategyController` (PR #148) existía en
`AnalyzeTickerController`: `GET /analysis/ticker/abc` caía en el handler de detalle
con `id="abc"` y explotaba en la conversión a `long`, mostrando la página de error
con un detalle técnico críptico.

Ahora los 4 mapeos con `{id}` exigen id numérico (`{id:\d+}`): detalle, gráfico,
JSON de velas y update. Un id no numérico ya no invoca lógica de negocio: cae a la
página de error genérica sin crash de conversión.

## Código generado

### 1. `presentation/controller/AnalyzeTickerController.java`

- `@GetMapping("/ticker/{id}")` → `@GetMapping("/ticker/{id:\\d+}")` (detalle).
- `@GetMapping("/ticker/{id}/chart")` → `{id:\\d+}` (vista de gráfico).
- `@GetMapping("/ticker/{id}/candles")` → `{id:\\d+}` (JSON para los gráficos JS).
- `@PostMapping("/ticker/{id}/update")` → `{id:\\d+}` (re-evaluación desde el detalle).

### 2. `AnalyzeTickerControllerRoutingTest.java` (nuevo)

- `shouldFallBackToErrorPageForNonNumericTickerIds`: `GET /analysis/ticker/abc`,
  `.../abc/chart` y `.../abc/candles` → 200 con la vista de error (sin crash),
  y `verify(..., never())` de `findStockDataById` / `findCandlesByStockId`.

## Decisiones técnicas

- Mismo patrón ya aprobado en la PR #148 para `StrategyController`; sin cambios
  de dominio ni de flujo feliz.
- Las acciones POST-only de análisis (`/update`, `/delete`, `/getTickerData`) no
  necesitan landing GET como `/strategies/edit`: no tienen mapeo GET con `{id}`,
  así que caen a la página de error sin crash. `safeReferer()` las cubre de forma
  genérica si aparecen como `Referer`.
- Trabajo recuperado íntegro del stash (código + test); solo se añade este doc.

## Cobertura de tests

- Nuevo `AnalyzeTickerControllerRoutingTest` (1 test, 3 URLs + `never()` en mocks).
- Ejecución: `mvn test` → `Tests run: 1030, Failures: 0, Errors: 0, BUILD SUCCESS`.
- Las líneas `ERROR` del log durante ese test son el `GlobalExceptionHandler`
  pintando la página de error a propósito, no fallos.
- Sin `lenient`; Mockito estricto en verde.

## Advertencias de SonarQube o arquitectura

- Solo enrutado en el controlador (sin lógica de negocio); CSRF, `th:text` y capas intactos.
- Expresiones de ruta triviales; complejidad y anidamiento sin cambios.

## Próximos pasos sugeridos

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Escribe a mano `/analysis/ticker/abc` en la barra de direcciones → ves la
  página de error genérica (NO un crash ni una pantalla en blanco). Es el
  comportamiento correcto: esa URL no existe.
2. Ve a `/analysis`, abre cualquier ticker y copia su URL (`/analysis/ticker/1517`,
  por ejemplo) → el detalle carga normal con sus datos. Resultado visible
  DISTINTO del paso 1: este es el flujo feliz, intacto.

### B. Para interpretar el aviso (informativo, NO es una tarea)

- Si alguna vez ves la página de error al navegar a un ticker, copia el detalle
  técnico que muestra: dice qué falló de verdad (recurso inexistente, dato no
  encontrado) en vez de un críptico error de conversión.
