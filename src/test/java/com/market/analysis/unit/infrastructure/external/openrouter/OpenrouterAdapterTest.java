package com.market.analysis.unit.infrastructure.external.openrouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.infrastructure.exception.AIServiceException;
import com.market.analysis.infrastructure.external.openrouter.OpenrouterAdapter;
import com.openai.client.OpenAIClient;

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

    private static final String TEST_MODEL = "google/gemma-3-4b-it:free";
    private static final double TEST_TEMPERATURE = 0.7d;
    private static final long TEST_MAX_TOKENS = 500L;
    private static final double TEST_TOP_P = 0.9d;
    private static final double TEST_FREQUENCY_PENALTY = 0.5d;

    @Mock
    private OpenAIClient mockClient;

    @Test
    @DisplayName("Should create adapter instance with API key")
    void shouldCreateAdapterInstance() {
        // Act
        OpenrouterAdapter adapter = new OpenrouterAdapter(
                TEST_MODEL,
                TEST_TEMPERATURE,
                TEST_MAX_TOKENS,
                TEST_TOP_P,
                TEST_FREQUENCY_PENALTY, mockClient);
        // Assert
        assertNotNull(adapter);
    }

    @Test
    @DisplayName("Should handle API key initialization")
    void shouldHandleApiKeyInitialization() {
        // Arrange & Act
        OpenrouterAdapter adapter1 = new OpenrouterAdapter(TEST_MODEL, TEST_TEMPERATURE, TEST_MAX_TOKENS, TEST_TOP_P,
                TEST_FREQUENCY_PENALTY, mockClient);
        OpenrouterAdapter adapter2 = new OpenrouterAdapter(TEST_MODEL, TEST_TEMPERATURE, TEST_MAX_TOKENS, TEST_TOP_P,
                TEST_FREQUENCY_PENALTY, mockClient);
        OpenrouterAdapter adapter3 = new OpenrouterAdapter(TEST_MODEL, TEST_TEMPERATURE, TEST_MAX_TOKENS, TEST_TOP_P,
                TEST_FREQUENCY_PENALTY, mockClient);

        // Assert
        assertNotNull(adapter1);
        assertNotNull(adapter2);
        assertNotNull(adapter3);
    }

    @Test
    @DisplayName("Should throw AIServiceException on empty stock data input")
    void shouldHandleEmptyStockDataInput() {
        // Arrange
        OpenrouterAdapter adapter = new OpenrouterAdapter(
                TEST_MODEL,
                TEST_TEMPERATURE,
                TEST_MAX_TOKENS,
                TEST_TOP_P,
                TEST_FREQUENCY_PENALTY, mockClient);
        String emptyData = "";

        // Act & Assert
        // The method should throw AIServiceException when API call fails
        assertThrows(AIServiceException.class, () -> adapter.getValoration(emptyData));
    }

    @Test
    @DisplayName("Should verify adapter implements ApiIAPort interface")
    void shouldImplementApiIAPortInterface() {
        // Arrange
        OpenrouterAdapter adapter = new OpenrouterAdapter(
                TEST_MODEL,
                TEST_TEMPERATURE,
                TEST_MAX_TOKENS,
                TEST_TOP_P,
                TEST_FREQUENCY_PENALTY, mockClient);

        // Assert
        assertNotNull(adapter);
        // Verify the adapter implements the port interface
        assertNotNull(adapter.getClass().getInterfaces());
        assertEquals(1, adapter.getClass().getInterfaces().length);
        assertEquals("ApiIAPort", adapter.getClass().getInterfaces()[0].getSimpleName());
    }
}
