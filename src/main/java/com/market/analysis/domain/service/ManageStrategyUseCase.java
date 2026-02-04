package com.market.analysis.domain.service;

import java.util.List;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.model.Strategy;

public interface ManageStrategyUseCase {
    Strategy createStrategy(Strategy strategy);

    List<Strategy> getAllStrategies();

    List<RuleDefinition> getAvailableRuleDefinitions(); // Para llenar los combos de la vista
}
