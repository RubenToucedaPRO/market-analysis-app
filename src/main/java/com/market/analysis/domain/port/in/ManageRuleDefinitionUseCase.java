package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.domain.model.RuleDefinition;

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
    RuleDefinition createRuleDefinition(RuleDefinition ruleDefinition);

    /**
     * Retrieves all available rule definitions.
     * 
     * @return list of all rule definitions
     */
    List<RuleDefinition> getAllRuleDefinitions();

    /**
     * Retrieves a specific rule definition by its ID.
     * 
     * @param id the rule definition ID
     * @return the rule definition
     * @throws RuntimeException if rule definition not found
     */
    RuleDefinition getRuleDefinitionById(Long id);

    /**
     * Updates an existing rule definition.
     * 
     * @param ruleDefinition the rule definition to update
     * @return the updated rule definition
     */
    RuleDefinition updateRuleDefinition(RuleDefinition ruleDefinition);

    /**
     * Deletes a rule definition by its ID.
     * 
     * @param id the rule definition ID
     */
    void deleteRuleDefinition(Long id);
}
