package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StrategyRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManageStrategyService implements ManageStrategyUseCase {

    private final StrategyRepository strategyRepository;
    private final RuleDefinitionRepository ruleDefinitionRepository;

    @Override
    public Strategy createStrategy(Strategy strategy) {
        // Aquí podrías aplicar el patrón Factory si la creación fuera muy compleja
        strategy.validateConsistency();
        return strategyRepository.save(strategy);
    }

    @Override
    public List<Strategy> getAllStrategies() {
        return strategyRepository.findAll();
    }

    @Override
    public Strategy getStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found with id: " + strategyId));
    }

    @Override
    public List<RuleDefinition> getAvailableRuleDefinitions() {
        return ruleDefinitionRepository.findAll();
    }

    @Override
    public void deleteStrategy(Long strategyId) {
        strategyRepository.deleteById(strategyId);
    }
}
