package com.market.analysis.unit.application.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;

import com.market.analysis.application.job.BackgroundJob;
import com.market.analysis.application.job.BackgroundJobService;
import com.market.analysis.application.job.JobRejectedException;
import com.market.analysis.application.job.JobStatus;

@DisplayName("BackgroundJobService Unit Tests")
class BackgroundJobServiceTest {

    private List<Runnable> queuedTasks;
    private BackgroundJobService jobService;

    @BeforeEach
    void setUp() {
        queuedTasks = new ArrayList<>();
        jobService = new BackgroundJobService(runnable -> queuedTasks.add(runnable), Duration.ofMinutes(60));
    }

    @Test
    @DisplayName("Should submit job without blocking and track it as pending")
    void testSubmitTracksPendingJob() {
        String jobId = jobService.submit("kind", 7L, job -> {
        });

        assertNotNull(jobId);
        assertEquals(1, queuedTasks.size());
        Optional<BackgroundJob> job = jobService.getJob(jobId);
        assertTrue(job.isPresent());
        assertEquals(JobStatus.PENDING, job.get().getStatus());
        assertEquals("kind", job.get().getSubjectKind());
        assertEquals(7L, job.get().getSubjectId());
    }

    @Test
    @DisplayName("Should isolate deduplication by kind")
    void testDedupIsScopedByKind() {
        String first = jobService.submit("kind-a", 7L, job -> {
        });
        String second = jobService.submit("kind-b", 7L, job -> {
        });

        assertNotEquals(first, second);
        assertEquals(2, queuedTasks.size());
    }

    @Test
    @DisplayName("Should reuse active job for same kind and subject")
    void testSubmitReusesActiveJob() {
        String first = jobService.submit("kind", 7L, job -> {
        });
        String second = jobService.submit("kind", 7L, job -> {
        });

        assertEquals(first, second);
        assertEquals(1, queuedTasks.size());
    }

    @Test
    @DisplayName("Should run task to DONE")
    void testWorkerCompletesJob() {
        AtomicBoolean executed = new AtomicBoolean(false);

        String jobId = jobService.submit("kind", 7L, job -> {
            job.setAttribute("answer", "42");
            executed.set(true);
        });
        queuedTasks.get(0).run();

        assertTrue(executed.get());
        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.DONE, job.getStatus());
        assertEquals("42", job.getAttribute("answer"));
        assertNotNull(job.getFinishedAt());
    }

    @Test
    @DisplayName("Should mark job FAILED when task throws")
    void testWorkerMarksFailedOnException() {
        String jobId = jobService.submit("kind", 7L, job -> {
            throw new IllegalStateException("boom");
        });
        queuedTasks.get(0).run();

        BackgroundJob job = jobService.getJob(jobId).orElseThrow();
        assertEquals(JobStatus.FAILED, job.getStatus());
        assertTrue(job.getErrorDetail().contains("boom"));
    }

    @Test
    @DisplayName("Should throw and forget job when executor rejects")
    void testSubmitRejectedRemovesJob() {
        BackgroundJobService rejectingService = new BackgroundJobService(
                runnable -> {
                    throw new TaskRejectedException("queue full");
                },
                Duration.ofMinutes(60));

        assertThrows(JobRejectedException.class,
                () -> rejectingService.submit("kind", 7L, job -> {
                }));
        assertTrue(rejectingService.findActiveJobId("kind", 7L).isEmpty());
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
        BackgroundJobService zeroTtlService = new BackgroundJobService(Runnable::run, Duration.ZERO);
        String doneId = zeroTtlService.submit("kind", 7L, job -> {
        });
        assertEquals(JobStatus.DONE, zeroTtlService.getJob(doneId).orElseThrow().getStatus());

        zeroTtlService.purgeExpiredJobs();

        assertTrue(zeroTtlService.getJob(doneId).isEmpty());

        String pendingId = jobService.submit("kind", 9L, job -> {
        });
        jobService.purgeExpiredJobs();

        assertTrue(jobService.getJob(pendingId).isPresent());
    }
}
