package com.market.analysis.application.job;

import java.util.Optional;

import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;

/**
 * Thin facade for asynchronous AI-valoration jobs over the shared
 * {@link BackgroundJobService} core.
 *
 * <p>The LLM call ({@link ManageAnalyzeTickerUseCase#getValorationIA}, seconds
 * to minutes with retries and model fallbacks) runs on a bounded background
 * executor while HTTP requests stay short. The valuation itself is persisted
 * on the stock by the use case (even the fallback text); the job only tracks
 * lifecycle plus whether a real valuation was generated.
 *
 * <p>Deduplication policy: at most one active job per ticker; a second submit
 * while one is active reuses the running job id instead of burning the shared
 * daily OpenRouter quota twice.
 */
public class IaValorationJobService {

    static final String JOB_KIND = "ia-valoration";
    static final String ATTR_GENERATED = "generated";

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;
    private final BackgroundJobService jobs;

    public IaValorationJobService(ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase,
            BackgroundJobService jobs) {
        this.manageAnalyzeTickerUseCase = manageAnalyzeTickerUseCase;
        this.jobs = jobs;
    }

    /**
     * Submits a valuation job for the given ticker without blocking.
     *
     * @param tickerId the stock id to valuate
     * @return the new job id, or the id of the already active job for the ticker
     * @throws JobRejectedException if the background queue is full
     */
    public String submitValorationJob(long tickerId) {
        return jobs.submit(JOB_KIND, tickerId, job -> {
            boolean generated = manageAnalyzeTickerUseCase.getValorationIA(tickerId);
            job.setAttribute(ATTR_GENERATED, String.valueOf(generated));
        });
    }

    /**
     * Finds the active job of a ticker, if any. Used to re-attach the
     * progress banner when the user navigates back to the detail page
     * without the {@code ?iaJob=} parameter in the URL.
     *
     * @param tickerId the stock id to look up
     * @return the active job id, or empty when none is running
     */
    public Optional<String> findActiveJobIdByTickerId(long tickerId) {
        return jobs.findActiveJobId(JOB_KIND, tickerId);
    }

    /**
     * Returns the current state of a job, if still known.
     *
     * @param jobId the job id returned at submit time
     * @return the job, or empty when unknown or already purged
     */
    public Optional<BackgroundJob> getJob(String jobId) {
        return jobs.getJob(jobId);
    }
}
