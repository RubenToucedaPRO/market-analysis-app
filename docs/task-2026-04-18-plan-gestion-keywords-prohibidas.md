# Task: Plan de mejora para gestión de keywords de tickers prohibidos

## Resumen de la tarea
- Se analiza el estado actual de la prohibición de tickers: la detección por keyword está hardcodeado en `CompanyProfile` (`PROHIBITED_KEYWORDS`) y la vista `/prohibited-tickers` solo permite listar/eliminar tickers ya bloqueados.
- Se define una planificación por fases para migrar la lista de keywords prohibidas a base de datos y hacerla visible/editable en un lateral de la vista de tickers prohibidos.
- No se implementan cambios funcionales en esta tarea: el objetivo es dejar un plan ejecutable y ordenado para desarrollo incremental.

## Código generado (si aplica)
- No aplica. Se añade únicamente documentación de planificación.

## Decisiones técnicas tomadas
- Mantener la arquitectura hexagonal: nueva gestión de keywords mediante puertos de dominio (`port.in`/`port.out`), caso de uso en `application` y adapter JPA en `infrastructure`.
- Separar conceptos:
  - `prohibited_tickers`: resultado de bloqueos (instancias concretas).
  - nueva tabla `prohibited_keywords`: reglas configurables de bloqueo por texto.
- Evitar “big bang”: migración por fases con compatibilidad temporal para no romper el flujo de análisis actual.

## Planificación por fases numeradas

### Fase 1 — Modelado de dominio y persistencia de keywords
1. Crear entidad de dominio `ProhibitedKeyword` (ej.: `keyword`, `active`, `createdAt`, `updatedAt`).
2. Crear puerto de salida `ProhibitedKeywordRepository` con operaciones mínimas: listar, crear, eliminar, existencia por keyword.
3. Crear entidad JPA `ProhibitedKeywordEntity` y tabla `prohibited_keywords`.
4. Crear repositorio JPA e implementación SQL del puerto.
5. Definir restricción única para `keyword` y normalización a mayúsculas en capa Application antes de persistir (misma normalización en búsquedas y validaciones) para mantener consistencia e independencia del motor de BD.

### Fase 2 — Caso de uso de gestión de keywords
1. Crear `ManageProhibitedKeywordUseCase` en `domain.port.in`.
2. Implementar `ManageProhibitedKeywordService` en `application`.
3. Añadir DTO + mapper (`ProhibitedKeywordDTO`, mapper application/infrastructure según patrón actual).
4. Reglas de validación:
   - Keyword no vacía.
   - Sin duplicados (normalizadas).
   - Longitud máxima razonable.

### Fase 3 — Integración con lógica de bloqueo actual
1. Extraer la lógica hardcodeada de `CompanyProfile` para que deje de depender de lista estática.
2. Introducir servicio de dominio (ej. `ProhibitedKeywordMatcher`) que reciba la lista desde repositorio/caso de uso.
3. Ajustar `ManageAnalyzeStockService` para usar el matcher y mantener el mismo resultado funcional (`isProhibited`/razón).
4. Estrategia de transición: fallback temporal a lista estática solo si la tabla está vacía (retirable en fase final).

### Fase 4 — UI: lista lateral visible y editable en `/prohibited-tickers`
1. Extender `ProhibitedTickerController` para cargar también `prohibitedKeywords`.
2. Modificar `templates/prohibited-tickers/list.html`:
   - Mantener tabla principal de tickers prohibidos.
   - Añadir panel lateral (sidebar) con listado de keywords.
   - Hacer el sidebar responsive/collapsible en móvil (Bootstrap 5) para preservar usabilidad y accesibilidad.
   - Añadir formulario de alta rápida de keyword.
   - Añadir acción de borrado por keyword.
3. Mantener mensajes flash (`UiNotification`) y estilo Bootstrap existentes.
4. Evitar lógica de negocio en Thymeleaf; solo renderizado de datos y acciones POST.

### Fase 5 — Seguridad, calidad y pruebas
1. Añadir pruebas unitarias:
   - Servicio de gestión de keywords.
   - Matcher de prohibición por keyword.
2. Añadir/ajustar pruebas de controlador (`MockMvc`) para alta/baja y listado lateral de keywords.
3. Añadir pruebas de repositorio SQL/JPA para unicidad y consultas.
4. Verificar cobertura mínima en módulos modificados (objetivo >= 80%).
5. Validar CSRF y uso de `th:text` (sin `th:utext`).

### Fase 6 — Migración de datos y despliegue controlado
1. Cargar seed inicial en `prohibited_keywords` con valores actuales hardcodeados.
2. Verificar en entorno dev/test que la detección no cambia respecto al comportamiento previo.
3. Eliminar fallback estático tras confirmar paridad funcional.
4. Documentar operación: cómo añadir/quitar keywords desde UI y criterios recomendados.

## Cobertura de tests y pruebas añadidas si faltan
- En esta tarea no se añade código de producción, por lo que no se requieren tests nuevos.
- Validación de estado base ejecutada antes de planificar: `mvn test` en verde (`Tests run: 1034, Failures: 0, Errors: 0, Skipped: 0`).

## Advertencias de SonarQube o arquitectura
- Riesgo principal a controlar en implementación: no introducir dependencia de infraestructura dentro del dominio.
- Riesgo funcional: evitar que una keyword demasiado genérica genere falsos positivos; se recomienda revisión funcional de seeds iniciales.

## Próximos pasos sugeridos
1. Ejecutar Fase 1 y Fase 2 en un PR técnico de backend (sin cambios de UI).
2. Ejecutar Fase 4 en PR separado de UI/controlador para facilitar revisión.
3. Cerrar con PR de hardening (Fase 5 y 6) tras validar paridad funcional.
