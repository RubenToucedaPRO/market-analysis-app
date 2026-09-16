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
  Referers normales se conservan; ausente → sección de la petición (`sectionOf()`,
  mejora fusionada desde `main`); ilegible → `/`.
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
- `GlobalExceptionHandlerTest` (5 en total tras fusionar `main`): referer `…/strategies/edit`
  → `redirect:/strategies`; `…/rule-definitions/delete` → `redirect:/rule-definitions`;
  `…/strategies/new` se conserva; sin referer + `/analysis/ticker/999` → `redirect:/analysis`;
  sin referer + `/` → `redirect:/`.
- Ejecución: suite completa tras fusionar `main` → `Tests run: 1029, Failures: 0, Errors: 0, Skipped: 0`.

## Advertencias de SonarQube o arquitectura

- `th:text`, CSRF y capas intactos; métodos nuevos con complejidad trivial y anidamiento < 4.
- Las restricciones `{id:\\d+}` son solo enrutado (sin lógica de negocio en el controlador).
- Tras la fusión, el fichero conserva `sectionOf()` de `main` y los tests de ambas ramas
  (parametrizado de POST-only + 2 de sección); verificado en verde.

## Próximos pasos sugeridos

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Escribe a mano `/strategies/edit` en la barra de direcciones → NO ves página
  de error: vuelves a `/strategies`. (Antes: página de error con el detalle
  "For input string: edit".) Lo mismo con `/strategies/delete`.
  Resultado visible DISTINTO del de una URL inexistente cualquiera.
2. Provoca un error de validación al guardar con origen en una acción POST-only
  → vuelves a la sección padre (`/strategies`) conservando el mensaje flash
  de validación, nunca a una URL sin vista. (Caso cubierto también por los
  tests de `Referer` de arriba.)

### B. Ideas futuras (opcionales, NO hacer ahora)

- El mensaje críptico enmascaraba el error REAL de validación del guardado. Si al
  reintentar un guardado aparece un mensaje nuevo (p. ej. periodo SMA no soportado,
  regla sin parámetro), copiar ese mensaje para diagnosticar la causa primaria.
- No se re-renderiza el formulario con errores (cambio mayor, fuera de alcance);
  se mantiene el patrón PRG con flash ya existente, pero a una URL válida.
