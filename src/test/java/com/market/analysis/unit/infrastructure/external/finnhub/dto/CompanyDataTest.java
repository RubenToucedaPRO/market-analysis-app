package com.market.analysis.unit.infrastructure.external.finnhub.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.external.finnhub.dto.CompanyData;

/**
 * Unit tests for CompanyData DTO.
 */
@DisplayName("CompanyData DTO Tests")
class CompanyDataTest {

    @Test
    @DisplayName("Should create CompanyData with all fields using builder")
    void testCompanyDataBuilder() {
        // Arrange
        LocalDateTime lastUpdated = LocalDateTime.now();
        
        // Act
        CompanyData companyData = CompanyData.builder()
                .name("Apple Inc.")
                .country("US")
                .ticker("AAPL")
                .exchange("NASDAQ")
                .finnhubIndustry("Technology")
                .ipo("1980-12-12")
                .logo("https://example.com/logo.png")
                .marketCapitalization(2500000000000.0)
                .shareOutstanding(16000000000.0)
                .weburl("https://www.apple.com")
                .lastUpdated(lastUpdated)
                .build();
        
        // Assert
        assertThat(companyData).isNotNull();
        assertThat(companyData.getName()).isEqualTo("Apple Inc.");
        assertThat(companyData.getCountry()).isEqualTo("US");
        assertThat(companyData.getTicker()).isEqualTo("AAPL");
        assertThat(companyData.getExchange()).isEqualTo("NASDAQ");
        assertThat(companyData.getFinnhubIndustry()).isEqualTo("Technology");
        assertThat(companyData.getIpo()).isEqualTo("1980-12-12");
        assertThat(companyData.getLogo()).isEqualTo("https://example.com/logo.png");
        assertThat(companyData.getMarketCapitalization()).isEqualTo(2500000000000.0);
        assertThat(companyData.getShareOutstanding()).isEqualTo(16000000000.0);
        assertThat(companyData.getWeburl()).isEqualTo("https://www.apple.com");
        assertThat(companyData.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should validate company data with valid name")
    void testIsValidWithValidName() {
        // Arrange
        CompanyData companyData = CompanyData.builder()
                .name("Apple Inc.")
                .ticker("AAPL")
                .build();
        
        // Act
        boolean isValid = companyData.isValid();
        
        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate company data with null name")
    void testIsValidWithNullName() {
        // Arrange
        CompanyData companyData = CompanyData.builder()
                .ticker("AAPL")
                .build();
        
        // Act
        boolean isValid = companyData.isValid();
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate company data with empty name")
    void testIsValidWithEmptyName() {
        // Arrange
        CompanyData companyData = CompanyData.builder()
                .name("")
                .ticker("AAPL")
                .build();
        
        // Act
        boolean isValid = companyData.isValid();
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should check if data is outdated")
    void testIsOutdatedWithOldData() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.now().minusDays(31);
        CompanyData companyData = CompanyData.builder()
                .name("Apple Inc.")
                .lastUpdated(oldDate)
                .build();
        
        // Act
        boolean isOutdated = companyData.isOutdated();
        
        // Assert
        assertThat(isOutdated).isTrue();
    }

    @Test
    @DisplayName("Should check if data is not outdated")
    void testIsOutdatedWithRecentData() {
        // Arrange
        LocalDateTime recentDate = LocalDateTime.now().minusDays(15);
        CompanyData companyData = CompanyData.builder()
                .name("Apple Inc.")
                .lastUpdated(recentDate)
                .build();
        
        // Act
        boolean isOutdated = companyData.isOutdated();
        
        // Assert
        assertThat(isOutdated).isFalse();
    }

    @Test
    @DisplayName("Should consider data outdated when lastUpdated is null")
    void testIsOutdatedWithNullLastUpdated() {
        // Arrange
        CompanyData companyData = CompanyData.builder()
                .name("Apple Inc.")
                .build();
        
        // Act
        boolean isOutdated = companyData.isOutdated();
        
        // Assert
        assertThat(isOutdated).isTrue();
    }

    @Test
    @DisplayName("Should support no-args constructor")
    void testNoArgsConstructor() {
        // Act
        CompanyData companyData = new CompanyData();
        
        // Assert
        assertThat(companyData).isNotNull();
        assertThat(companyData.getName()).isNull();
    }

    @Test
    @DisplayName("Should support all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        LocalDateTime lastUpdated = LocalDateTime.now();
        
        // Act
        CompanyData companyData = new CompanyData(
                "Apple Inc.",
                "US",
                "AAPL",
                "NASDAQ",
                "Technology",
                "1980-12-12",
                "https://example.com/logo.png",
                2500000000000.0,
                16000000000.0,
                "https://www.apple.com",
                lastUpdated
        );
        
        // Assert
        assertThat(companyData).isNotNull();
        assertThat(companyData.getName()).isEqualTo("Apple Inc.");
        assertThat(companyData.getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
        // Arrange
        CompanyData companyData = new CompanyData();
        
        // Act
        companyData.setName("Microsoft");
        companyData.setTicker("MSFT");
        companyData.setCountry("US");
        
        // Assert
        assertThat(companyData.getName()).isEqualTo("Microsoft");
        assertThat(companyData.getTicker()).isEqualTo("MSFT");
        assertThat(companyData.getCountry()).isEqualTo("US");
    }
}
