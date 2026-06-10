package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.CompanyProfile;

@DisplayName("CompanyProfile Domain Model Tests")
class CompanyProfileTest {

    @Test
    @DisplayName("Should create valid CompanyProfile")
    void shouldCreateValidCompanyProfile() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .country("US")
                .exchange("NASDAQ")
                .industry("Technology")
                .build();

        // Assert
        assertNotNull(profile);
        assertTrue(profile.isValid());
        assertEquals("AAPL", profile.getTicker());
        assertEquals("Apple Inc.", profile.getName());
    }

    @Test
    @DisplayName("Should identify invalid profile when name is null")
    void shouldIdentifyInvalidProfileWhenNameIsNull() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("TEST")
                .name(null)
                .build();

        // Assert
        assertFalse(profile.isValid());
    }

    @Test
    @DisplayName("Should identify invalid profile when name is empty")
    void shouldIdentifyInvalidProfileWhenNameIsEmpty() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("TEST")
                .name("")
                .build();

        // Assert
        assertFalse(profile.isValid());
    }

    @Test
    @DisplayName("Should identify outdated profile when lastUpdated is null")
    void shouldIdentifyOutdatedProfileWhenLastUpdatedIsNull() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(null)
                .build();

        // Assert
        assertTrue(profile.isOutdated());
    }

    @Test
    @DisplayName("Should identify outdated profile when lastUpdated is older than 30 days")
    void shouldIdentifyOutdatedProfileWhenOlderThan30Days() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(Instant.now().minus(31, java.time.temporal.ChronoUnit.DAYS))
                .build();

        // Assert
        assertTrue(profile.isOutdated());
    }

    @Test
    @DisplayName("Should identify fresh profile when lastUpdated is within 30 days")
    void shouldIdentifyFreshProfileWhenWithin30Days() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(Instant.now().minus(15, java.time.temporal.ChronoUnit.DAYS))
                .build();

        // Assert
        assertFalse(profile.isOutdated());
    }

}
