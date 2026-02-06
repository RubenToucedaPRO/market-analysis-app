package com.market.analysis.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.market.analysis.application.usecase.ManageProhibitedTickerService;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StrategyRepository;

@Configuration
public class BeanConfig {

    @Bean
    public ManageStrategyUseCase manageStrategyUseCase(
            StrategyRepository strategyRepository,
            RuleDefinitionRepository ruleDefinitionRepository) {
        return new ManageStrategyService(strategyRepository, ruleDefinitionRepository);
    }

    @Bean
    public ManageRuleDefinitionUseCase manageRuleDefinitionUseCase(
            RuleDefinitionRepository ruleDefinitionRepository) {
        return new ManageRuleDefinitionService(ruleDefinitionRepository);
    }

    @Bean
    public ManageProhibitedTickerUseCase manageProhibitedTickerUseCase(
            ProhibitedTickerRepository prohibitedTickerRepository) {
        return new ManageProhibitedTickerService(prohibitedTickerRepository);
    }
}
