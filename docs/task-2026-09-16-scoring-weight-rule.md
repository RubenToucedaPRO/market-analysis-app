# Scoring (a): peso en Rule + migración BD

- Fecha: 2026-09-16
- Slug: `scoring-weight-rule`
- Alcance: `Rule` (dominio), `RuleEntity`, `RuleMapper`, `script-bd.sql` + tests
- Parte 1 de 4 del scoring ponderado (plan TFM Día 1 PM)

## Resumen

Primer paso del scoring 0-100 (opción B del plan): cada regla lleva un `weight`
(importancia relativa, entero ≥ 1, por defecto 1). Esta PR solo crea la capacidad:
dominio + persistencia + migración. **No cambia ningún comportamiento**: con todo
a peso 1 y sin `threshold` aún (parte b), la evaluación sigue siendo AND puro.

## Código generado

### 1. `domain/model/Rule.java`

- Nuevo `weight` (`int`, `@Builder.Default = 1`): los 18 ficheros de test que usan
  `Rule.builder()` sin peso siguen compilando y valiendo 1.
- `validate()` rechaza `weight < 1` con `IllegalArgumentException` (mismo estilo
  que el resto de validaciones; se ejecuta vía `Strategy.validateConsistency()`).

### 2. `infrastructure/persistence/entity/RuleEntity.java`

- Nueva columna `weight` (`Integer`, nullable): las filas antiguas (valor NULL)
  se leen como peso 1, sin migración de datos.

### 3. `infrastructure/persistence/mapper/RuleMapper.java`

- `toEntity`: copia el peso. `toDomain`: `null` → 1 (filas legacy).

### 4. `config/database/script-bd.sql`

- `rules.weight int DEFAULT 1` en el `CREATE TABLE` (BDs nuevas, incluido Railway el Día 3).

## Decisiones técnicas

## Decisiones técnicas

- `int` primitivo en dominio (siempre definido, default 1) frente a `Integer`
  nullable en entidad (tolera filas legacy). La frontera la resuelve el mapper.
- La validación vive en `validate()`, no en el constructor: sigue el patrón
  existente de `Rule` (builder + validación explícita).
- `RuleDTO` y formulario quedan para la parte (c): aún nada escribe pesos, así
  que todo persiste con el default y el comportamiento es idéntico.
- Sin cambios en `RuleEvaluator` ni `EvaluateStrategyService` (partes b/c).

## Cobertura de tests

- `RuleValidateTest` (+4): default 1, peso 3 válido, 0 y negativo rechazados.
- `RuleMapperTest` (+2): ida y vuelta con peso 3; entidad con `weight` NULL → 1.
- Ejecución: `mvn test` → `Tests run: 1036, Failures: 0, Errors: 0, BUILD SUCCESS`.
- Sin `lenient`; Mockito estricto en verde.

## Advertencias de SonarQube o arquitectura

- Dominio puro: sin imports de Spring/JPA en `Rule`; Lombok ya usado en el proyecto.
- `@Builder.Default` documentado; complejidad trivial, anidamiento < 4.
- Revisar en Sonar: nada nuevo esperable (1 campo + 1 `if`).
- **Gestión de esquema (hallazgo)**: el contenedor Docker solo lleva `app.jar`, sin
  `config/`; por tanto `config/application-docker.properties` (`ddl-auto=update`)
  **no se carga** y en Docker no hay migración automática (verificado: la columna
  `weight` hubo que crearla a mano con `ALTER TABLE rules ADD COLUMN weight INT
  DEFAULT 1`). Tests usan `create-drop` (sin problema). Para Railway (Día 3): las
  BDs nuevas se crean desde `script-bd.sql` (ya incluye `weight`), pero cualquier
  cambio futuro de esquema en una BD con datos exigirá ALTER manual.

## Próximos pasos sugeridos

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Ve a `/strategies` → la estrategia "Precio superior a 20" sigue ahí con sus
  tickers (AAPL, PL, MRAM, BNAI, ZIM): la migración no borró ni rompió datos.
  (Verificado en BD: las 3 reglas existentes tienen `weight = 1` por defecto.)
2. Abre esa estrategia y pulsa Guardar sin cambiar nada → caja VERDE de
  éxito, igual que antes (el peso aún no afecta a nada: todo vale 1).

### B. Ideas futuras (NO hacer ahora, son las partes b/c/d)

- (b) `threshold` en `Strategy` + score 0-100 en `EvaluateStrategyService`.
- (c) Campo peso en `RuleDTO` + formulario, y mostrar el score en vistas.
- (d) Tests de scoring + JaCoCo verify.
- Llevar `config/*.properties` a la imagen Docker (o montar `config/` como volumen)
  para que `ddl-auto` y el resto de perfiles apliquen también en contenedor.
