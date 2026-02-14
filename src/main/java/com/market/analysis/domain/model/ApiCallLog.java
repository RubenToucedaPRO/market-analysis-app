package com.market.analysis.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCallLog {

    private Long id;

    private String ticker;

    private Instant ocurredAt;

}
