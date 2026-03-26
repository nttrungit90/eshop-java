package com.eshop.webhooks.dto;

import com.eshop.webhooks.model.WebhookSubscription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookSubscriptionRequest {

    @NotBlank(message = "Url is required")
    private String url;

    private String token;

    @NotBlank(message = "Event is required")
    private String event;

    @NotBlank(message = "GrantUrl is required")
    private String grantUrl;

    public WebhookSubscriptionRequest() {
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getGrantUrl() { return grantUrl; }
    public void setGrantUrl(String grantUrl) { this.grantUrl = grantUrl; }

    public WebhookSubscription.WebhookType toWebhookType() {
        try {
            // Convert PascalCase (e.g. "OrderPaid") or camelCase to UPPER_SNAKE_CASE (e.g. "ORDER_PAID")
            String normalized = event.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
            return WebhookSubscription.WebhookType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid event name: " + event);
        }
    }

    public boolean hasValidUrls() {
        return isValidUri(url) && isValidUri(grantUrl);
    }

    private static boolean isValidUri(String uri) {
        try {
            URI parsed = URI.create(uri);
            return parsed.isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }
}
