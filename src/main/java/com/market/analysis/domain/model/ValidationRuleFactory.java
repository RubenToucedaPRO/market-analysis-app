package com.market.analysis.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating and managing ValidationRule instances.
 * Provides access to all available validation rules in the system.
 */
public class ValidationRuleFactory {

    private static final Map<String, ValidationRule> RULES = new HashMap<>();

    static {
        // Register all available validation rules
        registerRule(new LogoPresentRule());
        registerRule(new PriceAboveSma200Rule());
        registerRule(new VolumeAboveAverageRule());
    }

    private ValidationRuleFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Registers a validation rule in the factory.
     * 
     * @param rule the rule to register
     */
    private static void registerRule(ValidationRule rule) {
        RULES.put(rule.getRuleId(), rule);
    }

    /**
     * Gets a validation rule by its ID.
     * 
     * @param ruleId the unique identifier of the rule
     * @return an Optional containing the rule if found, empty otherwise
     */
    public static Optional<ValidationRule> getRuleById(String ruleId) {
        return Optional.ofNullable(RULES.get(ruleId));
    }

    /**
     * Gets all available validation rules.
     * 
     * @return a list of all registered validation rules
     */
    public static List<ValidationRule> getAllRules() {
        return List.copyOf(RULES.values());
    }

    /**
     * Checks if a rule with the given ID exists.
     * 
     * @param ruleId the rule ID to check
     * @return true if the rule exists, false otherwise
     */
    public static boolean ruleExists(String ruleId) {
        return RULES.containsKey(ruleId);
    }
}
