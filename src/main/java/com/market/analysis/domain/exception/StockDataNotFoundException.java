package com.market.analysis.domain.exception;

/**
 * Domain exception thrown when ticker stock data is not found.
 *
 * <p>The domain throws this exception with an {@code errorCode} (unique key)
 * and optional parameters. The {@code GlobalExceptionHandler} in Presentation
 * resolves the final message using {@code MessageSource}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new StockDataNotFoundException("ticker.not_found", tickerId);
 * </pre>
 */
public class StockDataNotFoundException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] params;

    public StockDataNotFoundException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public StockDataNotFoundException(String errorCode, Object... params) {
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
