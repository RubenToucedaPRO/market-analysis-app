package com.market.analysis.application.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.market.analysis.domain.entities.HealthStatus;
import com.market.analysis.domain.interfaces.HealthCheckPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application Service for managing health check operations.
 * Orchestrates the verification of system dependencies and compiles
 * the overall health status of the application.
 *
 * This service is responsible for:
 * - Verifying database connectivity
 * - Aggregating component health information
 * - Determining overall system status
 * - Providing health status information for monitoring and diagnostics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final HealthCheckPort healthCheckPort;

    /**
     * Performs a comprehensive health check of the application.
     * Verifies database connectivity and evaluates overall system status.
     *
     * @return HealthStatus containing current system state and component details
     */
    public HealthStatus performHealthCheck() {
        log.debug("Starting health check");

        long startTime = System.currentTimeMillis();
        boolean databaseHealthy = healthCheckPort.isDatabaseHealthy();
        long databaseConnectionTime = healthCheckPort.getDatabaseConnectionTime();
        long totalTime = System.currentTimeMillis() - startTime;

        String status = determineOverallStatus(databaseHealthy);
        String description = generateDescription(status);
        String details = generateDetails(databaseHealthy, databaseConnectionTime);

        log.info("Health check completed. Status: {}, DatabaseHealthy: {}, TotalTime: {}ms",
                status, databaseHealthy, totalTime);

        return HealthStatus.builder()
                .status(status)
                .timestamp(LocalDateTime.now())
                .databaseHealthy(databaseHealthy)
                .description(description)
                .details(details)
                .build();
    }

    /**
     * Determines the overall system status based on component health.
     *
     * @param databaseHealthy true if database is operational
     * @return status string: "UP", "DOWN", or "DEGRADED"
     */
    private String determineOverallStatus(boolean databaseHealthy) {
        if (!databaseHealthy) {
            log.warn("Database is not healthy. Overall status: DOWN");
            return "DOWN";
        }
        return "UP";
    }

    /**
     * Generates a human-readable description of the health status.
     *
     * @param status the overall system status
     * @return descriptive message
     */
    private String generateDescription(String status) {
        return switch (status) {
            case "UP" -> "Application is fully operational. All dependencies are healthy.";
            case "DOWN" -> "Application is not operational. Critical dependencies are unavailable.";
            case "DEGRADED" -> "Application is partially operational. Some dependencies have issues.";
            default -> "Unknown status";
        };
    }

    /**
     * Generates detailed information about component health checks.
     *
     * @param databaseHealthy        true if database is operational
     * @param databaseConnectionTime connection verification time in milliseconds
     * @return detailed status information
     */
    private String generateDetails(boolean databaseHealthy, long databaseConnectionTime) {
        return String.format("Database: %s (%dms)",
                databaseHealthy ? "Healthy" : "Unhealthy",
                databaseConnectionTime);
    }
}
