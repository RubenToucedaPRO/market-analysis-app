package com.market.analysis.domain.model;

import java.util.Set;

/**
 * Describes the runtime capability of a rule indicator.
 * Encapsulates whether a parameter is required and which values are accepted.
 */
public final class RuleCapability {

    private final boolean requiresParam;
    private final boolean anyParamAllowed;
    private final Set<Double> allowedParams;

    private RuleCapability(boolean requiresParam, boolean anyParamAllowed, Set<Double> allowedParams) {
        this.requiresParam = requiresParam;
        this.anyParamAllowed = anyParamAllowed;
        this.allowedParams = allowedParams;
    }

    /**
     * Creates a capability for an indicator that does not require a parameter.
     */
    public static RuleCapability noParam() {
        return new RuleCapability(false, false, Set.of());
    }

    /**
     * Creates a capability for an indicator that requires one of a fixed set of parameters.
     */
    public static RuleCapability withAllowedParams(Set<Double> allowedParams) {
        return new RuleCapability(true, false, Set.copyOf(allowedParams));
    }

    /**
     * Creates a capability for an indicator that requires a parameter but accepts any numeric value.
     */
    public static RuleCapability anyParam() {
        return new RuleCapability(true, true, Set.of());
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
     * Returns the set of explicitly allowed parameter values.
     * Empty when the capability accepts any parameter or no parameter.
     */
    public Set<Double> getAllowedParams() {
        return allowedParams;
    }
}
