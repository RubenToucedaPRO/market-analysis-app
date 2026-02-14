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

    @Test
    @DisplayName("Should identify prohibited profile with ETF keyword")
    void shouldIdentifyProhibitedProfileWithETFKeyword() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .build();

        // Assert
        assertTrue(profile.isProhibited());
        assertEquals("ETF", profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should identify prohibited profile with FUND keyword")
    void shouldIdentifyProhibitedProfileWithFUNDKeyword() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("VXUS")
                .name("Vanguard International Stock Index Fund")
                .build();

        // Assert
        assertTrue(profile.isProhibited());
        assertEquals("FUND", profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should identify prohibited profile with ACQUISITION keyword")
    void shouldIdentifyProhibitedProfileWithACQUISITIONKeyword() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("SPAC")
                .name("Company Acquisition Corp")
                .build();

        // Assert
        assertTrue(profile.isProhibited());
        assertEquals("ACQUISITION", profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should identify prohibited profile with THERAPEUTICS keyword")
    void shouldIdentifyProhibitedProfileWithTHERAPEUTICSKeyword() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("BIO")
                .name("BioTech Therapeutics Inc.")
                .build();

        // Assert
        assertTrue(profile.isProhibited());
        assertEquals("THERAPEUTICS", profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should identify prohibited profile with leveraged keyword (2X)")
    void shouldIdentifyProhibitedProfileWithLeveragedKeyword() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("UPRO")
                .name("ProShares UltraPro S&P500 3X")
                .build();

        // Assert
        assertTrue(profile.isProhibited());
        assertNotNull(profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should not identify regular company as prohibited")
    void shouldNotIdentifyRegularCompanyAsProhibited() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .build();

        // Assert
        assertFalse(profile.isProhibited());
        assertNull(profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should handle null name when checking prohibited status")
    void shouldHandleNullNameWhenCheckingProhibited() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("TEST")
                .name(null)
                .build();

        // Assert
        assertFalse(profile.isProhibited());
        assertNull(profile.getProhibitionReason());
    }

    @Test
    @DisplayName("Should match prohibited keywords case-insensitively")
    void shouldMatchProhibitedKeywordsCaseInsensitively() {
        // Arrange & Act
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("TEST")
                .name("test fund company")
                .build();

        // Assert
        assertTrue(profile.isProhibited());
        assertEquals("FUND", profile.getProhibitionReason());
    }
}
