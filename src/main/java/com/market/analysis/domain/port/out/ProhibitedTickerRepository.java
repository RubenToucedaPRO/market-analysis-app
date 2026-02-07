package com.market.analysis.domain.port.out;

import java.util.List;

import com.market.analysis.domain.model.ProhibitedTicker;

/**
 * Output port (repository interface) for ProhibitedTicker persistence
 * operations.
 * Defines the contract for accessing and managing prohibited tickers without
 * binding
 * to specific persistence technologies.
 * 
 * This interface follows Clean Architecture principles, allowing the domain
 * layer
 * to remain independent of infrastructure details while defining what data
 * operations
 * it needs.
 * 
 * No Spring or framework annotations should be present here to maintain
 * technology independence in the domain layer.
 */
public interface ProhibitedTickerRepository {

    /**
     * Retrieves all prohibited tickers from the database.
     * 
     * @return List of ProhibitedTicker representing prohibited tickers.
     */
    public List<ProhibitedTicker> findAll();

    /**
     * Checks if a ticker is prohibited by searching for it in the database.
     * 
     * @param ticker
     * @return
     */
    public boolean existsByTicker(String ticker);

    /**
     * Saves a prohibited ticker to the database.
     * 
     * @param ticker
     */
    public ProhibitedTicker save(ProhibitedTicker ticker);

    /**
     * Deletes a prohibited ticker from the database.
     * 
     * @param ticker
     */
    public void deleteById(Long id);
}
