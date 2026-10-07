package com.market.analysis.application.job;

import java.util.Optional;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;

/**
 * Thin facade for asynchronous ticker-suggestion jobs over the shared
 * {@link BackgroundJobService} core.
 *
 * <p>The long-running use case ({@link SuggestTickersUseCase#suggestTickers},
 * minutes due to the Polygon rate limit) runs on a bounded background
 * executor while HTTP requests stay short. The business result is persisted
 * as a suggestion snapshot by the use case itself; the job only tracks
 * lifecycle plus summary counts as attributes.
 *
 * <p>Deduplication policy: at most one active job per strategy; a second
 * submit while one is active reuses the running job id instead of consuming
 * API quota twice.
 */
public class SuggestTickerJobService {

    static final String JOB_KIND = "suggest-tickers";
    private static final String ATTR_SUGGESTED = "suggested";
    private static final String ATTR_DISCARDED = "discarded";

    private final SuggestTickersUseCase suggestTickersUseCase;
    private final BackgroundJobService jobs;

    public SuggestTickerJobService(SuggestTickersUseCase suggestTickersUseCase,
            BackgroundJobService jobs) {
        this.suggestTickersUseCase = suggestTickersUseCase;
        this.jobs = jobs;
    }

    /**
     * Submits a suggestion job for the given strategy without blocking.
     *
     * @param strategyId the strategy to analyze
     * @return the new job id, or the id of the already active job for the strategy
     * @throws JobRejectedException if the background queue is full
     */
    public String submitSuggestionJob(long strategyId) {
        return jobs.submit(JOB_KIND, strategyId, job -> {
            SuggestTickersResponseDTO response = suggestTickersUseCase.suggestTickers(
                    SuggestTickersRequestDTO.builder()
                            .strategyId(strategyId)
                            .build());
            job.setAttribute(ATTR_SUGGESTED,
                    String.valueOf(countByStatus(response, TickerSuitabilityStatus.APTO)));
            job.setAttribute(ATTR_DISCARDED,
                    String.valueOf(countByStatus(response, TickerSuitabilityStatus.NO_APTO)));
        });
    }

    /**
     * Finds the active job of a strategy, if any. Used to re-attach the
     * progress banner when the user navigates back to the detail page
     * without the {@code ?jobId=} parameter in the URL.
     *
     * @param strategyId the strategy to look up
     * @return the active job id, or empty when none is running
     */
    public Optional<String> findActiveJobIdByStrategyId(long strategyId) {
        return jobs.findActiveJobId(JOB_KIND, strategyId);
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

    static int countByStatus(SuggestTickersResponseDTO response, TickerSuitabilityStatus status) {
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
