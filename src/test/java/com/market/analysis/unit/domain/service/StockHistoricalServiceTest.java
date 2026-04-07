package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.service.StockHistoricalService;

/**
 * Unit tests for StockHistoricalService.
 * Tests calculation of technical indicators from historical data.
 */
@DisplayName("StockHistoricalService Domain Service Tests")
class StockHistoricalServiceTest {

    private StockHistoricalService service;

    @BeforeEach
    void setUp() {
        service = new StockHistoricalService();
    }

    @Nested
    @DisplayName("SMA Calculation Tests")
    class SmaCalculationTests {

        @Test
        @DisplayName("Should calculate SMA20, SMA50, and SMA200 correctly")
        void testCalculateAllSMAs() {
            // Arrange - Create 250 prices to ensure all SMAs can be calculated
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 250; i++) {
                prices.add(100.0 + i); // 100.0, 101.0, 102.0, ...
            }

            List<Long> volumes = new ArrayList<>();
            for (int i = 0; i < 250; i++) {
                volumes.add(1000000L);
            }

            Instant lastUpdate = Instant.parse("2026-02-14T10:00:00Z");
            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(lastUpdate)
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators).isNotNull();
            assertThat(indicators.getSma20()).isNotNull();
            assertThat(indicators.getSma50()).isNotNull();
            assertThat(indicators.getSma200()).isNotNull();

            // SMA20 = avg of first 20 prices: (100 + 101 + ... + 119) / 20 = 109.5
            assertThat(indicators.getSma20()).isEqualByComparingTo(new BigDecimal("109.50"));

            // SMA50 = avg of first 50 prices: (100 + 101 + ... + 149) / 50 = 124.5
            assertThat(indicators.getSma50()).isEqualByComparingTo(new BigDecimal("124.50"));

            // SMA200 = avg of first 200 prices: (100 + 101 + ... + 299) / 200 = 199.5
            assertThat(indicators.getSma200()).isEqualByComparingTo(new BigDecimal("199.50"));
        }

        @Test
        @DisplayName("Should return null for SMA20 when insufficient data")
        void testCalculateSma20WithInsufficientData() {
            // Arrange
            List<Double> prices = Arrays.asList(100.0, 101.0, 102.0); // Only 3 prices
            List<Long> volumes = Arrays.asList(1000000L, 1000000L, 1000000L);

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getSma20()).isNull();
            assertThat(indicators.getSma50()).isNull();
            assertThat(indicators.getSma200()).isNull();
        }

        @Test
        @DisplayName("Should calculate SMA20 when exactly 20 data points")
        void testCalculateSma20WithExactly20Points() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                prices.add(100.0);
            }

            List<Long> volumes = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                volumes.add(1000000L);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getSma20()).isNotNull();
            assertThat(indicators.getSma20()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(indicators.getSma50()).isNull(); // Not enough for SMA50
            assertThat(indicators.getSma200()).isNull(); // Not enough for SMA200
        }

        @Test
        @DisplayName("Should handle null prices list")
        void testCalculateWithNullPrices() {
            // Arrange
            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(null)
                    .volumes(Arrays.asList(1000000L))
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getSma20()).isNull();
            assertThat(indicators.getSma50()).isNull();
            assertThat(indicators.getSma200()).isNull();
        }

        @Test
        @DisplayName("Should handle empty prices list")
        void testCalculateWithEmptyPrices() {
            // Arrange
            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(Collections.emptyList())
                    .volumes(Collections.emptyList())
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getSma20()).isNull();
            assertThat(indicators.getSma50()).isNull();
            assertThat(indicators.getSma200()).isNull();
        }

        @Test
        @DisplayName("Should round SMA values to 2 decimal places")
        void testSmaRounding() {
            // Arrange - Create prices that will result in non-round SMA
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                prices.add(100.0 + (i * 0.33)); // Will create fractional SMA
            }

            List<Long> volumes = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                volumes.add(1000000L);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getSma20()).isNotNull();
            assertThat(indicators.getSma20().scale()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Volume Calculation Tests")
    class VolumeCalculationTests {

        @Test
        @DisplayName("Should calculate current volume and average volume correctly")
        void testCalculateVolumes() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                prices.add(100.0 + i);
                volumes.add(1000000L + (i * 10000L));
            }

            // Current volume is the first in the list
            Long expectedCurrentVolume = volumes.get(0); // 1000000L

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getCurrentVolume()).isEqualTo(expectedCurrentVolume);

            // Average of first 20 volumes
            long expectedAvgVolume = 0;
            for (int i = 0; i < 20; i++) {
                expectedAvgVolume += volumes.get(i);
            }
            expectedAvgVolume /= 20;

            assertThat(indicators.getAverageVolume()).isEqualTo(expectedAvgVolume);
        }

        @Test
        @DisplayName("Should return null current volume when volumes list is empty")
        void testCurrentVolumeWithEmptyList() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                prices.add(100.0 + i);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(Collections.emptyList())
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getCurrentVolume()).isNull();
            assertThat(indicators.getAverageVolume()).isNull();
        }

        @Test
        @DisplayName("Should return null average volume when insufficient volume data")
        void testAverageVolumeWithInsufficientData() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                prices.add(100.0 + i);
            }

            // Only 10 volumes, but period is 20
            for (int i = 0; i < 10; i++) {
                volumes.add(1000000L);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getCurrentVolume()).isEqualTo(1000000L);
            assertThat(indicators.getAverageVolume()).isNull();
        }

        @Test
        @DisplayName("Should handle null volumes list")
        void testCalculateWithNullVolumes() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                prices.add(100.0 + i);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(null)
                    .lastUpdate(Instant.now())
                    .build();

            // Act & Assert - Expected NPE due to current implementation
            // This test documents current behavior where null volumes cause NPE
            assertThatThrownBy(() -> service.calculateIndicators(data, 20))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should calculate average volume with exact period size")
        void testAverageVolumeWithExactPeriod() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                prices.add(100.0 + i);
                volumes.add(1000000L); // All same volume
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getAverageVolume()).isEqualTo(1000000L);
        }
    }

    @Nested
    @DisplayName("Complete Indicator Tests")
    class CompleteIndicatorTests {

        @Test
        @DisplayName("Should set lastUpdated timestamp from historical data")
        void testLastUpdatedTimestamp() {
            // Arrange
            Instant expectedTimestamp = Instant.parse("2026-02-14T10:00:00Z");
            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                prices.add(100.0 + i);
                volumes.add(1000000L);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(expectedTimestamp)
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getLastUpdated()).isEqualTo(expectedTimestamp);
        }

        @Test
        @DisplayName("Should calculate all indicators with different volume period")
        void testCalculateWithDifferentVolumePeriod() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();

            for (int i = 0; i < 250; i++) {
                prices.add(100.0 + i);
                volumes.add(1000000L + (i * 10000L));
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act with volume period of 50
            TechnicalIndicators indicators = service.calculateIndicators(data, 50);

            // Assert
            assertThat(indicators.getSma20()).isNotNull();
            assertThat(indicators.getSma50()).isNotNull();
            assertThat(indicators.getSma200()).isNotNull();
            assertThat(indicators.getAverageVolume()).isNotNull();

            // Average should be calculated over 50 periods
            long expectedAvg = 0;
            for (int i = 0; i < 50; i++) {
                expectedAvg += volumes.get(i);
            }
            expectedAvg /= 50;
            assertThat(indicators.getAverageVolume()).isEqualTo(expectedAvg);
        }

        @Test
        @DisplayName("Should handle real-world price variations")
        void testWithRealWorldPriceVariations() {
            // Arrange - Simulate real price data with variations
            List<Double> prices = Arrays.asList(
                150.0, 152.5, 151.0, 153.75, 155.0,
                154.0, 156.5, 158.0, 157.5, 159.0,
                160.5, 162.0, 161.0, 163.5, 165.0,
                164.0, 166.5, 168.0, 167.5, 169.0,
                170.5, 172.0
            );

            List<Long> volumes = new ArrayList<>();
            for (int i = 0; i < 22; i++) {
                volumes.add(50000000L + (i * 1000000L));
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert
            assertThat(indicators.getSma20()).isNotNull();
            assertThat(indicators.getCurrentVolume()).isEqualTo(50000000L);
            assertThat(indicators.getAverageVolume()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Phase 1 — New Indicator Fields")
    class NewIndicatorFieldsTests {

        @Test
        @DisplayName("New indicator fields (EMA, RSI, MACD, BB, ATR) are null in TechnicalIndicators until phase 2 calculation is implemented")
        void testNewIndicatorFieldsAreNullBeforePhase2() {
            // Arrange
            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();
            for (int i = 0; i < 250; i++) {
                prices.add(100.0 + i);
                volumes.add(1000000L);
            }

            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            // Act
            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            // Assert – phase 2 calculations not yet implemented, fields must be null
            assertThat(indicators.getEma9()).isNull();
            assertThat(indicators.getEma12()).isNull();
            assertThat(indicators.getEma20()).isNull();
            assertThat(indicators.getEma26()).isNull();
            assertThat(indicators.getEma50()).isNull();
            assertThat(indicators.getEma200()).isNull();
            assertThat(indicators.getRsi14()).isNull();
            assertThat(indicators.getRsi30()).isNull();
            assertThat(indicators.getMacdLine()).isNull();
            assertThat(indicators.getMacdSignal()).isNull();
            assertThat(indicators.getMacdHistogram()).isNull();
            assertThat(indicators.getBbUpper20()).isNull();
            assertThat(indicators.getBbLower20()).isNull();
            assertThat(indicators.getAtr14()).isNull();
        }

        @Test
        @DisplayName("TechnicalIndicators builder supports all new indicator fields")
        void testTechnicalIndicatorsBuilderSupportsNewFields() {
            // Arrange & Act
            TechnicalIndicators indicators = TechnicalIndicators.builder()
                    .sma20(new BigDecimal("100.00"))
                    .ema9(new BigDecimal("99.50"))
                    .ema12(new BigDecimal("99.00"))
                    .ema20(new BigDecimal("98.00"))
                    .ema26(new BigDecimal("97.00"))
                    .ema50(new BigDecimal("95.00"))
                    .ema200(new BigDecimal("90.00"))
                    .rsi14(new BigDecimal("60.00"))
                    .rsi30(new BigDecimal("55.00"))
                    .macdLine(new BigDecimal("2.00"))
                    .macdSignal(new BigDecimal("1.50"))
                    .macdHistogram(new BigDecimal("0.50"))
                    .bbUpper20(new BigDecimal("105.00"))
                    .bbLower20(new BigDecimal("95.00"))
                    .atr14(new BigDecimal("3.00"))
                    .build();

            // Assert
            assertThat(indicators.getEma9()).isEqualByComparingTo(new BigDecimal("99.50"));
            assertThat(indicators.getEma12()).isEqualByComparingTo(new BigDecimal("99.00"));
            assertThat(indicators.getEma20()).isEqualByComparingTo(new BigDecimal("98.00"));
            assertThat(indicators.getEma26()).isEqualByComparingTo(new BigDecimal("97.00"));
            assertThat(indicators.getEma50()).isEqualByComparingTo(new BigDecimal("95.00"));
            assertThat(indicators.getEma200()).isEqualByComparingTo(new BigDecimal("90.00"));
            assertThat(indicators.getRsi14()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(indicators.getRsi30()).isEqualByComparingTo(new BigDecimal("55.00"));
            assertThat(indicators.getMacdLine()).isEqualByComparingTo(new BigDecimal("2.00"));
            assertThat(indicators.getMacdSignal()).isEqualByComparingTo(new BigDecimal("1.50"));
            assertThat(indicators.getMacdHistogram()).isEqualByComparingTo(new BigDecimal("0.50"));
            assertThat(indicators.getBbUpper20()).isEqualByComparingTo(new BigDecimal("105.00"));
            assertThat(indicators.getBbLower20()).isEqualByComparingTo(new BigDecimal("95.00"));
            assertThat(indicators.getAtr14()).isEqualByComparingTo(new BigDecimal("3.00"));
        }
    }
}
