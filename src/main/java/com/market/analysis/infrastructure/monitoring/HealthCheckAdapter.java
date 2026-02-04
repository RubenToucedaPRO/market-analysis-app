package com.market.analysis.infrastructure.monitoring;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.port.out.HealthCheckPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Infrastructure Adapter for health check operations.
 * Implements the HealthCheckPort interface by directly accessing the DataSource
 * to verify database connectivity and performance.
 *
 * This adapter is responsible for:
 * - Acquiring connections from the DataSource
 * - Executing validation queries
 * - Measuring connection time
 * - Handling database-related exceptions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckAdapter implements HealthCheckPort {

    private final DataSource dataSource;

    /**
     * Checks if the database connection is healthy and responsive.
     * Attempts to acquire a connection from the DataSource and validates it.
     *
     * @return true if a valid connection is obtained, false if connection fails
     */
    @Override
    public boolean isDatabaseHealthy() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(5);
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }

    /**
     * Gets the time taken to verify database connectivity in milliseconds.
     * Measures the time required to acquire and validate a connection.
     *
     * @return connection verification time in milliseconds, or -1 if connection
     *         fails
     */
    @Override
    public long getDatabaseConnectionTime() {
        long startTime = System.currentTimeMillis();

        try {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    return System.currentTimeMillis() - startTime;
                }
            }
        } catch (Exception e) {
            log.error("Failed to measure database connection time", e);
        }

        return -1;
    }
}
