package com.market.analysis.application.dto;

import java.time.Instant;

import com.market.analysis.application.job.BackgroundJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON contract for polling the state of an asynchronous AI-valoration job.
 * The {@code message} is already localized by the presentation layer; terminal
 * states carry it, active states leave it null (the page shows a spinner).
 * {@code generated} is only set on {@code DONE}: true when the LLM produced a
 * real valuation, false when the fallback text was persisted instead.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IaValorationJobStatusDTO {

    private String jobId;
    private Long tickerId;
    private String status;
    private Instant startedAt;
    private Instant finishedAt;
    private Boolean generated;
    private String message;

    public static IaValorationJobStatusDTO from(BackgroundJob job, String message) {
        return IaValorationJobStatusDTO.builder()
                .jobId(job.getJobId())
                .tickerId(job.getSubjectId())
                .status(job.getStatus().name())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .generated(generatedOf(job))
                .message(message)
                .build();
    }

    private static Boolean generatedOf(BackgroundJob job) {
        String value = job.getAttribute("generated");
        return value == null ? null : Boolean.valueOf(value);
    }
}
