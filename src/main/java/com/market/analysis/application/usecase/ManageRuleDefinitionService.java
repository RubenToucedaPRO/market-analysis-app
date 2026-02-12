package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.domain.exception.StockDataNotFoundException;
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
    private final RuleDefinitionDTOMapper ruleDefinitionMapper;

    @Override
    public RuleDefinitionDTO createRuleDefinition(RuleDefinitionDTO ruleDefinitionDto) {
        if (ruleDefinitionDto == null) {
            throw new IllegalArgumentException("RuleDefinition cannot be null");
        }

        if (ruleDefinitionDto.getCode() == null || ruleDefinitionDto.getCode().isBlank()) {
            throw new IllegalArgumentException("RuleDefinition code cannot be null or empty");
        }

        if (ruleDefinitionRepository.existsByCode(ruleDefinitionDto.getCode())) {
            throw new IllegalArgumentException(
                    "RuleDefinition with code '" + ruleDefinitionDto.getCode() + "' already exists");
        }
        RuleDefinition ruleDefinition = ruleDefinitionMapper.toDomain(ruleDefinitionDto);
        RuleDefinition savedRule = ruleDefinitionRepository.save(ruleDefinition);
        return ruleDefinitionMapper.toDTO(savedRule);
    }

    @Override
    public List<RuleDefinitionDTO> getAllRuleDefinitions() {
        return ruleDefinitionRepository.findAll().stream().map(ruleDefinitionMapper::toDTO).toList();
    }

    @Override
    public RuleDefinitionDTO getRuleDefinitionById(Long id) {
        return ruleDefinitionRepository.findById(id)
                .map(ruleDefinitionMapper::toDTO)
                .orElse(null);
    }

    @Override
    public RuleDefinitionDTO updateRuleDefinition(RuleDefinitionDTO ruleDefinitionDto) {
        if (ruleDefinitionDto == null) {
            throw new IllegalArgumentException("RuleDefinition cannot be null");
        }

        if (ruleDefinitionDto.getId() == null) {
            throw new IllegalArgumentException("RuleDefinition ID cannot be null for update");
        }

        if (!ruleDefinitionRepository.existsById(ruleDefinitionDto.getId())) {
            throw new StockDataNotFoundException("RuleDefinition not found with id: " + ruleDefinitionDto.getId());
        }
        RuleDefinition ruleDefinition = ruleDefinitionMapper.toDomain(ruleDefinitionDto);
        RuleDefinition savedRule = ruleDefinitionRepository.save(ruleDefinition);
        return ruleDefinitionMapper.toDTO(savedRule);
    }

    @Override
    public void deleteRuleDefinition(Long id) {
        if (!ruleDefinitionRepository.existsById(id)) {
            throw new StockDataNotFoundException("RuleDefinition not found with id: " + id);
        }
        ruleDefinitionRepository.deleteById(id);
    }
}
