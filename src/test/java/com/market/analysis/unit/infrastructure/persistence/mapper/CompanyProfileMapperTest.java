package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.CompanyProfileMapper;

/**
 * Unit tests for CompanyProfileMapper.
 */
@DisplayName("CompanyProfileMapper Unit Tests")
class CompanyProfileMapperTest {

    private CompanyProfileMapper companyProfileMapper;

    @BeforeEach
    void setUp() {
        companyProfileMapper = new CompanyProfileMapper();
    }

    @Test
    @DisplayName("Should map CompanyProfile domain to CompanyProfileEntity")
    void testToEntity() {
        // Arrange
        Instant lastUpdated = Instant.now();
        CompanyProfile profile = CompanyProfile.builder()
                .name("Apple Inc.")
                .country("US")
                .ticker("AAPL")
                .exchange("NASDAQ")
                .industry("Technology")
                .ipo("1980-12-12")
                .logo("https://example.com/logo.png")
                .marketCapitalization(2500000000000.0)
                .shareOutstanding(16000000000.0)
                .website("https://www.apple.com")
                .lastUpdated(lastUpdated)
                .build();

        // Act
        CompanyProfileEntity entity = companyProfileMapper.toEntity(profile);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Apple Inc.");
        assertThat(entity.getCountry()).isEqualTo("US");
        assertThat(entity.getTicker()).isEqualTo("AAPL");
        assertThat(entity.getExchange()).isEqualTo("NASDAQ");
        assertThat(entity.getIndustry()).isEqualTo("Technology");
        assertThat(entity.getIpo()).isEqualTo("1980-12-12");
        assertThat(entity.getLogo()).isEqualTo("https://example.com/logo.png");
        assertThat(entity.getMarketCapitalization()).isEqualTo(2500000000000.0);
        assertThat(entity.getShareOutstanding()).isEqualTo(16000000000.0);
        assertThat(entity.getWebsite()).isEqualTo("https://www.apple.com");
        assertThat(entity.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should map CompanyProfileEntity to CompanyProfile domain")
    void testToDomain() {
        // Arrange
        Instant lastUpdated = Instant.now();
        CompanyProfileEntity entity = CompanyProfileEntity.builder()
                .id(1L)
                .name("Apple Inc.")
                .country("US")
                .ticker("AAPL")
                .exchange("NASDAQ")
                .industry("Technology")
                .ipo("1980-12-12")
                .logo("https://example.com/logo.png")
                .marketCapitalization(2500000000000.0)
                .shareOutstanding(16000000000.0)
                .website("https://www.apple.com")
                .lastUpdated(lastUpdated)
                .build();

        // Act
        CompanyProfile profile = companyProfileMapper.toDomain(entity);

        // Assert
        assertThat(profile).isNotNull();
        assertThat(profile.getName()).isEqualTo("Apple Inc.");
        assertThat(profile.getTicker()).isEqualTo("AAPL");
        assertThat(profile.getExchange()).isEqualTo("NASDAQ");
        assertThat(profile.getIndustry()).isEqualTo("Technology");
        assertThat(profile.getIpo()).isEqualTo("1980-12-12");
        assertThat(profile.getLogo()).isEqualTo("https://example.com/logo.png");
        assertThat(profile.getMarketCapitalization()).isEqualTo(2500000000000.0);
        assertThat(profile.getShareOutstanding()).isEqualTo(16000000000.0);
        assertThat(profile.getWebsite()).isEqualTo("https://www.apple.com");
        assertThat(profile.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should return null when mapping null CompanyProfile to entity")
    void testToEntityWithNull() {
        // Act
        CompanyProfileEntity entity = companyProfileMapper.toEntity(null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null CompanyProfileEntity to domain")
    void testToDomainWithNull() {
        // Act
        CompanyProfile profile = companyProfileMapper.toDomain(null);

        // Assert
        assertThat(profile).isNull();
    }

    @Test
    @DisplayName("Should correctly map company profile with minimal fields")
    void testToEntityWithMinimalFields() {
        // Arrange
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("GOOGL")
                .name("Alphabet Inc.")
                .build();

        // Act
        CompanyProfileEntity entity = companyProfileMapper.toEntity(profile);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo("GOOGL");
        assertThat(entity.getName()).isEqualTo("Alphabet Inc.");
        assertThat(entity.getCountry()).isNull();
        assertThat(entity.getExchange()).isNull();
    }
}
