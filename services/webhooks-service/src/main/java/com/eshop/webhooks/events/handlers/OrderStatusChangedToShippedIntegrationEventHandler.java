package com.eshop.webhooks.events.handlers;

import com.eshop.eventbus.IntegrationEventHandler;
import com.eshop.webhooks.events.OrderStatusChangedToShippedIntegrationEvent;
import com.eshop.webhooks.model.WebhookData;
import com.eshop.webhooks.model.WebhookSubscription;
import com.eshop.webhooks.service.WebhooksRetriever;
import com.eshop.webhooks.service.WebhooksSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OrderStatusChangedToShippedIntegrationEventHandler
        implements IntegrationEventHandler<OrderStatusChangedToShippedIntegrationEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToShippedIntegrationEventHandler.class);

    private final WebhooksRetriever retriever;
    private final WebhooksSender sender;
    private final ObjectMapper objectMapper;

    public OrderStatusChangedToShippedIntegrationEventHandler(
            WebhooksRetriever retriever,
            WebhooksSender sender,
            ObjectMapper objectMapper) {
        this.retriever = retriever;
        this.sender = sender;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<Void> handle(OrderStatusChangedToShippedIntegrationEvent event) {
        return CompletableFuture.runAsync(() -> {
            log.info("Handling integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

            List<WebhookSubscription> subscriptions = retriever.getSubscriptionsByType(
                    WebhookSubscription.WebhookType.ORDER_SHIPPED);

            log.info("Received OrderStatusChangedToShippedIntegrationEvent and got {} subscriptions to process",
                    subscriptions.size());

            WebhookData data = WebhookData.create(WebhookSubscription.WebhookType.ORDER_SHIPPED, event, objectMapper);
            sender.sendAll(subscriptions, data);
        });
    }
}
