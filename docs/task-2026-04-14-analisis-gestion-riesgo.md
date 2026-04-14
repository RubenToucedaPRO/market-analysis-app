# Task: Análisis y Corrección del Área de Gestión de Riesgo

**Fecha:** 2026-04-14  
**Slug:** analisis-gestion-riesgo  

---

## Resumen

Análisis exhaustivo del área de gestión de riesgo en la creación/edición de estrategias. Se identificó y corrigió un bug crítico: cuando el tipo de objetivo (`targetType`) o stop-loss (`stopLossType`) es `SMA`, el valor asociado debía restringirse a los períodos predefinidos 20, 50 o 200, pero la validación en `StrategyObjective.validate()` solo verificaba que fuera positivo.

---

## Código Generado / Modificado

### 1. `StrategyObjective.java` (modificado)

- **Añadido:** Constante `ALLOWED_SMA_PERIODS = Set.of(20, 50, 200)`
- **Añadido:** Método privado `validateSmaValue(ObjectiveType, BigDecimal, String)` que valida que el valor sea uno de los períodos permitidos cuando el tipo es SMA.
- **Modificado:** `validate()` invoca `validateSmaValue()` para `targetValue` y `stopLossValue` al final de la validación.

### 2. `StrategyObjectiveTest.java` (modificado)

- **Añadidos 8 tests** cubriendo validación de períodos SMA válidos (20, 50, 200) e inválidos (10, 100).

### 3. `create.html` (modificado)

- **Añadido:** `<select>` con opciones SMA 20/50/200 para campos `targetValue` y `stopLossValue`, que se muestra solo cuando el tipo seleccionado es SMA.
- **Añadido:** `onchange` en los selects de tipo para invocar `toggleObjectiveValueField()`.

### 4. `strategy-manager.js` (modificado)

- **Añadida:** Función `toggleObjectiveValueField(field)` que alterna entre dropdown SMA e input libre.
- **Modificado:** `DOMContentLoaded` inicializa la visibilidad correcta.

### 5. `gestion_riesgo.md` (nuevo)

- Documento de análisis completo con errores identificados, mejoras propuestas y plan de implementación en 4 fases.

---

## Decisiones Técnicas

| Decisión | Justificación |
|---|---|
| Validar SMA en `StrategyObjective.validate()` | Fail-fast en dominio; coherente con `RiskRewardCalculator.resolveSmaValue()` |
| `Set<Integer>` estático inmutable | Constante clara y eficiente para verificar períodos permitidos |
| Toggle JS entre dropdown y input | UX mejorada sin duplicar formularios; un solo template |
| Deshabilitar campo inactivo + eliminar `name` | Previene envío de valores duplicados al servidor |

---

## Cobertura de Tests

### `StrategyObjectiveTest.java`
- ✅ 28 tests (20 existentes + 8 nuevos)
- ✅ `shouldThrowExceptionWhenSmaTargetValueIsInvalid`
- ✅ `shouldThrowExceptionWhenSmaStopLossValueIsInvalid`
- ✅ `shouldAcceptSmaTargetValue20`
- ✅ `shouldAcceptSmaTargetValue50`
- ✅ `shouldAcceptSmaTargetValue200`
- ✅ `shouldAcceptSmaStopLossValidValues`
- ✅ `shouldRejectSmaTargetValue10`

---

## Advertencias de SonarQube o Arquitectura

- Sin advertencias. Cambios respetan Clean Architecture y SRP.
- Validación en dominio puro, sin dependencias de infraestructura.
- `Set.of()` inmutable garantiza thread-safety.

---

## Próximos Pasos Sugeridos

1. **Fase 2:** Implementar validaciones cruzadas en `StrategyObjective` (target > stop-loss para FIXED_PRICE, rango de PERCENTAGE).
2. **Fase 3:** Mejorar UX con textos de ayuda dinámicos y validación client-side.
3. **Fase 4:** Migrar tipos a `@Enumerated` en JPA e internacionalizar mensajes.

Referencia completa en `docs/gestion_riesgo.md`.
