# Flash Messages — Inventario y Propuesta

**Fecha:** 2026-04-13  
**Rama:** improve/flash-messages  
**Propósito:** Documentar todos los mensajes flash que se deben añadir al código para informar al usuario del resultado de cada acción CRUD de la aplicación.

---

## Estado Actual

El sistema de mensajes flash ya está parcialmente implementado:

- `fragments/message.html` — fragmento Bootstrap Alert reutilizable (soporta tipos `success` y `danger`).
- `GlobalExceptionHandler.redirectWithError()` — añade flash de error (`messageType=danger`) en todos los casos de excepción redirigidos.
- `RuleDefinitionController.deleteRuleDefinition()` — único caso de éxito con flash implementado (aunque con texto incorrecto, ver §Correcciones).

Solo dos plantillas incluyen actualmente el fragmento de mensaje:
- `strategies/list.html`
- `rule-definitions/list.html`

---

## Mensajes Flash Propuestos por Controlador

### 1. `AnalyzeTickerController` (`/analysis`)

| Método | Endpoint | Acción del usuario | Tipo | Texto del mensaje |
|--------|----------|--------------------|------|-------------------|
| `getTickerData` | `POST /analysis/getTickerData` | Añadir ticker(s) para análisis | `success` | `"Ticker(s) añadidos y analizados correctamente."` |
| `updateTicker` | `POST /analysis/update` | Actualizar datos de un ticker desde la lista | `success` | `"Datos del ticker actualizados correctamente."` |
| `updateTickerFromDetail` | `POST /analysis/ticker/{id}/update` | Actualizar datos desde la vista de detalle | `success` | `"Datos del ticker actualizados correctamente."` |
| `deleteTicker` | `POST /analysis/delete` | Eliminar un ticker del análisis | `success` | `"Ticker '{ticker}' eliminado correctamente."` |
| `getValorationIA` | `POST /analysis/getValorationIA` | Solicitar valoración IA de un ticker | `success` | `"Valoración IA generada y guardada correctamente."` |

> **Plantillas que deben incluir el fragmento de mensaje:**
> - `analysis/analysis.html` — destino principal de los redirects de análisis.
> - `analysis/ticker-detail.html` — destino del redirect de `updateTickerFromDetail`.

---

### 2. `StrategyController` (`/strategies`)

| Método | Endpoint | Acción del usuario | Tipo | Texto del mensaje |
|--------|----------|--------------------|------|-------------------|
| `saveStrategy` (create, `id == null`) | `POST /strategies` | Crear nueva estrategia | `success` | `"Estrategia creada correctamente."` |
| `saveStrategy` (update, `id != null`) | `POST /strategies` | Guardar cambios de una estrategia existente | `success` | `"Estrategia actualizada correctamente."` |
| `deleteStrategy` | `POST /strategies/delete` | Eliminar una estrategia | `success` | `"Estrategia eliminada correctamente."` |

> **Nota:** El método `saveStrategy` ya recibe el `StrategyDTO`; si `strategyDTO.getId() != null` se trata de una actualización, en caso contrario es creación. Añadir `RedirectAttributes` al método para inyectar el flash adecuado.
>
> **Plantillas que deben incluir el fragmento de mensaje:**
> - `strategies/list.html` ✅ ya incluido.
> - `strategies/detail.html` — puede recibir flash si se redirige aquí tras editar.
> - `strategies/create.html` — puede mostrar errores de validación procedentes del `GlobalExceptionHandler`.

---

### 3. `RuleDefinitionController` (`/rule-definitions`)

| Método | Endpoint | Acción del usuario | Tipo | Texto del mensaje |
|--------|----------|--------------------|------|-------------------|
| `saveRuleDefinition` (create, `id == null`) | `POST /rule-definitions` | Crear nueva definición de regla | `success` | `"Definición de regla creada correctamente."` |
| `saveRuleDefinition` (update, `id != null`) | `POST /rule-definitions` | Guardar cambios de una definición de regla | `success` | `"Definición de regla actualizada correctamente."` |
| `deleteRuleDefinition` | `POST /rule-definitions/delete` | Eliminar una definición de regla | `success` | `"Definición de regla eliminada con éxito."` |

> **Plantillas que deben incluir el fragmento de mensaje:**
> - `rule-definitions/list.html` ✅ ya incluido.
> - `rule-definitions/create.html` — puede mostrar errores procedentes del `GlobalExceptionHandler`.

---

### 4. `ProhibitedTickerController` (`/prohibited-tickers`)

| Método | Endpoint | Acción del usuario | Tipo | Texto del mensaje |
|--------|----------|--------------------|------|-------------------|
| `deleteProhibitedTicker` | `POST /prohibited-tickers/delete` | Desbloquear / eliminar un ticker prohibido | `success` | `"Ticker '{ticker}' desbloqueado y eliminado correctamente."` |

> **Plantillas que deben incluir el fragmento de mensaje:**
> - `prohibited-tickers/list.html` — destino del redirect; actualmente **no incluye** el fragmento de mensaje.

---

## Correcciones Necesarias en Mensajes Existentes

| Ubicación | Texto actual (incorrecto) | Texto correcto propuesto |
|-----------|--------------------------|--------------------------|
| `RuleDefinitionController.deleteRuleDefinition()` | `"Estrategia eliminada con éxito."` | `"Definición de regla eliminada con éxito."` |

---

## Mensajes de Error ya Gestionados (GlobalExceptionHandler)

Los siguientes errores ya generan mensajes flash de tipo `danger` de forma centralizada y no requieren cambios en los controladores:

| Excepción | Mensaje mostrado |
|-----------|-----------------|
| `RuleDefinitionNotFoundException` | Mensaje original de la excepción |
| `StockDataNotFoundException` | Mensaje original de la excepción |
| `EntityInUseException` | `"No se puede eliminar el recurso porque tiene dependencias asociadas"` |
| `FinnhubException` | `"El servicio de datos de mercado no está disponible temporalmente"` |
| `PolygonException` | `"El servicio de datos de mercado no está disponible temporalmente"` |
| `AIServiceException` | `"El servicio de datos de mercado no está disponible temporalmente"` |
| `StockException` | `"El servicio de datos de mercado no está disponible temporalmente"` |

---

## Resumen de Plantillas — Inclusión del Fragmento `fragments/message`

| Plantilla | Incluye fragmento | Acción necesaria |
|-----------|:-----------------:|-----------------|
| `strategies/list.html` | ✅ | — |
| `strategies/detail.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `strategies/create.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `rule-definitions/list.html` | ✅ | — |
| `rule-definitions/create.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `analysis/analysis.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `analysis/ticker-detail.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `analysis/ticker-chart.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `prohibited-tickers/list.html` | ❌ | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |

---

## Plan de Implementación por Fases

---

### Fase 1 — Corrección del mensaje incorrecto existente

**Alcance:** Corregir el único mensaje de éxito ya implementado, que contiene un texto erróneo.

**Archivos a modificar:**

| Archivo | Cambio |
|---------|--------|
| `src/main/java/com/market/analysis/presentation/controller/RuleDefinitionController.java` | Cambiar `"Estrategia eliminada con éxito."` → `"Definición de regla eliminada con éxito."` |

**Tests a actualizar/añadir:**

- `RuleDefinitionControllerTest` — verificar que el flash attribute `message` contiene el texto correcto tras `DELETE /rule-definitions/delete`.

**Criterio de aceptación:** El texto mostrado al usuario al eliminar una definición de regla es correcto y coherente con la entidad afectada.

---

### Fase 2 — Añadir mensajes flash en `RuleDefinitionController`

**Alcance:** Completar los mensajes de éxito en las operaciones de creación y actualización de definiciones de regla.

**Archivos a modificar:**

| Archivo | Cambio |
|---------|--------|
| `RuleDefinitionController.java` | Añadir `RedirectAttributes` a `saveRuleDefinition()`; emitir `"Definición de regla creada correctamente."` o `"Definición de regla actualizada correctamente."` según `id == null` |
| `src/main/resources/templates/rule-definitions/create.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` tras el header de la página |

**Tests a añadir:**

- `RuleDefinitionControllerTest` — verificar flash `success` en `POST /rule-definitions` (crear).
- `RuleDefinitionControllerTest` — verificar flash `success` en `POST /rule-definitions` (actualizar, `id != null`).

**Criterio de aceptación:** Al crear o editar una definición de regla, aparece un banner verde de confirmación en la vista de lista.

---

### Fase 3 — Añadir mensajes flash en `StrategyController`

**Alcance:** Añadir mensajes de éxito para crear, actualizar y eliminar estrategias.

**Archivos a modificar:**

| Archivo | Cambio |
|---------|--------|
| `StrategyController.java` | Añadir `RedirectAttributes` a `saveStrategy()`; emitir `"Estrategia creada correctamente."` o `"Estrategia actualizada correctamente."` según `strategyDTO.getId() == null` |
| `StrategyController.java` | Añadir `RedirectAttributes` a `deleteStrategy()`; emitir `"Estrategia eliminada correctamente."` |
| `src/main/resources/templates/strategies/detail.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |
| `src/main/resources/templates/strategies/create.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` |

**Tests a añadir:**

- `StrategyControllerTest` — verificar flash `success` en `POST /strategies` (crear).
- `StrategyControllerTest` — verificar flash `success` en `POST /strategies` (actualizar).
- `StrategyControllerTest` — verificar flash `success` en `POST /strategies/delete`.

**Criterio de aceptación:** Al crear, editar o eliminar una estrategia, aparece un banner verde de confirmación en la vista de lista.

---

### Fase 4 — Añadir mensajes flash en `ProhibitedTickerController`

**Alcance:** Añadir mensaje de éxito al eliminar un ticker prohibido e incluir el fragmento de mensaje en la plantilla de lista.

**Archivos a modificar:**

| Archivo | Cambio |
|---------|--------|
| `ProhibitedTickerController.java` | Añadir `RedirectAttributes` a `deleteProhibitedTicker()`; emitir `"Ticker '{ticker}' desbloqueado y eliminado correctamente."` con el valor del parámetro `ticker` interpolado |
| `src/main/resources/templates/prohibited-tickers/list.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` tras el header de la página |

**Tests a añadir:**

- `ProhibitedTickerControllerTest` — verificar flash `success` en `POST /prohibited-tickers/delete`.

**Criterio de aceptación:** Al eliminar un ticker prohibido, aparece un banner verde con el nombre del ticker en la vista de lista.

---

### Fase 5 — Añadir mensajes flash en `AnalyzeTickerController`

**Alcance:** Añadir mensajes de éxito para todas las operaciones de análisis: añadir ticker, actualizar, eliminar y solicitar valoración IA.

**Archivos a modificar:**

| Archivo | Cambio |
|---------|--------|
| `AnalyzeTickerController.java` | Añadir `RedirectAttributes` a `getTickerData()`; emitir `"Ticker(s) añadidos y analizados correctamente."` |
| `AnalyzeTickerController.java` | Añadir `RedirectAttributes` a `updateTicker()`; emitir `"Datos del ticker actualizados correctamente."` |
| `AnalyzeTickerController.java` | Añadir `RedirectAttributes` a `updateTickerFromDetail()`; emitir `"Datos del ticker actualizados correctamente."` |
| `AnalyzeTickerController.java` | Añadir `RedirectAttributes` a `deleteTicker()`; emitir `"Ticker '{ticker}' eliminado correctamente."` con el valor del parámetro `ticker` interpolado |
| `AnalyzeTickerController.java` | Añadir `RedirectAttributes` a `getValorationIA()`; emitir `"Valoración IA generada y guardada correctamente."` |
| `src/main/resources/templates/analysis/analysis.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` tras el header de la página |
| `src/main/resources/templates/analysis/ticker-detail.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` tras el header de la página |
| `src/main/resources/templates/analysis/ticker-chart.html` | Añadir `<div th:replace="~{fragments/message :: message}"></div>` tras el header de la página |

**Tests a añadir:**

- `AnalyzeTickerControllerTest` — verificar flash `success` en `POST /analysis/getTickerData`.
- `AnalyzeTickerControllerTest` — verificar flash `success` en `POST /analysis/update`.
- `AnalyzeTickerControllerTest` — verificar flash `success` en `POST /analysis/ticker/{id}/update`.
- `AnalyzeTickerControllerTest` — verificar flash `success` en `POST /analysis/delete`.
- `AnalyzeTickerControllerTest` — verificar flash `success` en `POST /analysis/getValorationIA`.

**Criterio de aceptación:** Todas las operaciones sobre tickers muestran feedback inmediato al usuario en el banner de la vista de análisis o de detalle según corresponda.

---

### Resumen de Fases

| Fase | Descripción | Controlador(es) | Ficheros Java | Plantillas | Tests |
|------|-------------|-----------------|---------------|------------|-------|
| **1** | Corrección mensaje existente | `RuleDefinitionController` | 1 | 0 | 1 |
| **2** | Flash en operaciones de Definiciones de Regla | `RuleDefinitionController` | 1 | 1 | 2 |
| **3** | Flash en operaciones de Estrategias | `StrategyController` | 1 | 2 | 3 |
| **4** | Flash en operaciones de Tickers Prohibidos | `ProhibitedTickerController` | 1 | 1 | 1 |
| **5** | Flash en operaciones de Análisis de Tickers | `AnalyzeTickerController` | 1 | 3 | 5 |
