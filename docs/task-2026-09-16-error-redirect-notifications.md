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
- Ramas nuevas `safeReferer` (referer nulo/vacío, POST-only, ilegible) y `sectionOf`
  (URI nulo/vacío, multi-segmento, raíz) cubiertas por los tests añadidos; sin Mockito `lenient`.

## Advertencias de SonarQube o arquitectura

- Sin `th:utext` (se usa `th:replace` del fragmento + `th:text` interno); CSRF y `@ControllerAdvice` sin cambios.
- Sin números mágicos: redirecciones vía `WebConstants.DEFAULT_REFERER` y claves vía `WebConstants`.
- `GlobalExceptionHandler` sigue en capa Presentation (no toca dominio); helpers privados con complejidad trivial y anidamiento < 4.
- Logging SLF4J (`warn` dominio, `error` infra, `debug` referer ilegible); sin `System.out`.

## Próximos pasos sugeridos

Son dos cosas distintas, las separo para que no se mezclen:

### A. Lo que tienes que probar tú ahora en el navegador (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Abre `/`: la home carga normal, sin ningún aviso.
2. En una pestaña nueva pega `/analysis/ticker/999999`: debes volver a `/analysis` viendo un aviso de error (antes caías a `/` sin aviso).
3. Entra en `/strategies` y desde ahí navega a `/analysis/ticker/999999` SIN
  tocar la barra de direcciones (ver nota): debes volver a `/strategies`
  (tu página de origen) con el aviso. El navegador envía solo la página de
  origen en la cabecera `Referer`, y la app te devuelve a ella.
  NOTA: escribir la URL a mano en la barra de direcciones NO envía origen
  (el navegador lo trata como "dirección tecleada" y no dice de dónde vienes,
  así que caes en `/analysis` como en el paso 2). Para hacerlo bien: en
  `/strategies`, abre DevTools (F12) → pestaña Console → pega
  `location.href='/analysis/ticker/999999'` → Enter. Si la consola dice
  "Don't paste code..." y no te deja pegar: escribe a mano `allow pasting`,
  pulsa Enter, y ya te deja pegar. (Ese aviso es una protección de Chrome
  contra código desconocido; esta línea es nuestra, solo ordena navegar a
  esa dirección, no hace nada más.)

### B. Ideas de futuro (opcionales, NO hacer ahora)

- Unificar los avisos de `login.html` y `error.html` con el mismo fragmento (se dejaron fuera a propósito).
- Docker ya verificado para esta tarea (contenedores Up, `GET /` → 200).
