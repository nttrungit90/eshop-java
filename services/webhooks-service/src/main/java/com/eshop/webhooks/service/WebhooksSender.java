package com.eshop.webhooks.service;

import com.eshop.webhooks.model.WebhookData;
import com.eshop.webhooks.model.WebhookSubscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class WebhooksSender {

    private static final Logger log = LoggerFactory.getLogger(WebhooksSender.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WebhooksSender(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public void sendAll(List<WebhookSubscription> subscriptions, WebhookData data) {
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize webhook data", e);
            return;
        }

        CompletableFuture<?>[] futures = subscriptions.stream()
                .map(sub -> CompletableFuture.runAsync(() -> sendData(sub, json)))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private void sendData(WebhookSubscription subscription, String jsonData) {
        try {
            var requestSpec = restClient.post()
                    .uri(subscription.getDestUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonData);

            if (subscription.getToken() != null && !subscription.getToken().isBlank()) {
                requestSpec = requestSpec.header("X-eshop-whtoken", subscription.getToken());
            }

            requestSpec.retrieve().toBodilessEntity();

            log.debug("Sent webhook to {} of type {}", subscription.getDestUrl(), subscription.getType());
        } catch (Exception e) {
            log.warn("Failed to send webhook to {}: {}", subscription.getDestUrl(), e.getMessage());
        }
    }
}
