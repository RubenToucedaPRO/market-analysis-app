package com.market.analysis.application.dto;

/**
 * Execution policy when some strategy rules cannot be represented in Finviz.
 */
public enum FinvizExecutionMode {
    TOLERANT,
    STRICT
}
