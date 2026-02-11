package com.market.analysis.unit.infrastructure.external.finnhub.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;

/**
 * Unit tests for QuoteData DTO.
 */
@DisplayName("QuoteData DTO Tests")
class QuoteDataTest {

    @Test
    @DisplayName("Should create QuoteData with all fields using builder")
    void testQuoteDataBuilder() {
        // Act
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .c(new BigDecimal("150.50"))
                .d(2.50)
                .dp(new BigDecimal("1.69"))
                .h(new BigDecimal("151.00"))
                .l(new BigDecimal("148.50"))
                .o(new BigDecimal("149.00"))
                .pc(new BigDecimal("148.00"))
                .t(1234567890L)
                .build();
        
        // Assert
        assertThat(quoteData).isNotNull();
        assertThat(quoteData.getSymbol()).isEqualTo("AAPL");
        assertThat(quoteData.getC()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(quoteData.getD()).isEqualTo(2.50);
        assertThat(quoteData.getDp()).isEqualByComparingTo(new BigDecimal("1.69"));
        assertThat(quoteData.getH()).isEqualByComparingTo(new BigDecimal("151.00"));
        assertThat(quoteData.getL()).isEqualByComparingTo(new BigDecimal("148.50"));
        assertThat(quoteData.getO()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(quoteData.getPc()).isEqualByComparingTo(new BigDecimal("148.00"));
        assertThat(quoteData.getT()).isEqualTo(1234567890L);
    }

    @Test
    @DisplayName("Should validate quote data with valid current price")
    void testIsValidWithValidPrice() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .c(new BigDecimal("150.50"))
                .build();
        
        // Act
        boolean isValid = quoteData.isValid();
        
        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate quote data with null current price")
    void testIsValidWithNullPrice() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .build();
        
        // Act
        boolean isValid = quoteData.isValid();
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate quote data with zero current price")
    void testIsValidWithZeroPrice() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .c(BigDecimal.ZERO)
                .build();
        
        // Act
        boolean isValid = quoteData.isValid();
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate quote data with negative current price")
    void testIsValidWithNegativePrice() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .symbol("AAPL")
                .c(new BigDecimal("-10.00"))
                .build();
        
        // Act
        boolean isValid = quoteData.isValid();
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should support no-args constructor")
    void testNoArgsConstructor() {
        // Act
        QuoteData quoteData = new QuoteData();
        
        // Assert
        assertThat(quoteData).isNotNull();
        assertThat(quoteData.getSymbol()).isNull();
        assertThat(quoteData.getC()).isNull();
    }

    @Test
    @DisplayName("Should support all-args constructor")
    void testAllArgsConstructor() {
        // Act
        QuoteData quoteData = new QuoteData(
                "AAPL",
                new BigDecimal("150.50"),
                2.50,
                new BigDecimal("1.69"),
                new BigDecimal("151.00"),
                new BigDecimal("148.50"),
                new BigDecimal("149.00"),
                new BigDecimal("148.00"),
                1234567890L
        );
        
        // Assert
        assertThat(quoteData).isNotNull();
        assertThat(quoteData.getSymbol()).isEqualTo("AAPL");
        assertThat(quoteData.getC()).isEqualByComparingTo(new BigDecimal("150.50"));
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
        // Arrange
        QuoteData quoteData = new QuoteData();
        
        // Act
        quoteData.setSymbol("GOOGL");
        quoteData.setC(new BigDecimal("100.00"));
        quoteData.setH(new BigDecimal("102.00"));
        
        // Assert
        assertThat(quoteData.getSymbol()).isEqualTo("GOOGL");
        assertThat(quoteData.getC()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(quoteData.getH()).isEqualByComparingTo(new BigDecimal("102.00"));
    }

    @Test
    @DisplayName("Should handle minimal valid quote data")
    void testMinimalValidQuoteData() {
        // Arrange
        QuoteData quoteData = QuoteData.builder()
                .c(new BigDecimal("0.01"))
                .build();
        
        // Act
        boolean isValid = quoteData.isValid();
        
        // Assert
        assertThat(isValid).isTrue();
    }
}
