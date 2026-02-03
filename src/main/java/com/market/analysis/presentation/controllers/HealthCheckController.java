package com.market.analysis.presentation.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.market.analysis.application.mappers.HealthCheckMapper;
import com.market.analysis.application.services.HealthCheckService;
import com.market.analysis.domain.entities.HealthStatus;
import com.market.analysis.presentation.dto.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for health check operations.
 * Exposes HTTP endpoints for monitoring application and dependency health.
 *
 * Endpoints:
 * - GET /health: Returns the current health status of the application
 */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    private final HealthCheckService healthCheckService;
    private final HealthCheckMapper healthCheckMapper;

    /**
     * Retrieves the current health status of the application.
     * Performs active verification of critical dependencies including database
     * connectivity.
     *
     * @return ResponseEntity containing HealthCheckResponse with appropriate HTTP
     *         status
     *         - 200 OK if the application is healthy
     *         - 503 SERVICE_UNAVAILABLE if the application is unhealthy
     */
    @GetMapping
    public ResponseEntity<HealthCheckResponse> getHealth() {
        log.debug("Health check endpoint called");

        HealthStatus healthStatus = healthCheckService.performHealthCheck();
        HealthCheckResponse response = healthCheckMapper.toResponse(healthStatus);

        HttpStatus httpStatus = "UP".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        log.debug("Returning health check response with status: {}", httpStatus);
        return ResponseEntity.status(httpStatus).body(response);
    }
}
