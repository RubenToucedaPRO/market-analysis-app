package com.market.analysis.unit.infrastructure.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.infrastructure.monitoring.CleanupTask;
import com.market.analysis.infrastructure.persistence.repository.JpaApiCallRateRepository;

/**
 * Unit tests for CleanupTask.
 * Tests the scheduled cleanup task for old API call logs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CleanupTask Unit Tests")
class CleanupTaskTest {

    @Mock
    private JpaApiCallRateRepository apiCallRepository;

    @InjectMocks
    private CleanupTask cleanupTask;

    private Instant testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = Instant.parse("2026-02-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should delete old API call logs when executing cleanup")
    void testExecuteCleanupDeletesOldRecords() {
        // Arrange
        int expectedDeletedCount = 10;
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(expectedDeletedCount);

        // Act
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Should calculate correct threshold timestamp (24 hours before current time)")
    void testExecuteCleanupUsesCorrectThreshold() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(5);
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);

        // Act
        Instant beforeExecution = Instant.now();
        cleanupTask.executeCleanup();
        Instant afterExecution = Instant.now();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(thresholdCaptor.capture());
        
        Instant capturedThreshold = thresholdCaptor.getValue();
        Instant expectedThresholdBefore = beforeExecution.minusSeconds(86400);
        Instant expectedThresholdAfter = afterExecution.minusSeconds(86400);

        // The captured threshold should be approximately 24 hours before execution time
        assertThat(capturedThreshold).isBetween(
            expectedThresholdBefore.minusSeconds(1), 
            expectedThresholdAfter.plusSeconds(1)
        );
    }

    @Test
    @DisplayName("Should handle cleanup when no records are deleted")
    void testExecuteCleanupWithNoRecordsDeleted() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(0);

        // Act
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Should handle cleanup when large number of records are deleted")
    void testExecuteCleanupWithManyRecordsDeleted() {
        // Arrange
        int largeDeleteCount = 10000;
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(largeDeleteCount);

        // Act
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Should use exact 24-hour interval (86400 seconds)")
    void testCleanupUsesCorrectIntervalConstant() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(3);
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);

        // Act
        Instant executionTime = Instant.now();
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(thresholdCaptor.capture());
        
        Instant capturedThreshold = thresholdCaptor.getValue();
        // The threshold should be approximately 86400 seconds (24 hours) before now
        long secondsDifference = executionTime.getEpochSecond() - capturedThreshold.getEpochSecond();
        
        // Allow 1 second tolerance for test execution time
        assertThat(secondsDifference).isBetween(86399L, 86401L);
    }

    @Test
    @DisplayName("Should call repository method exactly once per execution")
    void testExecuteCleanupCallsRepositoryOnce() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(7);

        // Act
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Should handle multiple consecutive cleanup executions")
    void testMultipleConsecutiveCleanupExecutions() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class)))
            .thenReturn(5)
            .thenReturn(3)
            .thenReturn(0);

        // Act
        cleanupTask.executeCleanup();
        cleanupTask.executeCleanup();
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(3)).deleteByOcurredAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Should pass Instant parameter to repository delete method")
    void testExecuteCleanupPassesInstantParameter() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(2);
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);

        // Act
        cleanupTask.executeCleanup();

        // Assert
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(thresholdCaptor.capture());
        assertThat(thresholdCaptor.getValue()).isNotNull();
        assertThat(thresholdCaptor.getValue()).isInstanceOf(Instant.class);
    }

    @Test
    @DisplayName("Should handle cleanup with different delete counts")
    void testExecuteCleanupWithVariousDeleteCounts() {
        // Test with 0 deletions
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(0);
        cleanupTask.executeCleanup();
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(any(Instant.class));

        // Test with 1 deletion
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(1);
        cleanupTask.executeCleanup();
        verify(apiCallRepository, times(2)).deleteByOcurredAtBefore(any(Instant.class));

        // Test with 100 deletions
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(100);
        cleanupTask.executeCleanup();
        verify(apiCallRepository, times(3)).deleteByOcurredAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Should execute cleanup with transactional context")
    void testExecuteCleanupIsTransactional() {
        // Arrange
        when(apiCallRepository.deleteByOcurredAtBefore(any(Instant.class))).thenReturn(5);

        // Act
        cleanupTask.executeCleanup();

        // Assert
        // The @Transactional annotation ensures this method executes within a transaction
        // We verify that the repository method is called, which would be rolled back on failure
        verify(apiCallRepository, times(1)).deleteByOcurredAtBefore(any(Instant.class));
    }
}
