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

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;

/**
 * Orchestrates asynchronous ticker-suggestion jobs.
 *
 * <p>Submitting is non-blocking: the long-running use case
 * ({@link SuggestTickersUseCase#suggestTickers}, minutes due to the Polygon
 * rate limit) runs on a bounded background executor while HTTP requests stay
 * short. State lives in memory; the business result is persisted as a
 * suggestion snapshot by the use case itself, so page refreshes already show
 * finished results without extra plumbing.
 *
 * <p>Deduplication policy: at most one active job per strategy; a second
 * submit while one is active reuses the running job id instead of consuming
 * API quota twice.
 */
public class SuggestTickerJobService {

    private static final Logger log = LoggerFactory.getLogger(SuggestTickerJobService.class);

    private final SuggestTickersUseCase suggestTickersUseCase;
    private final TaskExecutor taskExecutor;
    private final Duration jobTtl;
    private final ConcurrentMap<String, SuggestTickerJob> jobs = new ConcurrentHashMap<>();

    public SuggestTickerJobService(SuggestTickersUseCase suggestTickersUseCase,
            TaskExecutor taskExecutor,
            Duration jobTtl) {
        this.suggestTickersUseCase = suggestTickersUseCase;
        this.taskExecutor = taskExecutor;
        this.jobTtl = jobTtl;
    }

    /**
     * Submits a suggestion job for the given strategy without blocking.
     *
     * @param strategyId the strategy to analyze
     * @return the new job id, or the id of the already active job for the strategy
     * @throws SuggestJobRejectedException if the background queue is full
     */
    public String submitSuggestionJob(long strategyId) {
        synchronized (jobs) {
            Optional<String> active = jobs.values().stream()
                    .filter(job -> job.getStrategyId() == strategyId && job.isActive())
                    .map(SuggestTickerJob::getJobId)
                    .findFirst();
            if (active.isPresent()) {
                log.info("suggest_job_reused jobId={} strategyId={}", active.get(), strategyId);
                return active.get();
            }
            SuggestTickerJob job = new SuggestTickerJob(UUID.randomUUID().toString(), strategyId, Instant.now());
            jobs.put(job.getJobId(), job);
            try {
                taskExecutor.execute(() -> runJob(job));
            } catch (TaskRejectedException ex) {
                jobs.remove(job.getJobId());
                log.warn("suggest_job_rejected strategyId={} message={}", strategyId, ex.getMessage());
                throw new SuggestJobRejectedException("Suggestion queue is full", ex);
            }
            log.info("suggest_job_submitted jobId={} strategyId={}", job.getJobId(), strategyId);
            return job.getJobId();
        }
    }

    /**
     * Returns the current state of a job, if still known.
     *
     * @param jobId the job id returned at submit time
     * @return the job, or empty when unknown or already purged
     */
    public Optional<SuggestTickerJob> getJob(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(jobs.get(jobId));
    }

    /**
     * Removes terminal jobs older than the TTL. Runs on the existing
     * scheduling infrastructure; cadence comes from properties.
     */
    @Scheduled(fixedDelayString = "${suggest.job.cleanup-interval-ms:1800000}")
    public void purgeExpiredJobs() {
        Instant now = Instant.now();
        List<String> expired = jobs.values().stream()
                .filter(job -> job.isExpired(now, jobTtl))
                .map(SuggestTickerJob::getJobId)
                .toList();
        expired.forEach(jobs::remove);
        if (!expired.isEmpty()) {
            log.info("suggest_job_purged count={}", expired.size());
        }
    }

    private void runJob(SuggestTickerJob job) {
        job.markRunning();
        log.info("suggest_job_started jobId={} strategyId={}", job.getJobId(), job.getStrategyId());
        try {
            SuggestTickersResponseDTO response = suggestTickersUseCase.suggestTickers(
                    SuggestTickersRequestDTO.builder()
                            .strategyId(job.getStrategyId())
                            .build());
            job.complete(countByStatus(response, TickerSuitabilityStatus.APTO),
                    countByStatus(response, TickerSuitabilityStatus.NO_APTO));
            log.info("suggest_job_done jobId={} strategyId={} suggested={} discarded={}",
                    job.getJobId(), job.getStrategyId(), job.getSuggestedCount(), job.getDiscardedCount());
        } catch (RuntimeException ex) {
            log.error("suggest_job_failed jobId={} strategyId={} error={}",
                    job.getJobId(), job.getStrategyId(), ex.toString());
            job.fail(ex.toString());
        }
    }

    private int countByStatus(SuggestTickersResponseDTO response, TickerSuitabilityStatus status) {
        if (response == null || response.getSuggestedTickers() == null) {
            return 0;
        }
        int count = 0;
        for (SuggestedTickerDTO ticker : response.getSuggestedTickers()) {
            if (ticker != null && status == ticker.getSuitabilityStatus()) {
                count++;
            }
        }
        return count;
    }
}
