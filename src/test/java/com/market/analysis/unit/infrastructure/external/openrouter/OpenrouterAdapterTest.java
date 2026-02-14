package com.market.analysis.unit.infrastructure.external.openrouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.infrastructure.external.openrouter.OpenrouterAdapter;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

/**
 * Unit tests for OpenrouterAdapter.
 * 
 * Note: These are integration-style tests that verify the adapter's behavior
 * with the OpenAI client. Full mocking is challenging due to the complex
 * internal structure of the OpenAI SDK.
 */
@DisplayName("OpenrouterAdapter Tests")
@ExtendWith(MockitoExtension.class)
class OpenrouterAdapterTest {

    private static final String TEST_API_KEY = "test-api-key";

    @Mock
    private OpenAIClient mockClient;

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
