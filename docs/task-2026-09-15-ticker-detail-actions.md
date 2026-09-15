# Ticker Detail: botones de acción apilados en móvil

## Resumen de la tarea

En `analysis/ticker-detail.html`, los cuatro botones inferiores (Volver,
Actualizar Datos, Eliminar, Generar análisis) quedaban en una fila rígida sin
salto de línea en smartphone (~360px): se veían alineados y el último quedaba
cortado fuera del viewport. Causa: el contenedor interior
(`<div class="d-flex gap-2">`) no tenía `flex-wrap`; el `flex-wrap` del
contenedor exterior no servía al tener un único hijo.

## Código generado

- `templates/analysis/ticker-detail.html:340`: contenedor interior pasa a
  `class="d-flex gap-2 flex-wrap actions-stack"` (solo clases, sin cambios de
  expresiones Thymeleaf).
- `templates/strategies/detail.html:238`: misma operación en la botonera
  inferior (`Sugerir tickers`, `Editar`, `Eliminar`): `class="strategy-actions
  flex-wrap actions-stack"`, reutilizando las reglas responsive sin duplicar
  CSS.
- `static/css/styles.css` (bloque `Responsive Design`, `max-width: 768px`):
  `.actions-stack > * { flex: 1 1 100%; }` y
  `.actions-stack .btn { width: 100%; }` → en móvil/tablet cada botón ocupa su
  propia fila a ancho completo; en desktop todo queda idéntico.

## Decisiones técnicas tomadas

- Opción elegida por el usuario: apilados full-width (frente a filas ajustadas
  de tamaño natural) — más fácil de pulsar con el pulgar.
- Breakpoint 768px (convención ya existente en `styles.css`) en vez de 576px:
  a 768px los cuatro botones también desbordan.
- Sin claves i18n nuevas, sin cambios de lógica ni del fragmento
  `ai-analysis-button` (su botón ya trae `w-100`).

## Cobertura de tests y pruebas

- Total combinado `Tests run: 24, Failures: 0, Errors: 0`, `BUILD SUCCESS`:
  `AnalyzeTickerControllerTest` (16), `HomeControllerTest` (2) y
  `StrategyControllerViewTest` (6, incluyendo render 200 OK de
  `strategies/detail`, lo que valida la sintaxis del template modificado).
- El cambio es solo de clases CSS/Bootstrap, sin expresiones Thymeleaf nuevas:
  riesgo de `TemplateProcessingException` nulo.
- Pendiente verificación manual: vista a 360px (4 botones visibles, uno por
  fila, sin scroll horizontal) y en desktop (fila única como antes).

## Advertencias de SonarQube o arquitectura

- Sin lógica en la vista; sin `th:utext`; sin textos hardcodeados; sin
  duplicación de bloques.

## Próximos pasos sugeridos

- Revisión visual local y commit de la tarea.
- Valorar el mismo patrón en otras botoneras con formularios en fila
  (p. ej. columna de acciones en `analysis.html`) si se detecta el mismo
  desbordamiento en móvil.
