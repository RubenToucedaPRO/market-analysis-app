package com.market.analysis.domain.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing the health status of the application.
 * Contains information about the current system state and its components.
 */
@Getter
@Builder
@ToString
public class HealthStatus {

    /**
     * Overall status of the system: UP, DOWN, or DEGRADED.
     */
    private final String status;

    /**
     * Timestamp when the health check was performed.
     */
    private final LocalDateTime timestamp;

    /**
     * Indicates if the database connection is healthy.
     */
    private final boolean databaseHealthy;

    /**
     * Description of the current health state.
     */
    private final String description;

    /**
     * Additional details about the health check (e.g., database connection time).
     */
    private final String details;
}
