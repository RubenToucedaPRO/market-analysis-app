package com.market.analysis.infrastructure.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiKeyObfuscatorInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
            @NonNull ClientHttpRequestExecution execution)
            throws IOException {

        // Call the method to log request details with obfuscated API key
        logRequestDetails(request);

        // Proceed with the execution of the request
        return execution.execute(request, body);
    }

    private void logRequestDetails(HttpRequest request) {
        if (log.isDebugEnabled()) {
            String originalUri = request.getURI().toString();
            String method = request.getMethod().toString();

            String maskedUri = originalUri.toLowerCase().replaceAll("(apikey|token)=[^&]*", "$1=*****");

            log.debug("External Req: {} {}", method, maskedUri);
        }
    }
}