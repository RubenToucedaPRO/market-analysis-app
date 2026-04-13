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

## Próximos Pasos

1. Añadir `RedirectAttributes` como parámetro en los métodos de controlador que aún no lo tienen.
2. Llamar a `redirectAttributes.addFlashAttribute("message", "...")` y `redirectAttributes.addFlashAttribute("messageType", "success")` en cada acción exitosa según la tabla anterior.
3. Corregir el mensaje de `RuleDefinitionController.deleteRuleDefinition()`.
4. Incluir `<div th:replace="~{fragments/message :: message}"></div>` en todas las plantillas destino que faltan.
5. Añadir tests unitarios `MockMvc` para verificar la presencia del flash attribute correcto en cada redirect.
