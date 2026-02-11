package com.market.analysis.unit.presentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.HealthStatus;
import com.market.analysis.presentation.dto.HealthCheckResponse;
import com.market.analysis.presentation.mapper.HealthCheckMapper;

/**
 * Unit tests for HealthCheckMapper.
 */
@DisplayName("HealthCheckMapper Unit Tests")
class HealthCheckMapperTest {

    private HealthCheckMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new HealthCheckMapper();
    }

    @Test
    @DisplayName("Should map UP status to HTTP 200")
    void testToResponseWithUpStatus() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(now)
                .databaseHealthy(true)
                .description("Application is fully operational")
                .details("Database: Healthy (50ms)")
                .build();

        // Act
        HealthCheckResponse response = mapper.toResponse(healthStatus);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("UP");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.isDatabaseHealthy()).isTrue();
        assertThat(response.getDescription()).isEqualTo("Application is fully operational");
        assertThat(response.getDetails()).isEqualTo("Database: Healthy (50ms)");
        assertThat(response.getHttpStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should map DOWN status to HTTP 503")
    void testToResponseWithDownStatus() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("DOWN")
                .timestamp(now)
                .databaseHealthy(false)
                .description("Application is not operational")
                .details("Database: Unhealthy")
                .build();

        // Act
        HealthCheckResponse response = mapper.toResponse(healthStatus);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("DOWN");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.isDatabaseHealthy()).isFalse();
        assertThat(response.getDescription()).isEqualTo("Application is not operational");
        assertThat(response.getDetails()).isEqualTo("Database: Unhealthy");
        assertThat(response.getHttpStatusCode()).isEqualTo(503);
    }

    @Test
    @DisplayName("Should map DEGRADED status to HTTP 503")
    void testToResponseWithDegradedStatus() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("DEGRADED")
                .timestamp(now)
                .databaseHealthy(true)
                .description("Application is partially operational")
                .details("Some services degraded")
                .build();

        // Act
        HealthCheckResponse response = mapper.toResponse(healthStatus);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("DEGRADED");
        assertThat(response.getHttpStatusCode()).isEqualTo(503);
    }

    @Test
    @DisplayName("Should correctly map all fields from domain to response")
    void testToResponseWithAllFields() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.of(2024, 6, 15, 10, 30);
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(timestamp)
                .databaseHealthy(true)
                .description("All systems operational")
                .details("Database: 25ms, Cache: 10ms")
                .build();

        // Act
        HealthCheckResponse response = mapper.toResponse(healthStatus);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("UP");
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.isDatabaseHealthy()).isTrue();
        assertThat(response.getDescription()).isEqualTo("All systems operational");
        assertThat(response.getDetails()).isEqualTo("Database: 25ms, Cache: 10ms");
        assertThat(response.getHttpStatusCode()).isEqualTo(200);
    }
}
