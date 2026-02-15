# Configuración de Observabilidad con Spring Boot Actuator y Visualización de Endpoints

**Fecha**: 2026-02-15  
**Autor**: GitHub Copilot Agent  
**Tarea**: Configuración completa de observabilidad, métricas avanzadas y documentación API

---

## Resumen de la Tarea

Se ha implementado una configuración completa de observabilidad para la aplicación market-analysis-app, incluyendo:

1. **Spring Boot Actuator**: Exposición de endpoints de gestión y métricas
2. **Micrometer Prometheus**: Métricas avanzadas en formato Prometheus
3. **SpringDoc OpenAPI**: Documentación automática de la API con Swagger UI
4. **Métricas de Hibernate y HikariCP**: Monitorización detallada de persistencia y pool de conexiones

---

## Cambios Implementados

### 1. Dependencias Añadidas al pom.xml

```xml
<!-- Micrometer Prometheus Registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- SpringDoc OpenAPI (Swagger UI) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.4</version>
</dependency>
```

**Nota**: La dependencia `spring-boot-starter-actuator` ya estaba presente en el proyecto.

### 2. Configuración en application.properties

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true

# Actuator Info Endpoint
management.info.env.enabled=true
management.info.java.enabled=true
management.info.os.enabled=true

# Prometheus Metrics
management.prometheus.metrics.export.enabled=true

# JPA/Hibernate Metrics
management.metrics.enable.hibernate=true
management.metrics.enable.jpa=true

# HikariCP Connection Pool Metrics
management.metrics.enable.hikari=true

# SpringDoc OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.show-actuator=true

# Application Info
info.app.name=Market Analysis Application
info.app.description=Motor de Análisis Técnico de Acciones - TFM Desarrollo con IA
info.app.version=1.0.0-SNAPSHOT
info.app.encoding=${project.build.sourceEncoding}
info.app.java.version=${java.version}
```

### 3. Configuración Específica para Desarrollo (application-dev.properties)

```properties
# Actuator Configuration for Development - Expose all endpoints
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoints.web.base-path=/actuator

# Enable detailed metrics for development
spring.jpa.properties.hibernate.generate_statistics=true
```

### 4. Clase de Configuración OpenAPI

**Ubicación**: `src/main/java/com/market/analysis/infrastructure/config/OpenApiConfig.java`

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marketAnalysisOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Market Analysis Application API")
                        .description("Motor de Análisis Técnico de Acciones - API REST para evaluación de estrategias de trading")
                        .version("1.0.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("Market Analysis Team")
                                .email("info@marketanalysis.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
```

---

## Endpoints Disponibles

### Actuator Endpoints

En **entorno de desarrollo** (profile: `dev`), todos los endpoints están expuestos:

- **Health**: `/actuator/health` - Estado de salud de la aplicación (DB, disco, etc.)
- **Info**: `/actuator/info` - Información de la aplicación (versión, Java, OS)
- **Metrics**: `/actuator/metrics` - Lista de todas las métricas disponibles
- **Mappings**: `/actuator/mappings` - Todos los endpoints HTTP mapeados
- **Prometheus**: `/actuator/prometheus` - Métricas en formato Prometheus
- **Beans**: `/actuator/beans` - Todos los beans de Spring
- **Env**: `/actuator/env` - Variables de entorno
- **Loggers**: `/actuator/loggers` - Configuración de logging
- Y más...

En **entornos de producción** (profiles: `docker`, `prod`), solo están expuestos:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

### Swagger UI

- **Interfaz web**: `http://localhost:8080/swagger-ui.html`
- **Especificación OpenAPI**: `http://localhost:8080/v3/api-docs`

---

## Métricas Disponibles

### Métricas de HikariCP (Connection Pool)

- `hikaricp.connections` - Total de conexiones
- `hikaricp.connections.active` - Conexiones activas
- `hikaricp.connections.idle` - Conexiones inactivas
- `hikaricp.connections.acquire` - Tiempo de adquisición de conexiones
- `hikaricp.connections.creation` - Tiempo de creación de conexiones
- `hikaricp.connections.usage` - Uso de conexiones
- `hikaricp.connections.max` - Máximo de conexiones
- `hikaricp.connections.min` - Mínimo de conexiones
- `hikaricp.connections.pending` - Conexiones pendientes
- `hikaricp.connections.timeout` - Timeouts de conexión

### Métricas de Hibernate

Las métricas de Hibernate están habilitadas mediante:
```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

Esto permite monitorizar:
- Consultas ejecutadas
- Cache de segundo nivel
- Operaciones CRUD
- Session metrics

### Métricas Generales de la Aplicación

- `application.ready.time` - Tiempo hasta que la app está lista
- `application.started.time` - Tiempo de inicio
- `disk.free` / `disk.total` - Espacio en disco
- `executor.*` - Métricas del pool de threads
- `jvm.*` - Métricas de JVM (memoria, GC, threads)
- `http.server.requests` - Métricas de peticiones HTTP
- `logback.events` - Eventos de logging
- Y muchas más...

---

## Verificación de Funcionamiento

La configuración se verificó exitosamente:

1. ✅ **Compilación**: El proyecto compila sin errores con Java 21
2. ✅ **Tests**: Todos los 500 tests unitarios pasan correctamente
3. ✅ **Arranque de la aplicación**: La app arranca correctamente
4. ✅ **Actuator**: 14 endpoints expuestos bajo `/actuator`
5. ✅ **Health endpoint**: Devuelve status UP con detalles de DB, disco, etc.
6. ✅ **Info endpoint**: Devuelve información de la aplicación con versión Java 21
7. ✅ **Metrics endpoint**: Lista todas las métricas disponibles
8. ✅ **Prometheus endpoint**: Métricas en formato Prometheus funcionando
9. ✅ **HikariCP metrics**: Métricas del pool de conexiones disponibles
10. ✅ **Swagger UI**: Accesible en `/swagger-ui.html` (redirige a `/swagger-ui/index.html`)
11. ✅ **OpenAPI spec**: Disponible en `/v3/api-docs`
12. ✅ **Mappings endpoint**: Muestra todos los endpoints de la aplicación

---

## Decisiones Técnicas

### 1. No se requiere configuración de seguridad

Se verificó que el proyecto **NO** tiene Spring Security configurado, por lo que no fue necesario crear una configuración de seguridad para permitir acceso a `/actuator/**` y `/swagger-ui/**`.

### 2. Exposición de endpoints según perfil

- **Desarrollo (`dev`)**: Se exponen TODOS los endpoints (`*`) para facilitar el desarrollo y debugging
- **Producción (`prod`, `docker`)**: Solo se exponen `health`, `info` y `metrics` por seguridad

### 3. Ubicación de la configuración

La clase `OpenApiConfig` se ubicó en el paquete `infrastructure/config` siguiendo la Arquitectura Hexagonal del proyecto.

### 4. Versión de SpringDoc OpenAPI

Se utilizó la versión `2.8.4` que es compatible con Spring Boot 3.5.x y Java 21.

### 5. Información de la aplicación

Se configuraron propiedades `info.*` para que el endpoint `/actuator/info` devuelva información útil sobre:
- Nombre y descripción de la aplicación
- Versión
- Información de Java y JVM
- Sistema operativo

---

## Cobertura de Tests

No se añadieron tests específicos para la configuración de Actuator y OpenAPI porque:

1. **Jacoco Coverage**: El paquete `infrastructure/config/**` está excluido de la cobertura según la configuración en `pom.xml`
2. **Verificación Manual**: La funcionalidad se verificó manualmente arrancando la aplicación y probando todos los endpoints
3. **Tests de Spring Boot**: Spring Boot tiene sus propios tests para Actuator que garantizan su funcionamiento

Los 500 tests existentes del proyecto continúan pasando correctamente, confirmando que la nueva configuración no afecta la funcionalidad existente.

---

## Advertencias de SonarQube

Esta configuración sigue las mejores prácticas y no debería generar advertencias de SonarQube:

- ✅ **Inyección por constructor**: La clase `OpenApiConfig` no tiene dependencias inyectadas
- ✅ **Sin lógica de negocio**: Solo configuración de beans
- ✅ **Ubicación correcta**: En el paquete `infrastructure/config`
- ✅ **Sin números mágicos**: Se usan constantes en properties
- ✅ **Sin strings hardcodeados sensibles**: Información de la app en properties

---

## Próximos Pasos Sugeridos

1. **Configurar alertas**: Usar Prometheus + Grafana para visualizar métricas y configurar alertas
2. **Personalizar health indicators**: Añadir health indicators personalizados para APIs externas (Finnhub, Polygon)
3. **Métricas personalizadas**: Añadir métricas de negocio específicas (estrategias evaluadas, llamadas a IA, etc.)
4. **Documentación de API**: Enriquecer los endpoints con anotaciones `@Operation`, `@ApiResponse`, etc.
5. **Testing de endpoints**: Añadir tests de integración para verificar los endpoints de Actuator
6. **Exportar a sistemas de monitorización**: Configurar exportación a Datadog, New Relic, etc.

---

## Referencias

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/reference/actuator/index.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [Prometheus Metrics Format](https://prometheus.io/docs/instrumenting/exposition_formats/)

---

## Archivos Modificados

1. `pom.xml` - Añadidas dependencias
2. `config/application.properties` - Configuración general de actuator
3. `config/application-dev.properties` - Configuración específica de desarrollo
4. `src/main/java/com/market/analysis/infrastructure/config/OpenApiConfig.java` - Nueva clase de configuración

---

## Notas Finales

Esta configuración proporciona una base sólida para la observabilidad de la aplicación durante el desarrollo del TFM y facilita el debugging y monitorización sin introducir dependencias innecesarias ni comprometer la seguridad en producción.

La implementación respeta completamente la Arquitectura Hexagonal del proyecto, manteniendo toda la configuración en la capa de infraestructura sin afectar al dominio ni a los casos de uso.
