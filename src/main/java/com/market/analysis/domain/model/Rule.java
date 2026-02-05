package com.market.analysis.domain.model;

import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing a technical analysis rule.
 * Rules are deterministic and self-contained, evaluating ticker data
 * according to specific technical criteria.
 */
@Getter
@Builder
@ToString
public class Rule {

    /**
     * Unique identifier for the rule.
     */
    private final Long id;

    /**
     * Name of the rule (e.g., "SMA Crossover", "Volume Spike").
     */
    private final String name;

    /**
     * The subject of the rule (ej. SMA, RSI, PRICE).
     */
    private final String subjectCode;
    /**
     * Parameter for the subject (e.g., 50 for SMA 50, 14 for RSI 14).
     * Can be null if the subject does not require a parameter.
     */
    private final Double subjectParam;
    /**
     * The operator for the rule (e.g., ">", "<", "crosses above").
     * Defines how the subject is compared to the target.
     */
    private final String operator;

    /**
     * The target of the rule (ej. SMA, RSI, PRICE).
     */
    private final String targetCode;
    /**
     * Parameter for the target (e.g., 200 for SMA 200, 30 for RSI 30).
     * Can be null if the target does not require a parameter.
     */
    private final Double targetParam;

    /**
     * Description of the rule and its purpose.
     */
    private final String description;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Rule rule = (Rule) o;
        return Objects.equals(id, rule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}