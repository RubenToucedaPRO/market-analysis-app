package com.market.analysis.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "company_profile")
@Getter
@Setter
public class CompanyProfileEntity {

    @Id
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
}
