package com.market.analysis.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.market.analysis.domain.model.Strategy;

/**
 * Output port (repository interface) for strategy persistence operations.
 * Defines the contract for accessing and managing strategies without binding
 * to specific persistence technologies.
 * 
 * This interface follows Clean Architecture principles, allowing the domain layer
 * to remain independent of infrastructure details while defining what data operations
 * it needs.
 * 
 * No Spring or framework annotations should be present here to maintain
 * technology independence in the domain layer.
 */
public interface StrategyRepository {

    /**
     * Saves a strategy to the repository.
     * 
     * @param strategy the strategy to save
     * @return the saved strategy with updated fields (e.g., generated ID)
     * @throws IllegalArgumentException if strategy is null
     */
    Strategy save(Strategy strategy);

    /**
     * Finds a strategy by its unique identifier.
     * 
     * @param id the strategy ID
     * @return Optional containing the strategy if found, empty otherwise
     */
    Optional<Strategy> findById(Long id);

    /**
     * Finds a strategy by its name.
     * 
     * @param name the strategy name
     * @return Optional containing the strategy if found, empty otherwise
     */
    Optional<Strategy> findByName(String name);

    /**
     * Retrieves all strategies from the repository.
     * 
     * @return list of all strategies
     */
    List<Strategy> findAll();

    /**
     * Deletes a strategy by its unique identifier.
     * 
     * @param id the strategy ID
     */
    void deleteById(Long id);

    /**
     * Checks if a strategy exists by its unique identifier.
     * 
     * @param id the strategy ID
     * @return true if the strategy exists, false otherwise
     */
    boolean existsById(Long id);
}
