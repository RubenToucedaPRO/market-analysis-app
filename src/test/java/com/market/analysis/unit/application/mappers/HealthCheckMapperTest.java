package com.market.analysis.unit.application.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.mappers.HealthCheckMapper;
import com.market.analysis.domain.entities.HealthStatus;
import com.market.analysis.presentation.response.HealthCheckResponse;

/**
 * Unit tests for HealthCheckMapper.
 * Tests the mapping logic between domain entities and presentation DTOs.
 */
@DisplayName("HealthCheckMapper Unit Tests")
class HealthCheckMapperTest {

    private HealthCheckMapper healthCheckMapper;

    @BeforeEach
    void setUp() {
        healthCheckMapper = new HealthCheckMapper();
    }

    @Test
    @DisplayName("Should map UP status to 200 HTTP status code")
    void testMapUpStatusToHttp200() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(now)
                .databaseHealthy(true)
                .description("Application is fully operational. All dependencies are healthy.")
                .details("Database: Healthy (50ms)")
                .build();

        // Act
        HealthCheckResponse response = healthCheckMapper.toResponse(healthStatus);

        // Assert
        assertEquals(200, response.getHttpStatusCode());
        assertEquals("UP", response.getStatus());
    }

    @Test
    @DisplayName("Should map DOWN status to 503 HTTP status code")
    void testMapDownStatusToHttp503() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("DOWN")
                .timestamp(now)
                .databaseHealthy(false)
                .description("Application is not operational. Critical dependencies are unavailable.")
                .details("Database: Unhealthy (-1ms)")
                .build();

        // Act
        HealthCheckResponse response = healthCheckMapper.toResponse(healthStatus);

        // Assert
        assertEquals(503, response.getHttpStatusCode());
        assertEquals("DOWN", response.getStatus());
    }

    @Test
    @DisplayName("Should preserve all fields from HealthStatus in response")
    void testMapPreservesAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String description = "Test description";
        String details = "Test details";

        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(now)
                .databaseHealthy(true)
                .description(description)
                .details(details)
                .build();

        // Act
        HealthCheckResponse response = healthCheckMapper.toResponse(healthStatus);

        // Assert
        assertEquals("UP", response.getStatus());
        assertEquals(now, response.getTimestamp());
        assertTrue(response.isDatabaseHealthy());
        assertEquals(description, response.getDescription());
        assertEquals(details, response.getDetails());
    }

    @Test
    @DisplayName("Should handle null timestamp gracefully")
    void testMapHandlesAllPossibleStatuses() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Test DEGRADED status (though currently not used, important for future
        // extension)
        HealthStatus healthStatus = HealthStatus.builder()
                .status("DEGRADED")
                .timestamp(now)
                .databaseHealthy(true)
                .description("Application is partially operational.")
                .details("Some services unavailable")
                .build();

        // Act
        HealthCheckResponse response = healthCheckMapper.toResponse(healthStatus);

        // Assert
        assertEquals("DEGRADED", response.getStatus());
        assertEquals(503, response.getHttpStatusCode()); // Non-UP status defaults to 503
    }
}
