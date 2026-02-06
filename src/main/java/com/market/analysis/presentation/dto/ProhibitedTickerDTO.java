package com.market.analysis.presentation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProhibitedTickerDTO {

    private Long id;

    /** The ticker symbol that is prohibited */
    private String ticker;

    /** Reason why the ticker was marked as prohibited */
    private String reason;

    /** Timestamp when the ticker was added to the prohibited list */
    private LocalDateTime createdAt;
}
