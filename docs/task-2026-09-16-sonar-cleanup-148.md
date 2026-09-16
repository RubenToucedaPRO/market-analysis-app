# Limpieza SonarQube + legibilidad en PR #148

- Fecha: 2026-09-16
- Slug: `sonar-cleanup-148`
- Alcance: templates de gestión de riesgo/detalle, `risk-management.js`,
  `GlobalExceptionHandlerTest` (sin cambios de comportamiento)

## Avisos tratados (captura SonarQube)

1. `Web:S1135` en `risk-management-fields.html` (Ln 2): falso positivo — el detector
   de TODO hacía match insensible a mayúsculas sobre "con **todo**s los campos".
   Reformulado a "con los campos". Verificado que no quedan más `todo` en los
   ficheros tocados.
2. `Web:S6819` (x2, Ln 65 y 148): `role="img"` en `<i>` de Bootstrap Icons.
   Sustituido por `aria-hidden="true"` (icono decorativo; el texto de la etiqueta
   y el `title` del tooltip aportan el significado).
3. `Web:InputWithoutLabelCheck` (x2, Ln 74 y 157): los `<select>` SMA no tenían
   etiqueta asociada (el `<label for>` apunta al `<input>` alterno). Añadido
   `aria-labelledby` hacia el `span` de la etiqueta en ambos selects.
4. `java:S5976` en `GlobalExceptionHandlerTest.java` (Ln 164): los 3 tests de
   Referer (edit/delete/new) unificados en un `@ParameterizedTest` con `@CsvSource`.
   Misma cobertura, sin `lenient`.
5. `javascript:S6582` en `risk-management.js` (Ln 38 y 71): `a && a.b` →
   `selectEl?.dataset ?? {}` y `selectEl.dataset?.defaultOption || ...`.
   Sintaxis verificada con `node --check`.

## Legibilidad (petición de revisión)

- Nuevo `templates/fragments/sma-value.html` (`smaValue(type, value)`): pinta el
  número con 2 decimales o `SMA 50 (Medio plazo)`.
- `strategies/detail.html`: los dos bloques duplicados de target/stop (~60 líneas
  con `th:with`+`th:if`+`th:text` mezclados) quedan en dos llamadas al fragmento.
  Mismo render, verificado por los tests de vista (`SMA 50`/`SMA 20`).

## Verificación

- `mvn test` → `Tests run: 1029, Failures: 0, Errors: 0, BUILD SUCCESS` (verificado
  tras fusionar `main`; incluye los 2 tests de sección de `main` y el parametrizado POST-only).
- No se añaden los micro-tests de cobertura de `safeReferer` (queda fuera por decisión).
