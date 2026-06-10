package com.market.analysis.domain.exception;

/**
 * Centralized registry of all domain validation error codes.
 *
 * <p>Each constant maps to a key in {@code messages.properties}.
 * The {@code GlobalExceptionHandler} in Presentation resolves the final
 * user-facing message via {@code MessageSource} using these codes.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new DomainValidationException(DomainErrorCodes.ENTRY_PRICE_NULL);
 *   throw new DomainValidationException(DomainErrorCodes.STRATEGY_NOT_FOUND, strategyId);
 * </pre>
 */
public final class DomainErrorCodes {

    private DomainErrorCodes() {}

    // ── Entry / Stock ─────────────────────────────────────────────────────
    public static final String ENTRY_PRICE_NULL       = "validation.entry_price_null";
    public static final String STOCK_NULL             = "validation.stock_null";
    public static final String STOCK_DATA_NULL        = "validation.stock_data_null";
    public static final String FIXED_PRICE_NULL       = "validation.fixed_price_null";

    // ── Strategy Objective ────────────────────────────────────────────────
    public static final String TARGET_TYPE_NULL       = "validation.target_type_null";
    public static final String STOP_LOSS_TYPE_NULL    = "validation.stop_loss_type_null";
    public static final String TARGET_VALUE_NULL      = "validation.target_value_null";
    public static final String STOP_LOSS_VALUE_NULL   = "validation.stop_loss_value_null";
    public static final String CAPITAL_TO_RISK_NULL   = "validation.capital_to_risk_null";
    public static final String DESCRIPTION_NULL       = "validation.description_null";
    public static final String TARGET_VALUE_ZERO      = "validation.target_value_zero";
    public static final String STOP_LOSS_VALUE_ZERO   = "validation.stop_loss_value_zero";
    public static final String CAPITAL_TO_RISK_ZERO   = "validation.capital_to_risk_zero";

    // ── Risk / Price ─────────────────────────────────────────────────────
    public static final String TARGET_PRICE_NULL      = "validation.target_price_null";
    public static final String STOP_PRICE_NULL        = "validation.stop_price_null";
    public static final String TARGET_BELOW_ENTRY     = "validation.target_below_entry";
    public static final String STOP_ABOVE_ENTRY       = "validation.stop_above_entry";
    public static final String RISK_ZERO              = "validation.risk_zero";
    public static final String CAPITAL_NULL           = "validation.capital_null";
    public static final String RISK_PER_SHARE_ZERO    = "validation.risk_per_share_zero";
    public static final String PERCENTAGE_NULL        = "validation.percentage_null";
    public static final String PERCENTAGE_ZERO        = "validation.percentage_zero";

    // ── SMA ──────────────────────────────────────────────────────────────
    public static final String SMA_PERIOD_NULL        = "validation.sma_period_null";
    public static final String SMA_PERIOD_UNSUPPORTED = "validation.sma_period_unsupported";
    public static final String SMA_PERIOD_INVALID     = "validation.sma_period_invalid";

    // ── Strategy ─────────────────────────────────────────────────────────
    public static final String STRATEGY_NULL          = "validation.strategy_null";
    public static final String STRATEGY_NAME_NULL     = "validation.strategy_name_null";
    public static final String STRATEGY_DESC_NULL     = "validation.strategy_desc_null";
    public static final String STRATEGY_NO_RULES      = "validation.strategy_no_rules";
    public static final String STRATEGY_NULL_RULE     = "validation.strategy_null_rule";
    public static final String STRATEGY_OBJECTIVE_NULL = "validation.strategy_objective_null";
    public static final String STRATEGY_NOT_FOUND     = "strategy.not_found";
    public static final String STRATEGY_ID_REQUIRED   = "validation.strategy_id_required";

    // ── Rule Definition ──────────────────────────────────────────────────
    public static final String RD_NULL                = "validation.rd_null";
    public static final String RD_CODE_NULL           = "validation.rd_code_null";
    public static final String RD_EXISTS              = "validation.rd_exists";
    public static final String RD_ID_NULL             = "validation.rd_id_null";
    public static final String RD_UNSUPPORTED_CODE    = "validation.rd_unsupported_code";
    public static final String RD_PARAM_CONFLICT      = "validation.rd_param_conflict";
    public static final String RD_NOT_FOUND           = "ruledefinition.not_found";

    // ── Keyword ──────────────────────────────────────────────────────────
    public static final String KEYWORD_NULL           = "validation.keyword_null";
    public static final String KEYWORD_EXISTS         = "validation.keyword_exists";
    public static final String KEYWORD_BLANK          = "validation.keyword_blank";
    public static final String KEYWORD_TOO_LONG       = "validation.keyword_too_long";

    // ── Rule ──────────────────────────────────────────────────────────────
    public static final String RULE_NOT_EVALUABLE     = "rule.not_evaluable";
    public static final String RULE_MISSING_INDICATOR = "rule.missing_indicator";

    // ── Application ──────────────────────────────────────────────────────
    public static final String REQUEST_NULL           = "validation.request_null";
    public static final String PROMPT_NULL            = "validation.prompt_null";
    public static final String TICKER_NOT_FOUND       = "ticker.not_found";
}
