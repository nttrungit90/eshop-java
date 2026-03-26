package com.eshop.webhooks.service;

import com.eshop.webhooks.model.WebhookSubscription;
import com.eshop.webhooks.repository.WebhookSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebhooksRetriever {

    private final WebhookSubscriptionRepository repository;

    public WebhooksRetriever(WebhookSubscriptionRepository repository) {
        this.repository = repository;
    }

    public List<WebhookSubscription> getSubscriptionsByType(WebhookSubscription.WebhookType type) {
        return repository.findByType(type);
    }
}
