# Carrusel Home: 1 captura grande por slide

## Resumen de la tarea

El carrusel de capturas de `home.html` mostraba 3 tarjetas por slide (`col-md-4`),
por lo que cada imagen (~1413×953 px) renderizaba a ~350–400 px y el texto de las
capturas quedaba ilegible. Se reestructuró a **1 captura grande por slide** (11 slides),
con tarjeta centrada (`col-12 col-lg-10 col-xl-9`) e imagen a ancho completo.

Trabajo previo relacionado (commit `72f2bc6`): fusión de atributos `class`
duplicados en `strategies/list.html`, `analysis/ticker-detail.html` y
`analysis/analysis.html`, más corrección de `TemplateProcessingException` en
`ticker-detail.html:91` (`#{...}` anidado dentro de `${...}`).

## Código generado

- `templates/fragments/screenshot-card.html` (nuevo): fragmento
  `card(img, alt, title, description)` con la tarjeta de captura. Uso:
  `~{fragments/screenshot-card :: card(img='screenshot-1.webp',
  alt=#{...}, title=#{...}, description=#{...})}`.
- `templates/home.html`: 11 `carousel-item` de 1 tarjeta vía el fragmento;
  11 indicadores generados con `th:each="i : ${#numbers.sequence(0, 10)}`;
  flechas con `aria-label` i18n (`carousel.prev`/`carousel.next`).
- `static/css/styles.css`: bloque `.screenshots-carousel` (imagen `aspect-ratio:
  1413 / 953` + `object-fit: cover` para altura estable; controles oscuros con
  `filter: invert(0.55)` visibles sobre `bg-light`; indicadores estáticos color
  `var(--primary-color)`).
- `messages.properties`: `carousel.prev=Anterior`, `carousel.next=Siguiente`,
  `carousel.slide=Diapositiva {0}`.

## Decisiones técnicas tomadas

- 1 captura por slide (opción elegida por el usuario frente a 2 por slide):
  imágenes ~3× más grandes, texto legible, y desaparece el hueco del último
  slide (antes 3+3+3+2).
- Fragmento con parámetros `#{...}` como argumentos (patrón válido Thymeleaf,
  sin preprocesamiento `__...__`); elimina las 11 repeticiones del bloque card
  (AGENTS.md §7: evitar duplicación, usar `th:fragment`).
- Indicadores generados por `th:each` en vez de 11 botones hardcodeados;
  `aria-label` parametrizado reuse el patrón existente
  (`#{analysis.count(${tickers.size()})}`).
- Sin colores hex hardcodeados en CSS (`var(--primary-color)` del proyecto);
  sin `th:utext`; sin lógica de negocio en la vista; sin textos hardcodeados.
- Flechas prev/next ocultas en móvil (`d-none d-md-flex`): en pantallas
  pequeñas tapaban la imagen y son difíciles de pulsar; la navegación queda en
  swipe táctil (Bootstrap `touch` por defecto) + indicadores, que conservan su
  área táctil (bordes transparentes de Bootstrap).

## Cobertura de tests y pruebas

- `HomeControllerTest` (MockMvc `@WebMvcTest`, renderiza Thymeleaf de verdad):
  `Tests run: 2, Failures: 0, Errors: 0` → `GET /` devuelve 200 con vista `home`,
  lo que valida sintaxis del template, resolución del fragmento y de las claves
  i18n nuevas.
- `AnalyzeTickerControllerTest`: 16/16 en verde (commit previo).
- Script de chequeo: ningún tag con doble `class=` en `home.html` ni en el
  nuevo fragmento.
- Pendiente verificación manual: arranque local y revisión visual desktop/móvil.

## Advertencias de SonarQube o arquitectura

- Expresiones `th:replace` multilínea del fragmento: cada línea < 120 caracteres
  (límite SpEL `S119` / guías Thymeleaf del proyecto).
- Complejidad de la vista sin cambios lógicos; solo presentación.
- `aspect-ratio` fija 1413/953: coincide con la resolución real de los 11 webp;
  si se añaden capturas de otra proporción, revisar.

## Próximos pasos sugeridos

- Revisión visual local y ajuste fino (intervalo `data-bs-interval`, altura
  máxima en pantallas ultrawide).
- Valorar `loading="lazy"` en las imágenes no visibles del carrusel.
- Commit separado de esta tarea cuando se apruebe el resultado visual.
