package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.HealthCheckResponse;
import com.market.analysis.domain.model.HealthStatus;

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
     * @return HealthCheckResponse suitable for HTTP response, or null if input is null
     */
    public HealthCheckResponse toResponse(HealthStatus healthStatus) {
        if (healthStatus == null) {
            return null;
        }
        
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
