package com.market.analysis.domain.exception;

/**
 * Strongly-typed business exception for domain validation errors.
 * Decouples the domain from rendering/i18n mechanisms.
 *
 * <p>The domain throws this exception with an {@code errorCode} (unique key)
 * and optional parameters. The {@code GlobalExceptionHandler} in Presentation
 * resolves the final message using {@code MessageSource}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new DomainValidationException("validation.target_price_null");
 *   throw new DomainValidationException("strategy.not_found", strategyId);
 * </pre>
 */
public class DomainValidationException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] params;

    public DomainValidationException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public DomainValidationException(String errorCode, Object... params) {
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
