package com.market.analysis.unit.presentation.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.usecase.HealthCheckService;
import com.market.analysis.domain.model.HealthStatus;
import com.market.analysis.presentation.dto.HealthCheckResponse;
import com.market.analysis.presentation.mapper.HealthCheckMapper;

/**
 * Integration tests for HealthCheckController using MockMvc.
 * Tests the HTTP endpoint and response handling for health checks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("HealthCheckController Integration Tests")
class HealthCheckControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private HealthCheckService healthCheckService;

        @MockitoBean
        private HealthCheckMapper healthCheckMapper;

        @Test
        @DisplayName("Should return 200 OK with UP status when application is healthy")
        void testGetHealthReturnsOkWhenUp() throws Exception {
                // Arrange
                LocalDateTime now = LocalDateTime.now();
                HealthStatus healthStatus = HealthStatus.builder()
                                .status("UP")
                                .timestamp(now)
                                .databaseHealthy(true)
                                .description("Application is fully operational. All dependencies are healthy.")
                                .details("Database: Healthy (50ms)")
                                .build();

                HealthCheckResponse response = HealthCheckResponse.builder()
                                .status("UP")
                                .timestamp(now)
                                .databaseHealthy(true)
                                .description("Application is fully operational. All dependencies are healthy.")
                                .details("Database: Healthy (50ms)")
                                .httpStatusCode(200)
                                .build();

                when(healthCheckService.performHealthCheck()).thenReturn(healthStatus);
                when(healthCheckMapper.toResponse(healthStatus)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/health")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.status", is("UP")))
                                .andExpect(jsonPath("$.database_healthy", is(true)))
                                .andExpect(jsonPath("$.description", containsString("fully operational")))
                                .andExpect(jsonPath("$.http_status_code", is(200)));
        }

        @Test
        @DisplayName("Should return 503 SERVICE_UNAVAILABLE when application is DOWN")
        void testGetHealthReturnsServiceUnavailableWhenDown() throws Exception {
                // Arrange
                LocalDateTime now = LocalDateTime.now();
                HealthStatus healthStatus = HealthStatus.builder()
                                .status("DOWN")
                                .timestamp(now)
                                .databaseHealthy(false)
                                .description("Application is not operational. Critical dependencies are unavailable.")
                                .details("Database: Unhealthy (-1ms)")
                                .build();

                HealthCheckResponse response = HealthCheckResponse.builder()
                                .status("DOWN")
                                .timestamp(now)
                                .databaseHealthy(false)
                                .description("Application is not operational. Critical dependencies are unavailable.")
                                .details("Database: Unhealthy (-1ms)")
                                .httpStatusCode(503)
                                .build();

                when(healthCheckService.performHealthCheck()).thenReturn(healthStatus);
                when(healthCheckMapper.toResponse(healthStatus)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/health")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.status", is("DOWN")))
                                .andExpect(jsonPath("$.database_healthy", is(false)))
                                .andExpect(jsonPath("$.http_status_code", is(503)));
        }

        @Test
        @DisplayName("Should include timestamp in response")
        void testGetHealthIncludesTimestamp() throws Exception {
                // Arrange
                LocalDateTime now = LocalDateTime.now();
                HealthStatus healthStatus = HealthStatus.builder()
                                .status("UP")
                                .timestamp(now)
                                .databaseHealthy(true)
                                .description("Application is fully operational. All dependencies are healthy.")
                                .details("Database: Healthy (25ms)")
                                .build();

                HealthCheckResponse response = HealthCheckResponse.builder()
                                .status("UP")
                                .timestamp(now)
                                .databaseHealthy(true)
                                .description("Application is fully operational. All dependencies are healthy.")
                                .details("Database: Healthy (25ms)")
                                .httpStatusCode(200)
                                .build();

                when(healthCheckService.performHealthCheck()).thenReturn(healthStatus);
                when(healthCheckMapper.toResponse(healthStatus)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/health")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should include database connection details in response")
        void testGetHealthIncludesDatabaseDetails() throws Exception {
                // Arrange
                LocalDateTime now = LocalDateTime.now();
                HealthStatus healthStatus = HealthStatus.builder()
                                .status("UP")
                                .timestamp(now)
                                .databaseHealthy(true)
                                .description("Application is fully operational. All dependencies are healthy.")
                                .details("Database: Healthy (75ms)")
                                .build();

                HealthCheckResponse response = HealthCheckResponse.builder()
                                .status("UP")
                                .timestamp(now)
                                .databaseHealthy(true)
                                .description("Application is fully operational. All dependencies are healthy.")
                                .details("Database: Healthy (75ms)")
                                .httpStatusCode(200)
                                .build();

                when(healthCheckService.performHealthCheck()).thenReturn(healthStatus);
                when(healthCheckMapper.toResponse(healthStatus)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/health")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.details", containsString("Database")))
                                .andExpect(jsonPath("$.details", containsString("ms")));
        }
}
