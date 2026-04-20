# Fase 4 — UI de keywords prohibidas en `/prohibited-tickers`

## Resumen de la tarea
- Se implementó la Fase 4 del plan `docs/gestion-keywords-prohibidas.md`.
- La vista de `/prohibited-tickers` ahora mantiene la tabla de tickers prohibidos y añade un panel lateral para listar, crear y eliminar keywords prohibidas.
- Se amplió el controlador para cargar y gestionar keywords usando el caso de uso ya existente.

## Código generado (si aplica)
- `src/main/java/com/market/analysis/presentation/controller/ProhibitedTickerController.java`
  - Carga `prohibitedKeywords` en el `GET /prohibited-tickers`.
  - Añade `POST /prohibited-tickers/keywords` para alta de keyword con notificaciones flash.
  - Añade `POST /prohibited-tickers/keywords/delete` para borrado de keyword con notificaciones flash.
- `src/main/resources/templates/prohibited-tickers/list.html`
  - Nuevo layout en 2 columnas (tabla + sidebar).
  - Sidebar responsive/collapsible en móvil con Bootstrap 5.
  - Formulario de alta rápida y listado con acción de borrado por keyword.
- `src/test/java/com/market/analysis/unit/presentation/controller/ProhibitedTickerControllerTest.java`
  - Nuevas pruebas para listado con `prohibitedKeywords`, alta y borrado de keyword, y error de validación.

## Decisiones técnicas tomadas
- Se reutiliza `ManageProhibitedKeywordUseCase` para mantener la lógica de negocio en Application, sin mover reglas al template.
- Se conserva la UX existente (flash messages con `UiNotification` + Bootstrap), extendiéndola a la gestión de keywords.
- El sidebar usa `collapse` de Bootstrap para móvil y se muestra fijo en pantallas grandes (`d-lg-block`).

## Cobertura de tests y pruebas añadidas si faltan
- Validación base previa: `mvn test` en verde antes de cambios.
- Pruebas añadidas/ajustadas:
  - `ProhibitedTickerControllerTest` (listado con keywords, alta OK/error, borrado OK).

## Advertencias de SonarQube o arquitectura
- Se mantiene `th:text` para renderizar valores dinámicos y evitar `th:utext`.
- No se introduce lógica de negocio en Thymeleaf; solo renderizado y envío de acciones POST.

## Próximos pasos sugeridos
1. Fase 5: ampliar pruebas de integración JPA para unicidad y consultas de keywords.
2. Revisar mensajes de validación para internacionalización completa en `messages.properties`.
