package com.market.analysis.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProhibitedKeywordDTO {

    private String keyword;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
