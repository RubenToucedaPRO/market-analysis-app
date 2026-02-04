package com.market.analysis.domain.port.out;

/**
 * Port interface for health check operations.
 * Defines the contract for checking the health status of external dependencies
 * without binding to specific implementations or frameworks.
 *
 * This interface allows the domain layer to remain technology-agnostic while
 * enabling infrastructure implementations to verify system connectivity and
 * status.
 */
public interface HealthCheckPort {

    /**
     * Checks if the database connection is healthy and responsive.
     *
     * @return true if the database is accessible and operational, false otherwise
     */
    boolean isDatabaseHealthy();

    /**
     * Gets the database connection time in milliseconds.
     * Useful for monitoring performance and detecting slow connections.
     *
     * @return the time taken to verify database connectivity in milliseconds
     */
    long getDatabaseConnectionTime();
}
