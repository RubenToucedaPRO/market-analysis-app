package com.market.analysis.unit.infrastructure.external.openrouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import com.openai.errors.RateLimitException;
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

    private static final String TEST_MODEL = "google/gemma-4-31b-it:free";
    private static final List<String> TEST_FALLBACKS = List.of("model-b:free", "model-c:free");
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
                TEST_FALLBACKS,
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
                TEST_FALLBACKS,
                TEST_TEMPERATURE,
                TEST_MAX_TOKENS,
                TEST_TOP_P,
                TEST_FREQUENCY_PENALTY,
                mockClient);
    }

    private OpenrouterAdapter createAdapterWithoutFallbacks() {
        return new OpenrouterAdapter(
                TEST_MODEL,
                List.of(),
                TEST_TEMPERATURE,
                TEST_MAX_TOKENS,
                TEST_TOP_P,
                TEST_FREQUENCY_PENALTY,
                mockClient);
    }

    @Test
    @DisplayName("Should try the next model when the first one is rate limited")
    void shouldFallbackToNextModelOnRateLimit() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();
        ChatCompletion chatCompletion = mock(ChatCompletion.class, Answers.RETURNS_DEEP_STUBS);
        when(chatCompletion.choices().get(0).message().content()).thenReturn(Optional.of("Fallback outlook"));
        when(mockClient.chat().completions().create((ChatCompletionCreateParams) any()))
                .thenThrow(mock(RateLimitException.class))
                .thenReturn(chatCompletion);

        // Act
        String result = adapter.getValoration("Price is above the moving averages");

        // Assert
        assertEquals("Fallback outlook", result);
        verify(mockClient.chat().completions(), times(2)).create((ChatCompletionCreateParams) any());
    }

    @Test
    @DisplayName("Should throw AIServiceException when every model is rate limited")
    void shouldThrowWhenAllModelsRateLimited() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();
        when(mockClient.chat().completions().create((ChatCompletionCreateParams) any()))
                .thenThrow(mock(RateLimitException.class));

        // Act & Assert
        assertThrows(AIServiceException.class, () -> adapter.getValoration("Neutral technical snapshot"));
        // 1 principal + 2 reservas
        verify(mockClient.chat().completions(), times(3)).create((ChatCompletionCreateParams) any());
    }

    @Test
    @DisplayName("Should fail fast on non-rate-limit errors without trying fallbacks")
    void shouldNotFallbackOnGenericError() {
        // Arrange
        OpenrouterAdapter adapter = createAdapter();
        when(mockClient.chat().completions().create((ChatCompletionCreateParams) any()))
                .thenThrow(new RuntimeException("bad credentials"));

        // Act & Assert
        assertThrows(AIServiceException.class, () -> adapter.getValoration("Neutral technical snapshot"));
        verify(mockClient.chat().completions(), times(1)).create((ChatCompletionCreateParams) any());
    }

    @Test
    @DisplayName("Should work with a single model when no fallbacks are configured")
    void shouldWorkWithoutFallbacks() {
        // Arrange
        OpenrouterAdapter adapter = createAdapterWithoutFallbacks();
        ChatCompletion chatCompletion = mockClient.chat().completions().create((ChatCompletionCreateParams) any());
        when(chatCompletion.choices().get(0).message().content()).thenReturn(Optional.of("Solo outlook"));

        // Act
        String result = adapter.getValoration("Price is above the moving averages");

        // Assert
        assertEquals("Solo outlook", result);
    }
}
