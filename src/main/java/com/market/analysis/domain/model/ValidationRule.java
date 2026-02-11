package com.market.analysis.domain.model;

/**
 * Interface representing a validation rule that can be applied to a Stock.
 * Each implementation provides specific logic to evaluate a stock against
 * a particular criterion (e.g., price, logo presence, volume).
 */
public interface ValidationRule {

    /**
     * Gets the unique identifier of the rule.
     * 
     * @return the rule identifier
     */
    String getRuleId();

    /**
     * Gets the human-readable name of the rule.
     * 
     * @return the rule name
     */
    String getRuleName();

    /**
     * Evaluates the stock against the rule criteria.
     * 
     * @param stock the stock to evaluate
     * @return true if the stock satisfies the rule, false otherwise
     */
    boolean evaluate(Stock stock);

    /**
     * Gets a description of what the rule validates.
     * 
     * @return the rule description
     */
    String getDescription();
}
