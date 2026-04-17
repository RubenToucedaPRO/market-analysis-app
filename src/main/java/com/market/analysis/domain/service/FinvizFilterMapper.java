package com.market.analysis.domain.service;

import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.Strategy;

/**
 * Domain mapper responsible for translating strategy rules to Finviz filters.
 */
public interface FinvizFilterMapper {

    FinvizFilterMappingResult map(Strategy strategy);
}
