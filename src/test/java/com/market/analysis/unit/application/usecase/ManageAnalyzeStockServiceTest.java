package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManageAnalyzeStockService Tests")
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
    private com.market.analysis.domain.port.in.EvaluateStrategyUseCase evaluateStrategyUseCase;

    @Mock
    private StockDataDTOMapper stockDataDTOMapper;

    @InjectMocks
    private ManageAnalyzeStockService service;

    private Stock stock;
    private CompanyProfile validCompanyProfile;
    private CompanyProfile prohibitedCompanyProfile;
    private com.market.analysis.domain.model.Strategy testStrategy;
    private com.market.analysis.domain.model.AnalysisResult analysisResult;

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

        analysisResult = com.market.analysis.domain.model.AnalysisResult.builder()
                .strategy(testStrategy)
                .ticker("AAPL")
                .analysisTimestamp(Instant.now())
                .ruleResults(Arrays.asList())
                .calculatedMetrics(new java.util.HashMap<>())
                .overallPassed(true)
                .summary("Test passed")
                .build();
    }

    @Test
    @DisplayName("Should get stock data for valid ticker")
    void shouldGetStockDataForValidTicker() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(strategyRepository, times(1)).findById(1L);
        verify(evaluateStrategyUseCase, times(1)).evaluateStrategy(any(), any());
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
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

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
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

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
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

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
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

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
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

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
        when(evaluateStrategyUseCase.evaluateStrategy(any(), any())).thenReturn(analysisResult);

        // Act
        service.getStockData("AAPL", 1L);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("AAPL");
        verify(companyProfileRepository, times(1)).save(validCompanyProfile);
    }
}
