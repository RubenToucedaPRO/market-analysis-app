package com.market.analysis.infrastructure.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.application.usecase.ManageProhibitedTickerService;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.infrastructure.external.finnhub.FinnhubAdapter;

@Configuration
public class BeanConfig {

    @Value("${finnhub.base.url:https:}")
    private String finnhubBaseUrl;

    @Value("${finnhub.api.token:}")
    private String finnhubToken;

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

    @Bean
    public ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase(StockDataRepository tickerDataRepository,
            CompanyProfileRepository companyProfileRepository, FinnhubAdapter finnhubAdapter) {
        return new ManageAnalyzeStockService(tickerDataRepository, companyProfileRepository, finnhubAdapter);
    }

    @Bean
    public RestClient finnhubRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);

        if (finnhubBaseUrl == null || finnhubBaseUrl.isEmpty()) {
            throw new IllegalStateException("Finnhub base URL is not configured properly.");
        }
        return builder
                .baseUrl(Objects.requireNonNull(finnhubBaseUrl))
                .requestFactory(factory)
                .requestInterceptor(new ApiKeyObfuscatorInterceptor())
                .build();
    }
}
