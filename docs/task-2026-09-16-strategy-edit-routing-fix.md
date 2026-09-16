# Fix: guardar estrategia fallaba con "Failed to convert ... For input string: edit"

- Fecha: 2026-09-16
- Slug: `strategy-edit-routing-fix`
- Alcance: `StrategyController`, `GlobalExceptionHandler` + tests

## Resumen

Al pulsar "Guardar Estrategia" la app mostraba la página de error con el detalle
técnico `Method parameter 'id': Failed to convert value of type 'java.lang.String'
to required type 'long'; For input string: "edit"`.

Causa raíz (defecto latente, no introducido por la mejora SMA):

1. `StrategyController` tenía `@GetMapping("/{id}")` sin restringir. Un `GET /strategies/edit`
   caía en el detalle con `id="edit"` y explotaba en la conversión a `long`.
2. El formulario de edición vive en la URL `/strategies/edit` (resultado de `POST /strategies/edit`).
   Si el guardado (`POST /strategies`) fallaba por validación de dominio, `GlobalExceptionHandler`
   redirigía al `Referer` (`/strategies/edit`) por GET, encadenando el error anterior y
   ocultando el mensaje real de validación.

Reproducido en test antes del fix: el log mostraba exactamente el mensaje del usuario
(`GlobalExceptionHandler: Unexpected exception occurred: Method parameter 'id' ... "edit"`).

## Código generado

### 1. `presentation/controller/StrategyController.java`

- `@GetMapping("/{id}")` → `@GetMapping("/{id:\\d+}")` (detalle solo con id numérico).
- Igual restricción en `@PostMapping("/{id:\\d+}/suggest-tickers")` y
  `@PostMapping("/{id:\\d+}/add-suggested-tickers")` (misma clase de fallo).
- Nuevo `@GetMapping({"/edit", "/delete"}) redirectPostOnlyActions()` → `redirect:/strategies`.
  Sin esto, `GET /strategies/edit` devolvía 405→página de error genérica (las acciones
  existen solo por POST); ahora hay landing segura para pestañas obsoletas o cadenas de redirect.

### 2. `presentation/exception/GlobalExceptionHandler.java`

- Nuevo `safeReferer(req)`: si el `Referer` apunta a una acción POST-only (path terminado en
  `/edit` o `/delete`), redirige a la sección padre (`/strategies`, `/rule-definitions`, …).
  Referers normales se conservan; ausente o ilegible → `/`.
- `redirectWithError()` lo usa; el mensaje flash de validación se preserva.

## Decisiones técnicas

- Sin cambios de dominio ni de flujo feliz: crear/editar/guardar funcionan igual cuando
  la validación pasa.
- No se re-renderiza el formulario con errores (cambio mayor, fuera de alcance); se mantiene
  el patrón PRG con flash ya existente, pero a una URL válida.
- `th:text`, CSRF y capas intactos; métodos nuevos con complejidad trivial (Sonar OK).

## Cobertura de tests

- `StrategyControllerViewTest.shouldNotRoutePostOnlyActionsToDetailView` (nuevo):
  `GET /strategies/edit` y `GET /strategies/delete` → 3xx a `/strategies`.
  Antes del fix: 200 con la página de error de conversión (reproduce el bug).
- `GlobalExceptionHandlerTest` (3 nuevos): referer `…/strategies/edit` → `redirect:/strategies`;
  `…/rule-definitions/delete` → `redirect:/rule-definitions`; `…/strategies/new` se conserva.
- Ejecución: `mvn test` → `Tests run: 1022, Failures: 0, Errors: 0, Skipped: 0`.

## Advertencias y próximos pasos

- El mensaje críptico enmascaraba el error REAL de validación del guardado. Tras el fix,
  reintentar el guardado mostrará el mensaje verdadero (p. ej. periodo SMA no soportado,
  regla sin parámetro). Si reaparece, copiar ese mensaje para diagnosticar la causa primaria.
- Pendiente de confirmar con el usuario si el fallo era creando o editando, y con qué
  valores de objetivo/stop, por si hay además un problema de binding del formulario SMA.
