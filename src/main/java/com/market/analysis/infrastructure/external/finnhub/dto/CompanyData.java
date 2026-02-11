package com.market.analysis.infrastructure.external.finnhub.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing company profile data from Finnhub API (/stock/profile2).
 * Contains basic company information including name, country, exchange, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyData {

    /** Company name */
    private String name;

    /** Country of incorporation */
    private String country;

    /** Ticker symbol */
    private String ticker;

    /** Stock exchange */
    private String exchange;

    /** Industry classification */
    private String finnhubIndustry;

    /** IPO date */
    private String ipo;

    /** Logo URL */
    private String logo;

    /** Market capitalization */
    private Double marketCapitalization;

    /** Number of shares outstanding */
    private Double shareOutstanding;

    /** Company website URL */
    private String weburl;

    private LocalDateTime lastUpdated;

    /**
     * Checks if the profile has valid data.
     * 
     * @return true if name is not null and not empty, and ticker is not null and
     *         not empty; false otherwise.
     */
    public boolean isValid() {
        return name != null && !name.isEmpty() && !name.isBlank() && ticker != null && !ticker.isEmpty()
                && !ticker.isBlank();
    }

    public boolean isOutdated() {
        return lastUpdated == null ||
                lastUpdated.isBefore(LocalDateTime.now().minusDays(30));
    }
}
