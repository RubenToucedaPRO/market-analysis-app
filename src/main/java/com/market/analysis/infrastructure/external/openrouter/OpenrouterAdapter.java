package com.market.analysis.infrastructure.external.openrouter;

import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.infrastructure.exception.AIServiceException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OpenrouterAdapter implements ApiIAPort {

    private final String apiKey;

    private final OpenAIClient client;

    private static final String BASE_URL = "https://openrouter.ai/api/v1";

    public OpenrouterAdapter(@Value("${openrouter.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = OpenAIOkHttpClient.builder()
                .baseUrl(BASE_URL)
                .apiKey(this.apiKey)
                .putHeader("HTTP-Referer", "http://localhost:8080")
                .build();
    }

    @Override
    public String getValoration(String datosAccion) {
        log.debug("Requesting AI valoration for: {}", datosAccion);

        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .addUserMessage(datosAccion)
                    .model("google/gemma-3-4b-it:free")
                    .build();

            ChatCompletion chatCompletion = client.chat().completions().create(params);

            String content = chatCompletion.choices().get(0).message().content().orElse(null);
            log.debug("AI response received: {}", content);

            return content;

        } catch (Exception e) {
            throw new AIServiceException("Error calling OpenRouter API", e);
        }
    }
}