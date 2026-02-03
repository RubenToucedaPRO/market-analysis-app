# Agents.md - Directrices para el asistente de IA

Este documento contiene las reglas, buenas prácticas y procedimientos que el asistente debe seguir al trabajar con el proyecto **market-analysis-app**.

---

## 1. Contexto del Proyecto

- Aplicación web para evaluación determinista de estrategias de análisis técnico sobre datos históricos y actuales.
- Estrategias definidas como composiciones de reglas técnicas independientes y reutilizables.
- Resultados numéricos (R:R, métricas de cumplimiento) son la base de toda evaluación.
- Modelos de lenguaje (IA) usados únicamente como **análisis interpretativo complementario**, sin afectar resultados ni decisiones.
- Arquitectura hexagonal basada en **Clean Architecture**: dominio puro, casos de uso en Application Layer, detalles en Infrastructure Layer.
- Persistencia: H2 para desarrollo, MariaDB opcional en producción.
- Frontend: Thymeleaf + HTMX + Bootstrap 5, interacción mínima con JavaScript.
- **README.md** debe ser consultado y respetado en todo momento para asegurar coherencia de descripciones, objetivos, stack y funcionalidades.

---

## 2. Principios de Operación del Asistente

1. Toda sugerencia de código o tarea debe **respetar la arquitectura limpia y desacoplada**.
2. Las reglas técnicas (`Rule`) deben permanecer **deterministas y autocontenidas**.
3. Integración IA: solo generación de texto interpretativo basado en métricas ya calculadas.
4. Nunca modificar la lógica de evaluación de reglas para cumplir análisis de IA.
5. **Verificar cobertura de tests unitarios** para cada tarea generada. Si no existe suficiente cobertura, añadir tests adicionales.
6. **No usar `lenient` en Mockito** salvo que sea estrictamente necesario y documentar el motivo.

---

## 3. Procedimiento para Tareas de Desarrollo

- Antes de generar código, verificar:
  - Cumple Clean Architecture.
  - Respeta SRP, DIP y patrones de diseño (Strategy, Factory, Repository).
  - No hay lógica de negocio en Thymeleaf ni en Frontend.
- Para cada tarea completada:
  1. Revisar cobertura de tests unitarios; asegurar que se cubre el comportamiento implementado.
  2. Crear un archivo Markdown en `/docs`.
  3. Nombrar: `task-<fecha>-<slug>.md`.
  4. Incluir:
     - Título descriptivo
     - Resumen de la tarea
     - Código generado (si aplica)
     - Decisiones técnicas tomadas
     - Cobertura de tests y pruebas añadidas si faltan
     - Advertencias de SonarQube o arquitectura
     - Próximos pasos sugeridos
  5. Archivo autocontenido y reconstruible sin referencia externa.

---

## 4. Variables de Entorno Controladas

| Variable                | Propósito                                     |
|-------------------------|-----------------------------------------------|
| `FINNHUB_API_TOKEN`     | Datos de mercado actuales                     |
| `POLYGON_API_TOKEN`     | Datos históricos y OHLCV                      |
| `OPENAI_API_KEY`        | Análisis interpretativo de estrategias       |
| `ANTHROPIC_API_KEY`     | Alternativa de IA para análisis interpretativo|
| `GOOGLE_API_KEY`        | Alternativa de IA para análisis interpretativo|
| `SPRING_PROFILES_ACTIVE`| Perfil de Spring Boot (dev, prod)            |

---

## 5. Patrones de Diseño a Respetar

- **Strategy**: composición de reglas configurables.
- **Factory**: construcción dinámica de reglas según parámetros.
- **Repository**: persistencia desacoplada de domain.
- **Facade** (opcional): agrupar servicios relacionados para cumplir S107 y SRP.

---

## 6. Buenas Prácticas de Desarrollo

- **Inyección por constructor** (`@Autowired` solo en constructor).
- **Evitar lógica compleja en Thymeleaf** (`th:if`, `th:each` >2 condiciones → mover al DTO).
- **Internacionalización**: evitar textos hardcodeados en vistas.
- **Cerrar recursos manuales** con `try-with-resources`.
- **Logging**: usar SLF4J (`log.info/debug/error`) y no `System.out.println`.
- **Versionado de dependencias**: versiones fijas en `pom.xml`.

---

## 7. Reglas de SonarQube para Spring Boot y Thymeleaf

### Seguridad (OWASP y Hotspots)

- Evitar `th:utext`; usar `th:text` siempre. Sanitizar solo si es crítico.
- Habilitar **CSRF** con Spring Security y `th:action`.
- No exponer StackTrace completo; usar `@ControllerAdvice`.

### Mantenibilidad y Arquitectura (Code Smells)

- Lógica de negocio fuera de vistas.
- Evitar field injection; usar constructor injection.
- Números mágicos y strings hardcodeados → usar constantes y `messages.properties`.

### Fiabilidad y Bugs

- Cierre correcto de recursos (`try-with-resources`).
- Logging correcto con SLF4J.

### Cobertura y Tests

- Cobertura mínima 80%.
- Tests unitarios con `JUnit 5 + Mockito`.
- Controladores testados con `MockMvc`.
- **Comprobar cobertura tras cada tarea generada**; añadir tests si falta cobertura.

### Métricas de Complejidad y Tamaño

- Máximo 7 parámetros en constructor (`S107`).
- Complejidad cognitiva < 15 (`S3776`).
- Profundidad de anidamiento < 4 (`S134`).
- Clases < 1000 líneas; evitar God Classes.
- **No usar `lenient` salvo necesidad justificada**.

### Métricas Thymeleaf (HTML/XML)

- Evitar expresiones SpEL largas (>120 caracteres) o con múltiples operadores.
- Evitar duplicación de bloques; usar `th:fragment`.

---

## 8. Procedimiento de Generación Automática de Documentos

- Para cada tarea completada:
  - Crear Markdown en `/docs`.
  - Nombre: `task-<fecha>-<slug>.md`.
  - Contenido mínimo:
    - Título descriptivo
    - Resumen
    - Código generado (si aplica)
    - Decisiones técnicas
    - Cobertura de tests y pruebas añadidas si falta
    - Advertencias SonarQube / arquitectura
    - Próximos pasos
  - Archivos autocontenidos y reconstruibles.

---

## 9. Consideraciones Finales para el Asistente

- Respetar siempre la **Arquitectura Hexagonal** aplicando **Clean Architecture** y **SRP**.
- IA **solo como análisis interpretativo**.
- No ejecutar cambios automáticos en la lógica de evaluación.
- Documentar cada tarea inmediatamente en `/docs`.
- Validar todas las recomendaciones con **SonarQube** y patrones de diseño.
- Comprobar cobertura de tests tras cada tarea y añadir pruebas si es necesario.
- Evitar `lenient` en Mockito salvo que sea estrictamente necesario.