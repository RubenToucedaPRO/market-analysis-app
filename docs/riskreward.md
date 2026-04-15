# Análisis y Plan de Mejora: RiskRewardCalculator

**Fecha:** 2026-04-15  
**Alcance:** Servicio de dominio `RiskRewardCalculator`, servicio de evaluación `EvaluateStrategyService`, validación de `StrategyObjective`, vistas Thymeleaf y formulario de gestión de riesgo.

---

## 1. Problemas Identificados

### 1.1 Excepciones no controladas en `EvaluateStrategyService`

**Archivo:** `src/main/java/com/market/analysis/domain/service/EvaluateStrategyService.java` (líneas 83-94)

El bloque `try-catch` del método `evaluateStrategy()` solo captura `MissingIndicatorException`, pero `RiskRewardCalculator` también lanza `IllegalArgumentException` en múltiples escenarios de incongruencia:

- `calculateStopLossPrice()` → `validateStopLossPrice()` lanza `IllegalArgumentException` cuando `stopLoss >= entryPrice` (línea 247 de `RiskRewardCalculator.java`).
- `calculateRiskRewardRatio()` lanza `IllegalArgumentException` cuando `targetPrice <= entryPrice` (línea 104) o `stopPrice >= entryPrice` (línea 107).
- `resolveSmaValue()` lanza `IllegalArgumentException` para periodos SMA no soportados (línea 176).

**Impacto:** Cuando un ticker cumple las reglas de la estrategia (`overallPassed = true`) pero los parámetros de risk/reward son incongruentes con el precio actual (por ejemplo, el SMA200 usado como stop-loss está por encima del precio actual), la `IllegalArgumentException` se propaga sin capturar, causando un error 500 en la aplicación en vez de mostrar un aviso informativo en el ticker correspondiente.

**Ejemplo concreto:** Si una estrategia usa SMA200 como stop-loss y el precio actual del ticker cae por debajo de la SMA200, entonces `stopLossPrice > entryPrice`, lo que es lógicamente incongruente y lanza excepción en vez de informar al usuario.

### 1.2 Periodos SMA hardcodeados en `RiskRewardCalculator`

**Archivo:** `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java` (líneas 172-178)

El método `resolveSmaValue()` usa un `switch` con valores fijos (20, 50, 200):

```java
BigDecimal smaValue = switch (period) {
    case 20 -> stock.getSma20();
    case 50 -> stock.getSma50();
    case 200 -> stock.getSma200();
    default -> throw new IllegalArgumentException(...);
};
```

**Impacto:** Si en el futuro se añaden nuevas SMAs (ej. SMA10, SMA100), hay que modificar este switch manualmente. No existe una constante compartida que defina los periodos válidos, provocando duplicación de lógica con `RuleCapabilityCatalog.resolveSma()`.

### 1.3 Falta de validación de periodos SMA en `StrategyObjective.validate()`

**Archivo:** `src/main/java/com/market/analysis/domain/model/StrategyObjective.java` (líneas 66-95)

El método `validate()` verifica que los valores sean positivos y no nulos, pero **no valida** que cuando `targetType` o `stopLossType` es `SMA`, el valor correspondiente sea un periodo SMA válido (20, 50 o 200). Un usuario puede configurar `stopLossType = SMA` con `stopLossValue = 99`, lo que solo fallará en tiempo de evaluación con una excepción no controlada.

### 1.4 Vista `analysis.html`: Acceso a campos de riesgo potencialmente nulos

**Archivo:** `src/main/resources/templates/analysis/analysis.html` (líneas 243-283)

La vista muestra los campos de riesgo (`riskRewardRatio`, `targetPrice`, `stopLossPrice`, `recommendedShares`) condicionados por `th:if="${ticker.evaluationPassed}"`. Sin embargo, `evaluationPassed` puede ser `true` mientras los campos de riesgo son `null` (cuando se captura `MissingIndicatorException` en `EvaluateStrategyService`).

**Líneas problemáticas:**
- Línea 248: `th:classappend="${ticker.riskRewardRatio >= 2.0 ? ...}"` — NPE si `riskRewardRatio` es null.
- Línea 249: `th:text="${#numbers.formatDecimal(ticker.riskRewardRatio, 1, 2)}"` — NPE si null.
- Línea 259: `th:text="${#numbers.formatDecimal(ticker.targetPrice, 1, 2)}"` — NPE si null.
- Línea 269: `th:text="${#numbers.formatDecimal(ticker.stopLossPrice, 1, 2)}"` — NPE si null.

### 1.5 Vista `ticker-detail.html`: Mismo problema de null-safety

**Archivo:** `src/main/resources/templates/analysis/ticker-detail.html` (líneas 138-204)

Misma situación que `analysis.html`. El bloque `th:if="${ticker.evaluationPassed}"` asume que todos los campos de riesgo tienen valor, pero pueden ser nulos.

### 1.6 Formulario de riesgo: Selección libre de valores para SMA

**Archivo:** `src/main/resources/templates/fragments/risk-management-fields.html` (líneas 41-57, 97-112)

Cuando el usuario selecciona tipo SMA para target o stop-loss, el campo de valor es un `<input type="number">` libre que permite cualquier valor numérico. No hay lógica JavaScript que:
1. Transforme el input en un `<select>` con las opciones válidas (20, 50, 200) al seleccionar SMA.
2. Restaure el `<input>` libre al seleccionar PERCENTAGE o FIXED_PRICE.

**Impacto:** El usuario puede introducir un periodo SMA inválido (ej. 15, 100, 300) que solo fallará en tiempo de evaluación.

### 1.7 Sin validación de coherencia lógica en la entrada

No existe validación que evite configuraciones lógicamente inválidas como:
- Stop-loss tipo `FIXED_PRICE` con un valor superior al precio de mercado típico del ticker.
- Target tipo `PERCENTAGE` con un porcentaje negativo o extremo (ej. 500%).
- Stop-loss SMA con periodo mayor que el target SMA (ej. stop=SMA200, target=SMA20), lo cual en muchos escenarios es incongruente.

---

## 2. Plan de Mejora

### 2.1 Resumen de cambios por capa

| Capa | Archivo | Cambio |
|------|---------|--------|
| **Dominio** | `EvaluateStrategyService.java` | Ampliar catch para capturar `IllegalArgumentException` además de `MissingIndicatorException` |
| **Dominio** | `RiskRewardCalculator.java` | Extraer constante `ALLOWED_SMA_PERIODS` compartida |
| **Dominio** | `StrategyObjective.java` | Añadir validación de periodos SMA válidos en `validate()` |
| **Vista** | `analysis.html` | Añadir null-checks para campos de riesgo, mostrar aviso cuando son nulos |
| **Vista** | `ticker-detail.html` | Añadir null-checks para campos de riesgo, mostrar aviso cuando son nulos |
| **Vista** | `risk-management-fields.html` | Cambiar input a select dinámico para SMA |
| **JavaScript** | `strategy-manager.js` (o nuevo fichero) | Lógica de toggle input/select según tipo de objetivo |
| **Tests** | Tests unitarios | Añadir tests para nuevos escenarios de error controlado |

### 2.2 Detalle de cada punto

#### A. Captura de `IllegalArgumentException` en `EvaluateStrategyService`

```java
// ANTES (solo captura MissingIndicatorException):
} catch (MissingIndicatorException e) {
    summary = summary + " Risk plan could not be calculated: " + e.getMessage();
}

// DESPUÉS (captura también IllegalArgumentException):
} catch (MissingIndicatorException | IllegalArgumentException e) {
    summary = summary + " Risk plan could not be calculated: " + e.getMessage();
}
```

Con este cambio, si el stop-loss SMA está por encima del precio actual, la evaluación permanece `compliant = true` pero los campos de riesgo quedan `null` y el summary incluye la explicación de por qué no se pudo calcular el plan de riesgo.

#### B. Constante compartida para periodos SMA

Crear una constante `ALLOWED_SMA_PERIODS` en el dominio (por ejemplo en `StrategyObjective` o en una clase de constantes) que sea reutilizada por:
- `RiskRewardCalculator.resolveSmaValue()`
- `StrategyObjective.validate()`
- La vista (para generar las opciones del selector)

```java
public static final Set<Integer> ALLOWED_SMA_PERIODS = Set.of(20, 50, 200);
```

#### C. Validación de periodos SMA en `StrategyObjective.validate()`

```java
if (targetType == ObjectiveType.SMA && !ALLOWED_SMA_PERIODS.contains(targetValue.intValue())) {
    throw new IllegalArgumentException(
        "targetValue must be a valid SMA period: " + ALLOWED_SMA_PERIODS);
}
if (stopLossType == ObjectiveType.SMA && !ALLOWED_SMA_PERIODS.contains(stopLossValue.intValue())) {
    throw new IllegalArgumentException(
        "stopLossValue must be a valid SMA period: " + ALLOWED_SMA_PERIODS);
}
```

#### D. Null-safety en vistas para campos de riesgo

En `analysis.html` y `ticker-detail.html`, el bloque de métricas de riesgo debe condicionarse no solo a `evaluationPassed` sino también a que los campos de riesgo tengan valor:

```html
<!-- Condición robusta -->
<th:block th:if="${ticker.evaluationPassed and ticker.riskRewardRatio != null}">
  <!-- Mostrar métricas de riesgo -->
</th:block>

<!-- Aviso cuando la evaluación pasa pero no hay plan de riesgo -->
<th:block th:if="${ticker.evaluationPassed and ticker.riskRewardRatio == null}">
  <div class="alert alert-warning">
    <i class="bi bi-exclamation-triangle me-1"></i>
    La estrategia cumple las reglas pero el plan de riesgo no se pudo calcular 
    por incongruencia en los parámetros de RiskReward con los datos actuales del ticker.
  </div>
</th:block>
```

#### E. Selector dinámico de SMA en formulario

En `risk-management-fields.html`, reemplazar los `<input type="number">` de target y stop-loss por una estructura dual input/select (similar a como se hace en `rule-row.html` para los parámetros de reglas):

1. Cuando el tipo seleccionado es **SMA**: mostrar un `<select>` con opciones 20, 50, 200.
2. Cuando el tipo es **PERCENTAGE** o **FIXED_PRICE**: mostrar un `<input type="number">` libre.

La lógica JavaScript debe:
- Escuchar el cambio en el `<select>` de tipo (targetType, stopLossType).
- Alternar entre `<select>` y `<input>` según el tipo seleccionado.
- Desabilitar/ocultar el control no activo para evitar envío de datos duplicados.

---

## 3. Fases de Implementación

### Fase 1: Manejo Robusto de Excepciones (Prioridad Alta — Backend)

**Objetivo:** Evitar errores 500 cuando los cálculos de riesgo son incongruentes.

**Tareas:**
1. Modificar `EvaluateStrategyService.evaluateStrategy()` para capturar `IllegalArgumentException` junto con `MissingIndicatorException`.
2. Añadir tests unitarios en `EvaluateStrategyServiceTest` para cubrir escenarios donde `RiskRewardCalculator` lanza `IllegalArgumentException` (ej. stop-loss > precio actual).
3. Verificar que `StrategyEvaluation` se construye correctamente con campos de riesgo `null` y summary informativo.

**Archivos afectados:**
- `src/main/java/com/market/analysis/domain/service/EvaluateStrategyService.java`
- `src/test/java/com/market/analysis/unit/application/usecase/EvaluateStrategyServiceTest.java`

**Estimación:** Baja complejidad, alto impacto.

---

### Fase 2: Null-Safety en Vistas (Prioridad Alta — Frontend)

**Objetivo:** Evitar errores Thymeleaf cuando los campos de riesgo son nulos en tickers que pasan la evaluación.

**Tareas:**
1. En `analysis.html`: añadir condición `ticker.riskRewardRatio != null` al bloque de métricas de riesgo (líneas 243-283).
2. En `analysis.html`: añadir bloque de aviso para tickers que pasan evaluación pero no tienen plan de riesgo.
3. En `ticker-detail.html`: añadir condición `ticker.riskRewardRatio != null` al bloque de plan de ejecución (líneas 138-204).
4. En `ticker-detail.html`: añadir bloque de aviso equivalente.

**Archivos afectados:**
- `src/main/resources/templates/analysis/analysis.html`
- `src/main/resources/templates/analysis/ticker-detail.html`

**Estimación:** Baja complejidad, alto impacto.

---

### Fase 3: Validación de Periodos SMA en Dominio (Prioridad Media — Backend)

**Objetivo:** Rechazar configuraciones inválidas de SMA antes de que lleguen al cálculo.

**Tareas:**
1. Definir constante `ALLOWED_SMA_PERIODS = Set.of(20, 50, 200)` en `StrategyObjective`.
2. Añadir validación en `StrategyObjective.validate()` para verificar que los valores SMA sean periodos válidos.
3. Refactorizar `RiskRewardCalculator.resolveSmaValue()` para usar la constante compartida.
4. Actualizar tests de `StrategyObjectiveTest` para cubrir la validación de periodos SMA.
5. Actualizar tests de `RiskRewardCalculatorTest` si se modifica `resolveSmaValue()`.

**Archivos afectados:**
- `src/main/java/com/market/analysis/domain/model/StrategyObjective.java`
- `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java`
- `src/test/java/com/market/analysis/unit/domain/model/StrategyObjectiveTest.java`
- `src/test/java/com/market/analysis/unit/domain/service/RiskRewardCalculatorTest.java`

**Estimación:** Complejidad media, impacto medio.

---

### Fase 4: Selector Dinámico de SMA en Formulario (Prioridad Media — Frontend)

**Objetivo:** Mejorar la UX del formulario de gestión de riesgo para evitar entradas inválidas.

**Tareas:**
1. Modificar `risk-management-fields.html` para incluir un `<select>` oculto con opciones SMA (20, 50, 200) junto al `<input>` existente para target y stop-loss.
2. Crear o extender JavaScript (en `strategy-manager.js` o fichero nuevo `risk-management.js`) para:
   - Detectar cambio en `objectiveTargetType` y `objectiveStopLossType`.
   - Si tipo = SMA: ocultar `<input>`, mostrar `<select>` con opciones SMA, habilitar required.
   - Si tipo ≠ SMA: ocultar `<select>`, mostrar `<input>`, habilitar required.
   - Al cargar la página (edición), aplicar la lógica según el valor actual del tipo.
3. Asegurar que el `name` del campo activo es el correcto (`objective.targetValue`, `objective.stopLossValue`) para que el bind con Thymeleaf funcione.
4. Actualizar placeholders para cada tipo: 
   - SMA: "(período)"
   - PERCENTAGE: "ej., 5.00 (%)"
   - FIXED_PRICE: "ej., 150.00 ($)"

**Archivos afectados:**
- `src/main/resources/templates/fragments/risk-management-fields.html`
- `src/main/resources/static/js/strategy-manager.js` (o nuevo `risk-management.js`)
- `src/main/resources/templates/strategies/create.html` (si se requiere import de nuevo JS)

**Estimación:** Complejidad media, impacto alto en UX.

---

### Fase 5: Validaciones Avanzadas de Coherencia (Prioridad Baja — Backend)

**Objetivo:** Detectar y advertir sobre configuraciones que, si bien son técnicamente válidas, son lógicamente improbables o riesgosas.

**Tareas (opcionales, a evaluar):**
1. Advertencia cuando porcentaje de stop-loss > 20% (riesgo extremo).
2. Advertencia cuando porcentaje de target > 100% (objetivo irrealista para corto plazo).
3. Validación en `StrategyObjective.validate()` para que cuando ambos son SMA, el periodo del target no sea igual al del stop-loss.
4. Logging de warnings en `EvaluateStrategyService` cuando los parámetros producen planes de riesgo con ratio < 1.0.

**Archivos afectados:**
- `src/main/java/com/market/analysis/domain/model/StrategyObjective.java`
- `src/main/java/com/market/analysis/domain/service/EvaluateStrategyService.java`

**Estimación:** Complejidad baja-media, impacto bajo. Estas validaciones son complementarias y pueden implementarse de forma incremental.

---

## 4. Priorización y Dependencias

```
Fase 1 (Backend exceptions) ──┐
                               ├──► Fase 2 (Vista null-safety) 
                               │      depende de Fase 1 para tener sentido completo
Fase 3 (SMA validation)  ─────┤
                               ├──► Fase 4 (Frontend selector) 
                               │      depende de Fase 3 para compartir constantes
Fase 5 (Validaciones avanzadas) ──► Independiente, puede ir en paralelo
```

**Orden recomendado:** Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5

---

## 5. Resumen de Impacto

| Aspecto | Estado Actual | Estado Deseado |
|---------|--------------|----------------|
| Error en cálculo incongruente | Excepción 500 | Aviso informativo en ticker |
| Periodos SMA válidos | Sin validación al crear | Validación en dominio + selector en UI |
| Vista con evaluación passed + risk null | NPE / error Thymeleaf | Mensaje de aviso UX-friendly |
| Entrada de valor SMA | Input libre (cualquier número) | Select con opciones 20, 50, 200 |
| Constantes SMA | Duplicadas (RiskRewardCalculator + RuleCapabilityCatalog) | Constante compartida en dominio |

---

## 6. Notas Técnicas

- **Arquitectura:** Todos los cambios respetan la Arquitectura Hexagonal y Clean Architecture. La lógica de validación permanece en el dominio, la presentación de errores en las vistas.
- **Tests:** Cada fase incluye tests unitarios. La Fase 1 y 3 requieren tests de dominio, la Fase 2 puede verificarse visualmente o con tests de integración MockMvc.
- **Compatibilidad:** Los cambios son retrocompatibles. Las estrategias existentes con valores SMA válidos no se ven afectadas. Las que tengan valores inválidos simplemente mostrarán el aviso en vez de causar error.
- **SonarQube:** Los cambios reducen code smells (magic numbers, duplicación de constantes) y eliminan potenciales bugs (NPE en vistas).
