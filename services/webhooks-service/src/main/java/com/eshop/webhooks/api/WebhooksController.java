/**
 * Converted from: src/Webhooks.API/Apis/WebhooksApi.cs
 * .NET Class: eShop.Webhooks.API.Apis.WebhooksApi
 *
 * REST API controller for webhook management.
 */
package com.eshop.webhooks.api;

import com.eshop.webhooks.dto.WebhookSubscriptionRequest;
import com.eshop.webhooks.model.WebhookSubscription;
import com.eshop.webhooks.repository.WebhookSubscriptionRepository;
import com.eshop.webhooks.service.GrantUrlTesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Webhook subscription management API")
public class WebhooksController {

    private final WebhookSubscriptionRepository repository;
    private final GrantUrlTesterService grantUrlTesterService;

    public WebhooksController(WebhookSubscriptionRepository repository, GrantUrlTesterService grantUrlTesterService) {
        this.repository = repository;
        this.grantUrlTesterService = grantUrlTesterService;
    }

    @GetMapping
    @Operation(summary = "Get subscriptions", description = "Get all webhook subscriptions for the current user")
    public ResponseEntity<List<WebhookSubscription>> getSubscriptions(Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        List<WebhookSubscription> subscriptions = repository.findByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription", description = "Get a webhook subscription by ID")
    public ResponseEntity<WebhookSubscription> getSubscription(@PathVariable Long id, Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        return repository.findById(id)
                .filter(sub -> sub.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create subscription", description = "Create a new webhook subscription")
    public ResponseEntity<?> createSubscription(
            @Valid @RequestBody WebhookSubscriptionRequest request,
            Principal principal) {
        if (!request.hasValidUrls()) {
            return ResponseEntity.badRequest().body("Invalid URL format");
        }

        String token = request.getToken() != null ? request.getToken() : "";
        boolean grantOk = grantUrlTesterService.testGrantUrl(request.getUrl(), request.getGrantUrl(), token);

        if (!grantOk) {
            return ResponseEntity.badRequest().body("Invalid grant URL: " + request.getGrantUrl());
        }

        String userId = principal != null ? principal.getName() : "anonymous";

        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setDestUrl(request.getUrl());
        subscription.setToken(request.getToken());
        subscription.setType(request.toWebhookType());
        subscription.setUserId(userId);
        subscription.setDescription(request.getEvent());

        WebhookSubscription saved = repository.save(subscription);
        return ResponseEntity.created(URI.create("/api/webhooks/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subscription", description = "Delete a webhook subscription")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id, Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        return repository.findById(id)
                .filter(sub -> sub.getUserId().equals(userId))
                .map(sub -> {
                    repository.delete(sub);
                    return ResponseEntity.accepted().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
