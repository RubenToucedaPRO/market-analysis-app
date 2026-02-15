package com.market.analysis.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de SpringDoc OpenAPI para documentación automática de la API.
 * 
 * Esta configuración expone:
 * - Swagger UI en /swagger-ui.html
 * - OpenAPI spec en /v3/api-docs
 * 
 * Permite visualizar todos los endpoints de la aplicación y los endpoints
 * de gestión de Actuator de forma integrada.
 * 
 * Ubicada en infrastructure/config siguiendo la Arquitectura Hexagonal.
 */
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
