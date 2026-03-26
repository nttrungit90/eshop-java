package com.eshop.webhooksclient.service;

import com.eshop.webhooksclient.config.WebhookClientProperties;
import com.eshop.webhooksclient.model.WebhookResponse;
import com.eshop.webhooksclient.model.WebhookSubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
public class WebhooksApiClient {

    private static final Logger log = LoggerFactory.getLogger(WebhooksApiClient.class);

    private final RestClient restClient;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public WebhooksApiClient(RestClient.Builder restClientBuilder,
                             WebhookClientProperties properties,
                             OAuth2AuthorizedClientService authorizedClientService) {
        this.restClient = restClientBuilder.baseUrl(properties.getApiUrl()).build();
        this.authorizedClientService = authorizedClientService;
    }

    public List<WebhookResponse> listWebhooks() {
        try {
            String token = getAccessToken();
            if (token == null) return Collections.emptyList();

            List<WebhookResponse> result = restClient.get()
                    .uri("/api/webhooks")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to load webhooks: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean registerWebhook(WebhookSubscriptionRequest request) {
        try {
            String token = getAccessToken();
            if (token == null) return false;

            ResponseEntity<Void> response = restClient.post()
                    .uri("/api/webhooks")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Failed to register webhook: {}", e.getMessage());
            return false;
        }
    }

    private String getAccessToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        return null;
    }
}
