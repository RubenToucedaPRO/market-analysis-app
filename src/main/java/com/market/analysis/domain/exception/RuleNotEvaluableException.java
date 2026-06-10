package com.market.analysis.domain.exception;

/**
 * Domain exception thrown when a rule cannot be evaluated because its indicator
 * code or operator is not supported by the evaluation engine.
 *
 * <p>The domain throws this exception with an {@code errorCode} (unique key)
 * and optional parameters. The {@code GlobalExceptionHandler} in Presentation
 * resolves the final message using {@code MessageSource}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new RuleNotEvaluableException("rule.not_evaluable");
 * </pre>
 */
public class RuleNotEvaluableException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] params;

    public RuleNotEvaluableException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public RuleNotEvaluableException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }
}
