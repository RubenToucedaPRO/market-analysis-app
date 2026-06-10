# Extracción de subfragmento reutilizable para parámetros de regla

## Resumen
Se extrajo el bloque repetido de parámetro de sujeto/objetivo a un subfragmento Thymeleaf reutilizable para reducir duplicación en la plantilla de reglas.

El fragmento principal `rule-row.html` ahora delega el renderizado del bloque de parámetros a `rule-parameter-field.html`, manteniendo el comportamiento anterior:
- el contenedor sigue oculto por defecto
- el script sigue decidiendo si se muestra `select` o `input`
- el valor actual se conserva al alternar entre controles

## Código modificado
- [src/main/resources/templates/fragments/rule-row.html](../src/main/resources/templates/fragments/rule-row.html)
- [src/main/resources/templates/fragments/rule-parameter-field.html](../src/main/resources/templates/fragments/rule-parameter-field.html)

## Decisiones técnicas
- Se creó un subfragmento genérico con parámetros explícitos para `containerId`, `selectId`, `inputId`, `fieldName`, `label` y `value`.
- Se evitó reintroducir lógica condicional compleja en Thymeleaf.
- La decisión de visibilidad y tipo de control sigue en JavaScript, que ya está centralizando el comportamiento dinámico.

## Cobertura de tests
- Se ejecutaron los tests de vista y controlador relacionados con estrategias.
- Resultado de validación: `12` tests ejecutados, `0` fallos.

## Advertencias de SonarQube o arquitectura
- Se redujo la duplicación de markup en la capa de presentación.
- El refactor mantiene la separación de responsabilidades: Thymeleaf renderiza y JavaScript orquesta la interacción.
- No se detectaron errores de compilación en la plantilla nueva ni en el script.

## Próximos pasos sugeridos
- Reutilizar `rule-parameter-field.html` en otras vistas que necesiten el mismo patrón de entrada condicional.
- Si se desea una plantilla aún más compacta, evaluar extraer también el selector de indicador a un fragmento común.
