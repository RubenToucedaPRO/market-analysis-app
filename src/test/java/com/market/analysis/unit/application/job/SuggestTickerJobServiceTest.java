package com.market.analysis.unit.application.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.application.job.SuggestJobRejectedException;
import com.market.analysis.application.job.SuggestJobStatus;
import com.market.analysis.application.job.SuggestTickerJob;
import com.market.analysis.application.job.SuggestTickerJobService;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;

@DisplayName("SuggestTickerJobService Unit Tests")
@ExtendWith(MockitoExtension.class)
class SuggestTickerJobServiceTest {

    @Mock
    private SuggestTickersUseCase suggestTickersUseCase;

    private List<Runnable> queuedTasks;
    private SuggestTickerJobService jobService;

    @BeforeEach
    void setUp() {
        queuedTasks = new ArrayList<>();
        jobService = new SuggestTickerJobService(suggestTickersUseCase, runnable -> queuedTasks.add(runnable),
                Duration.ofMinutes(60));
    }

    @Test
    @DisplayName("Should submit job without blocking and track it as pending")
    void testSubmitTracksPendingJob() {
        String jobId = jobService.submitSuggestionJob(7L);

        assertNotNull(jobId);
        assertEquals(1, queuedTasks.size());
        Optional<SuggestTickerJob> job = jobService.getJob(jobId);
        assertTrue(job.isPresent());
        assertEquals(SuggestJobStatus.PENDING, job.get().getStatus());
        assertEquals(7L, job.get().getStrategyId());
    }

    @Test
    @DisplayName("Should reuse active job for same strategy instead of duplicating work")
    void testSubmitReusesActiveJob() {
        String first = jobService.submitSuggestionJob(7L);
        String second = jobService.submitSuggestionJob(7L);

        assertEquals(first, second);
        assertEquals(1, queuedTasks.size());
    }

    @Test
    @DisplayName("Should create separate jobs for different strategies")
    void testSubmitCreatesSeparateJobsPerStrategy() {
        String first = jobService.submitSuggestionJob(7L);
        String second = jobService.submitSuggestionJob(8L);

        assertNotEquals(first, second);
        assertEquals(2, queuedTasks.size());
    }

    @Test
    @DisplayName("Should run worker to DONE and count tickers by suitability")
    void testWorkerCompletesJob() {
        SuggestTickersResponseDTO response = SuggestTickersResponseDTO.builder()
                .suggestedTickers(List.of(
                        SuggestedTickerDTO.builder().ticker("AAPL").suitabilityStatus(TickerSuitabilityStatus.APTO).build(),
                        SuggestedTickerDTO.builder().ticker("TSLA").suitabilityStatus(TickerSuitabilityStatus.NO_APTO).build()))
                .build();
        when(suggestTickersUseCase.suggestTickers(any())).thenReturn(response);

        String jobId = jobService.submitSuggestionJob(7L);
        queuedTasks.get(0).run();

        SuggestTickerJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(SuggestJobStatus.DONE, job.getStatus());
        assertEquals(1, job.getSuggestedCount());
        assertEquals(1, job.getDiscardedCount());
        assertNotNull(job.getFinishedAt());
        verify(suggestTickersUseCase).suggestTickers(any());
    }

    @Test
    @DisplayName("Should mark job FAILED when use case throws")
    void testWorkerMarksFailedOnException() {
        when(suggestTickersUseCase.suggestTickers(any())).thenThrow(new RuntimeException("Polygon down"));

        String jobId = jobService.submitSuggestionJob(7L);
        queuedTasks.get(0).run();

        SuggestTickerJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(SuggestJobStatus.FAILED, job.getStatus());
        assertNotNull(job.getFinishedAt());
        assertTrue(job.getErrorDetail().contains("Polygon down"));
    }

    @Test
    @DisplayName("Should throw and forget job when executor rejects")
    void testSubmitRejectedRemovesJob() {
        SuggestTickerJobService rejectingService = new SuggestTickerJobService(suggestTickersUseCase,
                runnable -> {
                    throw new TaskRejectedException("queue full");
                },
                Duration.ofMinutes(60));

        assertThrows(SuggestJobRejectedException.class, () -> rejectingService.submitSuggestionJob(7L));
    }

    @Test
    @DisplayName("Should return empty for unknown or blank job ids")
    void testGetJobUnknown() {
        assertTrue(jobService.getJob("no-such-job").isEmpty());
        assertTrue(jobService.getJob(null).isEmpty());
        assertTrue(jobService.getJob("  ").isEmpty());
    }

    @Test
    @DisplayName("Should purge expired terminal jobs but keep active ones")
    void testPurgeExpiredJobs() {
        SuggestTickerJobService zeroTtlService = new SuggestTickerJobService(suggestTickersUseCase,
                Runnable::run, Duration.ZERO);
        when(suggestTickersUseCase.suggestTickers(any())).thenReturn(SuggestTickersResponseDTO.builder().build());

        String doneId = zeroTtlService.submitSuggestionJob(7L);
        assertEquals(SuggestJobStatus.DONE, zeroTtlService.getJob(doneId).orElseThrow().getStatus());

        zeroTtlService.purgeExpiredJobs();

        assertTrue(zeroTtlService.getJob(doneId).isEmpty());

        String pendingId = jobService.submitSuggestionJob(9L);
        jobService.purgeExpiredJobs();

        assertTrue(jobService.getJob(pendingId).isPresent());
    }

    @Test
    @DisplayName("Should reject invalid state transitions")
    void testInvalidTransitions() {
        SuggestTickerJob job = new SuggestTickerJob("job-1", 7L, Instant.now());
        assertTrue(!job.isTerminal());
        assertTrue(job.isActive());

        job.markRunning();
        job.complete(0, 0);

        assertTrue(job.isTerminal());
        assertTrue(!job.isActive());
        assertThrows(IllegalStateException.class, job::markRunning);
        assertThrows(IllegalStateException.class, () -> job.complete(0, 0));
        assertThrows(IllegalStateException.class, () -> job.fail("x"));
    }

    @Test
    @DisplayName("Should expire only terminal jobs older than TTL")
    void testJobExpiryBoundary() {
        SuggestTickerJob job = new SuggestTickerJob("job-1", 7L, Instant.now().minusSeconds(3600));

        assertTrue(!job.isExpired(Instant.now(), Duration.ZERO));

        job.markRunning();

        assertTrue(!job.isExpired(Instant.now(), Duration.ZERO));

        job.complete(0, 0);

        assertTrue(job.isExpired(Instant.now(), Duration.ZERO));
        assertTrue(!job.isExpired(Instant.now(), Duration.ofHours(2)));
    }
}
