package com.market.analysis.unit.infrastructure.external.finnhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.external.finnhub.FinnhubMapper;
import com.market.analysis.infrastructure.external.finnhub.dto.CompanyData;
import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;

@DisplayName("FinnhubMapper Tests")
class FinnhubMapperTest {

    private FinnhubMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FinnhubMapper();
    }

    @Test
    @DisplayName("Should map QuoteData to Stock domain model")
    void shouldMapQuoteDataToStockDomainModel() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .c(BigDecimal.valueOf(150.50))
                .h(BigDecimal.valueOf(152.00))
                .l(BigDecimal.valueOf(149.00))
                .o(BigDecimal.valueOf(150.00))
                .pc(BigDecimal.valueOf(149.50))
                .t(1704110400L) // 2024-01-01 12:00:00 UTC
                .build();

        // Act
        Stock stock = mapper.toDomain(quoteData);

        // Assert
        assertNotNull(stock);
        assertEquals("AAPL", stock.getTicker());
        assertEquals(BigDecimal.valueOf(150.50), stock.getCurrentPrice());
        assertEquals(BigDecimal.valueOf(152.00), stock.getHighOfDay());
        assertEquals(BigDecimal.valueOf(149.00), stock.getLowOfDay());
        assertEquals(BigDecimal.valueOf(150.00), stock.getOpenPrice());
        assertEquals(BigDecimal.valueOf(149.50), stock.getPreviousClose());
        assertNotNull(stock.getLastUpdated());
    }

    @Test
    @DisplayName("Should return null when QuoteData is null")
    void shouldReturnNullWhenQuoteDataIsNull() {
        // Act
        Stock stock = mapper.toDomain((QuoteData) null);

        // Assert
        assertNull(stock);
    }

    @Test
    @DisplayName("Should return null when QuoteData is invalid")
    void shouldReturnNullWhenQuoteDataIsInvalid() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .c(BigDecimal.ZERO) // Invalid price
                .build();

        // Act
        Stock stock = mapper.toDomain(quoteData);

        // Assert
        assertNull(stock);
    }

    @Test
    @DisplayName("Should map CompanyData to CompanyProfile domain model")
    void shouldMapCompanyDataToCompanyProfileDomainModel() {
        // Arrange
        Instant lastUpdated = Instant.now();
        CompanyData companyData = CompanyData.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .country("US")
                .exchange("NASDAQ")
                .finnhubIndustry("Technology")
                .ipo("1980-12-12")
                .logo("https://static.finnhub.io/logo/87cb30d8-80df-11ea-8951-00000000092a.png")
                .marketCapitalization(2500000.0)
                .shareOutstanding(16000.0)
                .weburl("https://www.apple.com")
                .lastUpdated(lastUpdated)
                .build();

        // Act
        CompanyProfile profile = mapper.toDomain(companyData);

        // Assert
        assertNotNull(profile);
        assertEquals("AAPL", profile.getTicker());
        assertEquals("Apple Inc.", profile.getName());
        assertEquals("US", profile.getCountry());
        assertEquals("NASDAQ", profile.getExchange());
        assertEquals("Technology", profile.getIndustry());
        assertEquals("1980-12-12", profile.getIpo());
        assertEquals("https://static.finnhub.io/logo/87cb30d8-80df-11ea-8951-00000000092a.png", profile.getLogo());
        assertEquals(2500000.0, profile.getMarketCapitalization());
        assertEquals(16000.0, profile.getShareOutstanding());
        assertEquals("https://www.apple.com", profile.getWebsite());
        assertEquals(lastUpdated, profile.getLastUpdated());
    }

    @Test
    @DisplayName("Should return null when CompanyData is null")
    void shouldReturnNullWhenCompanyDataIsNull() {
        // Act
        CompanyProfile profile = mapper.toDomain((CompanyData) null);

        // Assert
        assertNull(profile);
    }

    @Test
    @DisplayName("Should return null when CompanyData is invalid")
    void shouldReturnNullWhenCompanyDataIsInvalid() {
        // Arrange
        CompanyData companyData = CompanyData.builder()
                .ticker("AAPL")
                .name("") // Invalid empty name
                .build();

        // Act
        CompanyProfile profile = mapper.toDomain(companyData);

        // Assert
        assertNull(profile);
    }

    @Test
    @DisplayName("Should handle QuoteData with minimal fields")
    void shouldHandleQuoteDataWithMinimalFields() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("TSLA")
                .c(BigDecimal.valueOf(700.00))
                .t(1704110400L)
                .build();

        // Act
        Stock stock = mapper.toDomain(quoteData);

        // Assert
        assertNotNull(stock);
        assertEquals("TSLA", stock.getTicker());
        assertEquals(BigDecimal.valueOf(700.00), stock.getCurrentPrice());
        assertNull(stock.getHighOfDay());
        assertNull(stock.getLowOfDay());
        assertNull(stock.getOpenPrice());
        assertNull(stock.getPreviousClose());
    }

    @Test
    @DisplayName("Should handle CompanyData with minimal fields")
    void shouldHandleCompanyDataWithMinimalFields() {
        // Arrange
        Instant lastUpdated = Instant.now();
        CompanyData companyData = CompanyData.builder()
                .ticker("GOOGL")
                .name("Alphabet Inc.")
                .lastUpdated(lastUpdated)
                .build();

        // Act
        CompanyProfile profile = mapper.toDomain(companyData);

        // Assert
        assertNotNull(profile);
        assertEquals("GOOGL", profile.getTicker());
        assertEquals("Alphabet Inc.", profile.getName());
        assertEquals(lastUpdated, profile.getLastUpdated());
        assertNull(profile.getCountry());
        assertNull(profile.getExchange());
    }
}
