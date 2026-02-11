package com.market.analysis.unit.presentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.presentation.dto.CompanyProfileDto;
import com.market.analysis.presentation.mapper.CompanyProfileDTOMapper;

/**
 * Unit tests for CompanyProfileDTOMapper.
 */
@DisplayName("CompanyProfileDTOMapper Unit Tests")
class CompanyProfileDTOMapperTest {

    private CompanyProfileDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CompanyProfileDTOMapper();
    }

    @Test
    @DisplayName("Should map CompanyProfile domain to CompanyProfileDto")
    void testToDTO() {
        // Arrange
        CompanyProfile companyProfile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .industry("Technology")
                .website("https://www.apple.com")
                .logo("https://example.com/logo.png")
                .build();

        // Act
        CompanyProfileDto dto = mapper.toDTO(companyProfile);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("AAPL");
        assertThat(dto.getName()).isEqualTo("Apple Inc.");
        assertThat(dto.getIndustry()).isEqualTo("Technology");
        assertThat(dto.getWebsite()).isEqualTo("https://www.apple.com");
        assertThat(dto.getLogo()).isEqualTo("https://example.com/logo.png");
    }

    @Test
    @DisplayName("Should return null when mapping null CompanyProfile")
    void testToDTOWithNull() {
        // Act
        CompanyProfileDto dto = mapper.toDTO(null);

        // Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("Should correctly map company profile with minimal fields")
    void testToDTOWithMinimalFields() {
        // Arrange
        CompanyProfile companyProfile = CompanyProfile.builder()
                .ticker("GOOGL")
                .name("Alphabet Inc.")
                .build();

        // Act
        CompanyProfileDto dto = mapper.toDTO(companyProfile);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("GOOGL");
        assertThat(dto.getName()).isEqualTo("Alphabet Inc.");
        assertThat(dto.getIndustry()).isNull();
        assertThat(dto.getWebsite()).isNull();
        assertThat(dto.getLogo()).isNull();
    }

    @Test
    @DisplayName("Should correctly map all fields from domain to DTO")
    void testToDTOWithAllFields() {
        // Arrange
        CompanyProfile companyProfile = CompanyProfile.builder()
                .ticker("MSFT")
                .name("Microsoft Corporation")
                .industry("Software")
                .website("https://www.microsoft.com")
                .logo("https://example.com/msft-logo.png")
                .country("US")
                .exchange("NASDAQ")
                .build();

        // Act
        CompanyProfileDto dto = mapper.toDTO(companyProfile);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("MSFT");
        assertThat(dto.getName()).isEqualTo("Microsoft Corporation");
        assertThat(dto.getIndustry()).isEqualTo("Software");
        assertThat(dto.getWebsite()).isEqualTo("https://www.microsoft.com");
        assertThat(dto.getLogo()).isEqualTo("https://example.com/msft-logo.png");
    }
}
