package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @InjectMocks
    private ManageAnalyzeStockService service;

    private Stock stock;
    private CompanyProfile validCompanyProfile;
    private CompanyProfile prohibitedCompanyProfile;

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
                .lastUpdated(LocalDateTime.now())
                .build();

        prohibitedCompanyProfile = CompanyProfile.builder()
                .ticker("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .country("US")
                .exchange("NYSE")
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get stock data for valid ticker")
    void shouldGetStockDataForValidTicker() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.getStockData("AAPL", null);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).saveStockData(stock);
    }

    @Test
    @DisplayName("Should get stock data for multiple tickers")
    void shouldGetStockDataForMultipleTickers() {
        // Arrange
        Stock stock2 = Stock.builder().ticker("GOOGL").currentPrice(BigDecimal.valueOf(2800.00)).build();
        CompanyProfile profile2 = CompanyProfile.builder()
                .ticker("GOOGL")
                .name("Alphabet Inc.")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(companyProfileRepository.findByTicker("GOOGL")).thenReturn(Optional.of(profile2));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(stockProviderPort.getQuote("GOOGL")).thenReturn(stock2);

        // Act
        service.getStockData("AAPL,GOOGL", null);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockProviderPort, times(1)).getQuote("GOOGL");
        verify(stockDataRepository, times(1)).saveStockData(stock);
        verify(stockDataRepository, times(1)).saveStockData(stock2);
    }

    @Test
    @DisplayName("Should parse and normalize tickers")
    void shouldParseAndNormalizeTickers() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(companyProfileRepository.findByTicker("GOOGL")).thenReturn(Optional.of(validCompanyProfile));
        when(companyProfileRepository.findByTicker("TSLA")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote(anyString())).thenReturn(stock);

        // Act
        service.getStockData("  aapl  ,  googl  ,  TSLA  ", null);

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

        // Act
        service.getStockData("AAPL,,  ,", null);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).saveStockData(stock);
    }

    @Test
    @DisplayName("Should update company profile when it does not exist")
    void shouldUpdateCompanyProfileWhenItDoesNotExist() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.empty());
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("AAPL")).thenReturn(validCompanyProfile);
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.getStockData("AAPL", null);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("AAPL");
        verify(companyProfileRepository, times(1)).save(validCompanyProfile);
        verify(stockProviderPort, times(1)).getQuote("AAPL");
    }

    @Test
    @DisplayName("Should update company profile when it is outdated")
    void shouldUpdateCompanyProfileWhenItIsOutdated() {
        // Arrange
        CompanyProfile outdatedProfile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(LocalDateTime.now().minusDays(31))
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(outdatedProfile));
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(false);
        when(stockProviderPort.getCompanyProfile("AAPL")).thenReturn(validCompanyProfile);
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.getStockData("AAPL", null);

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

        // Act
        service.getStockData("SPY", null);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("SPY");
        verify(prohibitedTickerRepository, times(1)).save(any(ProhibitedTicker.class));
        verify(stockProviderPort, never()).getQuote("SPY");
        verify(stockDataRepository, never()).saveStockData(any());
    }

    @Test
    @DisplayName("Should skip ticker when it is already marked as prohibited")
    void shouldSkipTickerWhenItIsAlreadyMarkedAsProhibited() {
        // Arrange
        when(companyProfileRepository.findByTicker("SPY")).thenReturn(Optional.empty());
        when(prohibitedTickerRepository.existsByTicker("SPY")).thenReturn(true);

        // Act
        service.getStockData("SPY", null);

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

        // Act
        service.getStockData("INVALID", null);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("INVALID");
        verify(stockProviderPort, never()).getQuote("INVALID");
        verify(stockDataRepository, never()).saveStockData(any());
    }

    @Test
    @DisplayName("Should not save stock data when quote is null")
    void shouldNotSaveStockDataWhenQuoteIsNull() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);

        // Act
        service.getStockData("AAPL", null);

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, never()).saveStockData(any());
    }

    @Test
    @DisplayName("Should find all stocks")
    void shouldFindAllStocks() {
        // Arrange
        Stock stock2 = Stock.builder().ticker("GOOGL").currentPrice(BigDecimal.valueOf(2800.00)).build();
        List<Stock> stocks = Arrays.asList(stock, stock2);
        when(stockDataRepository.findAllStocks()).thenReturn(stocks);

        // Act
        List<Stock> result = service.findAllStocks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stockDataRepository, times(1)).findAllStocks();
    }

    @Test
    @DisplayName("Should find stock data by ticker")
    void shouldFindStockDataByTicker() {
        // Arrange
        when(stockDataRepository.findByTicker("AAPL")).thenReturn(Optional.of(stock));

        // Act
        Stock result = service.findStockDataByTicker("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        verify(stockDataRepository, times(1)).findByTicker("AAPL");
    }

    @Test
    @DisplayName("Should throw StockDataNotFoundException when ticker not found")
    void shouldThrowStockDataNotFoundExceptionWhenTickerNotFound() {
        // Arrange
        when(stockDataRepository.findByTicker("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockDataNotFoundException.class, () -> {
            service.findStockDataByTicker("INVALID");
        });
        verify(stockDataRepository, times(1)).findByTicker("INVALID");
    }

    @Test
    @DisplayName("Should update stock data for existing ticker")
    void shouldUpdateStockDataForExistingTicker() {
        // Arrange
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.updateStockData("AAPL");

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).updateStockData(stock);
    }

    @Test
    @DisplayName("Should not update stock data when quote is null")
    void shouldNotUpdateStockDataWhenQuoteIsNull() {
        // Arrange
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);

        // Act
        service.updateStockData("AAPL");

        // Assert
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, never()).updateStockData(any());
    }

    @Test
    @DisplayName("Should delete stock data by ticker")
    void shouldDeleteStockDataByTicker() {
        // Act
        service.deleteStockDataByTicker("AAPL");

        // Assert
        verify(stockDataRepository, times(1)).deleteByTicker("AAPL");
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

        // Act
        service.getStockData("AAPL", null);

        // Assert
        verify(stockProviderPort, times(1)).getCompanyProfile("AAPL");
        verify(companyProfileRepository, times(1)).save(validCompanyProfile);
    }

    @Test
    @DisplayName("Should apply validation rule when rule ID is provided")
    void shouldApplyValidationRuleWhenRuleIdIsProvided() {
        // Arrange
        Stock stockWithLogo = Stock.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(BigDecimal.valueOf(150.00))
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stockWithLogo);

        // Act
        service.getStockData("AAPL", "LOGO_PRESENT");

        // Assert
        verify(stockDataRepository, times(1)).saveStockData(argThat(stock -> 
            stock.getAppliedRuleId() != null &&
            stock.getAppliedRuleId().equals("LOGO_PRESENT") &&
            stock.getRuleValidationResult() != null &&
            stock.getRuleValidationResult() == true
        ));
    }

    @Test
    @DisplayName("Should not apply rule when rule ID is null")
    void shouldNotApplyRuleWhenRuleIdIsNull() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.getStockData("AAPL", null);

        // Assert
        verify(stockDataRepository, times(1)).saveStockData(argThat(s -> 
            s.getAppliedRuleId() == null &&
            s.getRuleValidationResult() == null
        ));
    }

    @Test
    @DisplayName("Should not apply rule when rule ID is empty")
    void shouldNotApplyRuleWhenRuleIdIsEmpty() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.getStockData("AAPL", "");

        // Assert
        verify(stockDataRepository, times(1)).saveStockData(argThat(s -> 
            s.getAppliedRuleId() == null &&
            s.getRuleValidationResult() == null
        ));
    }

    @Test
    @DisplayName("Should handle invalid rule ID gracefully")
    void shouldHandleInvalidRuleIdGracefully() {
        // Arrange
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);

        // Act
        service.getStockData("AAPL", "INVALID_RULE");

        // Assert - Should still save the stock, just without rule validation
        verify(stockDataRepository, times(1)).saveStockData(stock);
    }

    @Test
    @DisplayName("Should evaluate PriceAboveSma200 rule correctly")
    void shouldEvaluatePriceAboveSma200RuleCorrectly() {
        // Arrange
        Stock stockWithSma = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(200.00))
                .sma200(BigDecimal.valueOf(150.00))
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stockWithSma);

        // Act
        service.getStockData("AAPL", "PRICE_ABOVE_SMA200");

        // Assert
        verify(stockDataRepository, times(1)).saveStockData(argThat(stock -> 
            stock.getAppliedRuleId().equals("PRICE_ABOVE_SMA200") &&
            stock.getRuleValidationResult() == true
        ));
    }

    @Test
    @DisplayName("Should evaluate VolumeAboveAverage rule correctly")
    void shouldEvaluateVolumeAboveAverageRuleCorrectly() {
        // Arrange
        Stock stockWithVolume = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(2000000L)
                .averageVolume(1000000L)
                .build();

        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(validCompanyProfile));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stockWithVolume);

        // Act
        service.getStockData("AAPL", "VOLUME_ABOVE_AVERAGE");

        // Assert
        verify(stockDataRepository, times(1)).saveStockData(argThat(stock -> 
            stock.getAppliedRuleId().equals("VOLUME_ABOVE_AVERAGE") &&
            stock.getRuleValidationResult() == true
        ));
    }
}
