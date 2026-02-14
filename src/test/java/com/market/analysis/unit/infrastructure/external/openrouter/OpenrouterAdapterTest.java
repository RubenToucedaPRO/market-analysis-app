package com.market.analysis.unit.infrastructure.external.openrouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.external.openrouter.OpenrouterAdapter;

/**
 * Unit tests for OpenrouterAdapter.
 * 
 * Note: These are integration-style tests that verify the adapter's behavior
 * with the OpenAI client. Full mocking is challenging due to the complex
 * internal structure of the OpenAI SDK.
 */
@DisplayName("OpenrouterAdapter Tests")
class OpenrouterAdapterTest {

    private static final String TEST_API_KEY = "test-api-key";

    @Test
    @DisplayName("Should create adapter instance with API key")
    void shouldCreateAdapterInstance() {
        // Act
        OpenrouterAdapter adapter = new OpenrouterAdapter(TEST_API_KEY);

        // Assert
        assertNotNull(adapter);
    }

    @Test
    @DisplayName("Should handle API key initialization")
    void shouldHandleApiKeyInitialization() {
        // Arrange & Act
        OpenrouterAdapter adapter1 = new OpenrouterAdapter("key1");
        OpenrouterAdapter adapter2 = new OpenrouterAdapter("key2");
        OpenrouterAdapter adapter3 = new OpenrouterAdapter("");

        // Assert
        assertNotNull(adapter1);
        assertNotNull(adapter2);
        assertNotNull(adapter3);
    }

    @Test
    @DisplayName("Should return null when API call fails due to invalid key")
    void shouldReturnNullWhenApiCallFailsWithInvalidKey() {
        // Arrange
        OpenrouterAdapter adapter = new OpenrouterAdapter("invalid-key");
        String stockData = "Ticker: AAPL, Price: 150.00";

        // Act
        String result = adapter.getValoration(stockData);

        // Assert
        // The method should catch the exception and return null
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty stock data input")
    void shouldHandleEmptyStockDataInput() {
        // Arrange
        OpenrouterAdapter adapter = new OpenrouterAdapter(TEST_API_KEY);
        String emptyData = "";

        // Act
        String result = adapter.getValoration(emptyData);

        // Assert
        // The method should handle empty input gracefully
        // Result will be null because API call will fail with invalid credentials
        assertNull(result);
    }

    @Test
    @DisplayName("Should verify adapter implements ApiIAPort interface")
    void shouldImplementApiIAPortInterface() {
        // Arrange
        OpenrouterAdapter adapter = new OpenrouterAdapter(TEST_API_KEY);

        // Assert
        assertNotNull(adapter);
        // Verify the adapter implements the port interface
        assertNotNull(adapter.getClass().getInterfaces());
        assertEquals(1, adapter.getClass().getInterfaces().length);
        assertEquals("ApiIAPort", adapter.getClass().getInterfaces()[0].getSimpleName());
    }
}
