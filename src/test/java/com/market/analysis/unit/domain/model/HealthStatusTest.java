package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.HealthStatus;

/**
 * Unit tests for HealthStatus domain model.
 * Tests the builder pattern and getter methods.
 */
@DisplayName("HealthStatus Domain Model Tests")
class HealthStatusTest {

    @Test
    @DisplayName("Should create HealthStatus with all fields using builder")
    void testHealthStatusBuilder() {
        // Arrange
        Instant now = Instant.now();

        // Act
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(now)
                .databaseHealthy(true)
                .description("Application is fully operational")
                .details("Database: Healthy (50ms)")
                .build();

        // Assert
        assertThat(healthStatus).isNotNull();
        assertThat(healthStatus.getStatus()).isEqualTo("UP");
        assertThat(healthStatus.getTimestamp()).isEqualTo(now);
        assertThat(healthStatus.isDatabaseHealthy()).isTrue();
        assertThat(healthStatus.getDescription()).isEqualTo("Application is fully operational");
        assertThat(healthStatus.getDetails()).isEqualTo("Database: Healthy (50ms)");
    }

    @Test
    @DisplayName("Should create HealthStatus with DOWN status")
    void testHealthStatusWithDownStatus() {
        // Arrange
        Instant now = Instant.now();

        // Act
        HealthStatus healthStatus = HealthStatus.builder()
                .status("DOWN")
                .timestamp(now)
                .databaseHealthy(false)
                .description("Application is not operational")
                .details("Database: Unhealthy")
                .build();

        // Assert
        assertThat(healthStatus).isNotNull();
        assertThat(healthStatus.getStatus()).isEqualTo("DOWN");
        assertThat(healthStatus.isDatabaseHealthy()).isFalse();
    }

    @Test
    @DisplayName("Should have correct toString representation")
    void testToString() {
        // Arrange
        Instant now = Instant.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(now)
                .databaseHealthy(true)
                .description("Test description")
                .details("Test details")
                .build();

        // Act
        String toString = healthStatus.toString();

        // Assert
        assertThat(toString).contains("UP", "true", "Test description", "Test details");
    }
}
