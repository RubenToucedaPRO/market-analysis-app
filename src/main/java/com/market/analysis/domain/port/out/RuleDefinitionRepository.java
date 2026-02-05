package com.market.analysis.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.market.analysis.domain.model.RuleDefinition;

/**
 * Output port (repository interface) for RuleDefinition persistence operations.
 * Defines the contract for accessing and managing rule definitions without binding
 * to specific persistence technologies.
 * 
 * This interface follows Clean Architecture principles, allowing the domain layer
 * to remain independent of infrastructure details while defining what data operations
 * it needs.
 * 
 * No Spring or framework annotations should be present here to maintain
 * technology independence in the domain layer.
 */
public interface RuleDefinitionRepository {

    /**
     * Saves a rule definition to the repository.
     * 
     * @param ruleDefinition the rule definition to save
     * @return the saved rule definition with updated fields (e.g., generated ID)
     * @throws IllegalArgumentException if ruleDefinition is null
     */
    RuleDefinition save(RuleDefinition ruleDefinition);

    /**
     * Finds a rule definition by its unique identifier.
     * 
     * @param id the rule definition ID
     * @return Optional containing the rule definition if found, empty otherwise
     */
    Optional<RuleDefinition> findById(Long id);

    /**
     * Finds a rule definition by its code.
     * 
     * @param code the rule definition code
     * @return Optional containing the rule definition if found, empty otherwise
     */
    Optional<RuleDefinition> findByCode(String code);

    /**
     * Retrieves all rule definitions from the repository.
     * 
     * @return list of all rule definitions
     */
    List<RuleDefinition> findAll();

    /**
     * Deletes a rule definition by its unique identifier.
     * 
     * @param id the rule definition ID
     */
    void deleteById(Long id);

    /**
     * Checks if a rule definition exists by its unique identifier.
     * 
     * @param id the rule definition ID
     * @return true if the rule definition exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Checks if a rule definition exists by its code.
     * 
     * @param code the rule definition code
     * @return true if the rule definition exists, false otherwise
     */
    boolean existsByCode(String code);
}
