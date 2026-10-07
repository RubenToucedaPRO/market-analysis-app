package com.market.analysis.application.dto;

import java.time.Instant;

import com.market.analysis.application.job.SuggestTickerJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON contract for polling the state of an asynchronous suggestion job.
 * The {@code message} is already localized by the presentation layer; terminal
 * states carry it, active states leave it null (the page shows a spinner).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestJobStatusDTO {

    private String jobId;
    private Long strategyId;
    private String status;
    private Instant startedAt;
    private Instant finishedAt;
    private Integer suggestedCount;
    private Integer discardedCount;
    private String message;

    public static SuggestJobStatusDTO from(SuggestTickerJob job, String message) {
        return SuggestJobStatusDTO.builder()
                .jobId(job.getJobId())
                .strategyId(job.getStrategyId())
                .status(job.getStatus().name())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .suggestedCount(job.getSuggestedCount())
                .discardedCount(job.getDiscardedCount())
                .message(message)
                .build();
    }
}
