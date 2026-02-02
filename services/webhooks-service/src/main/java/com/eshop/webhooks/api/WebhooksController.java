/**
 * Converted from: src/Webhooks.API/Apis/WebhooksApi.cs
 * .NET Class: eShop.Webhooks.API.Apis.WebhooksApi
 *
 * REST API controller for webhook management.
 */
package com.eshop.webhooks.api;

import com.eshop.webhooks.model.WebhookSubscription;
import com.eshop.webhooks.repository.WebhookSubscriptionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Webhook subscription management API")
public class WebhooksController {

    private final WebhookSubscriptionRepository repository;

    public WebhooksController(WebhookSubscriptionRepository repository) {
        this.repository = repository;
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
    public ResponseEntity<WebhookSubscription> getSubscription(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create subscription", description = "Create a new webhook subscription")
    public ResponseEntity<WebhookSubscription> createSubscription(
            @RequestBody WebhookSubscription subscription,
            Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        subscription.setUserId(userId);
        WebhookSubscription saved = repository.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subscription", description = "Delete a webhook subscription")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id, Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        return repository.findById(id)
                .filter(sub -> sub.getUserId().equals(userId))
                .map(sub -> {
                    repository.delete(sub);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
