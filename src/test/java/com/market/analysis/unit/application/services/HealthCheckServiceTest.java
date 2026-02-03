package com.market.analysis.unit.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.services.HealthCheckService;
import com.market.analysis.domain.entities.HealthStatus;
import com.market.analysis.domain.interfaces.HealthCheckPort;

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

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckService(healthCheckPort);
    }

    @Test
    @DisplayName("Should return UP status when database is healthy")
    void testHealthCheckReturnsUpWhenDatabaseHealthy() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(50L);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertNotNull(healthStatus);
        assertEquals("UP", healthStatus.getStatus());
        assertTrue(healthStatus.isDatabaseHealthy());
        assertNotNull(healthStatus.getTimestamp());
        assertNotNull(healthStatus.getDescription());
        assertTrue(healthStatus.getDescription().contains("fully operational"));
        assertNotNull(healthStatus.getDetails());
        assertTrue(healthStatus.getDetails().contains("Healthy"));
        assertTrue(healthStatus.getDetails().contains("50ms"));
    }

    @Test
    @DisplayName("Should return DOWN status when database is unhealthy")
    void testHealthCheckReturnsDownWhenDatabaseUnhealthy() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(false);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(-1L);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertNotNull(healthStatus);
        assertEquals("DOWN", healthStatus.getStatus());
        assertFalse(healthStatus.isDatabaseHealthy());
        assertNotNull(healthStatus.getTimestamp());
        assertNotNull(healthStatus.getDescription());
        assertTrue(healthStatus.getDescription().contains("not operational"));
        assertNotNull(healthStatus.getDetails());
        assertTrue(healthStatus.getDetails().contains("Unhealthy"));
    }

    @Test
    @DisplayName("Should capture current timestamp in health status")
    void testHealthCheckCapturesCurrentTimestamp() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(25L);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertNotNull(healthStatus.getTimestamp());
        assertNotNull(healthStatus.getTimestamp().toString());
    }

    @Test
    @DisplayName("Should include database connection time in details")
    void testHealthCheckIncludesDatabaseConnectionTime() {
        // Arrange
        long connectionTime = 123L;
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(connectionTime);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertTrue(healthStatus.getDetails().contains("123ms"));
    }

    @Test
    @DisplayName("Should handle database connection time -1 for failed connections")
    void testHealthCheckHandlesFailedConnectionTime() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(false);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(-1L);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertTrue(healthStatus.getDetails().contains("-1ms"));
    }

    @Test
    @DisplayName("Should provide descriptive message for UP status")
    void testHealthCheckProvidesMeaningfulDescriptionForUpStatus() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(true);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(10L);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertEquals("Application is fully operational. All dependencies are healthy.",
                healthStatus.getDescription());
    }

    @Test
    @DisplayName("Should provide descriptive message for DOWN status")
    void testHealthCheckProvidesMeaningfulDescriptionForDownStatus() {
        // Arrange
        when(healthCheckPort.isDatabaseHealthy()).thenReturn(false);
        when(healthCheckPort.getDatabaseConnectionTime()).thenReturn(-1L);

        // Act
        HealthStatus healthStatus = healthCheckService.performHealthCheck();

        // Assert
        assertEquals("Application is not operational. Critical dependencies are unavailable.",
                healthStatus.getDescription());
    }
}
