package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.util.Objects;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;

public class PromptBuilder {

    private static final String NOT_AVAILABLE = "N/A";

    public String buildAnalysisPrompt(Stock stock, StrategyEvaluation evaluation) {
        Stock safeStock = Objects.requireNonNull(stock, "Stock cannot be null");
        String ticker = safe(safeStock.getTicker());

        return """
                You are an expert financial analyst.
                This analysis is interpretative and not personalized financial advice.
                Analyze the following technical snapshot and respond in Spanish.

                Ticker: %s
                Current Price: %s
                SMA20: %s
                SMA50: %s
                SMA200: %s
                Volume: %s
                Average Volume: %s
                Strategy Name: %s
                Compliance Rate: %s
                Strategy Summary: %s
                Risk/Reward Ratio: %s
                Target Price: %s
                Stop Loss Price: %s

                Provide one concise sentence with strengths, weaknesses, and overall outlook.
                """.formatted(
                ticker,
                decimal(safeStock.getCurrentPrice()),
                decimal(safeStock.getSma20()),
                decimal(safeStock.getSma50()),
                decimal(safeStock.getSma200()),
                whole(safeStock.getVolume()),
                whole(safeStock.getAverageVolume()),
                safe(evaluation == null ? null : evaluation.getStrategyName()),
                decimal(evaluation == null ? null : evaluation.getComplianceRate()),
                safe(evaluation == null ? null : evaluation.getSummary()),
                decimal(evaluation == null ? null : evaluation.getRiskRewardRatio()),
                decimal(evaluation == null ? null : evaluation.getTargetPrice()),
                decimal(evaluation == null ? null : evaluation.getStopLossPrice()));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? NOT_AVAILABLE : value;
    }

    private String decimal(BigDecimal value) {
        return value == null ? NOT_AVAILABLE : String.format("%.2f", value);
    }

    private String whole(Long value) {
        return value == null ? NOT_AVAILABLE : String.valueOf(value);
    }
}
