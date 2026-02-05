package com.market.analysis.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.StrategyRepository;

@Configuration
public class BeanConfig {

    @Bean
    public ManageStrategyUseCase manageStrategyUseCase(StrategyRepository strategyRepository) {
        return new ManageStrategyService(strategyRepository);
    }
}
