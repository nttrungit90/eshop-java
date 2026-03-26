package com.eshop.webhooks.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public record WebhookData(Instant when, String type, String payload) {

    public static WebhookData create(WebhookSubscription.WebhookType hookType, Object data, ObjectMapper objectMapper) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            return new WebhookData(Instant.now(), hookType.name(), payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize webhook payload", e);
        }
    }
}
