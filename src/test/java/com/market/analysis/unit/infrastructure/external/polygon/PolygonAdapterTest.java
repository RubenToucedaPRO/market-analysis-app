package com.market.analysis.unit.infrastructure.external.polygon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.external.polygon.PolygonAdapter;

/**
 * Unit tests for PolygonAdapter.
 * Tests historical data retrieval from Polygon API with rate limiting.
 */
@DisplayName("PolygonAdapter Unit Tests")
@ExtendWith(MockitoExtension.class)
class PolygonAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private PolygonAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        adapter = new PolygonAdapter(restTemplate, objectMapper);

        ReflectionTestUtils.setField(adapter, "apiToken", "test-api-key");
        ReflectionTestUtils.setField(adapter, "baseUrl", "https://api.polygon.io");
    }

    @Nested
    @DisplayName("Successful Data Retrieval Tests")
    class SuccessfulDataRetrievalTests {

        @Test
        @DisplayName("Should fetch and parse historical data successfully")
        void testFetchHistoricalDataSuccess() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "ticker": "AAPL",
                    "queryCount": 3,
                    "resultsCount": 3,
                    "adjusted": true,
                    "results": [
                        {
                            "v": 50000000,
                            "vw": 150.5,
                            "o": 149.0,
                            "c": 151.0,
                            "h": 152.0,
                            "l": 148.5,
                            "t": 1707868800000,
                            "n": 100000
                        },
                        {
                            "v": 48000000,
                            "vw": 149.5,
                            "o": 150.0,
                            "c": 149.0,
                            "h": 151.0,
                            "l": 148.0,
                            "t": 1707782400000,
                            "n": 95000
                        },
                        {
                            "v": 52000000,
                            "vw": 148.5,
                            "o": 148.0,
                            "c": 148.5,
                            "h": 149.5,
                            "l": 147.5,
                            "t": 1707696000000,
                            "n": 105000
                        }
                    ],
                    "status": "OK",
                    "request_id": "test-request-id"
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTicker()).isEqualTo(ticker);
            assertThat(result.getClosingPrices()).hasSize(3);
            assertThat(result.getClosingPrices()).containsExactly(151.0, 149.0, 148.5);
            assertThat(result.getVolumes()).hasSize(3);
            assertThat(result.getVolumes()).containsExactly(50000000L, 48000000L, 52000000L);
            assertThat(result.getLastUpdate()).isNotNull();

            verify(restTemplate, times(1)).getForEntity(any(URI.class), eq(String.class));
        }

        @Test
        @DisplayName("Should handle empty results array")
        void testFetchHistoricalDataWithEmptyResults() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "ticker": "AAPL",
                    "queryCount": 0,
                    "resultsCount": 0,
                    "adjusted": true,
                    "results": [],
                    "status": "OK"
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTicker()).isEqualTo(ticker);
            assertThat(result.getClosingPrices()).isEmpty();
            assertThat(result.getVolumes()).isEmpty();
            assertThat(result.getLastUpdate()).isNotNull();
        }

        @Test
        @DisplayName("Should handle response with single result")
        void testFetchHistoricalDataWithSingleResult() {
            // Arrange
            String ticker = "MSFT";
            String jsonResponse = """
                {
                    "ticker": "MSFT",
                    "queryCount": 1,
                    "resultsCount": 1,
                    "adjusted": true,
                    "results": [
                        {
                            "v": 25000000,
                            "c": 380.5,
                            "h": 382.0,
                            "l": 379.0,
                            "o": 380.0,
                            "t": 1707868800000
                        }
                    ],
                    "status": "OK"
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTicker()).isEqualTo(ticker);
            assertThat(result.getClosingPrices()).hasSize(1);
            assertThat(result.getClosingPrices().get(0)).isEqualTo(380.5);
            assertThat(result.getVolumes()).hasSize(1);
            assertThat(result.getVolumes().get(0)).isEqualTo(25000000L);
        }

        @Test
        @DisplayName("Should handle ticker with different formats")
        void testFetchHistoricalDataWithDifferentTickerFormats() {
            // Arrange
            String ticker = "BRK.B";
            String jsonResponse = """
                {
                    "ticker": "BRK.B",
                    "results": [
                        {"v": 1000000, "c": 350.0}
                    ],
                    "status": "OK"
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTicker()).isEqualTo(ticker);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw PolygonException on rate limit (429)")
        void testFetchHistoricalDataRateLimitError() {
            // Arrange
            String ticker = "AAPL";
            HttpClientErrorException rateLimitException = 
                new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");

            when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(rateLimitException);

            // Act & Assert
            assertThatThrownBy(() -> adapter.fetchHistoricalData(ticker))
                .isInstanceOf(PolygonException.class)
                .hasMessageContaining("Error communicating with Polygon");
        }

        @Test
        @DisplayName("Should throw PolygonException on HTTP client error")
        void testFetchHistoricalDataHttpClientError() {
            // Arrange
            String ticker = "AAPL";
            HttpClientErrorException httpException = 
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad request");

            when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(httpException);

            // Act & Assert
            assertThatThrownBy(() -> adapter.fetchHistoricalData(ticker))
                .isInstanceOf(PolygonException.class)
                .hasMessageContaining("Error communicating with Polygon");
        }

        @Test
        @DisplayName("Should throw PolygonException on JSON parsing error")
        void testFetchHistoricalDataInvalidJson() {
            // Arrange
            String ticker = "AAPL";
            String invalidJsonResponse = "{ invalid json }";

            ResponseEntity<String> responseEntity = ResponseEntity.ok(invalidJsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act & Assert
            assertThatThrownBy(() -> adapter.fetchHistoricalData(ticker))
                .isInstanceOf(PolygonException.class)
                .hasMessageContaining("Error mapping historical data");
        }

        @Test
        @DisplayName("Should throw PolygonException on unexpected exception")
        void testFetchHistoricalDataUnexpectedException() {
            // Arrange
            String ticker = "AAPL";
            when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

            // Act & Assert
            assertThatThrownBy(() -> adapter.fetchHistoricalData(ticker))
                .isInstanceOf(PolygonException.class)
                .hasMessageContaining("Unexpected error processing Polygon data");
        }

        @Test
        @DisplayName("Should handle response without results field")
        void testFetchHistoricalDataWithoutResultsField() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "ticker": "AAPL",
                    "status": "NO_DATA"
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTicker()).isEqualTo(ticker);
            assertThat(result.getClosingPrices()).isEmpty();
            assertThat(result.getVolumes()).isEmpty();
        }

        @Test
        @DisplayName("Should handle results that are not an array")
        void testFetchHistoricalDataWithNonArrayResults() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "ticker": "AAPL",
                    "results": "not an array",
                    "status": "OK"
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTicker()).isEqualTo(ticker);
            assertThat(result.getClosingPrices()).isEmpty();
            assertThat(result.getVolumes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Data Mapping Tests")
    class DataMappingTests {

        @Test
        @DisplayName("Should correctly map close price and volume fields")
        void testMapCloseAndVolumeFields() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "results": [
                        {"c": 150.25, "v": 50000000},
                        {"c": 151.75, "v": 48000000},
                        {"c": 149.50, "v": 52000000}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getClosingPrices()).containsExactly(150.25, 151.75, 149.50);
            assertThat(result.getVolumes()).containsExactly(50000000L, 48000000L, 52000000L);
        }

        @Test
        @DisplayName("Should handle missing close price as 0.0")
        void testMapMissingClosePrice() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "results": [
                        {"v": 50000000},
                        {"c": 151.0, "v": 48000000}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getClosingPrices()).containsExactly(0.0, 151.0);
            assertThat(result.getVolumes()).containsExactly(50000000L, 48000000L);
        }

        @Test
        @DisplayName("Should handle missing volume as 0")
        void testMapMissingVolume() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "results": [
                        {"c": 150.0},
                        {"c": 151.0, "v": 48000000}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getClosingPrices()).containsExactly(150.0, 151.0);
            assertThat(result.getVolumes()).containsExactly(0L, 48000000L);
        }

        @Test
        @DisplayName("Should handle decimal volumes by converting to long")
        void testMapDecimalVolume() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "results": [
                        {"c": 150.0, "v": 50000000.7}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getVolumes().get(0)).isEqualTo(50000000L);
        }

        @Test
        @DisplayName("Should handle large price values")
        void testMapLargePriceValues() {
            // Arrange
            String ticker = "BRK.A";
            String jsonResponse = """
                {
                    "results": [
                        {"c": 525000.50, "v": 100}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getClosingPrices().get(0)).isEqualTo(525000.50);
        }

        @Test
        @DisplayName("Should handle very large volume values")
        void testMapLargeVolumeValues() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "results": [
                        {"c": 150.0, "v": 999999999999}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getVolumes().get(0)).isEqualTo(999999999999L);
        }

        @Test
        @DisplayName("Should preserve order of data points")
        void testPreserveDataOrder() {
            // Arrange
            String ticker = "AAPL";
            String jsonResponse = """
                {
                    "results": [
                        {"c": 100.0, "v": 1000000},
                        {"c": 101.0, "v": 1100000},
                        {"c": 102.0, "v": 1200000},
                        {"c": 103.0, "v": 1300000}
                    ]
                }
                """;

            ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(responseEntity);

            // Act
            HistoricalData result = adapter.fetchHistoricalData(ticker);

            // Assert
            assertThat(result.getClosingPrices()).containsExactly(100.0, 101.0, 102.0, 103.0);
            assertThat(result.getVolumes()).containsExactly(1000000L, 1100000L, 1200000L, 1300000L);
        }
    }
}
