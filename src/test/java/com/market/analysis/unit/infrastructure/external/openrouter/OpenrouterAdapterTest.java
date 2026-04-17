package com.market.analysis.unit.infrastructure.external.openrouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.infrastructure.exception.AIServiceException;
import com.market.analysis.infrastructure.external.openrouter.OpenrouterAdapter;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
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

    private static final String TEST_MODEL = "google/gemma-3-4b-it:free";
    private static final double TEST_TEMPERATURE = 0.7d;
    private static final long TEST_MAX_TOKENS = 500L;
    private static final double TEST_TOP_P = 0.9d;
    private static final double TEST_FREQUENCY_PENALTY = 0.5d;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
    @DisplayName("Should return the AI response content when the client succeeds")
    void shouldReturnValorationWhenClientResponds() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();
        ChatCompletion chatCompletion = mockClient.chat().completions().create((ChatCompletionCreateParams) any());
        when(chatCompletion.choices().get(0).message().content()).thenReturn(Optional.of("Bullish outlook"));

        // Act
        String result = adapter.getValoration("Price is above the moving averages");

        // Assert
        assertEquals("Bullish outlook", result);
    }

    @Test
    @DisplayName("Should return null when the AI response has no content")
    void shouldReturnNullWhenClientResponseHasEmptyContent() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();
        ChatCompletion chatCompletion = mockClient.chat().completions().create((ChatCompletionCreateParams) any());
        when(chatCompletion.choices().get(0).message().content()).thenReturn(Optional.empty());

        // Act
        String result = adapter.getValoration("Neutral technical snapshot");

        // Assert
        assertEquals(null, result);
    }

    @Test
    @DisplayName("Should throw AIServiceException on null stock data input")
    void shouldHandleNullStockDataInput() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();

        // Act & Assert
        assertThrows(AIServiceException.class, () -> adapter.getValoration(null));
    }

    @Test
    @DisplayName("Should verify adapter implements ApiIAPort interface")
    void shouldImplementApiIAPortInterface() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();

        // Assert
        assertNotNull(adapter);
        // Verify the adapter implements the port interface
        assertNotNull(adapter.getClass().getInterfaces());
        assertEquals(1, adapter.getClass().getInterfaces().length);
        assertEquals("ApiIAPort", adapter.getClass().getInterfaces()[0].getSimpleName());
    }

    private OpenrouterAdapter createAdapter() {
        return new OpenrouterAdapter(
                TEST_MODEL,
                TEST_TEMPERATURE,
                TEST_MAX_TOKENS,
                TEST_TOP_P,
                TEST_FREQUENCY_PENALTY,
                mockClient);
    }
}
