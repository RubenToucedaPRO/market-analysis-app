# task-2026-04-13-global-exception-handler-refactor.md

## Título
Refactorización de GlobalExceptionHandler con estrategia de redirección y agrupación por comportamiento

## Resumen de la Tarea
Refactorización completa de `GlobalExceptionHandler` siguiendo principios de Clean Architecture y UX. En lugar de enviar todas las excepciones a `error.html`, la mayoría redirige al usuario a la página anterior (Referer) usando `RedirectAttributes` con un `flashAttribute` llamado `errorMessage`. Se creó también la excepción de dominio `EntityInUseException`.

---

## Cambios Realizados

### 1. Nueva excepción de dominio: `EntityInUseException`
**Archivo:** `src/main/java/com/market/analysis/domain/exception/EntityInUseException.java`

Excepción de dominio lanzada cuando un recurso no puede eliminarse porque tiene dependencias asociadas. Reemplaza el uso de `IllegalArgumentException` para este caso.

### 2. `SqlRuleDefinitionRepository` – cambio de excepción en `deleteById()`
**Archivo:** `src/main/java/com/market/analysis/infrastructure/persistence/repository/SqlRuleDefinitionRepository.java`

Ahora lanza `EntityInUseException` (dominio) en lugar de `IllegalArgumentException` cuando se intenta eliminar una regla usada en estrategias.

### 3. `GlobalExceptionHandler` – refactorización completa
**Archivo:** `src/main/java/com/market/analysis/presentation/exception/GlobalExceptionHandler.java`

#### Agrupación por comportamiento:

| Grupo | Excepciones | Estrategia | Log |
|-------|-------------|------------|-----|
| Dominio/Navegación | `RuleDefinitionNotFoundException`, `StockDataNotFoundException` | Redirigir con mensaje original | `log.warn` |
| Integridad | `EntityInUseException` | Redirigir con mensaje fijo de usuario | `log.warn` |
| Servicios Externos | `FinnhubException`, `PolygonException`, `AIServiceException`, `StockException` | Redirigir con mensaje amigable fijo | `log.error` |
| Críticos | `PersistenceException`, `Exception.class` | Mantener `error.html` con detalles técnicos | `log.error` |

#### Refactorización DRY:
Método privado `redirectWithError(String message, RedirectAttributes ra, HttpServletRequest req)` centraliza la lógica de obtención del Referer y la adición del `flashAttribute`.

#### Mensajes de usuario (constantes):
- Servicios externos: `"El servicio de datos de mercado no está disponible temporalmente"`
- Integridad: `"No se puede eliminar el recurso porque tiene dependencias asociadas"`
- Fallback referer: `"/"`

#### Se eliminó:
- El manejador `handleRuntimeException` (RuntimeException) – ahora gestionado por `Exception.class`

### 4. `RuleDefinitionController` – limpieza de try-catch
**Archivo:** `src/main/java/com/market/analysis/presentation/controller/RuleDefinitionController.java`

Eliminado el bloque `try-catch` en `deleteRuleDefinition()`. La gestión de errores es ahora responsabilidad exclusiva del `GlobalExceptionHandler` centralizado.

---

## Código Generado (resumen)

```java
// Método DRY en GlobalExceptionHandler
private String redirectWithError(String message, RedirectAttributes ra, HttpServletRequest req) {
    ra.addFlashAttribute(ATTR_ERROR_MESSAGE, message);
    String referer = req.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : DEFAULT_REFERER);
}
```

---

## Decisiones Técnicas

- **EntityInUseException en dominio**: Semánticamente correcto – la restricción de integridad es una regla de negocio del dominio. La capa de infraestructura puede referenciar excepciones de dominio.
- **Eliminación de RuntimeException handler**: El handler `Exception.class` cubre todos los casos críticos restantes; tener un handler de `RuntimeException` era redundante.
- **Mensaje fijo para servicios externos**: El usuario no necesita detalles técnicos de la API; un mensaje amigable y consistente mejora la UX.
- **Referer fallback a "/"**: Garantiza que la redirección funcione aunque el header no esté presente.

---

## Cobertura de Tests y Pruebas

### Archivos de test actualizados:

| Archivo | Cambios |
|---------|---------|
| `GlobalExceptionHandlerTest` | Reescrito completamente: nuevos mocks (`RedirectAttributes`, `HttpServletRequest`), tests para todos los grupos de comportamiento, tests de edge cases (null message, Referer ausente) |
| `RuleDefinitionControllerTest` | `testDeleteRuleDefinitionUsedInStrategy` actualizado para verificar que `EntityInUseException` se propaga al handler global |
| `SqlRuleDefinitionRepositoryTest` | Tests `testDeleteByIdUsedAsSubject` y `testDeleteByIdUsedAsTarget` actualizados para esperar `EntityInUseException` |

### Resultado:
- **953 tests, 0 failures, 0 errors** ✅

---

## Advertencias de Arquitectura

- Se respeta el principio SRP: el controlador solo orquesta, el handler maneja errores.
- No hay lógica de negocio en la capa de presentación.
- Sin uso de `lenient` en Mockito (se siguió el principio del proyecto).

---

## Próximos Pasos Sugeridos

- Asegurar que las vistas que reciben el `flashAttribute errorMessage` lo muestren correctamente (fragmento de alerta Bootstrap).
- Considerar añadir un handler para `MethodArgumentNotValidException` (validación de formularios) siguiendo la misma estrategia de redirección.
