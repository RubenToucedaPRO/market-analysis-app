# Notificacion de error y fallback visible en valoracion IA

## Resumen de la tarea
Se ajusto el flujo de generacion de valoracion IA para que, cuando la API no devuelva una respuesta valida, la interfaz muestre una notificacion de error y el detalle del ticker presente el texto fallback guardado en base de datos.

## Codigo generado
Archivos modificados:
- `src/main/java/com/market/analysis/domain/port/in/ManageAnalyzeTickerUseCase.java`
- `src/main/java/com/market/analysis/application/usecase/ManageAnalyzeStockService.java`
- `src/main/java/com/market/analysis/presentation/controller/AnalyzeTickerController.java`
- `src/main/resources/templates/fragments/ai-valoration.html`
- `src/test/java/com/market/analysis/unit/presentation/controller/AnalyzeTickerControllerTest.java`
- `src/test/java/com/market/analysis/unit/application/usecase/ManageAnalyzeStockServiceTest.java`

Cambios principales:
- `getValorationIA(Long id)` ahora devuelve `boolean` para indicar si la respuesta IA fue valida o si se uso fallback.
- El controller redirige al detalle del ticker en todos los casos.
- Si la generacion es valida, se muestra `UiNotification.success(...)`.
- Si se usa fallback, se muestra `UiNotification.error(...)`.
- El fragmento `ai-valoration` detecta si el texto tiene secciones estructuradas; si no, renderiza el mensaje fallback completo guardado en BD.

## Decisiones tecnicas tomadas
- Se mantuvo el guardado del fallback en base de datos para no perder trazabilidad de la respuesta de IA.
- Se evito cambiar la logica de evaluacion determinista: solo se modifico el flujo de presentacion y orquestacion de la respuesta IA.
- Se preservo `th:text` en la vista para mantener salida escapada y segura.

## Cobertura de tests y pruebas añadidas
- Se actualizaron los tests unitarios del controller para cubrir:
  - caso exitoso
  - caso con fallback y notificacion de error
- Se actualizaron los tests unitarios del servicio para verificar que el metodo devuelve `true` cuando la respuesta es valida y `false` cuando se usa fallback.
- Se ejecuto la suite focalizada de tests modificados: 53 tests pasaron, 0 fallos.

## Advertencias de SonarQube o arquitectura
- No se introdujo logica de negocio en la capa de vista.
- No se uso `th:utext`, por lo que no se incrementa el riesgo de XSS.
- La modificacion en la firma del puerto de entrada es acotada y coherente con el flujo de control UI.

## Proximos pasos sugeridos
1. Revisar si otras acciones similares de IA deben seguir el mismo patron de notificacion y redireccion.
2. Considerar una pequena etiqueta visual en el fragmento para diferenciar claramente contenido estructurado y fallback.
