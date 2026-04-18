package com.market.analysis.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the company profile data retrieved from Finnhub API.
 * Contains information about the company such as name, industry, market cap,
 * etc.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfile {
    private Long id;

    /** Company name */
    private String name;

    /** Country of incorporation */
    private String country;

    /** Ticker symbol */
    private String ticker;

    /** Stock exchange */
    private String exchange;

    /** Industry classification */
    private String industry;

    /** IPO date */
    private String ipo;

    /** Logo URL */
    private String logo;

    /** Market capitalization */
    private Double marketCapitalization;

    /** Number of shares outstanding */
    private Double shareOutstanding;

    /** Company website URL */
    private String website;

    private Instant lastUpdated;

    /**
     * Checks if the profile has valid data.
     * 
     * @return true if name is not null and not empty
     */
    public boolean isValid() {
        return name != null && !name.isEmpty();
    }

    public boolean isOutdated() {
        return lastUpdated == null ||
                lastUpdated.isBefore(Instant.now().minusSeconds(24 * 60 * 60 * 30));
    }

}
