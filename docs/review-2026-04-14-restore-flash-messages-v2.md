# Revisión de PR: restore-flash-messages-v2

**Fecha:** 2026-04-14  
**Rama:** `restore-flash-messages-v2`  
**Alcance revisado:** commits `ddfd6a1..d8b4cd9` (12 commits)

---

## Resumen general

La PR implementa el sistema completo de flash messages (fases 1–5), añade la cobertura del servicio `ManageAnalyzeStockService` y mejora la UX con alertas auto-cerrables con barra de progreso. La dirección general es correcta y el código está bien estructurado, pero se han identificado varios problemas que deben corregirse.

---

## 🔴 Bugs críticos

### B1 — `StrategyController.saveStrategy()`: la rama de edición llama a `createStrategy` en lugar de `updateStrategy`

**Archivo:** `src/main/java/.../controller/StrategyController.java`

```java
// Código actual (incorrecto)
} else {
    manageStrategyUseCase.createStrategy(strategyDTO);  // ← SIEMPRE crea, nunca actualiza
    redirectAttributes.addFlashAttribute(..., UiNotification.success("Estrategia actualizada correctamente."));
}
```

La interfaz `ManageStrategyUseCase` no expone un método `updateStrategy()`. En la rama `else` (cuando `strategyDTO.getId() != null`), se llama a `createStrategy` igual que en la creación, lo que puede duplicar estrategias en lugar de actualizar la existente.

**Corrección propuesta:**
1. Añadir `updateStrategy(StrategyDTO strategy)` a `ManageStrategyUseCase` (y su implementación en `ManageStrategyService`).
2. Llamar a `updateStrategy` en la rama `else` del controlador.
3. Actualizar `testSaveStrategyUpdate()` en `StrategyControllerTest` para que verifique `updateStrategy`, no `createStrategy`.

---

### B2 — Fragmento `notification.html` incluido dentro de `<head>` en 9 plantillas

**Archivos:** `analysis/analysis.html`, `analysis/ticker-chart.html`, `analysis/ticker-detail.html`, `prohibited-tickers/list.html`, `rule-definitions/create.html`, `rule-definitions/list.html`, `strategies/create.html`, `strategies/detail.html`, `strategies/list.html`

El `th:replace` se colocó justo antes de `</head>`, dentro del bloque `<head>`. El fragmento renderiza un `<div>` (elemento de bloque), que es **HTML inválido dentro de `<head>`**. Los navegadores corregirán la estructura automáticamente pero el comportamiento puede ser impredecible y SonarQube lo marcará.

```html
<!-- Incorrecto: notification dentro de <head> -->
<link th:href="@{/css/styles.css}" rel="stylesheet" />
<!-- Message Alert -->
<div th:replace="~{fragments/notification :: notification}"></div>
</head>
<body ...>
```

**Corrección:** Mover todos los `th:replace` al inicio del `<body>`, justo después del `<div th:replace="~{fragments/navbar :: navbar}">`:

```html
</head>
<body class="d-flex flex-column min-vh-100">
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <!-- Message Alert -->
  <div th:replace="~{fragments/notification :: notification}"></div>
  <main ...>
```

---

### B3 — `strategies/create.html` incluye el fragmento de notificación dos veces

**Archivo:** `src/main/resources/templates/strategies/create.html`

El fragmento se incluyó en la línea 25 (dentro de `<head>`, bug B2) y también en la línea 34 (dentro de `<main>`). Cuando haya una notificación, se renderizará dos veces.

```html
<!-- Línea 25: dentro de <head> (incorrecto) -->
<div th:replace="~{fragments/notification :: notification}"></div>
</head>
<body ...>
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <main ...>
    <!-- Línea 34: dentro de <main> (correcto, pero duplicado) -->
    <div th:replace="~{fragments/notification :: notification}"></div>
```

**Corrección:** Eliminar la inclusión de `<head>` y conservar solo la de `<main>`, o moverla a `<body>` justo tras el navbar (consistente con las demás plantillas).

---

### B4 — `th:class` en `notification.html` sobreescribe las clases de utilidad del icono

**Archivo:** `src/main/resources/templates/fragments/notification.html`

```html
<!-- Incorrecto: th:class reemplaza completamente class="bi fs-5 me-3" -->
<i
  th:class="${uiNotification.type == 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'}"
  class="bi fs-5 me-3"
></i>
```

En Thymeleaf, `th:class` **reemplaza** el atributo `class`. El elemento `<i>` resultante solo tendrá `bi-check-circle-fill` o `bi-exclamation-triangle-fill`, perdiendo `bi`, `fs-5` y `me-3`. El icono no se renderizará visualmente (Bootstrap Icons requiere la clase `bi`).

**Corrección:**

```html
<i
  class="bi fs-5 me-3"
  th:classappend="${uiNotification.type == 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'}"
></i>
```

O bien con literal de plantilla:

```html
<i
  th:class="|bi fs-5 me-3 ${uiNotification.type == 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'}|"
></i>
```

---

## 🟠 Problemas de arquitectura y diseño

### A1 — `<script>` incrustado en un fragmento condicional

**Archivo:** `src/main/resources/templates/fragments/notification.html`

```html
<div th:fragment="notification" th:if="${uiNotification}" ...>
  ...
  <script th:src="@{/js/notification-alert.js}"></script>  <!-- ← acoplado a th:if -->
</div>
```

El script solo se carga si hay una notificación activa. Si el script no se carga (sin notificación), la función `DOMContentLoaded` no se registra, lo cual es el comportamiento deseado, pero el acoplamiento entre lógica JS y presentación condicional es frágil. Si en el futuro se añaden notificaciones dinámicas (vía AJAX), el script no estaría disponible.

**Mejora propuesta:** Cargar `notification-alert.js` siempre desde el layout base (o añadirlo al `<body>` de cada plantilla junto al resto de scripts), independientemente de si hay notificación activa.

---

### A2 — `ManageStrategyUseCase` no tiene método `updateStrategy`

**Archivo:** `src/main/java/.../domain/port/in/ManageStrategyUseCase.java`

La interfaz solo expone `createStrategy`. Para respetar el **SRP** y la **Clean Architecture**, la actualización debería ser un caso de uso diferenciado (`updateStrategy`). Actualmente el servicio `ManageStrategyService.createStrategy()` parece actuar como upsert, pero la intención no está declarada en la interfaz.

**Mejora:** Añadir `StrategyDTO updateStrategy(StrategyDTO strategy)` a la interfaz y separar la lógica de creación de la de actualización en el servicio.

---

### A3 — Constante `ATTR_ERROR_MESSAGE` en `GlobalExceptionHandler` es un residuo parcialmente migrado

**Archivo:** `src/main/java/.../exception/GlobalExceptionHandler.java`

```java
private static final String ATTR_ERROR_MESSAGE = "errorMessage";
```

Esta constante ya no se usa para flash redirects (ahora se usa `UiNotification`), pero sigue siendo necesaria para las rutas de `error.html` (`PersistenceException` y `Exception` genérico). El nombre `ATTR_ERROR_MESSAGE` ahora es ambiguo: ¿es el atributo del modelo para `error.html` o para flash? 

**Mejora:** Renombrar a `ATTR_ERROR_PAGE_MESSAGE` (o similar) para clarificar que solo se usa en la vista de error, no en redirects. También sería coherente moverla a `WebConstants`.

---

## 🟡 Calidad de código

### C1 — Lógica de pausa/reanudación incorrecta en `notification-alert.js`

**Archivo:** `src/main/resources/static/js/notification-alert.js`

```javascript
const stopTimer = () => {
  clearTimeout(autoCloseTimeout);
  if (progressBar) {
    progressBar.style.transition = "none";
    // BUG: getBoundingClientRect().width devuelve píxeles absolutos
    const currentWidth = progressBar.getBoundingClientRect().width;
    progressBar.style.width = `${currentWidth}px`;  // ← mezcla unidades: px vs %
  }
};

const startTimer = () => {
  autoCloseTimeout = setTimeout(closeAlert, duration);  // ← siempre usa `duration` completo
  if (progressBar) {
    progressBar.style.transition = `width ${duration}ms linear`;  // ← siempre 3000ms
    progressBar.style.width = "0%";
  }
};
```

Dos problemas:
1. `stopTimer` guarda el ancho en píxeles absolutos pero `startTimer` trabaja con porcentajes. Cuando se reanuda, la transición no puede calcular correctamente el progreso restante.
2. `startTimer` siempre usa `duration` (3000ms) en la transición y fija el destino en `"0%"`, independientemente de cuánto tiempo haya transcurrido ya. El resultado es que después de una pausa, el temporizador **reinicia completamente** en lugar de reanudar desde donde se quedó.

**Corrección propuesta:** Registrar el tiempo de inicio y calcular el tiempo restante al reanudar:

```javascript
let startTime;
let remainingTime = duration;

const startTimer = () => {
  startTime = Date.now();
  autoCloseTimeout = setTimeout(closeAlert, remainingTime);
  if (progressBar) {
    progressBar.style.transition = `width ${remainingTime}ms linear`;
    progressBar.style.width = "0%";
  }
};

const stopTimer = () => {
  remainingTime -= Date.now() - startTime;
  clearTimeout(autoCloseTimeout);
  if (progressBar) {
    progressBar.style.transition = "none";
    const pct = (remainingTime / duration) * 100;
    progressBar.style.width = `${pct}%`;
  }
};
```

---

### C2 — Estilos inline en `notification.html` (violación SonarQube)

**Archivo:** `src/main/resources/templates/fragments/notification.html`

```html
<div ... style="z-index: 1060; min-width: 350px">
<div ... style="height: 3px; background-color: rgba(0, 0, 0, 0.1); overflow: hidden;">
```

SonarQube marca los estilos inline como code smell. Deberían moverse a `styles.css` como clases reutilizables:

```css
.notification-container {
  z-index: 1060;
  min-width: 350px;
}
.notification-progress-track {
  height: 3px;
  background-color: rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
```

---

### C3 — `@MockitoSettings(strictness = Strictness.LENIENT)` en `ManageAnalyzeStockServiceTest`

**Archivo:** `src/test/java/.../usecase/ManageAnalyzeStockServiceTest.java`

Las directrices del proyecto indican que no se debe usar `LENIENT` salvo que sea estrictamente necesario. El motivo en este caso es que `@BeforeEach` configura stubs genéricos que no todos los tests consumen.

**Mejora:** Refactorizar el `@BeforeEach` para que solo configure los mocks invariantes (p.ej. el `StrategyEvaluation`), y mover los stubs específicos de `historicalProviderPort`, `stockHistoricalService`, etc. a los tests individuales que los necesiten. Esto permitiría usar la strictness por defecto (`STRICT_STUBS`) y detectar stubs innecesarios automáticamente.

---

## 🔵 Calidad de tests

### T1 — `StrategyControllerTest.testSaveStrategyUpdate()` valida el comportamiento incorrecto

**Archivo:** `src/test/java/.../controller/StrategyControllerTest.java`

```java
// El test pasa, pero el comportamiento que verifica es un bug (B1):
verify(manageStrategyUseCase, times(1)).createStrategy(any(StrategyDTO.class));
```

Este test deberá actualizarse tras corregir B1 para verificar `updateStrategy`.

---

### T2 — `StrategyControllerTest.testShowEditForm()` no verifica el nuevo atributo `isEdit`

**Archivo:** `src/test/java/.../controller/StrategyControllerTest.java`

El método `showEditForm` añade `isEdit=true` al modelo (añadido en este PR), pero el test no lo verifica:

```java
void testShowEditForm() {
    // ...falta:
    verify(model).addAttribute("isEdit", true);
}
```

---

### T3 — `StrategyControllerTest.testShowCreateForm()` usa una aserción genérica tras el cambio de 2 a 3 atributos

**Archivo:** `src/test/java/.../controller/StrategyControllerTest.java`

```java
verify(model, times(3)).addAttribute(any(String.class), any());
```

La aserción `times(3)` verifica el número de llamadas pero no qué atributos se añaden. Debería ser más específica:

```java
verify(model).addAttribute("isEdit", false);
verify(model).addAttribute(eq(ATTR_STRATEGY), any());
verify(model).addAttribute(eq(ATTR_RULE_DEFINITIONS), anyList());
```

---

### T4 — No existe test para la restricción de CSRF en operaciones POST de los controladores

Los controladores `ProhibitedTickerController`, `RuleDefinitionController` y `StrategyController` tienen operaciones `@PostMapping` pero las pruebas con `MockMvc` no verifican la protección CSRF (al no enviar token CSRF las peticiones deberían ser rechazadas con 403 en producción). Sería conveniente añadir al menos un test negativo que verifique el rechazo sin token CSRF.

---

## 📄 Documentación

### D1 — `flashMessage.md` no refleja la implementación actual

**Archivo:** `docs/flashMessage.md`

El documento fue redactado antes de la implementación y todavía describe el sistema antiguo basado en `fragments/message :: message` y el atributo `errorMessage`. La implementación final usa:
- `fragments/notification :: notification` (nuevo fragmento)
- `UiNotification` record (en lugar de `String errorMessage`)
- `WebConstants.UI_NOTIFICATION_KEY = "uiNotification"`

El documento debería actualizarse para:
1. Reflejar el nuevo fragmento `notification.html` y su comportamiento (auto-cierre, barra de progreso).
2. Documentar la clase `UiNotification` y `WebConstants`.
3. Marcar todas las fases como ✅ completadas.
4. Añadir nota sobre el script `notification-alert.js`.

---

## Resumen de prioridades

| ID | Severidad | Área | Descripción breve |
|----|-----------|------|-------------------|
| B1 | 🔴 Crítico | Lógica | `saveStrategy` siempre llama a `createStrategy` (no actualiza) |
| B2 | 🔴 Crítico | HTML | `notification.html` dentro de `<head>` en 9 plantillas |
| B3 | 🔴 Crítico | HTML | Doble inclusión del fragmento en `strategies/create.html` |
| B4 | 🔴 Crítico | HTML | `th:class` sobreescribe clases de utilidad del icono Bootstrap |
| A1 | 🟠 Alto | Arquitectura | `<script>` acoplado a fragmento condicional |
| A2 | 🟠 Alto | Arquitectura | Falta `updateStrategy` en `ManageStrategyUseCase` |
| A3 | 🟡 Medio | Código | Constante `ATTR_ERROR_MESSAGE` ambigua tras migración |
| C1 | 🟡 Medio | JS | Lógica de pausa/reanudación de progreso incorrecta |
| C2 | 🟡 Medio | HTML | Estilos inline en `notification.html` |
| C3 | 🟡 Medio | Tests | `LENIENT` en `ManageAnalyzeStockServiceTest` sin justificación |
| T1 | 🟡 Medio | Tests | `testSaveStrategyUpdate` valida comportamiento incorrecto (B1) |
| T2 | 🔵 Bajo | Tests | `testShowEditForm` no verifica `isEdit=true` |
| T3 | 🔵 Bajo | Tests | Aserción genérica de `times(3)` en `testShowCreateForm` |
| T4 | 🔵 Bajo | Tests | Sin test de rechazo CSRF para operaciones POST |
| D1 | 🔵 Bajo | Docs | `flashMessage.md` desactualizado |
