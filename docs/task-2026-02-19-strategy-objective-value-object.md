# Task: Add StrategyObjective Value Object and ObjectiveType Enum

**Fecha:** 2026-02-19  
**Branch:** `copilot/add-strategy-objective-value-object`  
**PR:** Add StrategyObjective Value Object and ObjectiveType enum

## Resumen de la Tarea

Se ha implementado un nuevo Value Object `StrategyObjective` y el enum `ObjectiveType` en el paquete `com.market.analysis.domain.model` para representar los objetivos y parámetros de gestión de riesgo de una estrategia de trading.

## Código Generado

### 1. ObjectiveType Enum

**Archivo:** `src/main/java/com/market/analysis/domain/model/ObjectiveType.java`

```java
package com.market.analysis.domain.model;

/**
 * Enum representing the types of objectives for strategy targets and stop-loss levels.
 * Used to define how target and stop-loss values are calculated in a trading strategy.
 */
public enum ObjectiveType {
    
    /**
     * Simple Moving Average - based on SMA calculation.
     */
    SMA,
    
    /**
     * Percentage - based on percentage change.
     */
    PERCENTAGE,
    
    /**
     * Fixed Price - based on absolute price value.
     */
    FIXED_PRICE
}
```

**Características:**
- Tres valores: `SMA`, `PERCENTAGE`, `FIXED_PRICE`
- Documentación JavaDoc clara para cada valor
- Sin dependencias externas

### 2. StrategyObjective Value Object

**Archivo:** `src/main/java/com/market/analysis/domain/model/StrategyObjective.java`

**Campos:**
- `targetType`: ObjectiveType - Tipo de cálculo para el nivel objetivo
- `stopLossType`: ObjectiveType - Tipo de cálculo para el stop-loss
- `targetValue`: BigDecimal - Valor objetivo (debe ser > 0)
- `stopLossValue`: BigDecimal - Valor de stop-loss (debe ser > 0)
- `capitalToRisk`: BigDecimal - Capital a arriesgar (debe ser > 0)
- `description`: String - Descripción legible del objetivo

**Anotaciones Lombok:**
- `@Getter` - Getters inmutables
- `@Builder` - Patrón Builder para construcción
- `@ToString` - Implementación de toString()

**Método de Validación:**
```java
public void validate() {
    // Validación de campos no nulos
    if (targetType == null) throw new IllegalArgumentException("targetType cannot be null");
    if (stopLossType == null) throw new IllegalArgumentException("stopLossType cannot be null");
    if (targetValue == null) throw new IllegalArgumentException("targetValue cannot be null");
    if (stopLossValue == null) throw new IllegalArgumentException("stopLossValue cannot be null");
    if (capitalToRisk == null) throw new IllegalArgumentException("capitalToRisk cannot be null");
    if (description == null || description.isBlank()) 
        throw new IllegalArgumentException("description cannot be null or blank");
    
    // Validación de valores > 0
    if (targetValue.compareTo(BigDecimal.ZERO) <= 0)
        throw new IllegalArgumentException("targetValue must be greater than zero");
    if (stopLossValue.compareTo(BigDecimal.ZERO) <= 0)
        throw new IllegalArgumentException("stopLossValue must be greater than zero");
    if (capitalToRisk.compareTo(BigDecimal.ZERO) <= 0)
        throw new IllegalArgumentException("capitalToRisk must be greater than zero");
}
```

## Decisiones Técnicas

### 1. Arquitectura Limpia y Pureza de Dominio
- **Sin anotaciones de infraestructura:** No se utilizan anotaciones JPA, Hibernate o Jackson
- **Value Object inmutable:** Todos los campos son `final`
- **Autocontenido:** No depende de servicios externos
- **Validación en dominio:** Lógica de validación dentro del propio objeto

### 2. Precisión Financiera
- **BigDecimal para valores monetarios:** Evita errores de coma flotante en cálculos financieros
- **Validación estricta:** Todos los valores BigDecimal deben ser > 0
- **Tipos específicos:** ObjectiveType proporciona claridad semántica

### 3. Patrones de Diseño
- **Builder Pattern:** Construcción flexible mediante Lombok @Builder
- **Value Object Pattern:** Inmutabilidad completa con campos final
- **Validation Pattern:** Método validate() para garantizar consistencia

### 4. Consistencia con el Código Existente
- Sigue el mismo patrón que `Candle`, `RuleResult`, y `HealthStatus`
- Documentación JavaDoc completa y descriptiva
- Uso consistente de Lombok annotations

## Cobertura de Tests

Se han creado **26 tests unitarios** que cubren:

### ObjectiveTypeTest (5 tests)
- ✅ Verificación de cada valor del enum
- ✅ Listado de todos los valores
- ✅ Parsing desde String

### StrategyObjectiveTest (21 tests)
- ✅ Creación válida con todos los campos
- ✅ Creación con diferentes tipos de ObjectiveType (SMA, PERCENTAGE, FIXED_PRICE)
- ✅ Creación con tipos mixtos de objetivos
- ✅ Validación de targetType nulo
- ✅ Validación de stopLossType nulo
- ✅ Validación de targetValue nulo
- ✅ Validación de stopLossValue nulo
- ✅ Validación de capitalToRisk nulo
- ✅ Validación de description nula o vacía
- ✅ Validación de targetValue = 0
- ✅ Validación de targetValue < 0
- ✅ Validación de stopLossValue = 0
- ✅ Validación de stopLossValue < 0
- ✅ Validación de capitalToRisk = 0
- ✅ Validación de capitalToRisk < 0
- ✅ Verificación de inmutabilidad
- ✅ Verificación de toString()
- ✅ Valores BigDecimal muy pequeños (0.0001)
- ✅ Valores BigDecimal muy grandes (1,000,000)

**Resultado:** 26/26 tests pasados ✅

## Advertencias de SonarQube y Arquitectura

### ✅ Cumplimiento
- **Seguridad:** Sin vulnerabilidades detectadas (CodeQL: 0 alertas)
- **Mantenibilidad:** Código simple y bien estructurado
- **Fiabilidad:** Tests exhaustivos garantizan comportamiento correcto
- **Cobertura:** JaCoCo excluye domain model según configuración (línea 165 pom.xml)

### ✅ Patrones Respetados
- **Strategy Pattern:** El enum permite composición de estrategias
- **Arquitectura Hexagonal:** Dominio puro sin dependencias de infraestructura
- **SRP:** Cada clase tiene una única responsabilidad clara
- **DIP:** No hay dependencias de bajo nivel

### ✅ Buenas Prácticas
- Inyección por constructor (no aplica, es Value Object)
- Lógica en capa de dominio
- Sin números mágicos
- Logging no necesario (Value Object sin comportamiento)
- Recursos cerrados correctamente (no aplica)

## Resultado de Compilación

```bash
mvn clean compile -DskipTests
# ✅ BUILD SUCCESS

mvn test -Dtest="ObjectiveTypeTest,StrategyObjectiveTest"
# ✅ Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
```

## Seguridad

**CodeQL Analysis:** ✅ No alerts found

El análisis de seguridad no encontró vulnerabilidades en:
- ObjectiveType.java
- StrategyObjective.java

## Próximos Pasos Sugeridos

1. **Integración con Strategy:** Añadir campo `StrategyObjective` a la clase `Strategy`
2. **Persistencia:** Crear entidad JPA y mapper en la capa de infraestructura
3. **Casos de Uso:** Implementar servicios para gestionar objetivos de estrategia
4. **Validación de Negocio:** Añadir reglas de negocio adicionales (ej: stopLoss < target)
5. **UI:** Crear vistas para configurar objetivos de estrategia

## Notas Finales

Esta implementación cumple con todos los requisitos especificados:
- ✅ Value Object inmutable con campos final
- ✅ Enum ObjectiveType con valores SMA, PERCENTAGE, FIXED_PRICE
- ✅ Lombok annotations (@Getter, @ToString, @Builder)
- ✅ Validación completa en método validate()
- ✅ Pureza de dominio (sin anotaciones de infraestructura)
- ✅ BigDecimal para precisión financiera
- ✅ Encapsulamiento y autocontención
- ✅ Cobertura de tests exhaustiva
- ✅ Sin vulnerabilidades de seguridad

El código es **production-ready** y sigue todas las convenciones del proyecto.
