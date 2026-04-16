package com.market.analysis.infrastructure.external.openrouter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.infrastructure.exception.AIServiceException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OpenrouterAdapter implements ApiIAPort {

    private final String apiKey;
    private final String model;
    private final double temperature;
    private final long maxTokens;
    private final double topP;
    private final double frequencyPenalty;

    private final OpenAIClient client;

    private static final String BASE_URL = "https://openrouter.ai/api/v1";

    public OpenrouterAdapter(
            @Value("${openrouter.api.key}") String apiKey,
            @Value("${openrouter.model:google/gemma-3-4b-it:free}") String model,
            @Value("${openrouter.temperature:0.7}") double temperature,
            @Value("${openrouter.max-tokens:500}") long maxTokens,
            @Value("${openrouter.top-p:0.9}") double topP,
            @Value("${openrouter.frequency-penalty:0.5}") double frequencyPenalty) {
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.client = OpenAIOkHttpClient.builder()
                .baseUrl(BASE_URL)
                .apiKey(this.apiKey)
                .putHeader("HTTP-Referer", "http://localhost:8080")
                .build();
    }

    @Override
    public String getValoration(String datosAccion) {
        log.debug(
                "Requesting AI valoration model={} promptLength={} temperature={} maxTokens={} topP={} frequencyPenalty={}",
                model,
                datosAccion == null ? 0 : datosAccion.length(),
                temperature,
                maxTokens,
                topP,
                frequencyPenalty);

        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .addUserMessage(datosAccion)
                    .model(model)
                    .temperature(temperature)
                    .maxCompletionTokens(maxTokens)
                    .topP(topP)
                    .frequencyPenalty(frequencyPenalty)
                    .build();

            ChatCompletion chatCompletion = client.chat().completions().create(params);

            String content = chatCompletion.choices().get(0).message().content().orElse(null);
            log.debug("AI response received model={} contentLength={}", model, content == null ? 0 : content.length());

            return content;

        } catch (Exception e) {
            throw new AIServiceException("Error calling OpenRouter API", e);
        }
    }
}
