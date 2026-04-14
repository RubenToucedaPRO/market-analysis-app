# Análisis y Plan de Mejora — Gestión de Riesgo en Estrategias

**Fecha:** 2026-04-15  
**Área:** Creación/Edición de Estrategia → Sección "Gestión de Riesgo"  
**Archivos principales analizados:**

| Capa | Archivo | Responsabilidad |
|------|---------|----------------|
| Dominio | `StrategyObjective.java` | Value Object con validación de campos |
| Dominio | `ObjectiveType.java` | Enum: `SMA`, `PERCENTAGE`, `FIXED_PRICE` |
| Dominio | `Strategy.java` | Entidad raíz, invoca `objective.validate()` |
| Dominio | `RiskRewardCalculator.java` | Cálculo determinista de target/stop-loss/R:R/posición |
| Aplicación | `ManageStrategyService.java` | Caso de uso: crear/actualizar estrategia |
| Aplicación | `StrategyDTOMapper.java` | Conversión DTO ↔ dominio |
| Aplicación | `StrategyObjectiveDTO.java` | DTO de transporte de datos |
| Presentación | `StrategyController.java` | Controlador de formulario |
| Presentación | `create.html` | Formulario Thymeleaf de creación/edición |
| Presentación | `strategy-manager.js` | JS dinámico para reglas (no gestión de riesgo) |
| Test | `StrategyObjectiveTest.java` | Tests de validación del Value Object |
| Test | `RiskRewardCalculatorTest.java` | Tests del calculador de riesgo |

---

## 1. Estado Actual — Resumen

La gestión de riesgo se configura a través de un `StrategyObjective` (Value Object) que contiene:

- **Tipo de Objetivo (`targetType`)**: método de cálculo del target (`SMA`, `PERCENTAGE`, `FIXED_PRICE`)
- **Valor Objetivo (`targetValue`)**: valor numérico cuyo significado depende del tipo
- **Tipo de Stop Loss (`stopLossType`)**: método de cálculo del stop-loss
- **Valor Stop Loss (`stopLossValue`)**: valor numérico del stop-loss
- **Capital a Arriesgar (`capitalToRisk`)**: capital disponible para el cálculo de tamaño de posición
- **Descripción**: texto libre describiendo el objetivo

El cálculo de métricas de riesgo (precio target, precio stop-loss, ratio R:R, tamaño de posición recomendado) se realiza en `RiskRewardCalculator` durante la **evaluación** de la estrategia en `EvaluateStrategyService`, no durante la creación.

---

## 2. Problemas y Mejoras Detectados

### 🔴 CRÍTICO — C1: Dropdown de Tipo de Objetivo vacío

**Archivo:** `create.html` (líneas 120-125)

```html
<select class="form-select" id="objectiveTargetType" name="objective.targetType" required></select>
```

**Problema:** El `<select>` de Tipo de Objetivo no tiene opciones (`<option>`). Al estar marcado como `required`, el formulario nunca puede enviarse con un valor válido para este campo. En edición, el valor guardado no se restaura.

**Contraste:** El dropdown de Tipo de Stop Loss (líneas 155-185) sí tiene opciones hardcodeadas con lógica Thymeleaf `th:selected`.

**Impacto:** Bloquea completamente la creación y edición de estrategias.

**Corrección:** Poblar el `<select>` con las opciones del enum `ObjectiveType` incluyendo lógica `th:selected` para el modo edición, de forma idéntica al campo Stop Loss.

---

### 🔴 CRÍTICO — C2: Validación de periodos SMA ausente en dominio

**Archivo:** `StrategyObjective.java` (método `validate()`)

**Problema:** Cuando `targetType` o `stopLossType` es `SMA`, el `targetValue`/`stopLossValue` representa un **periodo de media móvil**. Los únicos periodos soportados son **20, 50 y 200** (definidos en `RiskRewardCalculator.resolveSmaValue()`). Sin embargo, `StrategyObjective.validate()` solo verifica que el valor sea `> 0`, permitiendo guardar periodos no soportados como 10, 100 o 300.

**Impacto:** La estrategia se guarda correctamente pero falla en tiempo de evaluación con `IllegalArgumentException` al intentar resolver el valor SMA. El usuario no recibe feedback hasta ese momento.

**Corrección:** Añadir validación de periodos SMA permitidos (20, 50, 200) en `StrategyObjective.validate()` cuando el tipo es `SMA`. Usar constante `ALLOWED_SMA_PERIODS` para mantener Single Source of Truth.

---

### 🟠 ALTO — A1: Adaptación dinámica del campo "Valor" según tipo seleccionado

**Problema:** El campo de valor objetivo y stop-loss es siempre un `<input type="number">` libre. Cuando el tipo es `SMA`, el usuario debería seleccionar de una lista cerrada (20, 50, 200), no introducir un número libre. Cuando es `PERCENTAGE`, el `step` debería ser `0.01` y el placeholder indicar "%" . Cuando es `FIXED_PRICE`, el placeholder debería indicar "$".

**Impacto:** Experiencia de usuario pobre; el usuario no sabe qué valores son válidos para cada tipo, y puede introducir valores inválidos que solo fallarán en la evaluación.

**Corrección:** Implementar lógica JavaScript `onchange` en los selectores de tipo que alterne entre `<select>` (con opciones 20, 50, 200 para SMA) e `<input>` (libre para PERCENTAGE/FIXED_PRICE). Patrón ya existente en `strategy-manager.js` con `toggleParameter()`.

---

### 🟠 ALTO — A2: Manejo de errores en conversión de enum en Mapper

**Archivo:** `StrategyDTOMapper.java` (líneas 82-84)

```java
.targetType(dto.getTargetType() != null ? ObjectiveType.valueOf(dto.getTargetType()) : null)
```

**Problema:** Si `dto.getTargetType()` contiene un string inválido (ej: "INVALID"), `ObjectiveType.valueOf()` lanza `IllegalArgumentException` no controlada. Se propaga como error 500 genérico sin mensaje útil para el usuario.

**Impacto:** Error no informativo para el usuario.

**Corrección:** Envolver en `try-catch` y lanzar excepción de dominio controlada que el `GlobalExceptionHandler` pueda mapear a un flash message.

---

### 🟡 MEDIO — M1: Ausencia de validación cruzada entre target y stop-loss

**Problema:** No existe validación que verifique coherencia entre `targetValue` y `stopLossValue` cuando ambos son `FIXED_PRICE`. Es posible crear una estrategia con target = $100 y stop-loss = $110, lo cual es ilógico para posiciones long.

**Nota:** Esta validación es compleja cuando los tipos son diferentes (ej: target SMA, stop-loss PERCENTAGE), ya que no se puede resolver sin datos de mercado. Solo tiene sentido cuando ambos son `FIXED_PRICE`.

**Corrección (parcial):** Validar en `StrategyObjective.validate()` que cuando ambos tipos son `FIXED_PRICE`, `targetValue > stopLossValue`.

---

### 🟡 MEDIO — M2: Campo `capitalToRisk` ambiguo

**Problema:** La documentación del campo indica "expresado como decimal" (ej: 0.02 = 2%), pero el placeholder del formulario dice "ej., 1000.00", sugiriendo un monto absoluto. `RiskRewardCalculator.calculatePositionSize()` lo usa como monto absoluto en la fórmula `capitalToRisk / riskPerShare`.

**Impacto:** Confusión para el usuario sobre qué valor introducir.

**Corrección:** 
- Actualizar la etiqueta y placeholder del formulario para aclarar que es un monto absoluto (ej: "Capital a arriesgar ($)" / "ej., 500.00").
- Actualizar la documentación en `StrategyObjective.java` para reflejar que es un monto absoluto.

---

### 🟡 MEDIO — M3: Falta información de riesgo en vista de detalle

**Archivo:** `detail.html`

**Problema:** La vista de detalle de una estrategia no muestra la configuración de gestión de riesgo (`targetType`, `targetValue`, `stopLossType`, `stopLossValue`, `capitalToRisk`, `description`). El usuario solo puede ver estos datos editando la estrategia.

**Corrección:** Añadir sección "Gestión de Riesgo" en `detail.html` mostrando los 6 campos del objetivo de forma legible.

---

### 🟡 MEDIO — M4: Ausencia de tests para validación SMA en `StrategyObjective`

**Archivo:** `StrategyObjectiveTest.java`

**Problema:** Existen tests para campos null, cero, negativos y tipos mixtos, pero no hay tests para periodos SMA inválidos (ej: 100, 10). Tampoco tests que verifiquen que los periodos 20, 50 y 200 sí son válidos.

**Corrección:** Añadir tests para validar periodos SMA permitidos y rechazados tras implementar C2.

---

### 🔵 BAJO — B1: Límites de rango en inputs numéricos del formulario

**Problema:** Los inputs de `targetValue`, `stopLossValue` y `capitalToRisk` no tienen `max` definido. Valores extremos (ej: 999999999) podrían causar problemas en cálculos o desbordamientos.

**Corrección:** Añadir validaciones de rango razonables en el formulario HTML y en `StrategyObjective.validate()`.

---

### 🔵 BAJO — B2: Estrategia creada con objetivo vacío en controller

**Archivo:** `StrategyController.java` (línea 61)

```java
.objective(StrategyObjectiveDTO.builder().build())
```

**Problema:** Al crear una nueva estrategia, se inicializa un `StrategyObjectiveDTO` completamente vacío (todos null). Esto es correcto para el formulario, pero si el formulario se envía sin rellenar los campos de riesgo, la validación de dominio fallará con mensajes genéricos.

**Corrección:** Ya controlado por `required` en HTML. Es un problema menor.

---

## 3. Plan de Implementación por Fases

### Fase 0 — Correcciones Críticas Bloqueantes

**Prioridad:** Inmediata  
**Impacto:** Sin estas correcciones, la creación/edición de estrategias no funciona correctamente.

| ID | Tarea | Archivo(s) | Tipo |
|----|-------|------------|------|
| C1 | Poblar `<select>` de Tipo de Objetivo con opciones y lógica `th:selected` | `create.html` | Fix HTML |
| C2 | Validar periodos SMA (20, 50, 200) en `StrategyObjective.validate()` | `StrategyObjective.java` | Fix dominio |
| M4 | Tests unitarios para validación SMA (periodos válidos e inválidos) | `StrategyObjectiveTest.java` | Test |

**Detalle C1 — Corrección del dropdown de Tipo de Objetivo:**

```html
<select class="form-select" id="objectiveTargetType" name="objective.targetType" required>
  <option value=""
    th:selected="${strategy.objective == null or strategy.objective.targetType == null}">
    Selecciona tipo...
  </option>
  <option value="SMA"
    th:selected="${strategy.objective != null and strategy.objective.targetType == 'SMA'}">
    SMA
  </option>
  <option value="PERCENTAGE"
    th:selected="${strategy.objective != null and strategy.objective.targetType == 'PERCENTAGE'}">
    PERCENTAGE
  </option>
  <option value="FIXED_PRICE"
    th:selected="${strategy.objective != null and strategy.objective.targetType == 'FIXED_PRICE'}">
    FIXED_PRICE
  </option>
</select>
```

**Detalle C2 — Validación SMA en dominio:**

```java
// En StrategyObjective.java
private static final Set<Integer> ALLOWED_SMA_PERIODS = Set.of(20, 50, 200);

public void validate() {
    // ... validaciones existentes ...
    
    if (targetType == ObjectiveType.SMA) {
        validateSmaPeriod(targetValue, "targetValue");
    }
    if (stopLossType == ObjectiveType.SMA) {
        validateSmaPeriod(stopLossValue, "stopLossValue");
    }
}

private void validateSmaPeriod(BigDecimal value, String fieldName) {
    int period = value.intValue();
    if (!ALLOWED_SMA_PERIODS.contains(period)) {
        throw new IllegalArgumentException(
            String.format("%s must be a valid SMA period (20, 50, 200), but was: %d",
                fieldName, period));
    }
}
```

---

### Fase 1 — UX Adaptativa del Formulario

**Prioridad:** Alta  
**Objetivo:** El formulario se adapta dinámicamente al tipo seleccionado, ofreciendo controles apropiados.

| ID | Tarea | Archivo(s) | Tipo |
|----|-------|------------|------|
| A1-a | Implementar JS `onchange` para Tipo de Objetivo: si SMA → mostrar `<select>` con opciones 20, 50, 200; si PERCENTAGE/FIXED_PRICE → mostrar `<input>` libre | `create.html`, `strategy-manager.js` | Feature JS/HTML |
| A1-b | Implementar JS `onchange` para Tipo de Stop Loss (misma lógica) | `create.html`, `strategy-manager.js` | Feature JS/HTML |
| A1-c | Adaptar placeholder y step según tipo: PERCENTAGE → `step="0.01"`, placeholder "ej., 5.00 %"; FIXED_PRICE → `step="0.01"`, placeholder "ej., 155.00 $" | `strategy-manager.js` | Mejora UX |

**Detalle técnico A1-a:**

```javascript
// En strategy-manager.js
function toggleObjectiveValue(selectElement, valueInputId, valueSelectId) {
    const selectedValue = selectElement.value;
    const input = document.getElementById(valueInputId);
    const select = document.getElementById(valueSelectId);
    
    if (selectedValue === 'SMA') {
        // Mostrar select con periodos predefinidos
        if (select) {
            select.innerHTML = '<option value="">-- Periodo SMA --</option>'
                + '<option value="20">SMA 20</option>'
                + '<option value="50">SMA 50</option>'
                + '<option value="200">SMA 200</option>';
            select.style.display = '';
            select.disabled = false;
            select.setAttribute('required', 'required');
            // Restaurar valor si existe
            if (input && input.value) {
                select.value = input.value;
            }
        }
        if (input) {
            input.style.display = 'none';
            input.disabled = true;
            input.removeAttribute('required');
        }
    } else {
        // Mostrar input libre
        if (input) {
            input.style.display = '';
            input.disabled = false;
            input.setAttribute('required', 'required');
            if (selectedValue === 'PERCENTAGE') {
                input.placeholder = 'ej., 5.00 %';
            } else {
                input.placeholder = 'ej., 155.00 $';
            }
        }
        if (select) {
            select.style.display = 'none';
            select.disabled = true;
            select.removeAttribute('required');
        }
    }
}
```

---

### Fase 2 — Robustez en Capas de Aplicación y Dominio

**Prioridad:** Media  
**Objetivo:** Fortalecer la validación y manejo de errores en el flujo de datos.

| ID | Tarea | Archivo(s) | Tipo |
|----|-------|------------|------|
| A2 | Envolver `ObjectiveType.valueOf()` en try-catch, lanzar excepción controlada | `StrategyDTOMapper.java` | Fix robustez |
| M1 | Validar `targetValue > stopLossValue` cuando ambos tipos son `FIXED_PRICE` | `StrategyObjective.java` | Validación dominio |
| M2 | Aclarar semántica de `capitalToRisk` (monto absoluto) en documentación y UI | `StrategyObjective.java`, `create.html` | Documentación/UX |
| T2 | Tests para mapper con enum inválido, tests de validación cruzada FIXED_PRICE | `StrategyDTOMapperTest.java`, `StrategyObjectiveTest.java` | Test |

**Detalle M1 — Validación cruzada:**

```java
// En StrategyObjective.validate(), al final
if (targetType == ObjectiveType.FIXED_PRICE && stopLossType == ObjectiveType.FIXED_PRICE) {
    if (targetValue.compareTo(stopLossValue) <= 0) {
        throw new IllegalArgumentException(
            "When both target and stop-loss use FIXED_PRICE, targetValue must be greater than stopLossValue");
    }
}
```

---

### Fase 3 — Enriquecimiento de Vista de Detalle

**Prioridad:** Media  
**Objetivo:** Mostrar la configuración de riesgo en la vista de solo lectura de la estrategia.

| ID | Tarea | Archivo(s) | Tipo |
|----|-------|------------|------|
| M3 | Añadir sección "Gestión de Riesgo" en vista de detalle con los 6 campos | `detail.html` | Feature HTML |
| T3 | Validar renderizado con `MockMvc` o inspección visual | Test integración / manual | Test |

**Detalle M3 — Sección en detail.html:**

```html
<!-- Risk Management Card -->
<div class="card mb-4" th:if="${strategy.objective != null}">
  <div class="card-header bg-light">
    <h5 class="mb-0">
      <i class="bi bi-shield-check text-primary me-2"></i>
      Gestión de Riesgo
    </h5>
  </div>
  <div class="card-body">
    <div class="row g-3">
      <div class="col-md-4">
        <p class="form-label fw-semibold text-muted small">Tipo de Objetivo</p>
        <p th:text="${strategy.objective.targetType}">PERCENTAGE</p>
      </div>
      <div class="col-md-4">
        <p class="form-label fw-semibold text-muted small">Valor Objetivo</p>
        <p th:text="${strategy.objective.targetValue}">5.00</p>
      </div>
      <div class="col-md-4">
        <p class="form-label fw-semibold text-muted small">Tipo de Stop Loss</p>
        <p th:text="${strategy.objective.stopLossType}">PERCENTAGE</p>
      </div>
      <div class="col-md-4">
        <p class="form-label fw-semibold text-muted small">Valor Stop Loss</p>
        <p th:text="${strategy.objective.stopLossValue}">2.00</p>
      </div>
      <div class="col-md-4">
        <p class="form-label fw-semibold text-muted small">Capital a Arriesgar ($)</p>
        <p th:text="${strategy.objective.capitalToRisk}">1000.00</p>
      </div>
      <div class="col-md-12">
        <p class="form-label fw-semibold text-muted small">Descripción del Objetivo</p>
        <p class="text-muted" th:text="${strategy.objective.description}">Descripción...</p>
      </div>
    </div>
  </div>
</div>
```

---

### Fase 4 — Mejoras de Experiencia y Defensivas

**Prioridad:** Baja  
**Objetivo:** Pulir la experiencia de usuario y prevenir edge cases.

| ID | Tarea | Archivo(s) | Tipo |
|----|-------|------------|------|
| B1 | Añadir `max` razonable en inputs numéricos del formulario | `create.html` | UX defensiva |
| B2 | Valores por defecto en StrategyObjectiveDTO para nueva estrategia | `StrategyController.java` | UX |
| B3 | Tooltip/ayuda contextual explicando cada campo de riesgo | `create.html` | UX |

---

## 4. Resumen de Prioridades

```
┌─────────────────────────────────────────────────────────┐
│  Fase 0 — BLOQUEANTE                                   │
│  C1: Dropdown vacío de Tipo de Objetivo                 │
│  C2: Validación SMA periods en dominio                  │
│  M4: Tests de validación SMA                            │
├─────────────────────────────────────────────────────────┤
│  Fase 1 — ALTA                                          │
│  A1: UX adaptativa (select SMA / input libre)           │
├─────────────────────────────────────────────────────────┤
│  Fase 2 — MEDIA                                         │
│  A2: Robustez en mapper                                 │
│  M1: Validación cruzada FIXED_PRICE                     │
│  M2: Clarificar capitalToRisk                           │
├─────────────────────────────────────────────────────────┤
│  Fase 3 — MEDIA                                         │
│  M3: Mostrar riesgo en detail.html                      │
├─────────────────────────────────────────────────────────┤
│  Fase 4 — BAJA                                          │
│  B1-B3: Límites, defaults, tooltips                     │
└─────────────────────────────────────────────────────────┘
```

---

## 5. Notas Técnicas Adicionales

### Flujo actual de validación (de UI a persistencia)

```
Formulario HTML (required, min=0)
       ↓
Spring MVC Binding (StrategyDTO ← form params)
       ↓
StrategyDTOMapper.toObjectiveDomain() — ObjectiveType.valueOf()
       ↓
Strategy.validateConsistency() → StrategyObjective.validate()
       ↓
StrategyRepository.save() — JPA persistencia
       ↓
EvaluateStrategyService → RiskRewardCalculator (cálculo real)
```

**Gap clave:** La validación de periodos SMA solo ocurre en el último paso (evaluación), cuando ya se guardó la estrategia. Debe moverse a `StrategyObjective.validate()`.

### Periodos SMA soportados

| Periodo | Stock field | Significado |
|---------|-------------|-------------|
| 20 | `stock.getSma20()` | SMA de corto plazo |
| 50 | `stock.getSma50()` | SMA de medio plazo |
| 200 | `stock.getSma200()` | SMA de largo plazo |

Cualquier otro periodo (10, 30, 100, etc.) lanzará `IllegalArgumentException` en `RiskRewardCalculator.resolveSmaValue()`.

### Cálculos de Riesgo (referencia)

| Métrica | Fórmula | Método |
|---------|---------|--------|
| Target Price (%) | `entry × (1 + value/100)` | `calculatePercentagePrice(entry, value, true)` |
| Stop-Loss Price (%) | `entry × (1 − value/100)` | `calculatePercentagePrice(entry, value, false)` |
| Target/SL (SMA) | `stock.getSma{period}()` | `resolveSmaValue(period, stock)` |
| Target/SL (FIXED) | `value` directo | `validateAndReturnFixedPrice(value)` |
| Ratio R:R | `(target − entry) / (entry − stopLoss)` | `calculateRiskRewardRatio()` |
| Posición | `capitalToRisk / (entry − stopLoss)` ↓ | `calculatePositionSize()` |

---

## 6. Decisiones Técnicas

1. **Constante `ALLOWED_SMA_PERIODS` en dominio:** Se define en `StrategyObjective` para que la validación de creación y la evaluación usen la misma fuente de verdad. Alternativamente se puede poner en `ObjectiveType` para centralizar.

2. **Patrón UX para SMA:** Se reutiliza el patrón ya implementado en `strategy-manager.js` (`toggleParameter()`) que alterna entre `<select>` y `<input>` según el tipo seleccionado.

3. **Validación cruzada limitada:** Solo se valida `targetValue > stopLossValue` cuando ambos son `FIXED_PRICE`. Para tipos mixtos (SMA + PERCENTAGE, etc.) la coherencia solo puede verificarse con datos de mercado reales, lo cual ocurre en tiempo de evaluación.

4. **`capitalToRisk` como monto absoluto:** Respeta el comportamiento actual de `calculatePositionSize()`. La documentación del Javadoc se debe corregir para reflejar esto.
