package com.market.analysis.application.job;

/**
 * Lifecycle states of an asynchronous background job.
 *
 * <p>Transitions are one-way: {@code PENDING -> RUNNING -> DONE} or
 * {@code PENDING -> RUNNING -> FAILED}. Terminal states ({@code DONE},
 * {@code FAILED}) are purged after the configured TTL.
 */
public enum JobStatus {

    /** Created and queued, worker not started yet. */
    PENDING,
    /** Worker running. */
    RUNNING,
    /** Finished successfully; the domain result was persisted by the worker. */
    DONE,
    /** Finished with error; technical detail kept server-side for logs. */
    FAILED
}
