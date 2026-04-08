# P1 – Alineación de modelo y evaluador

Fecha: 2026-04-08

## 1. Título descriptivo

P1: Explicit rule capability model, catalog-driven evaluator, and normalised business errors.

---

## 2. Resumen de la tarea

Implementación de las tareas 5-8 del backlog de mejora de definición de reglas (P1):

| Tarea | Descripción |
|-------|-------------|
| 5 | Introducir modelo explícito de capacidad de regla (Value Object con resolver, operadores permitidos y roles sujeto/objetivo) |
| 6 | Refactorizar `RuleEvaluator` para consumir capacidades del catálogo en lugar de `switch` con cadenas mágicas |
| 7 | Normalizar errores de negocio: reemplazar fallos silenciosos por `RuleNotEvaluableException` con mensaje accionable |
| 8 | Tests de regresión P1: `RuleEvaluatorP1Test` y `RuleCapabilityP1Test` |

---

## 3. Código generado

### 3.1. Nueva interfaz funcional `IndicatorResolver`

**Fichero:** `domain/model/IndicatorResolver.java`

```java
@FunctionalInterface
public interface IndicatorResolver {
    BigDecimal resolve(Double param, Stock stock);
}
```

Encapsula la forma en que cada indicador extrae su valor de `Stock`. Se registra en `RuleCapabilityCatalog` como referencia a método estático privado o lambda inline.

---

### 3.2. Nueva excepción de dominio `RuleNotEvaluableException`

**Fichero:** `domain/exception/RuleNotEvaluableException.java`

Lanzada cuando:
- El código de indicador no está registrado en el catálogo durante la evaluación.
- El operador no es soportado durante la evaluación.

Distingue el error de programación/configuración (código inválido en el catálogo) del fallo legítimo de negocio (dato de stock no disponible, que sigue retornando `FAILED: Missing …`).

---

### 3.3. `RuleCapability` extendida (Task 5)

**Fichero:** `domain/model/RuleCapability.java`

Nuevos campos y métodos añadidos:

| Campo / Método | Tipo | Descripción |
|----------------|------|-------------|
| `resolver` | `IndicatorResolver` | Resuelve el valor del indicador desde `Stock` |
| `allowedOperators` | `Set<String>` | Operadores válidos para este indicador |
| `subjectAllowed` | `boolean` | Puede usarse como sujeto de la regla |
| `targetAllowed` | `boolean` | Puede usarse como objetivo de la regla |
| `resolve(param, stock)` | `BigDecimal` | Delega al resolver registrado |
| `isOperatorAllowed(op)` | `boolean` | Comprueba si el operador está permitido |
| `isSubjectAllowed()` | `boolean` | Rol sujeto permitido |
| `isTargetAllowed()` | `boolean` | Rol objetivo permitido |

Los métodos factory (`noParam`, `withAllowedParams`, `anyParam`) se actualizaron para recibir resolver, operadores y roles.

---

### 3.4. `RuleCapabilityCatalog` actualizado (Task 5 + 6)

**Fichero:** `domain/model/RuleCapabilityCatalog.java`

- Cada entrada del mapa `CAPABILITIES` ahora incluye su `IndicatorResolver` (lambdas inline o referencias a métodos privados estáticos: `resolveSma`, `resolveEma`, `resolveRsi`).
- Los resolvers de BB_UPPER, BB_LOWER y ATR comprueban el periodo; devuelven `null` para periodos no soportados (manteniendo el comportamiento "FAILED: Missing data").
- Nuevos métodos públicos estáticos: `isSubjectAllowed(code)`, `isTargetAllowed(code)`.

---

### 3.5. `RuleEvaluator` refactorizado (Task 6 + 7)

**Fichero:** `domain/service/RuleEvaluator.java`

Cambios clave:

- El método `getIndicatorValue(...)` (switch de cadenas mágicas) se sustituye por `resolveIndicator(...)` que delega al catálogo:

  ```java
  private BigDecimal resolveIndicator(String indicatorCode, Double param, Stock stock) {
      RuleCapability cap = RuleCapabilityCatalog.getCapability(indicatorCode)
              .orElseThrow(() -> new RuleNotEvaluableException(...));
      return cap.resolve(param, stock);
  }
  ```

- El método `evaluateOperator(...)` ahora lanza `RuleNotEvaluableException` en lugar de retornar `false` para operadores no soportados.
- Eliminados los métodos privados `getSmaValue`, `getEmaValue`, `getRsiValue`, `getBbValue`, `getAtrValue` — su lógica vive ahora en los resolvers del catálogo.

---

### 3.6. `Rule.validate()` actualizado (Task 5 – roles)

**Fichero:** `domain/model/Rule.java`

`validate()` ahora también comprueba los roles sujeto/objetivo vía catálogo:

```java
private void validateIndicatorAsSubject(String code, Double param) {
    validateIndicator("subject", code, param);
    if (!RuleCapabilityCatalog.isSubjectAllowed(code)) {
        throw new IllegalArgumentException("Indicator '" + code + "' is not allowed as a rule subject.");
    }
}
```

---

## 4. Decisiones técnicas

### Por qué `Optional.orElseThrow` en lugar de `Optional.map(...).orElseThrow`

`Optional.map(f)` devuelve `Optional.empty()` cuando `f.apply(v)` retorna `null`. Esto provocaba que un código válido en el catálogo (p.ej. `RSI`) pero con dato ausente en el stock lanzara incorrectamente `RuleNotEvaluableException`. La solución es obtener primero el capability y luego llamar al resolver directamente:

```java
RuleCapability cap = RuleCapabilityCatalog.getCapability(code).orElseThrow(...);
return cap.resolve(param, stock); // puede retornar null → FAILED: Missing data
```

### Resolvers de BB/ATR comprueban periodo

Los resolvers de `BB_UPPER`, `BB_LOWER` y `ATR` comprueban explícitamente el periodo en el lambda (p.ej. `param != null && param.intValue() == 14`). Sin esta comprobación, un periodo no soportado como `ATR(30)` devolvería el valor de `atr14`, produciendo un resultado incorrecto en lugar del comportamiento esperado "FAILED: Missing".

### Roles sujeto/objetivo

Actualmente todos los indicadores tienen `subjectAllowed=true, targetAllowed=true`. El modelo soporta restricciones futuras sin cambios estructurales.

---

## 5. Cobertura de tests y pruebas añadidas

### `RuleEvaluatorP1Test` (nuevo)

- Verifica que códigos no soportados lanzan `RuleNotEvaluableException`.
- Verifica que operadores no soportados lanzan `RuleNotEvaluableException`.
- Verifica que datos ausentes en stock producen `FAILED: Missing` (sin excepción).
- Verifica resolución correcta de todos los indicadores del catálogo.
- Verifica todos los operadores soportados mediante `@ParameterizedTest`.

### `RuleCapabilityP1Test` (nuevo)

- Verifica que cada resolver del catálogo retorna el campo correcto de `Stock`.
- Verifica `isOperatorAllowed()` para operadores válidos e inválidos.
- Verifica `isSubjectAllowed()` / `isTargetAllowed()` para todos los indicadores.
- Verifica `RuleCapabilityCatalog.isSubjectAllowed()` / `isTargetAllowed()` helpers.
- Verifica que `Rule.validate()` respeta las restricciones de rol.

**Resultado:** 931 tests, 0 fallos, 0 errores.

---

## 6. Advertencias de arquitectura

- **SRP**: `RuleCapabilityCatalog` concentra tanto la validación de parámetros como la resolución de valores. Esto podría dividirse en dos responsabilidades en una fase futura si el catálogo crece sustancialmente.
- **Compatibilidad hacia atrás**: Los métodos factory de `RuleCapability` tienen nuevos parámetros obligatorios. El único consumidor es `RuleCapabilityCatalog` (clase de dominio interna), por lo que no hay impacto externo.

---

## 7. Próximos pasos sugeridos (P2)

- **Tarea 9** – UI guiada por capacidades: filtrar selects en el formulario para que solo muestre combinaciones válidas (requires API endpoint to expose catalog).
- **Tarea 10** – Migración de datos existentes.
- **Tarea 11** – Observabilidad: logging estructurado de rechazos de validación.
- **Tarea 12** – Checklist de release.
