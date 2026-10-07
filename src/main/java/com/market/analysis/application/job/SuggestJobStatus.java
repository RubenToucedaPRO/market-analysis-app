package com.market.analysis.application.job;

/**
 * Lifecycle states of an asynchronous ticker-suggestion job.
 *
 * <p>Transitions are one-way: {@code PENDING -> RUNNING -> DONE} or
 * {@code PENDING -> RUNNING -> FAILED}. Terminal states ({@code DONE},
 * {@code FAILED}) are purged after the configured TTL.
 */
public enum SuggestJobStatus {

    /** Created and queued, worker not started yet. */
    PENDING,
    /** Worker running (Finviz/Polygon/Finnhub calls in progress). */
    RUNNING,
    /** Finished successfully; result persisted as suggestion snapshot. */
    DONE,
    /** Finished with error; technical detail kept server-side for logs. */
    FAILED
}
