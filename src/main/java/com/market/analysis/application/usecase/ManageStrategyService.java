package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.StrategyRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManageStrategyService implements ManageStrategyUseCase {

    private final StrategyRepository strategyRepository;

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
    public List<RuleDefinition> getAvailableRuleDefinitions() {
        // Esto debería venir de un RuleDefinitionRepository que creamos antes
        // Por ahora, si no lo tienes, puedes devolver una lista fija para probar
        return List.of(); 
    }
}
