package com.market.analysis.domain.model;

/**
 * Canonical enumeration of application health status codes.
 *
 * <p>Used across the domain and application layers to represent
 * the overall health state of the system in a type-safe manner.</p>
 */
public enum HealthStatusCode {

    UP("UP"),
    DOWN("DOWN"),
    DEGRADED("DEGRADED");

    private final String status;

    HealthStatusCode(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Returns the {@link HealthStatusCode} for the given status string.
     *
     * @param status the status string (case-sensitive)
     * @return the matching enum constant
     * @throws IllegalArgumentException if no constant matches the status
     */
    public static HealthStatusCode fromStatus(String status) {
        for (HealthStatusCode code : values()) {
            if (code.status.equals(status)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown health status: " + status);
    }
}
