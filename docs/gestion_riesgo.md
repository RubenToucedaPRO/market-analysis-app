# Análisis y Plan de Mejora — Gestión de Riesgo en Estrategias

**Fecha:** 2026-04-14  
**Área:** Gestión de Riesgo (Creación / Edición de Estrategias)  
**Estado:** En progreso

---

## 1. Resumen Ejecutivo

Este documento analiza el área de **Gestión de Riesgo** dentro de la creación y edición de estrategias en la aplicación. Se identifican errores, carencias y oportunidades de mejora, y se propone un plan de implementación en fases para abordarlas de forma incremental y segura.

---

## 2. Componentes Analizados

| Componente | Tipo | Ubicación |
|---|---|---|
| `StrategyObjective` | Value Object (Dominio) | `domain/model/StrategyObjective.java` |
| `ObjectiveType` | Enum (Dominio) | `domain/model/ObjectiveType.java` |
| `RiskRewardCalculator` | Servicio de dominio | `domain/service/RiskRewardCalculator.java` |
| `StrategyObjectiveDTO` | DTO (Aplicación) | `application/dto/StrategyObjectiveDTO.java` |
| `StrategyDTOMapper` | Mapper (Aplicación) | `application/mapper/StrategyDTOMapper.java` |
| `ManageStrategyService` | Caso de uso (Aplicación) | `application/usecase/ManageStrategyService.java` |
| `create.html` | Vista Thymeleaf | `templates/strategies/create.html` |
| `strategy-manager.js` | JavaScript frontend | `static/js/strategy-manager.js` |
| `StrategyObjectiveEntity` | Entidad JPA | `infrastructure/persistence/entity/StrategyObjectiveEntity.java` |
| `StrategyMapper` | Mapper JPA | `infrastructure/persistence/mapper/StrategyMapper.java` |

---

## 3. Errores Identificados

### 3.1 🔴 CRÍTICO — Validación SMA: valores no restringidos a períodos válidos

**Descripción:**  
Cuando el `targetType` o `stopLossType` es `SMA`, el valor asociado (`targetValue` / `stopLossValue`) debe ser uno de los períodos predefinidos: **20, 50 o 200**. Sin embargo, `StrategyObjective.validate()` solo verificaba que el valor fuera positivo, sin comprobar que fuera uno de los períodos válidos.

**Impacto:**  
- Se podían guardar estrategias con períodos SMA inválidos (ej., 10, 100, 500).
- El error se manifestaba **en tiempo de ejecución** al calcular métricas de riesgo en `RiskRewardCalculator.resolveSmaValue()`, que sí tiene la restricción a 20, 50 y 200.
- El error solo aparecía al evaluar la estrategia con datos de mercado, no al crearla/editarla, dificultando el diagnóstico.

**Estado:** ✅ Corregido en esta PR

**Corrección aplicada:**
- Se añadió `ALLOWED_SMA_PERIODS = Set.of(20, 50, 200)` y el método `validateSmaValue()` en `StrategyObjective`.
- El método `validate()` ahora comprueba que si el tipo es `SMA`, el valor sea 20, 50 o 200.
- Se añadieron 8 tests unitarios cubriendo los casos válidos e inválidos.

### 3.2 🟠 MEDIO — UI permite entrada libre de valores SMA

**Descripción:**  
El formulario de creación/edición de estrategia (`create.html`) usaba un `<input type="number">` para `targetValue` y `stopLossValue` independientemente del tipo seleccionado. Esto permitía al usuario introducir cualquier valor numérico cuando el tipo es SMA.

**Estado:** ✅ Corregido en esta PR

**Corrección aplicada:**
- Se añadió un `<select>` alternativo con opciones SMA 20, SMA 50, SMA 200 para los campos de valor objetivo y stop-loss.
- Se añadió la función `toggleObjectiveValueField()` en `strategy-manager.js` que alterna entre el dropdown SMA y el input libre según el tipo seleccionado.
- El campo activo tiene el atributo `name` correcto y `required`; el campo inactivo se deshabilita y se elimina su `name`.

---

## 4. Mejoras Propuestas

### 4.1 🟡 Validación cruzada target vs. stop-loss para tipo FIXED_PRICE

**Descripción:**  
Actualmente no se valida que el `targetValue` sea mayor que el `stopLossValue` cuando ambos son de tipo `FIXED_PRICE`. El `RiskRewardCalculator` sí valida que el stop-loss sea menor que el precio de entrada, pero la validación debería anticiparse en el modelo de dominio.

**Ubicación afectada:** `StrategyObjective.validate()`

**Ejemplo de error silencioso:**  
Un usuario puede crear una estrategia con `targetValue = 100` y `stopLossValue = 150`, ambos `FIXED_PRICE`. Esto solo falla al calcular en `RiskRewardCalculator`.

### 4.2 🟡 Validación de rango para tipo PERCENTAGE

**Descripción:**  
No hay límite superior para los porcentajes. Un valor de `targetValue = 500` (500%) o `stopLossValue = 99` (99%) es técnicamente aceptado pero financieramente irrazonable. Considerar:
- `targetValue` PERCENTAGE: rango razonable 0.1% – 100%
- `stopLossValue` PERCENTAGE: rango razonable 0.1% – 50%

**Ubicación afectada:** `StrategyObjective.validate()`

### 4.3 🟡 Inconsistencia semántica en `capitalToRisk`

**Descripción:**  
El JavaDoc describe `capitalToRisk` como "expressed as a decimal" (ej., 0.02 para 2%), pero el placeholder de la UI dice "ej., 1000.00", sugiriendo un monto absoluto. Además, `RiskRewardCalculator.calculatePositionSize()` lo trata como un monto absoluto de capital. 

**Corrección necesaria:** Actualizar el JavaDoc para reflejar que es un monto absoluto de capital, no un decimal.

**Ubicación afectada:** `StrategyObjective.java` (JavaDoc), `StrategyObjectiveDTO.java`

### 4.4 🟢 Feedback visual mejorado en el formulario

**Descripción:**  
- No hay indicadores visuales que expliquen el significado de cada campo según el tipo seleccionado.
- Se podría añadir texto de ayuda dinámico debajo de cada campo de valor que se actualice según el tipo (ej., "Período de la media móvil simple" para SMA, "Porcentaje de variación" para PERCENTAGE).

**Ubicación afectada:** `create.html`, `strategy-manager.js`

### 4.5 🟢 Validación del lado del servidor con mensajes i18n

**Descripción:**  
Los mensajes de error de validación en `StrategyObjective.validate()` están hardcodeados en inglés. Según las buenas prácticas del proyecto, deberían usar `messages.properties` para internacionalización.

**Ubicación afectada:** `StrategyObjective.java`, `messages.properties`

### 4.6 🟢 Persistencia de `ObjectiveType` como enum en JPA

**Descripción:**  
`StrategyObjectiveEntity` almacena `targetType` y `stopLossType` como `String`. Se podría usar `@Enumerated(EnumType.STRING)` directamente con el tipo `ObjectiveType` para mayor type-safety y evitar errores de conversión en los mappers.

**Ubicación afectada:** `StrategyObjectiveEntity.java`, `StrategyMapper.java`

### 4.7 🟢 Validación de formulario pre-submit con JavaScript

**Descripción:**  
Actualmente no hay validación JavaScript antes del envío del formulario. La validación solo ocurre en el servidor. Añadir validación client-side mejoraría la UX y reduciría llamadas innecesarias al servidor.

**Ubicación afectada:** `strategy-manager.js`

---

## 5. Plan de Implementación por Fases

### Fase 1 — Corrección de errores críticos ✅ (esta PR)

| Tarea | Estado |
|---|---|
| Validar períodos SMA (20, 50, 200) en `StrategyObjective.validate()` | ✅ Completado |
| Añadir tests unitarios para validación SMA | ✅ Completado |
| Actualizar UI con dropdown SMA para campos de valor | ✅ Completado |
| Añadir JavaScript para alternar entre dropdown y input libre | ✅ Completado |

### Fase 2 — Validaciones de dominio adicionales

| Tarea | Prioridad |
|---|---|
| Validación cruzada `targetValue > stopLossValue` para `FIXED_PRICE` | Media |
| Validación de rango para `PERCENTAGE` (0.1%–100% target, 0.1%–50% stop-loss) | Media |
| Corregir JavaDoc de `capitalToRisk` (monto absoluto, no decimal) | Media |

**Archivos afectados:**
- `StrategyObjective.java` — ampliar `validate()` con validaciones cruzadas
- `StrategyObjectiveDTO.java` — actualizar documentación
- `StrategyObjectiveTest.java` — nuevos tests para validaciones cruzadas

### Fase 3 — Mejoras de experiencia de usuario (UX)

| Tarea | Prioridad |
|---|---|
| Texto de ayuda dinámico según tipo seleccionado | Baja |
| Validación client-side pre-submit en JavaScript | Baja |
| Placeholders dinámicos en los campos de valor | Baja |

**Archivos afectados:**
- `create.html` — añadir elementos `<small>` con `th:text`
- `strategy-manager.js` — lógica de ayuda dinámica y validación pre-submit

### Fase 4 — Mejoras de arquitectura e infraestructura

| Tarea | Prioridad |
|---|---|
| Migrar `targetType`/`stopLossType` a `@Enumerated` en JPA | Baja |
| Internacionalización de mensajes de validación | Baja |
| Añadir validación con `@Valid` + Bean Validation en DTOs | Baja |

**Archivos afectados:**
- `StrategyObjectiveEntity.java` — usar `@Enumerated(EnumType.STRING)`
- `StrategyMapper.java` — simplificar conversiones
- `messages.properties` — añadir claves de validación
- `StrategyObjectiveDTO.java` — añadir anotaciones `@NotNull`, `@Positive`

---

## 6. Resumen de Cambios Realizados (Fase 1)

### `StrategyObjective.java`
- **Añadido:** `ALLOWED_SMA_PERIODS = Set.of(20, 50, 200)`
- **Añadido:** Método privado `validateSmaValue(ObjectiveType type, BigDecimal value, String fieldName)`
- **Modificado:** `validate()` ahora invoca `validateSmaValue()` para `targetValue` y `stopLossValue`

### `StrategyObjectiveTest.java`
- **Añadidos 8 tests:**
  - `shouldThrowExceptionWhenSmaTargetValueIsInvalid` — rechaza período SMA no válido en target
  - `shouldThrowExceptionWhenSmaStopLossValueIsInvalid` — rechaza período SMA no válido en stop-loss
  - `shouldAcceptSmaTargetValue20` — acepta SMA 20 como target
  - `shouldAcceptSmaTargetValue50` — acepta SMA 50 como target
  - `shouldAcceptSmaTargetValue200` — acepta SMA 200 como target
  - `shouldAcceptSmaStopLossValidValues` — acepta SMA 20, 50, 200 como stop-loss
  - `shouldRejectSmaTargetValue10` — rechaza SMA 10 como target

### `create.html`
- **Añadido:** `<select>` con opciones SMA 20/50/200 para `targetValue` y `stopLossValue`
- **Añadido:** `onchange="toggleObjectiveValueField(...)"` en los selects de tipo
- **Mantenido:** `<input type="number">` para tipos PERCENTAGE y FIXED_PRICE

### `strategy-manager.js`
- **Añadido:** Función `toggleObjectiveValueField(field)` que alterna la visibilidad entre dropdown SMA y input numérico libre
- **Modificado:** `DOMContentLoaded` inicializa la visibilidad de los campos de valor según el tipo seleccionado

---

## 7. Decisiones Técnicas

| Decisión | Justificación |
|---|---|
| Validación en Value Object (`StrategyObjective`) | Sigue el principio de "fail fast" en el dominio. La validación debe ocurrir lo antes posible, no solo en el servicio de cálculo. |
| `Set<Integer>` para períodos SMA | Consistente con `RiskRewardCalculator.resolveSmaValue()` que usa `switch(period)` con los mismos valores. |
| Toggle JS en vez de formularios separados | Mantiene una sola vista (`create.html`) sin duplicar lógica de formulario. |
| Deshabilitar campo inactivo con `disabled` + eliminar `name` | Evita enviar valores duplicados o erróneos al servidor. El campo deshabilitado no se envía en el POST del formulario. |
