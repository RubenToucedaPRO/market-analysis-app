package com.market.analysis.unit.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.infrastructure.monitoring.HealthCheckAdapter;

/**
 * Unit tests for HealthCheckAdapter.
 * Tests the infrastructure adapter for database health verification.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckAdapter Unit Tests")
class HealthCheckAdapterTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private HealthCheckAdapter healthCheckAdapter;

    @BeforeEach
    void setUp() {
        healthCheckAdapter = new HealthCheckAdapter(dataSource);
    }

    @Test
    @DisplayName("Should return true when database connection is valid")
    void testIsDatabaseHealthyReturnsTrueWhenValid() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        // Act
        boolean result = healthCheckAdapter.isDatabaseHealthy();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when database connection is invalid")
    void testIsDatabaseHealthyReturnsFalseWhenInvalid() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        // Act
        boolean result = healthCheckAdapter.isDatabaseHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when connection acquisition fails")
    void testIsDatabaseHealthyReturnsFalseOnException() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        // Act
        boolean result = healthCheckAdapter.isDatabaseHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return positive connection time when database is healthy")
    void testGetDatabaseConnectionTimeReturnsPositiveWhenHealthy() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        // Act
        long connectionTime = healthCheckAdapter.getDatabaseConnectionTime();

        // Assert
        assertTrue(connectionTime >= 0);
    }

    @Test
    @DisplayName("Should return -1 connection time when connection validation fails")
    void testGetDatabaseConnectionTimeReturnsMinusOneWhenConnectionFails() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        // Act
        long connectionTime = healthCheckAdapter.getDatabaseConnectionTime();

        // Assert
        assertEquals(-1L, connectionTime);
    }

    @Test
    @DisplayName("Should return -1 connection time when connection acquisition fails")
    void testGetDatabaseConnectionTimeReturnsMinusOneOnException() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        // Act
        long connectionTime = healthCheckAdapter.getDatabaseConnectionTime();

        // Assert
        assertEquals(-1L, connectionTime);
    }

    @Test
    @DisplayName("Should measure reasonable connection time")
    void testGetDatabaseConnectionTimeMeasuresReasonableTime() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        // Act
        long connectionTime = healthCheckAdapter.getDatabaseConnectionTime();

        // Assert
        assertTrue(connectionTime >= 0);
        assertLessThan(connectionTime, 5000); // Should complete within 5 seconds
    }

    private void assertLessThan(long actual, long expected) {
        assertTrue(actual < expected, String.format("Expected %d to be less than %d", actual, expected));
    }
}
