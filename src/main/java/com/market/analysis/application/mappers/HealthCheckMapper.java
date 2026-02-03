package com.market.analysis.application.mappers;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.entities.HealthStatus;
import com.market.analysis.presentation.response.HealthCheckResponse;

/**
 * Mapper for converting domain HealthStatus entities to presentation DTOs.
 * Encapsulates the transformation logic to decouple domain models from
 * API response formats.
 */
@Component
public class HealthCheckMapper {

    /**
     * Converts a domain HealthStatus entity to a REST response DTO.
     * Determines the appropriate HTTP status code based on the application status.
     *
     * @param healthStatus the domain health status object
     * @return HealthCheckResponse suitable for HTTP response
     */
    public HealthCheckResponse toResponse(HealthStatus healthStatus) {
        int httpStatusCode = "UP".equals(healthStatus.getStatus()) ? 200 : 503;

        return HealthCheckResponse.builder()
                .status(healthStatus.getStatus())
                .timestamp(healthStatus.getTimestamp())
                .databaseHealthy(healthStatus.isDatabaseHealthy())
                .description(healthStatus.getDescription())
                .details(healthStatus.getDetails())
                .httpStatusCode(httpStatusCode)
                .build();
    }
}
