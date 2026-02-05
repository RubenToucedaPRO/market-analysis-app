package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service implementing rule definition management use cases.
 * Coordinates operations on rule definitions through the repository port.
 */
@RequiredArgsConstructor
public class ManageRuleDefinitionService implements ManageRuleDefinitionUseCase {

    private final RuleDefinitionRepository ruleDefinitionRepository;

    @Override
    public RuleDefinition createRuleDefinition(RuleDefinition ruleDefinition) {
        if (ruleDefinition == null) {
            throw new IllegalArgumentException("RuleDefinition cannot be null");
        }
        
        if (ruleDefinition.getCode() == null || ruleDefinition.getCode().isBlank()) {
            throw new IllegalArgumentException("RuleDefinition code cannot be null or empty");
        }
        
        if (ruleDefinitionRepository.existsByCode(ruleDefinition.getCode())) {
            throw new IllegalArgumentException("RuleDefinition with code '" + ruleDefinition.getCode() + "' already exists");
        }
        
        return ruleDefinitionRepository.save(ruleDefinition);
    }

    @Override
    public List<RuleDefinition> getAllRuleDefinitions() {
        return ruleDefinitionRepository.findAll();
    }

    @Override
    public RuleDefinition getRuleDefinitionById(Long id) {
        return ruleDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RuleDefinition not found with id: " + id));
    }

    @Override
    public RuleDefinition updateRuleDefinition(RuleDefinition ruleDefinition) {
        if (ruleDefinition == null) {
            throw new IllegalArgumentException("RuleDefinition cannot be null");
        }
        
        if (ruleDefinition.getId() == null) {
            throw new IllegalArgumentException("RuleDefinition ID cannot be null for update");
        }
        
        if (!ruleDefinitionRepository.existsById(ruleDefinition.getId())) {
            throw new RuntimeException("RuleDefinition not found with id: " + ruleDefinition.getId());
        }
        
        return ruleDefinitionRepository.save(ruleDefinition);
    }

    @Override
    public void deleteRuleDefinition(Long id) {
        if (!ruleDefinitionRepository.existsById(id)) {
            throw new RuntimeException("RuleDefinition not found with id: " + id);
        }
        ruleDefinitionRepository.deleteById(id);
    }
}
