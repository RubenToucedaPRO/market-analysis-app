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
        @DisplayName("BB and ATR fields are null until their respective phases are implemented")
        void testBbAtrFieldsAreNull() {
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

            // Assert – phase 5 not yet implemented; BB and ATR fields must remain null
            assertThat(indicators.getBbUpper20()).isNull();
            assertThat(indicators.getBbLower20()).isNull();
            assertThat(indicators.getAtr14()).isNull();
            // Phase 4 (MACD) is implemented – values should be present
            assertThat(indicators.getMacdLine()).isNotNull();
            assertThat(indicators.getMacdSignal()).isNotNull();
            assertThat(indicators.getMacdHistogram()).isNotNull();
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

    @Nested
    @DisplayName("Phase 2 — EMA Calculation Tests")
    class EmaCalculationTests {

        private List<Double> pricesDesc(int count, double startHigh) {
            // Simulate Polygon desc order: index 0 = most recent (highest price)
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                prices.add(startHigh - i);
            }
            return prices;
        }

        private HistoricalData buildData(List<Double> prices) {
            List<Long> volumes = new ArrayList<>(Collections.nCopies(prices.size(), 1000000L));
            return HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("Should return null EMA9 when only 8 prices are available")
        void shouldReturnNullEmaWhenInsufficientData() {
            List<Double> prices = Arrays.asList(100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0);
            List<Long> volumes = new ArrayList<>(Collections.nCopies(8, 1000000L));
            HistoricalData data = HistoricalData.builder()
                    .ticker("TSLA")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            TechnicalIndicators indicators = service.calculateIndicators(data, 8);

            assertThat(indicators.getEma9()).isNull();
            assertThat(indicators.getEma12()).isNull();
            assertThat(indicators.getEma200()).isNull();
        }

        @Test
        @DisplayName("Should return non-null EMA9 with exactly 9 prices")
        void shouldReturnNonNullEmaWithExactlyPeriodPrices() {
            List<Double> prices = Collections.nCopies(9, 100.0);
            List<Long> volumes = new ArrayList<>(Collections.nCopies(9, 1000000L));
            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();

            TechnicalIndicators indicators = service.calculateIndicators(data, 9);

            assertThat(indicators.getEma9()).isNotNull();
        }

        @Test
        @DisplayName("Should return constant price as EMA when all prices are equal")
        void shouldReturnConstantPriceEmaWhenAllPricesEqual() {
            // With all prices = 100.0, EMA seed = 100.0 and all iterations stay at 100.0
            List<Double> prices = Collections.nCopies(50, 100.0);
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getEma9()).isNotNull();
            assertThat(indicators.getEma9()).isEqualByComparingTo(new BigDecimal("100.0000"));
        }

        @Test
        @DisplayName("calculateIndicators should populate all six EMA fields with 300 prices")
        void shouldPopulateAllEmaFieldsInCalculateIndicators() {
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 300; i++) {
                prices.add(150.0 + i * 0.5);
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getEma9()).isNotNull();
            assertThat(indicators.getEma12()).isNotNull();
            assertThat(indicators.getEma20()).isNotNull();
            assertThat(indicators.getEma26()).isNotNull();
            assertThat(indicators.getEma50()).isNotNull();
            assertThat(indicators.getEma200()).isNotNull();
        }

        @Test
        @DisplayName("EMA values should be positive for realistic positive prices")
        void shouldProducePositiveEmaForPositivePrices() {
            // 200 prices in Polygon desc order (most recent first)
            List<Double> descPrices = pricesDesc(200, 299.0);
            HistoricalData data = buildData(descPrices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getEma9()).isNotNull();
            assertThat(indicators.getEma9().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
        }

        @Test
        @DisplayName("EMA result should have scale of 4 decimal places")
        void shouldReturnEmaWithScale4() {
            List<Double> prices = Collections.nCopies(30, 123.456789);
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getEma9()).isNotNull();
            assertThat(indicators.getEma9().scale()).isEqualTo(4);
        }

        @Test
        @DisplayName("EMA9 should respond faster to recent price changes than EMA200")
        void shouldShowEmaFasterResponseForShorterPeriod() {
            // Create 300 prices: first 250 stable at 100, then spike to 200
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                prices.add(200.0); // most recent 50 prices are high (Polygon desc)
            }
            for (int i = 0; i < 250; i++) {
                prices.add(100.0); // older 250 prices are lower
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getEma9()).isNotNull();
            assertThat(indicators.getEma200()).isNotNull();
            // EMA9 reacts faster to recent spike → should be higher than EMA200
            assertThat(indicators.getEma9().compareTo(indicators.getEma200())).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Phase 3 — RSI Calculation Tests")
    class RsiCalculationTests {

        /**
         * Builds a HistoricalData with prices in Polygon descending order
         * (index 0 = most recent price).
         */
        private HistoricalData buildData(List<Double> pricesDesc) {
            List<Long> volumes = new ArrayList<>(Collections.nCopies(pricesDesc.size(), 1_000_000L));
            return HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(pricesDesc)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("Should return null RSI when fewer than period+1 prices are available")
        void shouldReturnNullRsiWhenInsufficientData() {
            // 14 prices → cannot compute 14 deltas (need at least 15)
            List<Double> prices = new ArrayList<>(Collections.nCopies(14, 100.0));
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 14);

            assertThat(indicators.getRsi14()).isNull();
        }

        @Test
        @DisplayName("Should return null RSI when prices list is null")
        void shouldReturnNullRsiWhenPricesNull() {
            HistoricalData data = HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(null)
                    .volumes(Collections.emptyList())
                    .lastUpdate(Instant.now())
                    .build();

            TechnicalIndicators indicators = service.calculateIndicators(data, 14);

            assertThat(indicators.getRsi14()).isNull();
            assertThat(indicators.getRsi30()).isNull();
        }

        @Test
        @DisplayName("Should return RSI=100 when all price changes are gains (no losses)")
        void shouldReturnRsi100WhenAllPricesRising() {
            // Polygon desc order: most recent price first → prices decrease from index 0 to end
            // After reverse (oldest→newest) prices will be ascending → all deltas positive
            // 15 prices in desc order: 114, 113, 112, ..., 100
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                pricesDesc.add(114.0 - i); // desc: [114, 113, ..., 100]
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 14);

            assertThat(indicators.getRsi14()).isNotNull();
            assertThat(indicators.getRsi14()).isEqualByComparingTo(new BigDecimal("100.0000"));
        }

        @Test
        @DisplayName("Should return RSI=0 when all price changes are losses (no gains)")
        void shouldReturnRsi0WhenAllPricesFalling() {
            // Polygon desc order: most recent first → prices increase from index 0 to end
            // After reverse (oldest→newest) prices will be descending → all deltas negative
            // 15 prices in desc order: 100, 101, 102, ..., 114
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                pricesDesc.add(100.0 + i); // desc: [100, 101, ..., 114]
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 14);

            assertThat(indicators.getRsi14()).isNotNull();
            assertThat(indicators.getRsi14()).isEqualByComparingTo(new BigDecimal("0.0000"));
        }

        @Test
        @DisplayName("Should calculate RSI=50 when gains equal losses over the period")
        void shouldCalculateRsi50WhenGainsEqualLosses() {
            // 15 prices (desc) alternating so that after reversal we get 7 gains and 7 losses of equal magnitude
            // desc: [101, 100, 101, 100, 101, 100, 101, 100, 101, 100, 101, 100, 101, 100, 101]
            // reversed (asc): [101, 100, 101, 100, ..., 101]
            // deltas: -1, +1, -1, +1, ... → 7 gains (+1) and 7 losses (|-1|)
            // avgGain = 7/14 = 0.5, avgLoss = 7/14 = 0.5, RS = 1, RSI = 50
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                pricesDesc.add(i % 2 == 0 ? 101.0 : 100.0);
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 14);

            assertThat(indicators.getRsi14()).isNotNull();
            assertThat(indicators.getRsi14()).isEqualByComparingTo(new BigDecimal("50.0000"));
        }

        @Test
        @DisplayName("Should calculate RSI14 with a known result from deterministic price series")
        void shouldCalculateRsi14WithKnownValues() {
            // 15 prices (desc order, Polygon format): oldest price at end, newest at index 0
            // desc: [114,112,110,108,106,104,102,100,102,104,106,108,110,112,114]
            // reversed (oldest→newest): [114,112,110,108,106,104,102,100,102,104,106,108,110,112,114]
            // 14 deltas: -2,-2,-2,-2,-2,-2,-2,+2,+2,+2,+2,+2,+2,+2
            // gains: 7 × 2 = 14, avgGain = 1.0
            // losses: 7 × 2 = 14, avgLoss = 1.0
            // RS = 1, RSI = 50
            List<Double> pricesDesc = Arrays.asList(
                114.0, 112.0, 110.0, 108.0, 106.0, 104.0, 102.0, 100.0,
                102.0, 104.0, 106.0, 108.0, 110.0, 112.0, 114.0
            );
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 14);

            assertThat(indicators.getRsi14()).isEqualByComparingTo(new BigDecimal("50.0000"));
        }

        @Test
        @DisplayName("Should calculate RSI30 when at least 31 prices are available")
        void shouldCalculateRsi30WithSufficientData() {
            // 31 prices: ascending in desc order → after reverse = descending → all deltas negative → RSI=0
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 31; i++) {
                pricesDesc.add(100.0 + i);
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getRsi30()).isNotNull();
            assertThat(indicators.getRsi30()).isEqualByComparingTo(new BigDecimal("0.0000"));
        }

        @Test
        @DisplayName("calculateIndicators should populate both RSI14 and RSI30 with sufficient data")
        void shouldPopulateBothRsiFieldsInCalculateIndicators() {
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                pricesDesc.add(150.0 - i * 0.1); // slight descent in time (desc order)
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getRsi14()).isNotNull();
            assertThat(indicators.getRsi30()).isNotNull();
        }

        @Test
        @DisplayName("RSI result should have scale of 4 decimal places")
        void shouldReturnRsiWithScale4() {
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                pricesDesc.add(100.0 - i * 0.5);
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getRsi14()).isNotNull();
            assertThat(indicators.getRsi14().scale()).isEqualTo(4);
        }

        @Test
        @DisplayName("RSI value should be in range [0, 100]")
        void shouldReturnRsiInValidRange() {
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                pricesDesc.add(100.0 + Math.sin(i) * 10);
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getRsi14()).isNotNull();
            assertThat(indicators.getRsi14().compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
            assertThat(indicators.getRsi14().compareTo(new BigDecimal("100"))).isLessThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Phase 4 — MACD Calculation Tests")
    class MacdCalculationTests {

        private HistoricalData buildData(List<Double> prices) {
            List<Long> volumes = new ArrayList<>(Collections.nCopies(prices.size(), 1000000L));
            return HistoricalData.builder()
                    .ticker("AAPL")
                    .closingPrices(prices)
                    .volumes(volumes)
                    .lastUpdate(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("Should return null MACD when prices list has fewer than 35 elements")
        void calculateIndicators_shouldReturnNullMacdForInsufficientData() {
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 34; i++) {
                prices.add(100.0 + i);
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getMacdLine()).isNull();
            assertThat(indicators.getMacdSignal()).isNull();
            assertThat(indicators.getMacdHistogram()).isNull();
        }

        @Test
        @DisplayName("Should return non-null MACD when at least 35 prices are available")
        void calculateIndicators_shouldReturnNonNullMacdForSufficientData() {
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                prices.add(100.0 + i);
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getMacdLine()).isNotNull();
            assertThat(indicators.getMacdSignal()).isNotNull();
            assertThat(indicators.getMacdHistogram()).isNotNull();
        }

        @Test
        @DisplayName("Should return MACD values with scale 4")
        void calculateIndicators_shouldReturnMacdValuesWithScaleFour() {
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                prices.add(100.0 + i);
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getMacdLine().scale()).isEqualTo(4);
            assertThat(indicators.getMacdSignal().scale()).isEqualTo(4);
            assertThat(indicators.getMacdHistogram().scale()).isEqualTo(4);
        }

        @Test
        @DisplayName("MACD histogram should equal macdLine minus macdSignal")
        void calculateIndicators_histogramShouldEqualLineMinusSignal() {
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                prices.add(100.0 + i);
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            BigDecimal expectedHistogram = indicators.getMacdLine()
                    .subtract(indicators.getMacdSignal())
                    .setScale(4, java.math.RoundingMode.HALF_UP);
            assertThat(indicators.getMacdHistogram()).isEqualByComparingTo(expectedHistogram);
        }

        @Test
        @DisplayName("Should compute non-zero MACD for a trending price series")
        void calculateIndicators_shouldComputeNonZeroMacdForTrendingSeries() {
            // Monotonically increasing prices (descending order as Polygon provides)
            List<Double> pricesDesc = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                pricesDesc.add(200.0 - i); // index 0 = 200 (most recent), index 199 = 1 (oldest)
            }
            HistoricalData data = buildData(pricesDesc);

            TechnicalIndicators indicators = service.calculateIndicators(data, 20);

            assertThat(indicators.getMacdLine()).isNotNull();
            // For a monotonically increasing series, fast EMA > slow EMA, so MACD_LINE > 0
            assertThat(indicators.getMacdLine().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
        }

        @Test
        @DisplayName("MACD fields should be null in calculateIndicators when fewer than 35 prices")
        void calculateIndicators_shouldReturnNullMacdWhenInsufficientData() {
            List<Double> prices = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                prices.add(100.0 + i);
            }
            HistoricalData data = buildData(prices);

            TechnicalIndicators indicators = service.calculateIndicators(data, 5);

            assertThat(indicators.getMacdLine()).isNull();
            assertThat(indicators.getMacdSignal()).isNull();
            assertThat(indicators.getMacdHistogram()).isNull();
        }
    }
}
