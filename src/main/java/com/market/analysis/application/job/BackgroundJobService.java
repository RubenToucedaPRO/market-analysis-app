package com.market.analysis.application.job;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Flow-agnostic orchestrator of asynchronous background jobs.
 *
 * <p>Submitting is non-blocking: the given task runs on a bounded background
 * executor while HTTP requests stay short. Deduplication is scoped by
 * {@code (kind, subjectId)}: submitting while one is active reuses it instead
 * of consuming external API quota twice. State lives in memory; domain results
 * are persisted by each task, so page refreshes already show finished work.
 *
 * <p>One instance is wired per flow, each with its own executor (different
 * rate limits and SLAs must not starve each other).
 */
public class BackgroundJobService {

    private static final Logger log = LoggerFactory.getLogger(BackgroundJobService.class);

    /**
     * Unit of background work. Receives the job to record flow-specific
     * outcome attributes; lifecycle transitions are handled by the service.
     * Tasks only throw unchecked exceptions; failures mark the job FAILED.
     */
    @FunctionalInterface
    public interface JobTask {
        void run(BackgroundJob job);
    }

    private final TaskExecutor taskExecutor;
    private final Duration jobTtl;
    private final ConcurrentMap<String, BackgroundJob> jobs = new ConcurrentHashMap<>();

    public BackgroundJobService(TaskExecutor taskExecutor, Duration jobTtl) {
        this.taskExecutor = taskExecutor;
        this.jobTtl = jobTtl;
    }

    /**
     * Submits a job for the given subject without blocking.
     *
     * @param kind textual namespace isolating deduplication domains
     * @param subjectId the domain entity the job works on
     * @param task the background work to execute
     * @return the new job id, or the id of the already active job
     * @throws JobRejectedException if the background queue is full
     */
    public String submit(String kind, long subjectId, JobTask task) {
        synchronized (jobs) {
            Optional<String> active = findActiveJob(kind, subjectId).map(BackgroundJob::getJobId);
            if (active.isPresent()) {
                log.info("background_job_reused jobId={} kind={} subjectId={}", active.get(), kind, subjectId);
                return active.get();
            }
            BackgroundJob job = new BackgroundJob(UUID.randomUUID().toString(), kind, subjectId, Instant.now());
            jobs.put(job.getJobId(), job);
            try {
                taskExecutor.execute(() -> runJob(job, task));
            } catch (TaskRejectedException ex) {
                jobs.remove(job.getJobId());
                log.warn("background_job_rejected kind={} subjectId={} message={}", kind, subjectId, ex.getMessage());
                throw new JobRejectedException("Background job queue is full", ex);
            }
            log.info("background_job_submitted jobId={} kind={} subjectId={}", job.getJobId(), kind, subjectId);
            return job.getJobId();
        }
    }

    /**
     * Returns the current state of a job, if still known.
     *
     * @param jobId the job id returned at submit time
     * @return the job, or empty when unknown or already purged
     */
    public Optional<BackgroundJob> getJob(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(jobs.get(jobId));
    }

    /**
     * Finds the active job id for a subject, if any. Used to re-attach the
     * progress banner when the user returns to the page without the job id
     * in the URL.
     *
     * @param kind the deduplication namespace
     * @param subjectId the domain entity to look up
     * @return the active job id, or empty when none is running
     */
    public Optional<String> findActiveJobId(String kind, long subjectId) {
        synchronized (jobs) {
            return findActiveJob(kind, subjectId).map(BackgroundJob::getJobId);
        }
    }

    /**
     * Removes terminal jobs older than the TTL. Runs on the existing
     * scheduling infrastructure; cadence comes from properties.
     */
    @Scheduled(fixedDelayString = "${job.cleanup-interval-ms:1800000}")
    public void purgeExpiredJobs() {
        Instant now = Instant.now();
        List<String> expired = jobs.values().stream()
                .filter(job -> job.isExpired(now, jobTtl))
                .map(BackgroundJob::getJobId)
                .toList();
        expired.forEach(jobs::remove);
        if (!expired.isEmpty()) {
            log.info("background_job_purged count={}", expired.size());
        }
    }

    private Optional<BackgroundJob> findActiveJob(String kind, long subjectId) {
        return jobs.values().stream()
                .filter(job -> job.getSubjectKind().equals(kind)
                        && job.getSubjectId() == subjectId
                        && job.isActive())
                .findFirst();
    }

    private void runJob(BackgroundJob job, JobTask task) {
        job.markRunning();
        log.info("background_job_started jobId={} kind={} subjectId={}",
                job.getJobId(), job.getSubjectKind(), job.getSubjectId());
        try {
            task.run(job);
            job.markDone();
            log.info("background_job_done jobId={} kind={} subjectId={}",
                    job.getJobId(), job.getSubjectKind(), job.getSubjectId());
        } catch (RuntimeException ex) {
            log.error("background_job_failed jobId={} kind={} subjectId={} error={}",
                    job.getJobId(), job.getSubjectKind(), job.getSubjectId(), ex.toString());
            job.fail(ex.toString());
        }
    }
}
