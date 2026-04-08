package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Describes the runtime capability of a rule indicator.
 *
 * <p>Beyond the parameter constraints introduced in P0, this value object now
 * also carries:</p>
 * <ul>
 *   <li>An {@link IndicatorResolver} that knows how to extract the indicator
 *       value from a {@link Stock} instance, removing magic-string switch
 *       statements from the evaluator (P1 task 6).</li>
 *   <li>The set of comparison operators that are valid for this indicator
 *       (P1 task 5).</li>
 *   <li>Role flags ({@code subjectAllowed} / {@code targetAllowed}) that
 *       express whether the indicator can appear as the subject or target of a
 *       rule (P1 task 5).</li>
 * </ul>
 */
public final class RuleCapability {

    private final boolean requiresParam;
    private final boolean anyParamAllowed;
    private final Set<Double> allowedParams;
    private final IndicatorResolver resolver;
    private final Set<String> allowedOperators;
    private final boolean subjectAllowed;
    private final boolean targetAllowed;

    private RuleCapability(
            boolean requiresParam,
            boolean anyParamAllowed,
            Set<Double> allowedParams,
            IndicatorResolver resolver,
            Set<String> allowedOperators,
            boolean subjectAllowed,
            boolean targetAllowed) {
        this.requiresParam = requiresParam;
        this.anyParamAllowed = anyParamAllowed;
        this.allowedParams = allowedParams;
        this.resolver = resolver;
        this.allowedOperators = allowedOperators;
        this.subjectAllowed = subjectAllowed;
        this.targetAllowed = targetAllowed;
    }

    /**
     * Creates a capability for an indicator that does not require a parameter.
     *
     * @param resolver        indicator value resolver
     * @param allowedOperators operators valid for this indicator
     * @param subjectAllowed  whether the indicator may appear as rule subject
     * @param targetAllowed   whether the indicator may appear as rule target
     */
    public static RuleCapability noParam(
            IndicatorResolver resolver,
            Set<String> allowedOperators,
            boolean subjectAllowed,
            boolean targetAllowed) {
        return new RuleCapability(false, false, Set.of(), resolver, allowedOperators, subjectAllowed, targetAllowed);
    }

    /**
     * Creates a capability for an indicator that requires one of a fixed set of parameters.
     *
     * @param allowedParams   the accepted parameter values
     * @param resolver        indicator value resolver
     * @param allowedOperators operators valid for this indicator
     * @param subjectAllowed  whether the indicator may appear as rule subject
     * @param targetAllowed   whether the indicator may appear as rule target
     */
    public static RuleCapability withAllowedParams(
            Set<Double> allowedParams,
            IndicatorResolver resolver,
            Set<String> allowedOperators,
            boolean subjectAllowed,
            boolean targetAllowed) {
        return new RuleCapability(true, false, Set.copyOf(allowedParams), resolver, allowedOperators, subjectAllowed, targetAllowed);
    }

    /**
     * Creates a capability for an indicator that requires a parameter but accepts any numeric value.
     *
     * @param resolver        indicator value resolver
     * @param allowedOperators operators valid for this indicator
     * @param subjectAllowed  whether the indicator may appear as rule subject
     * @param targetAllowed   whether the indicator may appear as rule target
     */
    public static RuleCapability anyParam(
            IndicatorResolver resolver,
            Set<String> allowedOperators,
            boolean subjectAllowed,
            boolean targetAllowed) {
        return new RuleCapability(true, true, Set.of(), resolver, allowedOperators, subjectAllowed, targetAllowed);
    }

    /**
     * Resolves the indicator value from the given stock data.
     *
     * @param param the indicator parameter (may be {@code null} for no-param indicators)
     * @param stock the stock instance to resolve against
     * @return the resolved value, or {@code null} if stock data is unavailable for this parameter
     */
    public BigDecimal resolve(Double param, Stock stock) {
        return resolver.resolve(param, stock);
    }

    public boolean isRequiresParam() {
        return requiresParam;
    }

    /**
     * Returns whether the given parameter value is valid for this capability.
     *
     * @param param the parameter to check (may be null)
     * @return true if the parameter is accepted by this capability
     */
    public boolean isParamAllowed(Double param) {
        if (!requiresParam) {
            return param == null;
        }
        if (param == null) {
            return false;
        }
        if (anyParamAllowed) {
            return true;
        }
        return allowedParams.contains(param);
    }

    /**
     * Returns whether the given operator is valid for this capability.
     *
     * @param operator comparison operator string
     * @return true if the operator is accepted by this capability
     */
    public boolean isOperatorAllowed(String operator) {
        if (operator == null) {
            return false;
        }
        return allowedOperators.contains(operator) || allowedOperators.contains(operator.toUpperCase());
    }

    /**
     * Returns whether this indicator may be used as the subject of a rule.
     */
    public boolean isSubjectAllowed() {
        return subjectAllowed;
    }

    /**
     * Returns whether this indicator may be used as the target of a rule.
     */
    public boolean isTargetAllowed() {
        return targetAllowed;
    }

    /**
     * Returns the set of explicitly allowed parameter values.
     * Empty when the capability accepts any parameter or no parameter.
     */
    public Set<Double> getAllowedParams() {
        return allowedParams;
    }

    /**
     * Returns the set of operators valid for this indicator.
     */
    public Set<String> getAllowedOperators() {
        return allowedOperators;
    }
}
