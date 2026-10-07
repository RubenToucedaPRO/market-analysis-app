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
import com.market.analysis.application.job.BackgroundJob;
import com.market.analysis.application.job.BackgroundJobService;
import com.market.analysis.application.job.JobRejectedException;
import com.market.analysis.application.job.JobStatus;
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
        BackgroundJobService jobs = new BackgroundJobService(
                runnable -> queuedTasks.add(runnable), Duration.ofMinutes(60));
        jobService = new SuggestTickerJobService(suggestTickersUseCase, jobs);
    }

    @Test
    @DisplayName("Should submit job without blocking and track it as pending")
    void testSubmitTracksPendingJob() {
        String jobId = jobService.submitSuggestionJob(7L);

        assertNotNull(jobId);
        assertEquals(1, queuedTasks.size());
        Optional<BackgroundJob> job = jobService.getJob(jobId);
        assertTrue(job.isPresent());
        assertEquals(JobStatus.PENDING, job.get().getStatus());
        assertEquals(7L, job.get().getSubjectId());
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

        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.DONE, job.getStatus());
        assertEquals("1", job.getAttribute("suggested"));
        assertEquals("1", job.getAttribute("discarded"));
        assertNotNull(job.getFinishedAt());
        verify(suggestTickersUseCase).suggestTickers(any());
    }

    @Test
    @DisplayName("Should mark job FAILED when use case throws")
    void testWorkerMarksFailedOnException() {
        when(suggestTickersUseCase.suggestTickers(any())).thenThrow(new RuntimeException("Polygon down"));

        String jobId = jobService.submitSuggestionJob(7L);
        queuedTasks.get(0).run();

        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getFinishedAt());
        assertTrue(job.getErrorDetail().contains("Polygon down"));
    }

    @Test
    @DisplayName("Should throw and forget job when executor rejects")
    void testSubmitRejectedRemovesJob() {
        BackgroundJobService rejectingJobs = new BackgroundJobService(
                runnable -> {
                    throw new TaskRejectedException("queue full");
                },
                Duration.ofMinutes(60));
        SuggestTickerJobService rejectingService = new SuggestTickerJobService(suggestTickersUseCase, rejectingJobs);

        assertThrows(JobRejectedException.class, () -> rejectingService.submitSuggestionJob(7L));
    }

    @Test
    @DisplayName("Should return empty for unknown or blank job ids")
    void testGetJobUnknown() {
        assertTrue(jobService.getJob("no-such-job").isEmpty());
        assertTrue(jobService.getJob(null).isEmpty());
        assertTrue(jobService.getJob("  ").isEmpty());
    }

    @Test
    @DisplayName("Should find active job id by strategy")
    void testFindActiveJobIdByStrategyId() {
        String jobId = jobService.submitSuggestionJob(7L);
        jobService.submitSuggestionJob(8L);

        assertEquals(Optional.of(jobId), jobService.findActiveJobIdByStrategyId(7L));
        assertTrue(jobService.findActiveJobIdByStrategyId(99L).isEmpty());
    }

    @Test
    @DisplayName("Should not return terminal jobs from active lookup")
    void testFindActiveIgnoresTerminalJobs() {
        BackgroundJobService directJobs = new BackgroundJobService(Runnable::run, Duration.ZERO);
        SuggestTickerJobService directService = new SuggestTickerJobService(suggestTickersUseCase, directJobs);
        when(suggestTickersUseCase.suggestTickers(any())).thenReturn(SuggestTickersResponseDTO.builder().build());
        String jobId = directService.submitSuggestionJob(7L);

        assertEquals(JobStatus.DONE, directService.getJob(jobId).orElseThrow().getStatus());
        assertTrue(directService.findActiveJobIdByStrategyId(7L).isEmpty());
    }
}
