package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.CompanyProfileMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaCompanyProfileRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlCompanyProfileRepository;

/**
 * Unit tests for SqlCompanyProfileRepository.
 */
@DisplayName("SqlCompanyProfileRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlCompanyProfileRepositoryTest {

    @Mock
    private JpaCompanyProfileRepository jpaRepository;

    @Mock
    private CompanyProfileMapper mapper;

    @InjectMocks
    private SqlCompanyProfileRepository sqlRepository;

    private CompanyProfile testProfile;
    private CompanyProfileEntity testEntity;

    @BeforeEach
    void setUp() {
        Instant lastUpdated = Instant.now();

        testProfile = CompanyProfile.builder()
                .name("Apple Inc.")
                .ticker("AAPL")
                .exchange("NASDAQ")
                .industry("Technology")
                .logo("https://example.com/logo.png")
                .lastUpdated(lastUpdated)
                .build();

        testEntity = CompanyProfileEntity.builder()
                .id(1L)
                .name("Apple Inc.")
                .ticker("AAPL")
                .exchange("NASDAQ")
                .industry("Technology")
                .logo("https://example.com/logo.png")
                .lastUpdated(lastUpdated)
                .build();
    }

    @Test
    @DisplayName("Should save new company profile")
    void testSaveNewProfile() {
        // Arrange
        when(jpaRepository.findByTicker("AAPL")).thenReturn(Optional.empty());
        when(mapper.toEntity(testProfile)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);

        // Act
        sqlRepository.save(testProfile);

        // Assert
        verify(jpaRepository, times(1)).findByTicker("AAPL");
        verify(mapper, times(1)).toEntity(testProfile);
        verify(jpaRepository, times(1)).save(testEntity);
    }

    @Test
    @DisplayName("Should update existing company profile")
    void testSaveExistingProfile() {
        // Arrange
        CompanyProfileEntity existingEntity = CompanyProfileEntity.builder()
                .id(2L)
                .ticker("AAPL")
                .build();

        when(jpaRepository.findByTicker("AAPL")).thenReturn(Optional.of(existingEntity));
        when(mapper.toEntity(testProfile)).thenReturn(testEntity);
        when(jpaRepository.save(any(CompanyProfileEntity.class))).thenReturn(testEntity);

        // Act
        sqlRepository.save(testProfile);

        // Assert
        verify(jpaRepository, times(1)).findByTicker("AAPL");
        verify(mapper, times(1)).toEntity(testProfile);
        verify(jpaRepository, times(1)).save(any(CompanyProfileEntity.class));
    }

    @Test
    @DisplayName("Should find company profile by ticker")
    void testFindByTicker() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(java.util.Arrays.asList(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testProfile);

        // Act
        Optional<CompanyProfile> result = sqlRepository.findByTicker("AAPL");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("AAPL");
        assertThat(result.get().getName()).isEqualTo("Apple Inc.");
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty when company profile not found")
    void testFindByTickerNotFound() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(java.util.Arrays.asList());

        // Act
        Optional<CompanyProfile> result = sqlRepository.findByTicker("NOTFOUND");

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find company profile by ticker case insensitive")
    void testFindByTickerCaseInsensitive() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(java.util.Arrays.asList(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testProfile);

        // Act
        Optional<CompanyProfile> result = sqlRepository.findByTicker("aapl");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should update company profile")
    void testUpdate() {
        // Arrange
        when(mapper.toEntity(testProfile)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);

        // Act
        sqlRepository.update(testProfile);

        // Assert
        verify(mapper, times(1)).toEntity(testProfile);
        verify(jpaRepository, times(1)).save(testEntity);
    }

    @Test
    @DisplayName("Should delete company profile by ticker")
    void testDeleteByTicker() {
        // Act
        sqlRepository.deleteByTicker("AAPL");

        // Assert
        verify(jpaRepository, times(1)).deleteByTicker("AAPL");
    }
}
