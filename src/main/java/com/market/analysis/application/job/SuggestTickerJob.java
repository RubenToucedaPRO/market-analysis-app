package com.market.analysis.application.job;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Mutable state holder of a single asynchronous ticker-suggestion job.
 *
 * <p>Thread-safe: state transitions are synchronized because the submitting
 * request thread and the background worker thread touch the same instance.
 * The heavy business result itself is <strong>not</strong> stored here; on
 * success it is persisted as a suggestion snapshot by the use case, and the
 * job only keeps summary counts for the status payload.
 */
public class SuggestTickerJob {

    private final String jobId;
    private final long strategyId;
    private final Instant startedAt;

    private volatile SuggestJobStatus status;
    private volatile Instant finishedAt;
    private volatile String errorDetail;
    private volatile int suggestedCount;
    private volatile int discardedCount;

    public SuggestTickerJob(String jobId, long strategyId, Instant startedAt) {
        this.jobId = Objects.requireNonNull(jobId, "jobId");
        this.strategyId = strategyId;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.status = SuggestJobStatus.PENDING;
    }

    public String getJobId() {
        return jobId;
    }

    public long getStrategyId() {
        return strategyId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public SuggestJobStatus getStatus() {
        return status;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    /**
     * Technical failure detail for server logs. Never rendered raw in the UI;
     * the presentation layer resolves a localized message instead.
     */
    public String getErrorDetail() {
        return errorDetail;
    }

    public int getSuggestedCount() {
        return suggestedCount;
    }

    public int getDiscardedCount() {
        return discardedCount;
    }

    public boolean isActive() {
        return status == SuggestJobStatus.PENDING || status == SuggestJobStatus.RUNNING;
    }

    public boolean isTerminal() {
        return status == SuggestJobStatus.DONE || status == SuggestJobStatus.FAILED;
    }

    public synchronized void markRunning() {
        requireState(SuggestJobStatus.PENDING, "markRunning");
        this.status = SuggestJobStatus.RUNNING;
    }

    public synchronized void complete(int suggestedCount, int discardedCount) {
        requireActive("complete");
        this.suggestedCount = suggestedCount;
        this.discardedCount = discardedCount;
        this.status = SuggestJobStatus.DONE;
        this.finishedAt = Instant.now();
    }

    public synchronized void fail(String errorDetail) {
        requireActive("fail");
        this.errorDetail = errorDetail;
        this.status = SuggestJobStatus.FAILED;
        this.finishedAt = Instant.now();
    }

    /**
     * Tells whether a terminal job is older than the given TTL.
     * Used by the purge routine; active jobs never expire.
     */
    public boolean isExpired(Instant now, Duration ttl) {
        return isTerminal() && finishedAt != null && finishedAt.plus(ttl).isBefore(now);
    }

    private void requireState(SuggestJobStatus expected, String transition) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Cannot " + transition + " job " + jobId + " from state " + status);
        }
    }

    private void requireActive(String transition) {
        if (!isActive()) {
            throw new IllegalStateException(
                    "Cannot " + transition + " job " + jobId + " from terminal state " + status);
        }
    }
}
