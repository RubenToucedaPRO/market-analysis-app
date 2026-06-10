package com.market.analysis.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.market.analysis.application.dto.RuleCapabilityDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.RuleCapability;
import com.market.analysis.domain.model.RuleCapabilityCatalog;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementing rule definition management use cases.
 * Coordinates operations on rule definitions through the repository port.
 */
@RequiredArgsConstructor
@Slf4j
public class ManageRuleDefinitionService implements ManageRuleDefinitionUseCase {

    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final RuleDefinitionDTOMapper ruleDefinitionMapper;

    @Override
    public RuleDefinitionDTO createRuleDefinition(RuleDefinitionDTO ruleDefinitionDto) {
        if (ruleDefinitionDto == null) {
            throw new DomainValidationException(DomainErrorCodes.RD_NULL);
        }

        if (ruleDefinitionDto.getCode() == null || ruleDefinitionDto.getCode().isBlank()) {
            throw new DomainValidationException(DomainErrorCodes.RD_CODE_NULL);
        }

        validateAgainstCatalog(ruleDefinitionDto);

        if (ruleDefinitionRepository.existsByCode(ruleDefinitionDto.getCode())) {
            throw new DomainValidationException(DomainErrorCodes.RD_EXISTS, ruleDefinitionDto.getCode());
        }
        log.info("Creating new rule definition: {}", ruleDefinitionDto.getCode());
        RuleDefinition ruleDefinition = ruleDefinitionMapper.toDomain(ruleDefinitionDto);
        RuleDefinition savedRule = ruleDefinitionRepository.save(ruleDefinition);
        log.info("Rule definition created successfully with ID: {}", savedRule.getId());
        return ruleDefinitionMapper.toDTO(savedRule);
    }

    @Override
    public List<RuleDefinitionDTO> getAllRuleDefinitions() {
        log.debug("Retrieving all rule definitions");
        return ruleDefinitionRepository.findAll().stream()
                .map(ruleDefinitionMapper::toDTO)
                .map(this::enrichWithCatalog)
                .toList();
    }

    @Override
    public RuleDefinitionDTO getRuleDefinitionById(Long id) {
        return ruleDefinitionRepository.findById(id)
                .map(ruleDefinitionMapper::toDTO)
                .map(this::enrichWithCatalog)
                .orElse(null);
    }

    @Override
    public RuleDefinitionDTO updateRuleDefinition(RuleDefinitionDTO ruleDefinitionDto) {
        if (ruleDefinitionDto == null) {
            throw new DomainValidationException(DomainErrorCodes.RD_NULL);
        }

        if (ruleDefinitionDto.getId() == null) {
            throw new DomainValidationException(DomainErrorCodes.RD_ID_NULL);
        }

        if (!ruleDefinitionRepository.existsById(ruleDefinitionDto.getId())) {
            throw new DomainValidationException(DomainErrorCodes.RD_NOT_FOUND, ruleDefinitionDto.getId());
        }
        validateAgainstCatalog(ruleDefinitionDto);
        log.info("Updating rule definition with ID: {}", ruleDefinitionDto.getId());
        RuleDefinition ruleDefinition = ruleDefinitionMapper.toDomain(ruleDefinitionDto);
        RuleDefinition savedRule = ruleDefinitionRepository.save(ruleDefinition);
        log.info("Rule definition updated successfully: {}", savedRule.getId());
        return ruleDefinitionMapper.toDTO(savedRule);
    }

    @Override
    public void deleteRuleDefinition(Long id) {
        if (!ruleDefinitionRepository.existsById(id)) {
            throw new DomainValidationException(DomainErrorCodes.RD_NOT_FOUND, id);
        }
        log.info("Deleting rule definition with ID: {}", id);
        ruleDefinitionRepository.deleteById(id);
        log.info("Rule definition deleted successfully: {}", id);
    }

    @Override
    public List<RuleCapabilityDTO> getCatalogCapabilities() {
        Set<String> usedCodes = ruleDefinitionRepository.findAll().stream()
            .map(RuleDefinition::getCode)
            .filter(Objects::nonNull)
            .map(String::toUpperCase)
            .collect(Collectors.toSet());
        return RuleCapabilityCatalog.getSupportedCodes().stream()
                .filter(code-> !usedCodes.contains(code.toUpperCase()))
                .sorted()
                .map(code -> {
                    RuleCapability cap = RuleCapabilityCatalog.getCapability(code).orElseThrow();
                    return RuleCapabilityDTO.builder()
                            .code(code)
                            .requiresParam(cap.isRequiresParam())
                            .anyParamAllowed(cap.isAnyParamAllowed())
                            .allowedParams(cap.getAllowedParams())
                            .build();
                })
                .toList();
    }

    /**
     * Validates a RuleDefinitionDTO against the canonical capability catalog.
     * Ensures the code is supported and the requiresParam flag is consistent
     * with what the evaluator expects.
     */
    private void validateAgainstCatalog(RuleDefinitionDTO dto) {
        String code = dto.getCode();
        if (!RuleCapabilityCatalog.isSupported(code)) {
            log.warn("Rejected rule definition with unsupported code='{}'. Supported: {}",
                    code, RuleCapabilityCatalog.getSupportedCodes());
            throw new DomainValidationException(
                    DomainErrorCodes.RD_UNSUPPORTED_CODE, code);
        }

        boolean catalogRequiresParam = RuleCapabilityCatalog.getCapability(code)
                .map(RuleCapability::isRequiresParam)
                .orElse(false);
        if (dto.isRequiresParam() != catalogRequiresParam) {
            log.warn("Rejected rule definition code='{}': requiresParam={} conflicts with catalog value={}",
                    code, dto.isRequiresParam(), catalogRequiresParam);
            throw new DomainValidationException(
                    DomainErrorCodes.RD_PARAM_CONFLICT, code, catalogRequiresParam);
        }
    }

    /**
     * Enriches a RuleDefinitionDTO with catalog capability data (allowed params).
     * If the code is unknown in the catalog the DTO is returned unchanged.
     */
    private RuleDefinitionDTO enrichWithCatalog(RuleDefinitionDTO dto) {
        return RuleCapabilityCatalog.getCapability(dto.getCode())
                .map(cap -> RuleDefinitionDTO.builder()
                        .id(dto.getId())
                        .code(dto.getCode())
                        .name(dto.getName())
                        .requiresParam(dto.isRequiresParam())
                        .description(dto.getDescription())
                        .allowedParams(cap.getAllowedParams())
                        .anyParamAllowed(cap.isAnyParamAllowed())
                        .build())
                .orElse(dto);
    }
}
