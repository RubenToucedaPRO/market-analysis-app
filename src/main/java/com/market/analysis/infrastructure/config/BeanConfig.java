package com.market.analysis.infrastructure.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.market.analysis.application.mapper.CandleDTOMapper;
import com.market.analysis.application.mapper.ProhibitedKeywordDTOMapper;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.application.usecase.AnalyzeAndPersistStockService;
import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.application.usecase.ManageProhibitedKeywordService;
import com.market.analysis.application.usecase.ManageProhibitedTickerService;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.application.usecase.SuggestTickersService;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedKeywordUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.FinvizScreenerPort;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestedTickerRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.FinvizFilterMapper;
import com.market.analysis.domain.service.FinvizFilterMapperImpl;
import com.market.analysis.domain.service.ProhibitedKeywordMatcher;
import com.market.analysis.domain.service.PromptBuilder;
import com.market.analysis.domain.service.PromptResponseValidator;
import com.market.analysis.domain.service.RiskRewardCalculator;
import com.market.analysis.domain.service.RuleEvaluator;
import com.market.analysis.domain.service.StockHistoricalService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

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
    public ManageProhibitedKeywordUseCase manageProhibitedKeywordUseCase(
            ProhibitedKeywordRepository prohibitedKeywordRepository,
            ProhibitedKeywordDTOMapper prohibitedKeywordMapper) {
        return new ManageProhibitedKeywordService(prohibitedKeywordRepository, prohibitedKeywordMapper);
    }

    @Bean
    public ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase(StockDataRepository stockDataRepository,
            CandleHistoryRepository candleHistoryRepository,
            StrategyRepository strategyRepository,
            StockProviderPort stockProviderPort,
            ApiIAPort apiIAPort, StockDataDTOMapper stockMapper, CandleDTOMapper candleDTOMapper,
            AnalyzeAndPersistStockService analyzeAndPersistStockService) {
        return new ManageAnalyzeStockService(stockDataRepository,
                candleHistoryRepository, strategyRepository, stockProviderPort, apiIAPort,
                stockMapper, candleDTOMapper, analyzeAndPersistStockService, promptBuilder(),
                promptResponseValidator());
    }

    @Bean
    public AnalyzeAndPersistStockService analyzeAndPersistStockService(
            StockDataRepository stockDataRepository,
            StrategyEvaluationRepository strategyEvaluationRepository,
            ApiCallRateRepository apiCallRateRepository,
            CandleHistoryRepository candleHistoryRepository,
            CompanyProfileRepository companyProfileRepository,
            ProhibitedKeywordRepository prohibitedKeywordRepository,
            ProhibitedTickerRepository prohibitedTickerRepository,
            StockProviderPort stockProviderPort,
            HistoricalProviderPort historicalProviderPort,
            StockHistoricalService stockHistoricalService,
            EvaluateStrategyService evaluateStrategyService,
            ProhibitedKeywordMatcher prohibitedKeywordMatcher) {
        return new AnalyzeAndPersistStockService(
                stockDataRepository,
                strategyEvaluationRepository,
                apiCallRateRepository,
                candleHistoryRepository,
                companyProfileRepository,
                prohibitedKeywordRepository,
                prohibitedTickerRepository,
                stockProviderPort,
                historicalProviderPort,
                stockHistoricalService,
                evaluateStrategyService,
                prohibitedKeywordMatcher);
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
    public PromptBuilder promptBuilder() {
        return new PromptBuilder();
    }

    @Bean
    public ProhibitedKeywordMatcher prohibitedKeywordMatcher() {
        return new ProhibitedKeywordMatcher();
    }

    @Bean
    public PromptResponseValidator promptResponseValidator() {
        return new PromptResponseValidator();
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
    public FinvizFilterMapper finvizFilterMapper() {
        return new FinvizFilterMapperImpl();
    }

    @Bean
    public SuggestTickersUseCase suggestTickersUseCase(
            StrategyRepository strategyRepository,
            FinvizFilterMapper finvizFilterMapper,
            FinvizScreenerPort finvizScreenerPort,
            AnalyzeAndPersistStockService analyzeAndPersistStockService,
            SuggestionSnapshotRepository suggestionSnapshotRepository,
            SuggestedTickerRepository suggestedTickerRepository,
            StockDataRepository stockDataRepository) {
        return new SuggestTickersService(
                strategyRepository,
                finvizFilterMapper,
                finvizScreenerPort,
                analyzeAndPersistStockService,
                suggestionSnapshotRepository,
                suggestedTickerRepository,
                stockDataRepository);
    }

    @Bean
    public RestClient finnhubRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(90000);

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

    @Bean
    public OpenAIClient openAIClient(
            @Value("${openrouter.api.key}") String apiKey) {
        return OpenAIOkHttpClient.builder()
                .baseUrl(ApiConstants.OPENROUTER_BASE_URL)
                .apiKey(apiKey)
                .putHeader(ApiConstants.OPENROUTER_HEADER_REFERER, ApiConstants.OPENROUTER_DEFAULT_REFERER)
                .build();
    }
}
