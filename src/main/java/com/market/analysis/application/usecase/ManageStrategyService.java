package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageStrategyService implements ManageStrategyUseCase {

    private final StrategyRepository strategyRepository;
    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final StockDataRepository stockDataRepository;
    private final StrategyDTOMapper strategyMapper;
    private final RuleDefinitionDTOMapper ruleDefinitionMapper;
    private final EvaluateStrategyService evaluateStrategyService;

    @Override
    public StrategyDTO createStrategy(StrategyDTO strategy) {
        log.info("Creating new strategy: {}", strategy.getName());
        Strategy strategyDomain = strategyMapper.toDomain(strategy);
        strategyDomain.validateConsistency();
        Strategy savedStrategy = strategyRepository.save(strategyDomain);
        log.info("Strategy created successfully with ID: {}", savedStrategy.getId());

        List<Stock> stockDataList = stockDataRepository.findAllByStrategyId(savedStrategy.getId());
        for (Stock stock : stockDataList) {
            var evaluation = evaluateStrategyService.evaluateStrategy(savedStrategy, stock);
            var evaluationWithId = evaluation.toBuilder()
                    .id(stock.getStrategyEvaluation().getId())
                    .build();
            stock.setStrategyEvaluation(evaluationWithId);
            stock.setLastUpdated(evaluationWithId.getEvaluatedAt());
            stockDataRepository.updateStockData(stock);
        }

        return strategyMapper.toDTO(savedStrategy);
    }

    @Override
    public StrategyDTO updateStrategy(StrategyDTO strategy) {
        log.info("Updating strategy with ID: {}", strategy.getId());
        Strategy strategyDomain = strategyMapper.toDomain(strategy);
        strategyDomain.validateConsistency();
        Strategy savedStrategy = strategyRepository.save(strategyDomain);
        log.info("Strategy updated successfully with ID: {}", savedStrategy.getId());

        List<Stock> stockDataList = stockDataRepository.findAllByStrategyId(savedStrategy.getId());
        for (Stock stock : stockDataList) {
            var evaluation = evaluateStrategyService.evaluateStrategy(savedStrategy, stock);
            var evaluationWithId = evaluation.toBuilder()
                    .id(stock.getStrategyEvaluation().getId())
                    .build();
            stock.setStrategyEvaluation(evaluationWithId);
            stock.setLastUpdated(evaluationWithId.getEvaluatedAt());
            stockDataRepository.updateStockData(stock);
        }

        return strategyMapper.toDTO(savedStrategy);
    }

    @Override
    public List<StrategyDTO> getAllStrategies() {
        log.debug("Retrieving all strategies");
        return strategyRepository.findAll().stream().map(strategyMapper::toDTO).toList();
    }

    @Override
    public StrategyDTO getStrategyById(Long strategyId) {
        log.debug("Retrieving strategy with ID: {}", strategyId);
        return strategyRepository.findById(strategyId)
                .map(strategyMapper::toDTO)
                .orElseThrow(() -> new DomainValidationException("strategy.not_found", strategyId));
    }

    @Override
    public List<RuleDefinitionDTO> getAvailableRuleDefinitions() {
        return ruleDefinitionRepository.findAll().stream().map(ruleDefinitionMapper::toDTO).toList();
    }

    @Override
    public void deleteStrategy(Long strategyId) {
        log.info("Deleting strategy with ID: {}", strategyId);
        strategyRepository.deleteById(strategyId);
        log.info("Strategy deleted successfully: {}", strategyId);
    }
}
