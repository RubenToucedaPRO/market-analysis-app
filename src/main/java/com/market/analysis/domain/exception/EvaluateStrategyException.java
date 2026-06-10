package com.market.analysis.domain.exception;

/**
 * Domain exception thrown when strategy evaluation fails or cannot be completed.
 *
 * <p>The domain throws this exception with an {@code errorCode} (unique key)
 * and optional parameters. The {@code GlobalExceptionHandler} in Presentation
 * resolves the final message using {@code MessageSource}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new EvaluateStrategyException("strategy.evaluation_failed");
 * </pre>
 */
public class EvaluateStrategyException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] params;

    public EvaluateStrategyException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public EvaluateStrategyException(String errorCode, Object... params) {
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
