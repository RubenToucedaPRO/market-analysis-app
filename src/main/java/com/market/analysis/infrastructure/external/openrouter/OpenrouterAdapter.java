package com.market.analysis.infrastructure.external.openrouter;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.infrastructure.exception.AIServiceException;
import com.openai.client.OpenAIClient;
import com.openai.errors.RateLimitException;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OpenrouterAdapter implements ApiIAPort {

    private final List<String> models;
    private final double temperature;
    private final long maxTokens;
    private final double topP;
    private final double frequencyPenalty;

    private final OpenAIClient client;

    public OpenrouterAdapter(
            @Value("${openrouter.model:google/gemma-4-26b-a4b-it:free}") String model,
            @Value("${openrouter.fallback-models:meta-llama/llama-3.3-70b-instruct:free,openai/gpt-oss-20b:free}") List<String> fallbackModels,
            @Value("${openrouter.temperature:0.7}") double temperature,
            @Value("${openrouter.max-tokens:500}") long maxTokens,
            @Value("${openrouter.top-p:0.9}") double topP,
            @Value("${openrouter.frequency-penalty:0.5}") double frequencyPenalty, OpenAIClient client) {
        Assert.hasText(model, "openrouter.model must not be blank");
        Stream<String> fallbacks = fallbackModels == null ? Stream.empty()
                : fallbackModels.stream().filter(fallback -> fallback != null && !fallback.isBlank());
        this.models = Stream.concat(Stream.of(model), fallbacks).toList();
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.client = client;
    }

    @Override
    @Retry(name = "openrouterClient")
    public String getValoration(String datosAccion) {
        log.debug(
                "Requesting AI valoration models={} promptLength={} temperature={} maxTokens={} topP={} frequencyPenalty={}",
                models,
                datosAccion == null ? 0 : datosAccion.length(),
                temperature,
                maxTokens,
                topP,
                frequencyPenalty);

        AIServiceException lastRateLimitError = null;
        for (String currentModel : models) {
            try {
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .addUserMessage(datosAccion)
                        .model(currentModel)
                        .temperature(temperature)
                        .maxCompletionTokens(maxTokens)
                        .topP(topP)
                        .frequencyPenalty(frequencyPenalty)
                        .build();

                ChatCompletion chatCompletion = client.chat().completions().create(params);

                String content = chatCompletion.choices().get(0).message().content().orElse(null);
                log.debug("AI response received model={} contentLength={}", currentModel,
                        content == null ? 0 : content.length());

                return content;

            } catch (RateLimitException e) {
                // 429: el modelo o su proveedor están saturados (o sin cupo). Se prueba
                // con el siguiente modelo de la lista en vez de fallar directamente.
                log.warn("OpenRouter rate limited model={}: {}. Trying next model...", currentModel, e.getMessage());
                lastRateLimitError = new AIServiceException("Error calling OpenRouter API", e);
            } catch (Exception e) {
                // Otros errores (clave inválida, modelo inexistente, red...) no se
                // arreglan cambiando de modelo: fallo rápido al fallback del caso de uso.
                log.warn("OpenRouter API error model={} exceptionType={}: {}", currentModel,
                        e.getClass().getSimpleName(), e.getMessage());
                throw new AIServiceException("Error calling OpenRouter API", e);
            }
        }

        throw lastRateLimitError != null ? lastRateLimitError
                : new AIServiceException("Error calling OpenRouter API: no models configured");
    }
}
