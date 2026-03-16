package com.market.analysis.infrastructure.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.market.analysis.application.mapper.CandleDTOMapper;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.application.usecase.ManageProhibitedTickerService;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.RiskRewardCalculator;
import com.market.analysis.domain.service.RuleEvaluator;
import com.market.analysis.domain.service.StockHistoricalService;

@Configuration
public class BeanConfig {

    @Value("${finnhub.base.url:https:}")
    private String finnhubBaseUrl;

    @Value("${finnhub.api.token:}")
    private String finnhubToken;

    @Bean
    public ManageStrategyUseCase manageStrategyUseCase(
            StrategyRepository strategyRepository,
            RuleDefinitionRepository ruleDefinitionRepository, StockDataRepository stockDataRepository,
            StrategyDTOMapper strategyMapper,
            RuleDefinitionDTOMapper ruleDefinitionMapper,
            EvaluateStrategyService evaluateStrategyService) {
        return new ManageStrategyService(strategyRepository, ruleDefinitionRepository, stockDataRepository,
                strategyMapper, ruleDefinitionMapper, evaluateStrategyService);
    }

    @Bean
    public ManageRuleDefinitionUseCase manageRuleDefinitionUseCase(
            RuleDefinitionRepository ruleDefinitionRepository, RuleDefinitionDTOMapper ruleDefinitionMapper) {
        return new ManageRuleDefinitionService(ruleDefinitionRepository, ruleDefinitionMapper);
    }

    @Bean
    public ManageProhibitedTickerUseCase manageProhibitedTickerUseCase(
            ProhibitedTickerRepository prohibitedTickerRepository, ProhibitedTickerDTOMapper prohibitedTickerMapper) {
        return new ManageProhibitedTickerService(prohibitedTickerRepository, prohibitedTickerMapper);
    }

    @Bean
    public ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase(StockDataRepository stockDataRepository,
            CompanyProfileRepository companyProfileRepository, ProhibitedTickerRepository prohibitedTickerRepository,
            StrategyEvaluationRepository strategyEvaluationRepository,
            ApiCallRateRepository apiCallRateRepository, CandleHistoryRepository candleHistoryRepository,
            StrategyRepository strategyRepository,
            StockProviderPort stockProviderPort, HistoricalProviderPort historicalProviderPort,
            ApiIAPort apiIAPort, StockDataDTOMapper stockMapper, CandleDTOMapper candleDTOMapper,
            StockHistoricalService stockHistoricalService, EvaluateStrategyService evaluateStrategyService) {
        return new ManageAnalyzeStockService(stockDataRepository, companyProfileRepository,
                prohibitedTickerRepository, strategyEvaluationRepository, apiCallRateRepository,candleHistoryRepository, strategyRepository,stockProviderPort,
                historicalProviderPort,
                apiIAPort,
                stockMapper, candleDTOMapper, stockHistoricalService, evaluateStrategyService);
    }

    @Bean
    public RuleEvaluator ruleEvaluator() {
        return new RuleEvaluator();
    }

    @Bean
    public StockHistoricalService stockHistoricalService() {
        return new StockHistoricalService();
    }

    @Bean
    public RiskRewardCalculator riskRewardCalculator() {
        return new RiskRewardCalculator();
    }

    @Bean
    public EvaluateStrategyService evaluateStrategyService(RuleEvaluator ruleEvaluator, RiskRewardCalculator riskRewardCalculator) {
        return new EvaluateStrategyService(ruleEvaluator, riskRewardCalculator);
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

    @Bean
    public RestTemplate polygonRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    /**
     * Bean for Hibernate 6 StatementInspector to enable slow query logging.
     * 
     * <p>
     * This inspector provides centralized database observability by intercepting
     * SQL statements and applying security sanitization. Works in conjunction with
     * Hibernate's slow query logging configured in application.properties.
     * </p>
     * 
     * @return configured SlowQueryInspector instance
     */
    @Bean
    public SlowQueryInspector slowQueryInspector() {
        return new SlowQueryInspector();
    }
}
