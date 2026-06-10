# Tarea: Extraccion del bloque de trazabilidad de estrategia a fragmento Thymeleaf

## Resumen de la tarea
Se extrajo el bloque de trazabilidad de sugerencias de la vista de detalle de estrategia a un fragmento reutilizable de Thymeleaf.

## Codigo generado
Archivo nuevo:
- `src/main/resources/templates/fragments/strategy-traceability.html`

Archivo actualizado:
- `src/main/resources/templates/strategies/detail.html`

## Detalles del fragmento
El fragmento se definio como:
- `strategyTraceability(suggestedTickers, discardedTickers, unmappableRules)`

Incluye:
- lista de sugeridos
- lista de descartes
- listado de reglas no mapeables
- estado vacio para cada seccion

## Decisiones tecnicas
- Se mantuvo la condicion de visibilidad en el propio fragmento para que la vista de detalle quede mas limpia.
- Se reutilizo el mismo markup y las mismas ids para no romper estilos, scripts o tests existentes.
- Se siguio el patron de fragmentos ya existente en el proyecto.

## Validacion
Se ejecuto comprobacion de errores sobre:
- `src/main/resources/templates/strategies/detail.html`
- `src/main/resources/templates/fragments/strategy-traceability.html`

Resultado:
- No se encontraron errores.

## Proximos pasos sugeridos
1. Si se reutiliza este bloque en otras vistas, usar el mismo fragmento para evitar duplicacion.
2. Si se quiere simplificar aun mas, se puede separar cada subbloque en microfragmentos, pero no parece necesario por ahora.
