package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;

public interface ManageStrategyUseCase {
    StrategyDTO createStrategy(StrategyDTO strategy);

    List<StrategyDTO> getAllStrategies();

    StrategyDTO getStrategyById(Long strategyId);

    List<RuleDefinitionDTO> getAvailableRuleDefinitions();

    void deleteStrategy(Long strategyId);
}
