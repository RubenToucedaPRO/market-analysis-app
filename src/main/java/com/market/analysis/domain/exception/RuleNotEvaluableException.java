package com.market.analysis.domain.exception;

/**
 * Exception thrown when a rule cannot be evaluated because its indicator code
 * or operator is not supported by the rule engine.
 *
 * <p>This is a domain-level exception that signals a configuration or
 * programming error: a {@code Rule} with an unsupported combination was
 * allowed to reach the evaluator without being rejected by the P0 validation
 * layer ({@code Rule#validate()} or {@code ManageRuleDefinitionService}).</p>
 *
 * <p>Unlike a missing-data failure (where the indicator is supported but the
 * stock has no data for it), this exception indicates that the indicator code
 * itself is unknown to the engine.</p>
 */
public class RuleNotEvaluableException extends RuntimeException {

    /**
     * Constructs a new RuleNotEvaluableException with the specified detail message.
     *
     * @param message the detail message describing the unsupported indicator or operator
     */
    public RuleNotEvaluableException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleNotEvaluableException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public RuleNotEvaluableException(String message, Throwable cause) {
        super(message, cause);
    }
}
