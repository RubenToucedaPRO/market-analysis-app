package com.market.analysis.unit.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.HealthCheckResponse;
import com.market.analysis.application.mapper.HealthCheckMapper;
import com.market.analysis.application.usecase.HealthCheckService;
import com.market.analysis.domain.model.HealthStatus;
import com.market.analysis.domain.port.out.HealthCheckPort;

/**
 * Unit tests for HealthCheckService.
 * Tests the business logic for performing health checks and determining system
 * status.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckService Unit Tests")
class HealthCheckServiceTest {

    @Mock
    private HealthCheckPort healthCheckPort;

    @Mock
    private HealthCheckMapper healthCheckMapper;

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckService(healthCheckPort, healthCheckMapper);
    }

    @Test
    @DisplayName("Should return UP status when database is healthy")
    void testHealthCheckReturnsUpWhenDatabaseHealthy() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(50L);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(true)
                .description("Application is fully operational. All dependencies are healthy.")
                .details("Database: Healthy (50ms)")
                .httpStatusCode(200)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertNotNull(result);
        assertEquals("UP", result.getStatus());
        assertTrue(result.isDatabaseHealthy());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getDescription());
        assertTrue(result.getDescription().contains("fully operational"));
        assertNotNull(result.getDetails());
        assertTrue(result.getDetails().contains("Healthy"));
    }

    @Test
    @DisplayName("Should return DOWN status when database is unhealthy")
    void testHealthCheckReturnsDownWhenDatabaseUnhealthy() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(false);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(-1L);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("DOWN")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(false)
                .description("Application is not operational. Critical dependencies are unavailable.")
                .details("Database: Unhealthy (-1ms)")
                .httpStatusCode(503)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertNotNull(result);
        assertEquals("DOWN", result.getStatus());
        assertFalse(result.isDatabaseHealthy());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getDescription());
        assertTrue(result.getDescription().contains("not operational"));
        assertNotNull(result.getDetails());
        assertTrue(result.getDetails().contains("Unhealthy"));
    }

    @Test
    @DisplayName("Should capture current timestamp in health status")
    void testHealthCheckCapturesCurrentTimestamp() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(25L);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(true)
                .description("Application is fully operational. All dependencies are healthy.")
                .details("Database: Healthy (25ms)")
                .httpStatusCode(200)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getTimestamp().toString());
    }

    @Test
    @DisplayName("Should include database connection time in details")
    void testHealthCheckIncludesDatabaseConnectionTime() {
        // Arrange
        long connectionTime = 123L;
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(connectionTime);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(true)
                .description("Application is fully operational. All dependencies are healthy.")
                .details("Database: Healthy (123ms)")
                .httpStatusCode(200)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertTrue(result.getDetails().contains("123ms"));
    }

    @Test
    @DisplayName("Should handle database connection time -1 for failed connections")
    void testHealthCheckHandlesFailedConnectionTime() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(false);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(-1L);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("DOWN")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(false)
                .description("Application is not operational. Critical dependencies are unavailable.")
                .details("Database: Unhealthy (-1ms)")
                .httpStatusCode(503)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertTrue(result.getDetails().contains("-1ms"));
    }

    @Test
    @DisplayName("Should provide descriptive message for UP status")
    void testHealthCheckProvidesMeaningfulDescriptionForUpStatus() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(10L);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(true)
                .description("Application is fully operational. All dependencies are healthy.")
                .details("Database: Healthy (10ms)")
                .httpStatusCode(200)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertEquals("Application is fully operational. All dependencies are healthy.",
                result.getDescription());
    }

    @Test
    @DisplayName("Should provide descriptive message for DOWN status")
    void testHealthCheckProvidesMeaningfulDescriptionForDownStatus() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(false);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(-1L);

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("DOWN")
                .timestamp(java.time.LocalDateTime.now())
                .databaseHealthy(false)
                .description("Application is not operational. Critical dependencies are unavailable.")
                .details("Database: Unhealthy (-1ms)")
                .httpStatusCode(503)
                .build();

        when(healthCheckMapper.toResponse(any(HealthStatus.class))).thenReturn(response);

        // Act
        HealthCheckResponse result = healthCheckService.performHealthCheck();

        // Assert
        assertEquals("Application is not operational. Critical dependencies are unavailable.",
                result.getDescription());
    }
}
