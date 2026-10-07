package com.market.analysis.application.job;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable state holder of a single asynchronous background job.
 *
 * <p>Flow-agnostic: {@code subjectKind} namespaces deduplication (e.g.
 * {@code "suggest-tickers"}, {@code "ia-valoration"}) and {@code subjectId}
 * identifies the domain entity the job works on. Flow-specific outcome data
 * travels in {@link #attributes} as plain strings; the domain result itself
 * is persisted by the worker, never stored here.
 *
 * <p>Thread-safe: state transitions are synchronized because the submitting
 * request thread and the background worker thread touch the same instance.
 */
public class BackgroundJob {

    private final String jobId;
    private final String subjectKind;
    private final long subjectId;
    private final Instant startedAt;
    private final Map<String, String> attributes = new ConcurrentHashMap<>();

    private volatile JobStatus status;
    private volatile Instant finishedAt;
    private volatile String errorDetail;

    public BackgroundJob(String jobId, String subjectKind, long subjectId, Instant startedAt) {
        this.jobId = Objects.requireNonNull(jobId, "jobId");
        this.subjectKind = Objects.requireNonNull(subjectKind, "subjectKind");
        this.subjectId = subjectId;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.status = JobStatus.PENDING;
    }

    public String getJobId() {
        return jobId;
    }

    public String getSubjectKind() {
        return subjectKind;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public JobStatus getStatus() {
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

    public void setAttribute(String key, String value) {
        attributes.put(
                Objects.requireNonNull(key, "key"),
                Objects.requireNonNull(value, "value"));
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public boolean isActive() {
        return status == JobStatus.PENDING || status == JobStatus.RUNNING;
    }

    public boolean isTerminal() {
        return status == JobStatus.DONE || status == JobStatus.FAILED;
    }

    public synchronized void markRunning() {
        requireState(JobStatus.PENDING, "markRunning");
        this.status = JobStatus.RUNNING;
    }

    public synchronized void markDone() {
        requireActive("markDone");
        this.status = JobStatus.DONE;
        this.finishedAt = Instant.now();
    }

    public synchronized void fail(String errorDetail) {
        requireActive("fail");
        this.errorDetail = errorDetail;
        this.status = JobStatus.FAILED;
        this.finishedAt = Instant.now();
    }

    /**
     * Tells whether a terminal job is older than the given TTL.
     * Used by the purge routine; active jobs never expire.
     */
    public boolean isExpired(Instant now, Duration ttl) {
        return isTerminal() && finishedAt != null && finishedAt.plus(ttl).isBefore(now);
    }

    private void requireState(JobStatus expected, String transition) {
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
