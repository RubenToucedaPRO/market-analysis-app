package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
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
    private final StrategyDTOMapper strategyMapper;
    private final RuleDefinitionDTOMapper ruleDefinitionMapper;

    @Override
    public StrategyDTO createStrategy(StrategyDTO strategy) {
        Strategy strategyDomain = strategyMapper.toDomain(strategy);
        strategyDomain.validateConsistency();
        Strategy savedStrategy = strategyRepository.save(strategyDomain);
        return strategyMapper.toDTO(savedStrategy);
    }

    @Override
    public List<StrategyDTO> getAllStrategies() {
        return strategyRepository.findAll().stream().map(strategyMapper::toDTO).toList();
    }

    @Override
    public StrategyDTO getStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .map(strategyMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Strategy not found with id: " + strategyId));
    }

    @Override
    public List<RuleDefinitionDTO> getAvailableRuleDefinitions() {
        return ruleDefinitionRepository.findAll().stream().map(ruleDefinitionMapper::toDTO).toList();
    }

    @Override
    public void deleteStrategy(Long strategyId) {
        strategyRepository.deleteById(strategyId);
    }
}
