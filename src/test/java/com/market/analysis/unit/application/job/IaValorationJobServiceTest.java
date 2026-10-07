package com.market.analysis.unit.application.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.market.analysis.application.job.BackgroundJob;
import com.market.analysis.application.job.BackgroundJobService;
import com.market.analysis.application.job.IaValorationJobService;
import com.market.analysis.application.job.JobRejectedException;
import com.market.analysis.application.job.JobStatus;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;

@DisplayName("IaValorationJobService Unit Tests")
@ExtendWith(MockitoExtension.class)
class IaValorationJobServiceTest {

    @Mock
    private ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;

    private List<Runnable> queuedTasks;
    private IaValorationJobService jobService;

    @BeforeEach
    void setUp() {
        queuedTasks = new ArrayList<>();
        BackgroundJobService jobs = new BackgroundJobService(
                runnable -> queuedTasks.add(runnable), Duration.ofMinutes(60));
        jobService = new IaValorationJobService(manageAnalyzeTickerUseCase, jobs);
    }

    @Test
    @DisplayName("Should submit job without blocking and track it as pending")
    void testSubmitTracksPendingJob() {
        String jobId = jobService.submitValorationJob(7L);

        assertNotNull(jobId);
        assertEquals(1, queuedTasks.size());
        Optional<BackgroundJob> job = jobService.getJob(jobId);
        assertTrue(job.isPresent());
        assertEquals(JobStatus.PENDING, job.get().getStatus());
        assertEquals(7L, job.get().getSubjectId());
    }

    @Test
    @DisplayName("Should reuse active job for same ticker instead of burning quota twice")
    void testSubmitReusesActiveJob() {
        String first = jobService.submitValorationJob(7L);
        String second = jobService.submitValorationJob(7L);

        assertEquals(first, second);
        assertEquals(1, queuedTasks.size());
    }

    @Test
    @DisplayName("Should create separate jobs for different tickers")
    void testSubmitCreatesSeparateJobsPerTicker() {
        String first = jobService.submitValorationJob(7L);
        String second = jobService.submitValorationJob(8L);

        assertNotEquals(first, second);
        assertEquals(2, queuedTasks.size());
    }

    @Test
    @DisplayName("Should record generated flag when worker finishes")
    void testWorkerRecordsGeneratedFlag() {
        when(manageAnalyzeTickerUseCase.getValorationIA(7L)).thenReturn(true);

        String jobId = jobService.submitValorationJob(7L);
        queuedTasks.get(0).run();

        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.DONE, job.getStatus());
        assertEquals("true", job.getAttribute("generated"));
        verify(manageAnalyzeTickerUseCase).getValorationIA(7L);
    }

    @Test
    @DisplayName("Should record generated false when use case returns fallback")
    void testWorkerRecordsFallback() {
        when(manageAnalyzeTickerUseCase.getValorationIA(anyLong())).thenReturn(false);

        String jobId = jobService.submitValorationJob(7L);
        queuedTasks.get(0).run();

        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.DONE, job.getStatus());
        assertEquals("false", job.getAttribute("generated"));
    }

    @Test
    @DisplayName("Should mark job FAILED when use case throws")
    void testWorkerMarksFailedOnException() {
        when(manageAnalyzeTickerUseCase.getValorationIA(7L)).thenThrow(new RuntimeException("LLM down"));

        String jobId = jobService.submitValorationJob(7L);
        queuedTasks.get(0).run();

        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.FAILED, job.getStatus());
        assertTrue(job.getErrorDetail().contains("LLM down"));
    }

    @Test
    @DisplayName("Should throw and forget job when executor rejects")
    void testSubmitRejectedRemovesJob() {
        BackgroundJobService rejectingJobs = new BackgroundJobService(
                runnable -> {
                    throw new TaskRejectedException("queue full");
                },
                Duration.ofMinutes(60));
        IaValorationJobService rejectingService = new IaValorationJobService(manageAnalyzeTickerUseCase, rejectingJobs);

        assertThrows(JobRejectedException.class, () -> rejectingService.submitValorationJob(7L));
    }

    @Test
    @DisplayName("Should find active job id by ticker")
    void testFindActiveJobIdByTickerId() {
        String jobId = jobService.submitValorationJob(7L);

        assertEquals(Optional.of(jobId), jobService.findActiveJobIdByTickerId(7L));
        assertTrue(jobService.findActiveJobIdByTickerId(99L).isEmpty());
    }
}
