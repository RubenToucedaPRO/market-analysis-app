package com.market.analysis.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "company_profile")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Company name */
    private String name;

    /** Country of incorporation */
    private String country;

    /** Ticker symbol */
    @Column(unique = true, nullable = false)
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
}
