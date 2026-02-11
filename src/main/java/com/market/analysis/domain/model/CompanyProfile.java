package com.market.analysis.domain.model;

import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDateTime lastUpdated;

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
                lastUpdated.isBefore(LocalDateTime.now().minusDays(30));
    }

    /**
     * Keywords that indicate a ticker should be marked as prohibited.
     * Includes ETFs, funds, SPACs, biotech, and leveraged products.
     */
    private static final List<String> PROHIBITED_KEYWORDS = List.of(
            "ACQUISITION", "MERGER", "ETF", "FUND", "TRUST",
            "BULL", "BEAR", "2X", "3X",
            "THERAPEUTICS", "PHARMA", "BIO", "ONCOLOGY",
            "LP", "PARTNERS", "WARRANTS");

    public boolean isProhibited() {
        return getProhibitionReason() != null;
    }

    public String getProhibitionReason() {
        if (this.name == null)
            return null;
        String upperName = this.name.toUpperCase();
        return PROHIBITED_KEYWORDS.stream()
                .filter(upperName::contains)
                .findFirst()
                .orElse(null);
    }
}
