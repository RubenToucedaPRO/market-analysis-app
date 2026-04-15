package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.domain.model.Stock;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockDataDTOMapper {

        public StockDataDTO toDTO(Stock stock) {
                if (stock == null) {
                        return null;
                }

                return StockDataDTO.builder()
                                .id(stock.getId())
                                .ticker(stock.getTicker())
                                .logoUrl(stock.getLogoUrl())
                                .currentPrice(stock.getCurrentPrice())
                                .openPrice(stock.getOpenPrice())
                                .highOfDay(stock.getHighOfDay())
                                .lowOfDay(stock.getLowOfDay())
                                .previousClose(stock.getPreviousClose())
                                .sma20(stock.getSma20())
                                .sma50(stock.getSma50())
                                .sma200(stock.getSma200())
                                .volume(stock.getVolume())
                                .averageVolume(stock.getAverageVolume())
                                .lastUpdated(stock.getLastUpdated())
                                .strategyId(stock.getStrategyId())
                                .strategyName(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getStrategyName()
                                                                : null)
                                .complianceRate(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getComplianceRate()
                                                                : null)
                                .evaluationPassed(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().isCompliant()
                                                                : null)
                                .evaluationSummary(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getSummary()
                                                                : null)
                                .valorationIA(stock.getValorationIA())
                                .targetPrice(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getTargetPrice()
                                                                : null)
                                .stopLossPrice(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getStopLossPrice()
                                                                : null)
                                .riskRewardRatio(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getRiskRewardRatio()
                                                                : null)
                                .recommendedShares(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getRecommendedShares()
                                                                : null)
                                .riskWarnings(
                                                stock.getStrategyEvaluation() != null
                                                                ? stock.getStrategyEvaluation().getRiskWarnings()
                                                                : null)
                                .build();
        }

}
