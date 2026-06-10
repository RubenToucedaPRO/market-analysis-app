package com.market.analysis.domain.exception;

/**
 * Domain exception thrown when a required technical indicator is missing
 * from ticker stock data.
 *
 * <p>The domain throws this exception with an {@code errorCode} (unique key)
 * and optional parameters. The {@code GlobalExceptionHandler} in Presentation
 * resolves the final message using {@code MessageSource}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new MissingIndicatorException("rule.missing_indicator");
 * </pre>
 */
public class MissingIndicatorException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] params;

    public MissingIndicatorException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public MissingIndicatorException(String errorCode, Object... params) {
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
