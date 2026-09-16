# Avisos visibles y redirect con contexto de sección

- Fecha: 2026-09-16
- Slug: `error-redirect-notifications`
- Alcance: `home.html`, `GlobalExceptionHandler` + tests (sin cambios de comportamiento feliz)

## Resumen

Un ticker inexistente (`/analysis/ticker/999` sin `Referer`) redirigía a `/`, y allí
no se veía ningún aviso: `home.html` no incluía el fragmento de notificaciones
(las 9 vistas de app sí; `home`, `login` y `error` no). Además, `/` perdía el contexto
de sección. Ahora el flash se muestra y el redirect conserva la sección.

## Código generado

### 1. `templates/home.html`

- Añadida la línea del fragmento tras el navbar (igual que el resto de vistas).

### 2. `presentation/exception/GlobalExceptionHandler.java`

- Nuevo `safeReferer(req)`: conserva el `Referer` normal; sanea destinos POST-only
  (`…/edit`, `…/delete`) a su sección padre; sin `Referer`, deriva la sección del
  propio `requestURI` vía `sectionOf()` (`/analysis/ticker/999` → `/analysis`,
  `/` → `/`); ilegible → `/`.
- `redirectWithError()` lo usa; el flash se preserva en todos los casos.

## Decisiones técnicas

- `login.html` (mensajes propios por parámetro) y `error.html` (modelo directo, nunca
  recibe flash por redirect) quedan fuera a propósito.
- Sin cambios de dominio ni de flujos; helper privado de complejidad trivial.

## Cobertura de tests

- `HomeControllerTest.shouldRenderFlashNotification` (nuevo): `GET /` con flash
  `uiNotification` pinta el texto (fija el fragmento).
- `GlobalExceptionHandlerTest` (+2): sin referer + `/analysis/ticker/999` →
  `redirect:/analysis`; sin referer + `/` → `redirect:/`.
- Ejecución: `mvn test` → `Tests run: 1021, Failures: 0, Errors: 0, BUILD SUCCESS`.
