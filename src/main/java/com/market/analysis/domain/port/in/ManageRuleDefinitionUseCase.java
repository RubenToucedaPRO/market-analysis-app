package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.application.dto.RuleCapabilityDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;

/**
 * Use case interface for managing rule definitions.
 * Defines the operations that can be performed on rule definitions
 * from the application's perspective.
 */
public interface ManageRuleDefinitionUseCase {
    
    /**
     * Creates a new rule definition.
     * 
     * @param ruleDefinition the rule definition to create
     * @return the created rule definition with generated ID
     */
    RuleDefinitionDTO createRuleDefinition(RuleDefinitionDTO ruleDefinition);

    /**
     * Retrieves all available rule definitions, enriched with catalog capability data.
     * 
     * @return list of all rule definitions
     */
    List<RuleDefinitionDTO> getAllRuleDefinitions();

    /**
     * Retrieves a specific rule definition by its ID.
     * 
     * @param id the rule definition ID
     * @return the rule definition
     * @throws RuntimeException if rule definition not found
     */
    RuleDefinitionDTO getRuleDefinitionById(Long id);

    /**
     * Updates an existing rule definition.
     * 
     * @param ruleDefinition the rule definition to update
     * @return the updated rule definition
     */
    RuleDefinitionDTO updateRuleDefinition(RuleDefinitionDTO ruleDefinition);

    /**
     * Deletes a rule definition by its ID.
     * 
     * @param id the rule definition ID
     */
    void deleteRuleDefinition(Long id);

    /**
     * Returns the full canonical capability catalog as DTOs.
     * Used by the UI to populate code selects and constrain parameter inputs
     * to only valid values (P2 – UI guided by capabilities).
     *
     * @return ordered list of all supported indicator capabilities
     */
    List<RuleCapabilityDTO> getCatalogCapabilities();
}
