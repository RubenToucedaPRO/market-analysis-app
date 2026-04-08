# Definición de reglas: análisis actual y plan de mejora por fases

Fecha: 2026-04-08

## 1. Objetivo

Documentar cómo está resuelta hoy la definición de reglas en la aplicación, cuál es el defecto principal observado y qué plan de mejora conviene aplicar por fases para eliminar el acoplamiento entre la definición persistida y las capacidades reales del programa.

## 2. Estado actual

Hoy existen dos conceptos relacionados pero no plenamente alineados:

1. `RuleDefinition`: se usa como catálogo visible y persistido de reglas disponibles.
2. `Rule`: representa la regla ejecutable que termina evaluando el motor técnico.

El problema es que el catálogo editable por el usuario no está cerrado contra una fuente de verdad única del dominio. En la práctica, la validación es débil y se reparten responsabilidades entre persistencia, formulario y evaluador.

### 2.1. Qué valida hoy el sistema

- La creación y edición de definiciones solo comprueba que el código no esté vacío y que no exista duplicado en base de datos.
- La vista de estrategias carga las definiciones persistidas para poblar los selects.
- El evaluador interpreta los códigos con un `switch` de cadenas y devuelve `null` si el código o el parámetro no encajan con lo que espera.

### 2.2. Dónde está el defecto

El defecto no es solo de UI, sino de modelo:

- Se puede persistir una definición con un `code` que el motor no reconoce.
- Se puede declarar una regla que requiera parámetros sin restringir el dominio de valores válidos.
- Se puede exponer en la interfaz una definición que no tiene implementación real en el evaluador.
- Se puede crear una estrategia con combinaciones que pasan el formulario pero fallan al ejecutarse.

## 3. Lectura técnica del problema

### 3.1. Separación incompleta entre catálogo y ejecución

La clase de dominio `RuleDefinition` describe metadatos, pero no expresa con suficiente precisión:

- qué indicadores existen de verdad,
- qué parámetros acepta cada indicador,
- qué operadores son válidos para cada combinación,
- qué pares sujeto/objetivo son coherentes.

Eso deja a la persistencia almacenar combinaciones que luego el evaluador no puede resolver.

### 3.2. El evaluador está codificado por casos concretos

`RuleEvaluator` depende de códigos literales como `SMA`, `EMA`, `RSI`, `VOLUME`, `CONSTANT`, etc. Además, para algunos indicadores acepta solo un conjunto fijo de parámetros:

- `SMA`: 20, 50, 200
- `EMA`: 9, 12, 20, 26, 50, 200
- `RSI`: 14, 30
- `BB`: 20
- `ATR`: 14

Esto significa que la validez real de una definición no vive en `RuleDefinition`, sino dispersa en el código del evaluador.

### 3.3. La UI ayuda, pero no garantiza

La pantalla de creación de estrategias consume las definiciones persistidas y las muestra en los select. Eso mejora la experiencia, pero no evita el problema de fondo:

- si el catálogo persistido contiene algo inválido, la UI lo ofrecerá,
- si falta una definición persistida para una capacidad soportada, la UI no la mostrará,
- si el evaluador cambia y la definición no se actualiza, se rompe la coherencia.

## 4. Riesgos actuales

- Fallos en tiempo de ejecución por códigos no implementados.
- Estrategias aparentemente válidas que terminan con reglas no evaluables.
- Incoherencia entre frontend, persistencia y motor de evaluación.
- Mantenimiento caro: cada nuevo indicador exige tocar varios puntos sin una garantía centralizada.

## 5. Principio de solución

La mejora debe introducir una única fuente de verdad para las capacidades de regla.

Esa fuente de verdad debe definir, como mínimo:

- código de regla soportado,
- nombre visible,
- si requiere parámetro,
- dominio de parámetros permitidos,
- tipo de rol permitido como sujeto u objetivo,
- operadores permitidos,
- si la combinación es evaluable por el motor.

La persistencia debe convertirse en una representación de esa capacidad, no en una libreta de texto editable sin validación estructural.

## 6. Plan de mejora por fases

### Fase 0. Inventario y catálogo canónico

Objetivo: dejar identificadas todas las capacidades reales soportadas por el motor y convertirlas en un catálogo explícito.

Acciones:

- Listar todos los códigos aceptados hoy por `RuleEvaluator`.
- Listar todos los parámetros permitidos por indicador.
- Listar qué operadores son válidos para cada caso.
- Decidir qué reglas son realmente configurables por usuario y cuáles son internas o derivadas.

Resultado esperado:

- Catálogo único de capacidades técnicas de regla.
- Base documental para validar que la interfaz no ofrece combinaciones imposibles.

### Fase 1. Validación dura en creación y edición

Objetivo: evitar que entren definiciones imposibles en el sistema.

Acciones:

- Validar que el `code` exista en el catálogo canónico antes de guardar.
- Validar que `requiresParam` sea coherente con la capacidad real.
- Validar que, si la regla requiere parámetro, el valor o dominio de parámetros permitido esté definido.
- Rechazar la creación o actualización con mensajes claros de negocio.

Resultado esperado:

- No se persisten definiciones que el motor no puede ejecutar.
- Los errores aparecen al guardar, no en ejecución.

### Fase 2. Modelado explícito de capacidad de regla

Objetivo: dejar de depender de `switch` sueltos y llevar las restricciones al dominio.

Acciones:

- Introducir una estructura de dominio para describir capacidades de regla.
- Representar ahí parámetros válidos, necesidad de parámetro y operadores soportados.
- Hacer que `RuleDefinition` se construya a partir de esa capacidad, no al revés.
- Separar la definición editable del concepto de capacidad técnica real.

Resultado esperado:

- El dominio conoce qué es una regla válida sin depender de infraestructura.
- La UI y los casos de uso pueden consultar el mismo modelo.

### Fase 3. Reescritura del evaluador sobre catálogo de capacidades

Objetivo: hacer que la evaluación use el mismo catálogo que la definición.

Acciones:

- Sustituir progresivamente los `switch` literales por resolutores o estrategias por indicador.
- Centralizar la lectura de valores técnicos según capacidad declarada.
- Devolver errores de validación más precisos cuando una combinación no sea soportada.
- Mantener la lógica determinista y sin dependencia de IA.

Resultado esperado:

- Menos código frágil con cadenas mágicas.
- Evaluación alineada con la definición.

### Fase 4. Endurecimiento de la UI y experiencia de autoría

Objetivo: evitar que el usuario pueda construir combinaciones que el dominio ya sabe que son inválidas.

Acciones:

- Filtrar en el formulario solo las reglas permitidas para el contexto elegido.
- Ocultar o deshabilitar parámetros cuando el indicador no los soporte.
- Mostrar mensajes de validación claros antes del envío.
- Añadir ayudas contextuales para los valores válidos de cada regla.

Resultado esperado:

- Menos fricción al crear estrategias.
- Menos errores de validación en backend.

### Fase 5. Migración y limpieza técnica

Objetivo: cerrar la transición sin dejar deuda estructural.

Acciones:

- Migrar definiciones existentes a la nueva validación.
- Revisar registros persistidos que contengan códigos obsoletos o ambiguos.
- Eliminar rutas de código duplicadas o ya reemplazadas.
- Actualizar tests y documentación.

Resultado esperado:

- El sistema queda coherente de extremo a extremo.
- La definición de reglas deja de depender del estado accidental del código.

## 7. Criterios de éxito

La mejora estará bien cerrada cuando se cumplan estas condiciones:

- No se pueda guardar una `RuleDefinition` con un `code` inexistente en el catálogo canónico.
- No se pueda declarar un parámetro fuera del dominio permitido por el indicador.
- La UI no ofrezca combinaciones que el motor no puede evaluar.
- El evaluador y la definición compartan la misma fuente de verdad.
- Los tests cubran tanto reglas válidas como casos inválidos rechazados por validación.

## 8. Recomendación práctica

Si se quiere reducir riesgo, el orden más seguro es:

1. Fase 0 para inventariar capacidades reales.
2. Fase 1 para bloquear entradas inválidas.
3. Fase 2 para modelar la capacidad de forma explícita.
4. Fase 3 para alinear el evaluador.
5. Fase 4 para pulir la UI.
6. Fase 5 para limpiar y migrar datos existentes.

Ese orden permite corregir el problema sin romper la evaluación actual mientras se hace la transición.

## 9. Backlog técnico priorizado

### P0. Blindaje mínimo para cortar fallos en runtime

1. Crear un catálogo canónico inicial en dominio (solo lectura)
- Qué hacer: centralizar códigos válidos, necesidad de parámetro y parámetros permitidos por indicador.
- Dependencias: ninguna.
- Criterio de cierre: existe un único punto de consulta para capacidades soportadas.

2. Validar alta y edición de `RuleDefinition` contra el catálogo canónico
- Qué hacer: en el caso de uso de gestión de definiciones, rechazar códigos no soportados y combinaciones incoherentes (`requiresParam` inconsistente).
- Dependencias: tarea 1.
- Criterio de cierre: no se puede persistir una definición que no exista en capacidades reales.

3. Endurecer validación en creación de estrategia
- Qué hacer: validar cada `Rule` (subjectCode, targetCode, subjectParam, targetParam, operator) antes de guardar.
- Dependencias: tarea 1.
- Criterio de cierre: no entra ninguna estrategia con reglas no evaluables.

4. Tests de regresión P0
- Qué hacer: añadir tests unitarios para casos válidos e inválidos en servicios de gestión y creación de estrategia.
- Dependencias: tareas 2 y 3.
- Criterio de cierre: cobertura de ramas en validación crítica y evidencia de rechazo temprano.

### P1. Alineación de modelo y evaluador

5. Introducir modelo explícito de capacidad de regla
- Qué hacer: crear Value Object o agregado de capacidad (código, parámetros permitidos, operadores permitidos, roles sujeto/objetivo).
- Dependencias: tarea 1.
- Criterio de cierre: reglas de validez viven en dominio y no en condicionales dispersos.

6. Refactor del `RuleEvaluator` para consumir capacidades
- Qué hacer: encapsular resolución por indicador con estrategia/resolutor y eliminar dependencia directa de cadenas mágicas.
- Dependencias: tarea 5.
- Criterio de cierre: el evaluador usa el catálogo de capacidades como contrato de ejecución.

7. Normalización de errores de negocio
- Qué hacer: reemplazar fallos silenciosos por excepciones o resultados explícitos de validación con mensaje accionable.
- Dependencias: tareas 5 y 6.
- Criterio de cierre: errores consistentes en aplicación y presentación.

8. Tests de regresión P1
- Qué hacer: ampliar tests de `RuleEvaluator` para cubrir catálogos, límites de parámetros y operadores no permitidos.
- Dependencias: tareas 6 y 7.
- Criterio de cierre: pruebas de dominio cubren caminos felices y negativos de capacidad.

### P2. UX y migración segura

9. UI guiada por capacidades
- Qué hacer: filtrar selects y parámetros en formulario para que solo muestre combinaciones válidas.
- Dependencias: tareas 5 y 7.
- Criterio de cierre: frontend no permite construir reglas inválidas por diseño.

10. Migración de datos existentes
- Qué hacer: script de saneamiento para `rule_definitions` y estrategias existentes, marcando o corrigiendo registros no compatibles.
- Dependencias: tareas 2, 3 y 5.
- Criterio de cierre: dataset vigente consistente con catálogo canónico.

11. Observabilidad y trazabilidad
- Qué hacer: añadir logging estructurado de rechazos de validación y métricas de error por tipo.
- Dependencias: tareas 2, 3 y 7.
- Criterio de cierre: diagnóstico rápido de causas de rechazo sin inspección manual profunda.

12. Checklist de release
- Qué hacer: ejecutar suite completa, validar cobertura, revisar SonarQube y actualizar documentación funcional/técnica.
- Dependencias: tareas 4, 8, 10 y 11.
- Criterio de cierre: release candidate sin deuda crítica en reglas.

## 10. Orden recomendado de implementación (iteraciones)

Iteración A (estabilidad inmediata): 1, 2, 3, 4

Iteración B (alineación de arquitectura): 5, 6, 7, 8

Iteración C (adopción y cierre): 9, 10, 11, 12