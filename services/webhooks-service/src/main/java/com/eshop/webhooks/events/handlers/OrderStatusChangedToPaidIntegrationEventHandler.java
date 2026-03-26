package com.eshop.webhooks.events.handlers;

import com.eshop.eventbus.IntegrationEventHandler;
import com.eshop.webhooks.events.OrderStatusChangedToPaidIntegrationEvent;
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
public class OrderStatusChangedToPaidIntegrationEventHandler
        implements IntegrationEventHandler<OrderStatusChangedToPaidIntegrationEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToPaidIntegrationEventHandler.class);

    private final WebhooksRetriever retriever;
    private final WebhooksSender sender;
    private final ObjectMapper objectMapper;

    public OrderStatusChangedToPaidIntegrationEventHandler(
            WebhooksRetriever retriever,
            WebhooksSender sender,
            ObjectMapper objectMapper) {
        this.retriever = retriever;
        this.sender = sender;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<Void> handle(OrderStatusChangedToPaidIntegrationEvent event) {
        return CompletableFuture.runAsync(() -> {
            log.info("Handling integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

            List<WebhookSubscription> subscriptions = retriever.getSubscriptionsByType(
                    WebhookSubscription.WebhookType.ORDER_PAID);

            log.info("Received OrderStatusChangedToPaidIntegrationEvent and got {} subscriptions to process",
                    subscriptions.size());

            WebhookData data = WebhookData.create(WebhookSubscription.WebhookType.ORDER_PAID, event, objectMapper);
            sender.sendAll(subscriptions, data);
        });
    }
}
