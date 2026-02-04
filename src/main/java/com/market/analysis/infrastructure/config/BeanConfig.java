package com.market.analysis.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.ManageStrategyUseCase;

@Configuration
public class BeanConfig {

    @Bean
    public ManageStrategyUseCase manageStrategyUseCase(StrategyRepository strategyRepository) {
        return new ManageStrategyService(strategyRepository);
    }
}
