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

### 1.2 Periodos SMA hardcodeados y duplicados entre `RiskRewardCalculator` y `RuleCapabilityCatalog`

**Archivos:**
- `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java` (líneas 172-178)
- `src/main/java/com/market/analysis/domain/model/RuleCapabilityCatalog.java` (líneas 40-44, 184-194)

El método `resolveSmaValue()` de `RiskRewardCalculator` usa un `switch` con valores fijos (20, 50, 200):

```java
BigDecimal smaValue = switch (period) {
    case 20 -> stock.getSma20();
    case 50 -> stock.getSma50();
    case 200 -> stock.getSma200();
    default -> throw new IllegalArgumentException(...);
};
```

Sin embargo, `RuleCapabilityCatalog` ya es la **fuente única de verdad** para los periodos SMA válidos (línea 42: `Set.of(20.0, 50.0, 200.0)`) y tiene su propio resolver `resolveSma()` (línea 184). Esto genera **duplicación de lógica**: si se añade una nueva SMA (ej. SMA100), hay que modificar ambos ficheros manualmente, con riesgo de divergencia.

**Impacto:** Cualquier nueva SMA requiere cambios en al menos dos puntos del dominio, violando el principio DRY y aumentando el riesgo de inconsistencias.

### 1.3 Falta de validación de periodos SMA en `StrategyObjective.validate()`

**Archivo:** `src/main/java/com/market/analysis/domain/model/StrategyObjective.java` (líneas 66-95)

El método `validate()` verifica que los valores sean positivos y no nulos, pero **no valida** que cuando `targetType` o `stopLossType` es `SMA`, el valor correspondiente sea un periodo SMA válido. Un usuario puede configurar `stopLossType = SMA` con `stopLossValue = 99`, lo que solo fallará en tiempo de evaluación con una excepción no controlada.

**Nota:** La validación debería delegar en `RuleCapabilityCatalog.getCapability("SMA")` para obtener los periodos válidos dinámicamente, en vez de mantener una lista de valores hardcodeados en `StrategyObjective`. Así, al añadir una nueva SMA al catálogo, la validación se actualizaría automáticamente.

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
1. Transforme el input en un `<select>` con las opciones válidas (obtenidas dinámicamente desde `ruleDefinitions`) al seleccionar SMA.
2. Restaure el `<input>` libre al seleccionar PERCENTAGE o FIXED_PRICE.

**Impacto:** El usuario puede introducir un periodo SMA inválido (ej. 15, 100, 300) que solo fallará en tiempo de evaluación.

**Nota:** Los `ruleDefinitions` ya están disponibles en el modelo del formulario de estrategia (inyectados por `StrategyController`) y contienen los `allowedParams` de cada indicador. La misma infraestructura que se usa en `strategy-manager.js` para los parámetros de reglas puede reutilizarse para los selectores de target/stop-loss.

### 1.7 Sin validación de coherencia lógica en la entrada

No existe validación que evite configuraciones lógicamente inválidas como:
- Stop-loss tipo `FIXED_PRICE` con un valor superior al precio de mercado típico del ticker.
- Target tipo `PERCENTAGE` con un porcentaje negativo o extremo (ej. 500%).
- Stop-loss SMA con periodo mayor que el target SMA (ej. stop=SMA200, target=SMA20), lo cual en muchos escenarios es incongruente.

> **Nota:** Estas validaciones se plantean como advertencias informativas (warnings), no como restricciones duras (hard constraints), ya que diferentes perfiles de trading pueden tener umbrales de riesgo distintos.

---

## 2. Plan de Mejora

### 2.1 Resumen de cambios por capa

| Capa | Archivo | Cambio |
|------|---------|--------|
| **Dominio** | `EvaluateStrategyService.java` | Ampliar catch para capturar `IllegalArgumentException` además de `MissingIndicatorException` |
| **Dominio** | `RiskRewardCalculator.java` | Delegar resolución SMA a `RuleCapabilityCatalog` (eliminar switch duplicado) |
| **Dominio** | `StrategyObjective.java` | Validar periodos SMA consultando `RuleCapabilityCatalog` |
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

#### B. Reutilizar `RuleCapabilityCatalog` como fuente única de verdad para SMA

En lugar de crear una constante `ALLOWED_SMA_PERIODS` duplicada, **reutilizar la infraestructura existente** del catálogo de capacidades:

- `RuleCapabilityCatalog` ya define los periodos SMA válidos: `Set.of(20.0, 50.0, 200.0)` (línea 42) y su resolver `resolveSma()` (línea 184).
- `RuleCapabilityCatalog.getCapability("SMA").getAllowedParams()` devuelve el conjunto de periodos válidos.
- `RuleCapabilityCatalog.getCapability("SMA").resolve(param, stock)` resuelve el valor SMA del stock.

**Beneficio:** Al añadir una nueva SMA (ej. SMA100), basta con:
1. Añadir el campo en el modelo `Stock` (ej. `sma100`).
2. Actualizar el entry `"SMA"` en `RuleCapabilityCatalog` (añadir `100.0` al set y el case al resolver).
3. Todo lo demás (`RiskRewardCalculator`, `StrategyObjective.validate()`, la vista) se actualiza automáticamente.

Para `RiskRewardCalculator.resolveSmaValue()`:
```java
// ANTES (switch duplicado con valores fijos):
BigDecimal smaValue = switch (period) {
    case 20 -> stock.getSma20();
    case 50 -> stock.getSma50();
    case 200 -> stock.getSma200();
    default -> throw new IllegalArgumentException(...);
};

// DESPUÉS (delegar al catálogo):
RuleCapability smaCapability = RuleCapabilityCatalog.getCapability("SMA")
    .orElseThrow(() -> new IllegalStateException("SMA not found in catalog"));
if (!smaCapability.isParamAllowed((double) period)) {
    throw new IllegalArgumentException("SMA period " + period + " is not supported");
}
BigDecimal smaValue = smaCapability.resolve((double) period, stock);
```

#### C. Validación de periodos SMA en `StrategyObjective.validate()` vía catálogo

En lugar de mantener una lista de valores hardcodeados, consultar el catálogo dinámicamente:

```java
if (targetType == ObjectiveType.SMA) {
    Set<Double> allowedSmaPeriods = RuleCapabilityCatalog.getCapability("SMA")
        .map(RuleCapability::getAllowedParams)
        .orElse(Set.of());
    if (!allowedSmaPeriods.contains(targetValue.doubleValue())) {
        throw new IllegalArgumentException(
            "targetValue must be a valid SMA period: " + allowedSmaPeriods);
    }
}
if (stopLossType == ObjectiveType.SMA) {
    Set<Double> allowedSmaPeriods = RuleCapabilityCatalog.getCapability("SMA")
        .map(RuleCapability::getAllowedParams)
        .orElse(Set.of());
    if (!allowedSmaPeriods.contains(stopLossValue.doubleValue())) {
        throw new IllegalArgumentException(
            "stopLossValue must be a valid SMA period: " + allowedSmaPeriods);
    }
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

#### E. Selector dinámico de SMA en formulario reutilizando `ruleDefinitions`

En `risk-management-fields.html`, reemplazar los `<input type="number">` de target y stop-loss por una estructura dual input/select (similar a como se hace en `rule-row.html` para los parámetros de reglas):

1. Cuando el tipo seleccionado es **SMA**: mostrar un `<select>` cuyas opciones se generan dinámicamente desde `globalThis.ruleDefinitions` (entrada con `code === "SMA"` → `allowedParams`).
2. Cuando el tipo es **PERCENTAGE** o **FIXED_PRICE**: mostrar un `<input type="number">` libre.

La lógica JavaScript debe:
- Escuchar el cambio en el `<select>` de tipo (targetType, stopLossType).
- Buscar en `globalThis.ruleDefinitions` la entrada SMA para obtener `allowedParams`.
- Alternar entre `<select>` (poblado dinámicamente) y `<input>` según el tipo seleccionado.
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

### Fase 3: Validación de Periodos SMA mediante `RuleCapabilityCatalog` (Prioridad Media — Backend)

**Objetivo:** Rechazar configuraciones inválidas de SMA antes de que lleguen al cálculo, reutilizando el catálogo existente como fuente única de verdad.

**Principio clave:** `RuleCapabilityCatalog` ya es la fuente canónica de los periodos SMA válidos y sus resolvers. En vez de crear constantes duplicadas, `StrategyObjective` y `RiskRewardCalculator` deben **consultar el catálogo** para validar y resolver periodos SMA. De este modo, añadir una nueva SMA solo requiere actualizar `RuleCapabilityCatalog` (y el modelo `Stock`).

**Tareas:**
1. Modificar `StrategyObjective.validate()` para que cuando `targetType` o `stopLossType` sea `SMA`, consulte `RuleCapabilityCatalog.getCapability("SMA").getAllowedParams()` para validar el periodo.
2. Refactorizar `RiskRewardCalculator.resolveSmaValue()` para delegar la resolución al catálogo (`RuleCapabilityCatalog.getCapability("SMA").resolve(param, stock)`) eliminando el switch duplicado.
3. Actualizar tests de `StrategyObjectiveTest` para cubrir la validación de periodos SMA via catálogo.
4. Actualizar tests de `RiskRewardCalculatorTest` para reflejar la delegación al catálogo.

**Archivos afectados:**
- `src/main/java/com/market/analysis/domain/model/StrategyObjective.java`
- `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java`
- `src/test/java/com/market/analysis/unit/domain/model/StrategyObjectiveTest.java`
- `src/test/java/com/market/analysis/unit/domain/service/RiskRewardCalculatorTest.java`

**Estimación:** Complejidad media, impacto medio.

---

### Fase 4: Selector Dinámico de SMA en Formulario reutilizando `ruleDefinitions` (Prioridad Media — Frontend)

**Objetivo:** Mejorar la UX del formulario de gestión de riesgo para evitar entradas inválidas, reutilizando los `ruleDefinitions` que ya están disponibles en el modelo.

**Principio clave:** El `StrategyController` ya pasa `ruleDefinitions` al modelo (lista de `RuleDefinitionDTO` enriquecidos con `allowedParams` desde `RuleCapabilityCatalog`). El JavaScript ya usa `globalThis.ruleDefinitions` en `strategy-manager.js` para los parámetros de reglas. La misma infraestructura puede reutilizarse para los selectores de target/stop-loss tipo SMA, eliminando cualquier valor hardcodeado en la vista.

**Tareas:**
1. Modificar `risk-management-fields.html` para incluir un `<select>` oculto (sin opciones hardcodeadas) junto al `<input>` existente para target y stop-loss.
2. Extender JavaScript (en `strategy-manager.js` o fichero nuevo `risk-management.js`) para:
   - Al cambiar `objectiveTargetType` o `objectiveStopLossType`:
     - Si tipo = SMA: buscar en `globalThis.ruleDefinitions` la entrada con `code === "SMA"`, extraer `allowedParams`, poblar dinámicamente el `<select>` con esos valores, ocultar el `<input>`.
     - Si tipo ≠ SMA: ocultar `<select>`, mostrar `<input>`, habilitar required.
   - Al cargar la página (edición), aplicar la lógica según el valor actual del tipo.
3. Asegurar que el `name` del campo activo es el correcto (`objective.targetValue`, `objective.stopLossValue`) para que el bind con Thymeleaf funcione.
4. Actualizar placeholders para cada tipo: 
   - SMA: "(período)" — opciones generadas dinámicamente desde el catálogo
   - PERCENTAGE: "ej., 5.00 (%)"
   - FIXED_PRICE: "ej., 150.00 ($)"

**Beneficio:** Si se añade una nueva SMA al catálogo, las opciones del selector se actualizan automáticamente sin tocar la vista ni el JavaScript.

**Archivos afectados:**
- `src/main/resources/templates/fragments/risk-management-fields.html`
- `src/main/resources/static/js/strategy-manager.js` (o nuevo `risk-management.js`)
- `src/main/resources/templates/strategies/create.html` (si se requiere import de nuevo JS)

**Estimación:** Complejidad media, impacto alto en UX.

---

### Fase 5: Validaciones Avanzadas de Coherencia (Prioridad Baja — Backend)

**Objetivo:** Detectar y advertir (no bloquear) sobre configuraciones que, si bien son técnicamente válidas, son lógicamente improbables o riesgosas.

> **Estrategia de validación:** Estas validaciones se implementan como **warnings** (avisos informativos) que se registran en logs y se muestran al usuario, pero **no impiden** guardar la estrategia. Esto permite flexibilidad para traders con perfiles de riesgo distintos. Los umbrales sugeridos a continuación son orientativos y deberían ser configurables en el futuro.

**Tareas (opcionales, a evaluar):**
1. Advertencia cuando porcentaje de stop-loss > 20% (umbral orientativo para riesgo elevado; configurable según perfil de trader).
2. Advertencia cuando porcentaje de target > 100% (umbral orientativo para objetivos de largo plazo; podría ser válido en estrategias multi-meses).
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
Fase 3 (SMA via catálogo)  ───┤
                               ├──► Fase 4 (Frontend selector via ruleDefinitions) 
                               │      depende de Fase 3 para garantizar coherencia
Fase 5 (Validaciones avanzadas) ──► Independiente, puede ir en paralelo
```

**Orden recomendado:** Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5

---

## 5. Resumen de Impacto

| Aspecto | Estado Actual | Estado Deseado |
|---------|--------------|----------------|
| Error en cálculo incongruente | Excepción 500 | Aviso informativo en ticker |
| Periodos SMA válidos | Sin validación al crear; duplicación entre RiskRewardCalculator y RuleCapabilityCatalog | Validación vía catálogo en dominio + selector dinámico en UI desde `ruleDefinitions` |
| Vista con evaluación passed + risk null | NPE / error Thymeleaf | Mensaje de aviso UX-friendly |
| Entrada de valor SMA | Input libre (cualquier número) | Select con opciones dinámicas desde `ruleDefinitions` (catálogo) |
| Resolución SMA | Duplicada (RiskRewardCalculator.resolveSmaValue switch + RuleCapabilityCatalog.resolveSma) | Delegación única a `RuleCapabilityCatalog` |

---

## 6. Notas Técnicas

- **Arquitectura:** Todos los cambios respetan la Arquitectura Hexagonal y Clean Architecture. La lógica de validación permanece en el dominio, la presentación de errores en las vistas. `RuleCapabilityCatalog` se consolida como la **fuente única de verdad** para los indicadores técnicos válidos, sus parámetros y resolvers, tanto para reglas como para objetivos de estrategia.
- **Reutilización:** Los `ruleDefinitions` (DTOs enriquecidos desde el catálogo) ya se pasan al formulario de estrategia. El JavaScript existente en `strategy-manager.js` ya usa `globalThis.ruleDefinitions` para los parámetros de reglas. La misma infraestructura se reutiliza para los selectores de target/stop-loss.
- **Extensibilidad:** Para añadir una nueva SMA (ej. SMA100), basta con: (1) añadir el campo en `Stock`, (2) actualizar el entry `"SMA"` en `RuleCapabilityCatalog` (ampliar `allowedParams` y resolver). Todo lo demás se actualiza automáticamente.
- **Tests:** Cada fase incluye tests unitarios. La Fase 1 y 3 requieren tests de dominio, la Fase 2 puede verificarse visualmente o con tests de integración MockMvc.
- **Compatibilidad:** Los cambios son retrocompatibles. Las estrategias existentes con valores SMA válidos no se ven afectadas. Las que tengan valores inválidos simplemente mostrarán el aviso en vez de causar error.
- **SonarQube:** Los cambios reducen code smells (duplicación de resolvers, magic numbers) y eliminan potenciales bugs (NPE en vistas).
