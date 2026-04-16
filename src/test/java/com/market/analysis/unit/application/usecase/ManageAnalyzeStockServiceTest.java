package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManageAnalyzeStockService Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class ManageAnalyzeStockServiceTest {

    @Mock
    private StockDataRepository stockDataRepository;

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private ProhibitedTickerRepository prohibitedTickerRepository;

    @Mock
    private StockProviderPort stockProviderPort;

    @Mock
    private com.market.analysis.domain.port.out.StrategyRepository strategyRepository;

    @Mock
    private StockDataDTOMapper stockDataDTOMapper;

    @Mock
    private com.market.analysis.application.mapper.CandleDTOMapper candleDTOMapper;

    @Mock
    private com.market.analysis.domain.port.out.ApiCallRateRepository apiCallRateRepository;

    @Mock
    private com.market.analysis.domain.port.out.HistoricalProviderPort historicalProviderPort;

    @Mock
    private CandleHistoryRepository candleHistoryPort;

    @Mock
    private com.market.analysis.domain.service.StockHistoricalService stockHistoricalService;

    @Mock
    private com.market.analysis.domain.port.out.ApiIAPort apiIAPort;

    @Mock
    private com.market.analysis.domain.port.out.StrategyEvaluationRepository strategyEvaluationRepository;

    @Mock
    private com.market.analysis.domain.service.EvaluateStrategyService evaluateStrategyService;

    @Mock
    private com.market.analysis.domain.service.PromptBuilder promptBuilder;

    @Mock
    private com.market.analysis.domain.service.PromptResponseValidator promptResponseValidator;

    @InjectMocks
    private ManageAnalyzeStockService service;

    private Stock stock;
    private CompanyProfile validCompanyProfile;
    private CompanyProfile prohibitedCompanyProfile;
    private com.market.analysis.domain.model.Strategy testStrategy;

    @BeforeEach
    void setUp() {
        stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .build();

        validCompanyProfile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .country("US")
                .exchange("NASDAQ")
                .lastUpdated(Instant.now())
                .build();

        prohibitedCompanyProfile = CompanyProfile.builder()
                .ticker("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .country("US")
                .exchange("NYSE")
                .lastUpdated(Instant.now())
                .build();

        testStrategy = com.market.analysis.domain.model.Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("A test strategy")
                .rules(Arrays.asList(
                        com.market.analysis.domain.model.Rule.builder()
                                .id(1L)
                                .name("Price > SMA20")
                                .subjectCode("PRICE")
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(20.0)
                                .description("Price above SMA20")
                                .build()))
                .build();

        // Setup default mocks for historical data flow
        when(stockDataRepository.findByTickerAndLastUpdateBetween(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(null);

        // Mock historical data
        com.market.analysis.domain.model.HistoricalData historicalData = com.market.analysis.domain.model.HistoricalData
                .builder()
                .ticker("AAPL")
                .lastUpdate(Instant.now())
                .build();
        when(historicalProviderPort.fetchHistoricalData(anyString())).thenReturn(historicalData);

        // Mock technical indicators
        com.market.analysis.domain.model.TechnicalIndicators technicalIndicators = com.market.analysis.domain.model.TechnicalIndicators
                .builder()
                .sma20(BigDecimal.valueOf(150.00))
                .sma50(BigDecimal.valueOf(150.00))
                .sma200(BigDecimal.valueOf(150.00))
                .currentVolume(50000000L)
                .averageVolume(50000000L)
                .lastUpdated(Instant.now())
                .build();
        when(stockHistoricalService.calculateIndicators(any(), anyInt())).thenReturn(technicalIndicators);

        // Mock strategy evaluation
        StrategyEvaluation mockEvaluation = StrategyEvaluation.builder()
                .ticker("AAPL")
                .strategyId(1L)
                .strategyName("Test Strategy")
                .compliant(true)
                .complianceRate(BigDecimal.valueOf(100.00))
                .summary("Test passed")
                .evaluatedAt(Instant.now())
                .priceAtEvaluation(BigDecimal.valueOf(150.00))
                .isLatest(true)
                .build();
        when(evaluateStrategyService.evaluateStrategy(any(), any())).thenReturn(mockEvaluation);
    }

    @Test
    @DisplayName("Should get stock data for valid ticker")
    void shouldGetStockDataForValidTicker() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(strategyRepository, times(1)).findById(1L);
        verify(evaluateStrategyService, times(1)).evaluateStrategy(any(), any());
        verify(stockDataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should get stock data for multiple tickers")
    void shouldGetStockDataForMultipleTickers() {
        // Arrange
        Stock stock2 = Stock.builder().ticker("GOOGL").currentPrice(BigDecimal.valueOf(2800.00)).build();
        CompanyProfile profile2 = CompanyProfile.builder()
                .ticker("GOOGL")
                .name("Alphabet Inc.")
                .lastUpdated(Instant.now())
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(companyProfileRepository.findByTicker("GOOGL")).thenReturn(Optional.of(profile2));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(stockProviderPort.getQuote("GOOGL")).thenReturn(stock2);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL,GOOGL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockProviderPort, times(1)).getQuote("GOOGL");
        verify(stockDataRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should parse and normalize tickers")
    void shouldParseAndNormalizeTickers() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(companyProfileRepository.findByTicker("GOOGL")).thenReturn(Optional.of(validCompanyProfile));
        when(companyProfileRepository.findByTicker("TSLA")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote(anyString())).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("  aapl  ,  googl  ,  TSLA  ", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockProviderPort, times(1)).getQuote("GOOGL");
        verify(stockProviderPort, times(1)).getQuote("TSLA");
    }

    @Test
    @DisplayName("Should skip empty tickers after parsing")
    void shouldSkipEmptyTickersAfterParsing() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL,,  ,", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should update company profile when it does not exist")
    void shouldUpdateCompanyProfileWhenItDoesNotExist() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.empty());
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("AAPL")).thenReturn(validCompanyProfile);
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("AAPL");
        verify(companyProfileRepository, times(1)).save(validCompanyProfile);
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should update company profile when it is outdated")
    void shouldUpdateCompanyProfileWhenItIsOutdated() {
        // Arrange
        CompanyProfile outdatedProfile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(Instant.now().minus(31, java.time.temporal.ChronoUnit.DAYS))
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(outdatedProfile));
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("AAPL")).thenReturn(validCompanyProfile);
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("AAPL");
        verify(companyProfileRepository, times(1)).save(validCompanyProfile);
    }

    @Test
    @DisplayName("Should mark ticker as prohibited when company profile indicates it")
    void shouldMarkTickerAsProhibitedWhenCompanyProfileIndicatesIt() {
        // Arrange
        when(companyProfileRepository.findByTicker("SPY")).thenReturn(Optional.empty());
        when(prohibitedTickerRepository.existsByTicker("SPY")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("SPY")).thenReturn(prohibitedCompanyProfile);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("SPY", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("SPY");
        verify(prohibitedTickerRepository, times(1)).save(any(ProhibitedTicker.class));
        verify(stockProviderPort, never()).getQuote("SPY");
        verify(stockDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip ticker when it is already marked as prohibited")
    void shouldSkipTickerWhenItIsAlreadyMarkedAsProhibited() {
        // Arrange
        when(companyProfileRepository.findByTicker("SPY")).thenReturn(Optional.empty());
        when(prohibitedTickerRepository.existsByTicker("SPY")).thenReturn(true);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("SPY", 1L);

        // Assert
        verify(prohibitedTickerRepository, times(1)).existsByTicker("SPY");
        verify(stockProviderPort, never()).getCompanyProfile("SPY");
        verify(stockProviderPort, never()).getQuote("SPY");
    }

    @Test
    @DisplayName("Should skip ticker when company profile is not found")
    void shouldSkipTickerWhenCompanyProfileIsNotFound() {
        // Arrange
        when(companyProfileRepository.findByTicker("INVALID")).thenReturn(Optional.empty());
        when(prohibitedTickerRepository.existsByTicker("INVALID")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("INVALID")).thenReturn(null);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("INVALID", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("INVALID");
        verify(stockProviderPort, never()).getQuote("INVALID");
        verify(stockDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not save stock data when quote is null")
    void shouldNotSaveStockDataWhenQuoteIsNull() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find all stocks")
    void shouldFindAllStocks() {
        // Arrange
        Stock stock2 = Stock.builder().ticker("GOOGL").currentPrice(BigDecimal.valueOf(2800.00)).build();
        List<Stock> stocks = Arrays.asList(stock, stock2);
        when(stockDataRepository.findAllStocks()).thenReturn(stocks);

        StockDataDTO dto1 = StockDataDTO.builder().ticker("AAPL").currentPrice(BigDecimal.valueOf(150.00)).build();
        StockDataDTO dto2 = StockDataDTO.builder().ticker("GOOGL").currentPrice(BigDecimal.valueOf(2800.00)).build();
        when(stockDataDTOMapper.toDTO(stock)).thenReturn(dto1);
        when(stockDataDTOMapper.toDTO(stock2)).thenReturn(dto2);

        // Act
        List<StockDataDTO> result = service.findAllStocks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stockDataRepository, times(1)).findAllStocks();
    }

    @Test
    @DisplayName("Should find stock data by ticker")
    void shouldFindStockDataByTicker() {
        // Arrange
        when(stockDataRepository.findById(1L)).thenReturn(Optional.of(stock));

        StockDataDTO dto = StockDataDTO.builder().ticker("AAPL").currentPrice(BigDecimal.valueOf(150.00)).build();
        when(stockDataDTOMapper.toDTO(stock)).thenReturn(dto);

        // Act
        StockDataDTO result = service.findStockDataById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        verify(stockDataRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw StockDataNotFoundException when ticker not found")
    void shouldThrowStockDataNotFoundExceptionWhenTickerNotFound() {
        // Arrange
        when(stockDataRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockDataNotFoundException.class, () -> {
            service.findStockDataById(999L);
        });
        verify(stockDataRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should update stock data for existing ticker")
    void shouldUpdateStockDataForExistingTicker() {
        // Arrange
        when(stockDataRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.updateStockData(1L);

        // Assert
        verify(stockDataRepository, times(1)).findById(1L);
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should not update stock data when quote is null")
    void shouldNotUpdateStockDataWhenQuoteIsNull() {
        // Arrange
        Stock nullStock = Stock.builder().ticker("AAPL").build();
        when(stockDataRepository.findById(1L)).thenReturn(Optional.of(nullStock));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);

        // Act
        service.updateStockData(1L);

        // Assert
        verify(stockDataRepository, times(1)).findById(1L);
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle company profile with null lastUpdated")
    void shouldHandleCompanyProfileWithNullLastUpdated() {
        // Arrange
        CompanyProfile profileWithNullLastUpdated = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(null)
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(profileWithNullLastUpdated));
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("AAPL")).thenReturn(validCompanyProfile);
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("AAPL");
        verify(companyProfileRepository, times(1)).save(validCompanyProfile);
    }

    @Test
    @DisplayName("Should successfully get AI valoration for existing stock")
    void shouldGetAIValorationForExistingStock() {
        // Arrange
        Long stockId = 1L;
        String expectedValoration = "Esta acción muestra indicadores técnicos fuertes con un momentum alcista.";
        
        com.market.analysis.domain.model.StrategyEvaluation strategyEvaluation = 
            com.market.analysis.domain.model.StrategyEvaluation.builder()
                .strategyName("Test Strategy")
                .complianceRate(BigDecimal.valueOf(85.5))
                .summary("Strategy evaluation passed")
                .build();
        
        Stock stockWithEvaluation = Stock.builder()
                .id(stockId)
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .sma20(BigDecimal.valueOf(148.00))
                .sma50(BigDecimal.valueOf(145.00))
                .sma200(BigDecimal.valueOf(140.00))
                .volume(1000000L)
                .averageVolume(950000L)
                .strategyEvaluation(strategyEvaluation)
                .build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stockWithEvaluation));
        when(promptBuilder.buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class))).thenReturn("prompt");
        when(apiIAPort.getValoration(anyString())).thenReturn(expectedValoration);
        when(promptResponseValidator.isValid(expectedValoration)).thenReturn(true);
        when(stockDataRepository.save(any(Stock.class))).thenReturn(stockWithEvaluation);

        // Act
        boolean generated = service.getValorationIA(stockId);

        // Assert
        assertThat(generated).isTrue();
        verify(stockDataRepository, times(1)).findById(stockId);
        verify(promptBuilder, times(1)).buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class));
        verify(apiIAPort, times(1)).getValoration(anyString());
        verify(promptResponseValidator, times(1)).isValid(expectedValoration);
        verify(stockDataRepository, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Should throw exception when stock not found for AI valoration")
    void shouldThrowExceptionWhenStockNotFoundForAIValoration() {
        // Arrange
        Long stockId = 999L;
        when(stockDataRepository.findById(stockId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockDataNotFoundException.class, () -> service.getValorationIA(stockId));

        verify(stockDataRepository, times(1)).findById(stockId);
        verify(apiIAPort, never()).getValoration(anyString());
        verify(stockDataRepository, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("Should save stock even when AI valoration returns null")
    void shouldSaveStockWhenAIValorationReturnsNull() {
        // Arrange
        Long stockId = 1L;
        
        com.market.analysis.domain.model.StrategyEvaluation strategyEvaluation = 
            com.market.analysis.domain.model.StrategyEvaluation.builder()
                .strategyName("Test Strategy")
                .complianceRate(BigDecimal.valueOf(85.5))
                .summary("Strategy evaluation passed")
                .build();
        
        Stock stockWithEvaluation = Stock.builder()
                .id(stockId)
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .sma20(BigDecimal.valueOf(148.00))
                .sma50(BigDecimal.valueOf(145.00))
                .sma200(BigDecimal.valueOf(140.00))
                .volume(1000000L)
                .averageVolume(950000L)
                .strategyEvaluation(strategyEvaluation)
                .build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stockWithEvaluation));
        when(promptBuilder.buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class))).thenReturn("prompt");

        // Primera respuesta + validación
        when(apiIAPort.getValoration(anyString())).thenReturn(null);
        when(promptResponseValidator.isValid(null)).thenReturn(false);

        // Prompt de reintento + segunda respuesta + validación
        when(promptResponseValidator.buildRetryPrompt("prompt")).thenReturn("retry-prompt");
        when(apiIAPort.getValoration("retry-prompt")).thenReturn("No válido");
        when(promptResponseValidator.isValid("No válido")).thenReturn(false);
        when(stockDataRepository.save(any(Stock.class))).thenReturn(stockWithEvaluation);

        // Act
        boolean generated = service.getValorationIA(stockId);

        // Assert
        assertThat(generated).isFalse();
        verify(stockDataRepository, times(1)).findById(stockId);
        verify(promptBuilder, times(1)).buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class));
        verify(apiIAPort, times(2)).getValoration(anyString());
        verify(promptResponseValidator, times(1)).buildRetryPrompt("prompt");
        verify(stockDataRepository, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Should retry AI valoration when first response format is invalid and save retry response")
    void shouldRetryAIValorationWhenFirstResponseFormatIsInvalidAndSaveRetryResponse() {
        Long stockId = 1L;
        String invalidResponse = "Respuesta sin secciones";
        String validRetryResponse = """
                Resumen técnico: Tendencia alcista.
                Fortalezas: Precio sobre SMA20.
                Riesgos: Volumen bajo.
                Conclusión interpretativa: Contexto positivo con cautela.
                """;

        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder()
                .strategyName("Test Strategy")
                .complianceRate(BigDecimal.valueOf(85.5))
                .summary("Strategy evaluation passed")
                .build();

        Stock stockWithEvaluation = Stock.builder()
                .id(stockId)
                .ticker("AAPL")
                .strategyEvaluation(strategyEvaluation)
                .build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stockWithEvaluation));
        when(promptBuilder.buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class))).thenReturn("prompt");
        when(apiIAPort.getValoration("prompt")).thenReturn(invalidResponse);
        when(promptResponseValidator.isValid(invalidResponse)).thenReturn(false);
        when(promptResponseValidator.buildRetryPrompt("prompt")).thenReturn("retry-prompt");
        when(apiIAPort.getValoration("retry-prompt")).thenReturn(validRetryResponse);
        when(promptResponseValidator.isValid(validRetryResponse)).thenReturn(true);

        boolean generated = service.getValorationIA(stockId);

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        assertThat(generated).isTrue();
        verify(stockDataRepository).save(stockCaptor.capture());
        verify(apiIAPort, times(2)).getValoration(promptCaptor.capture());
        assertThat(promptCaptor.getAllValues()).containsExactly("prompt", "retry-prompt");
        assertThat(stockCaptor.getValue().getValorationIA()).isEqualTo(validRetryResponse);
    }

    @Test
    @DisplayName("Should save fallback valoration when both AI responses are invalid")
    void shouldSaveFallbackValorationWhenBothAIResponsesAreInvalid() {
        Long stockId = 1L;
        String firstInvalid = "Sin formato";
        String secondInvalid = "Todavía sin formato";

        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder()
                .strategyName("Test Strategy")
                .build();

        Stock stockWithEvaluation = Stock.builder()
                .id(stockId)
                .ticker("AAPL")
                .strategyEvaluation(strategyEvaluation)
                .build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stockWithEvaluation));
        when(promptBuilder.buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class))).thenReturn("prompt");
        when(apiIAPort.getValoration("prompt")).thenReturn(firstInvalid);
        when(promptResponseValidator.isValid(firstInvalid)).thenReturn(false);
        when(promptResponseValidator.buildRetryPrompt("prompt")).thenReturn("retry-prompt");
        when(apiIAPort.getValoration("retry-prompt")).thenReturn(secondInvalid);
        when(promptResponseValidator.isValid(secondInvalid)).thenReturn(false);

        boolean generated = service.getValorationIA(stockId);

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        assertThat(generated).isFalse();
        verify(stockDataRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getValorationIA())
                .isEqualTo("No se pudo generar una valoración interpretativa válida en este momento. Reintenta más tarde.");
        verify(apiIAPort, times(2)).getValoration(anyString());
    }

    @Test
    @DisplayName("Should truncate oversized prompt before requesting AI valoration")
    void shouldTruncateOversizedPromptBeforeRequestingAIValoration() {
        Long stockId = 1L;
        String longPrompt = "A".repeat(4500);
        String validResponse = """
                Resumen técnico: Tendencia alcista.
                Fortalezas: Precio sobre SMA20.
                Riesgos: Volumen bajo.
                Conclusión interpretativa: Contexto positivo con cautela.
                """;

        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder()
                .strategyName("Test Strategy")
                .build();

        Stock stockWithEvaluation = Stock.builder()
                .id(stockId)
                .ticker("AAPL")
                .strategyEvaluation(strategyEvaluation)
                .build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stockWithEvaluation));
        when(promptBuilder.buildAnalysisPrompt(any(Stock.class), any(StrategyEvaluation.class))).thenReturn(longPrompt);
        when(apiIAPort.getValoration(anyString())).thenReturn(validResponse);
        when(promptResponseValidator.isValid(validResponse)).thenReturn(true);

        boolean generated = service.getValorationIA(stockId);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        assertThat(generated).isTrue();
        verify(apiIAPort).getValoration(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).hasSize(4000);
        assertThat(promptCaptor.getValue()).isEqualTo(longPrompt.substring(0, 4000));
    }

    // -------------------------------------------------------------------------
    // getStockData – strategy validation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw IllegalArgumentException when strategyId is null")
    void getStockData_nullStrategyId_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getStockData("AAPL", null));

        assertThat(ex.getMessage()).contains("Strategy ID is required");
        verify(strategyRepository, never()).findById(any());
        verify(stockProviderPort, never()).getQuote(anyString());
        verify(stockDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when strategy is not found")
    void getStockData_strategyNotFound_throwsIllegalArgumentException() {
        when(strategyRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getStockData("AAPL", 99L));

        assertThat(ex.getMessage()).contains("99");
        verify(strategyRepository, times(1)).findById(99L);
        verify(stockProviderPort, never()).getQuote(anyString());
        verify(stockDataRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateStockData – not found path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw StockDataNotFoundException when stock not found for update")
    void updateStockData_stockNotFound_throwsStockDataNotFoundException() {
        when(stockDataRepository.findById(999L)).thenReturn(Optional.empty());

        StockDataNotFoundException ex = assertThrows(StockDataNotFoundException.class,
                () -> service.updateStockData(999L));

        assertThat(ex.getMessage()).contains("999");
        verify(stockDataRepository, times(1)).findById(999L);
        verify(stockProviderPort, never()).getQuote(anyString());
        verify(stockDataRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getDataFromProvider – cached daily metrics path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should use cached daily metrics when stock already exists for today and not call historical provider")
    void getDataFromProvider_stockExistsForToday_usesCachedMetricsAndSkipsHistoricalProvider() {
        // Arrange: simulate a stock already persisted today
        Stock cachedStock = Stock.builder()
                .ticker("AAPL")
                .sma20(BigDecimal.valueOf(148.00))
                .sma50(BigDecimal.valueOf(145.00))
                .sma200(BigDecimal.valueOf(140.00))
                .volume(1_000_000L)
                .averageVolume(900_000L)
                .lastUpdated(Instant.now())
                .ema9(BigDecimal.valueOf(149.00))
                .ema12(BigDecimal.valueOf(148.50))
                .ema20(BigDecimal.valueOf(148.00))
                .ema26(BigDecimal.valueOf(147.00))
                .ema50(BigDecimal.valueOf(145.00))
                .ema200(BigDecimal.valueOf(140.00))
                .rsi14(BigDecimal.valueOf(60.0))
                .rsi30(BigDecimal.valueOf(55.0))
                .macdLine(BigDecimal.valueOf(1.5))
                .macdSignal(BigDecimal.valueOf(1.2))
                .macdHistogram(BigDecimal.valueOf(0.3))
                .bbUpper20(BigDecimal.valueOf(155.00))
                .bbLower20(BigDecimal.valueOf(145.00))
                .atr14(BigDecimal.valueOf(2.5))
                .build();

        when(stockDataRepository.findByTickerAndLastUpdateBetween(eq("AAPL"), any(Instant.class), any(Instant.class)))
                .thenReturn(cachedStock);
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert – historical provider must NOT be called when a cached entry exists
        verify(historicalProviderPort, never()).fetchHistoricalData(anyString());
        verify(stockDataRepository, times(1)).save(any());
    }

    // -------------------------------------------------------------------------
    // enrichWithFreshHistoricalIndicators – null technical indicators
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should save stock without indicators when calculateIndicators returns null")
    void enrichWithFreshHistoricalIndicators_nullIndicators_stockSavedWithoutIndicators() {
        // Arrange
        when(stockHistoricalService.calculateIndicators(any(), anyInt())).thenReturn(null);
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert – stock is still saved even when indicators are null
        verify(stockHistoricalService, times(1)).calculateIndicators(any(), anyInt());
        verify(stockDataRepository, times(1)).save(any());
    }

    // -------------------------------------------------------------------------
    // F1.9 – Candle persistence orchestration (Use Case responsibility)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should call saveCandlesForTicker when historical data contains candles")
    void shouldPersistCandlesWhenHistoricalDataContainsCandles() {
        // Arrange
        Candle candle = Candle.builder()
                .ticker("AAPL")
                .dateTime(Instant.now())
                .openPrice(BigDecimal.valueOf(149.0))
                .highPrice(BigDecimal.valueOf(152.0))
                .lowPrice(BigDecimal.valueOf(148.5))
                .closePrice(BigDecimal.valueOf(151.0))
                .volume(50_000_000L)
                .build();

        com.market.analysis.domain.model.HistoricalData historicalDataWithCandles =
                com.market.analysis.domain.model.HistoricalData.builder()
                        .ticker("AAPL")
                        .lastUpdate(Instant.now())
                        .candles(List.of(candle))
                        .build();

        when(historicalProviderPort.fetchHistoricalData("AAPL")).thenReturn(historicalDataWithCandles);
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(candleHistoryPort, times(1)).saveCandlesForTicker(eq("AAPL"), anyList());
    }

    @Test
    @DisplayName("Should not call saveCandlesForTicker when historical data candles list is null")
    void shouldNotPersistCandlesWhenHistoricalDataCandlesIsNull() {
        // Arrange — historical data with explicitly null candles list
        com.market.analysis.domain.model.HistoricalData historicalDataNullCandles =
                com.market.analysis.domain.model.HistoricalData.builder()
                        .ticker("AAPL")
                        .lastUpdate(Instant.now())
                        .candles(null)
                        .build();

        when(historicalProviderPort.fetchHistoricalData("AAPL")).thenReturn(historicalDataNullCandles);
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(candleHistoryPort, never()).saveCandlesForTicker(anyString(), anyList());
    }

    @Test
    @DisplayName("Should not call saveCandlesForTicker when historical data has no candles")
    void shouldNotPersistCandlesWhenHistoricalDataHasNoCandles() {
        // Arrange — default historicalData from setUp() has an empty candles list
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(candleHistoryPort, never()).saveCandlesForTicker(anyString(), anyList());
    }

    // -------------------------------------------------------------------------
    // deleteById – conditional candle cleanup
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should delete associated candles when no stock data remains for ticker after deletion")
    void deleteById_noRemainingStockForTicker_deletesCandles() {
        Long id = 1L;
        String ticker = "AAPL";

        when(stockDataRepository.existsByTicker(ticker)).thenReturn(false);

        service.deleteById(id, ticker);

        verify(stockDataRepository, times(1)).deleteById(id);
        verify(candleHistoryPort, times(1)).deleteCandlesByTicker(ticker);
    }

    @Test
    @DisplayName("Should not delete candles when stock data still exists for ticker after deletion")
    void deleteById_stockDataStillExistsForTicker_skipsCandleDeletion() {
        Long id = 2L;
        String ticker = "MSFT";

        when(stockDataRepository.existsByTicker(ticker)).thenReturn(true);

        service.deleteById(id, ticker);

        verify(stockDataRepository, times(1)).deleteById(id);
        verify(candleHistoryPort, never()).deleteCandlesByTicker(anyString());
    }

    // -------------------------------------------------------------------------
    // findCandlesByStockId (F2.6)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findCandlesByStockId: should throw StockDataNotFoundException when stock not found")
    void findCandlesByStockId_stockNotFound_throwsException() {
        when(stockDataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.market.analysis.domain.exception.StockDataNotFoundException.class,
                () -> service.findCandlesByStockId(999L));

        verify(candleHistoryPort, never()).findCandlesByTicker(anyString());
    }

    @Test
    @DisplayName("findCandlesByStockId: should delegate to candleDTOMapper with stock and candles")
    void findCandlesByStockId_noCandles_delegatesToMapper() {
        Stock stockWithSmas = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .sma20(BigDecimal.valueOf(148.00))
                .sma50(BigDecimal.valueOf(145.00))
                .sma200(BigDecimal.valueOf(140.00))
                .build();
        com.market.analysis.application.dto.CandleChartDTO expected =
                com.market.analysis.application.dto.CandleChartDTO.builder()
                        .ticker("AAPL")
                        .candles(List.of())
                        .sma20(BigDecimal.valueOf(148.00))
                        .sma50(BigDecimal.valueOf(145.00))
                        .sma200(BigDecimal.valueOf(140.00))
                        .build();
        when(stockDataRepository.findById(1L)).thenReturn(Optional.of(stockWithSmas));
        when(candleHistoryPort.findCandlesByTicker("AAPL")).thenReturn(List.of());
        when(candleDTOMapper.toChartDTO(stockWithSmas, List.of())).thenReturn(expected);

        com.market.analysis.application.dto.CandleChartDTO result = service.findCandlesByStockId(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        assertThat(result.getCandles()).isEmpty();
        verify(candleDTOMapper, times(1)).toChartDTO(stockWithSmas, List.of());
    }

    @Test
    @DisplayName("findCandlesByStockId: should pass candle list to mapper")
    void findCandlesByStockId_withCandles_passesCandles() {
        Instant candle1Time = Instant.parse("2024-01-15T00:00:00Z");
        Candle candle1 = Candle.builder()
                .ticker("AAPL")
                .dateTime(candle1Time)
                .openPrice(BigDecimal.valueOf(181.00))
                .highPrice(BigDecimal.valueOf(183.50))
                .lowPrice(BigDecimal.valueOf(180.00))
                .closePrice(BigDecimal.valueOf(182.75))
                .volume(55_000_000L)
                .build();

        Stock stockWithSmas = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .sma20(BigDecimal.valueOf(148.00))
                .sma50(null)
                .sma200(null)
                .build();

        com.market.analysis.application.dto.CandleChartDTO expected =
                com.market.analysis.application.dto.CandleChartDTO.builder()
                        .ticker("AAPL")
                        .candles(List.of())
                        .build();

        when(stockDataRepository.findById(1L)).thenReturn(Optional.of(stockWithSmas));
        when(candleHistoryPort.findCandlesByTicker("AAPL")).thenReturn(List.of(candle1));
        when(candleDTOMapper.toChartDTO(eq(stockWithSmas), anyList())).thenReturn(expected);

        service.findCandlesByStockId(1L);

        verify(candleHistoryPort, times(1)).findCandlesByTicker("AAPL");
        verify(candleDTOMapper, times(1)).toChartDTO(eq(stockWithSmas), eq(List.of(candle1)));
    }
}
