package com.market.analysis.presentation.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * DTO for presenting health check information to REST clients.
 * Maps the domain HealthStatus entity to a JSON-serializable format
 * suitable for HTTP responses.
 */
@Getter
@Builder
@ToString
public class HealthCheckResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("database_healthy")
    private boolean databaseHealthy;

    @JsonProperty("description")
    private String description;

    @JsonProperty("details")
    private String details;

    /**
     * HTTP status code for the health check response.
     * 200 for UP, 503 for DOWN.
     */
    @JsonProperty("http_status_code")
    private int httpStatusCode;
}
