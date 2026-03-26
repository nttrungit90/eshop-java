package com.eshop.webhooks.events.handlers;

import com.eshop.webhooks.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.webhooks.events.OrderStatusChangedToShippedIntegrationEvent;
import com.eshop.webhooks.events.ProductPriceChangedIntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class WebhooksEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebhooksEventListener.class);

    private final ObjectMapper objectMapper;
    private final OrderStatusChangedToPaidIntegrationEventHandler paidHandler;
    private final OrderStatusChangedToShippedIntegrationEventHandler shippedHandler;
    private final ProductPriceChangedIntegrationEventHandler priceChangedHandler;

    public WebhooksEventListener(
            @Qualifier("eventBusObjectMapper") ObjectMapper objectMapper,
            OrderStatusChangedToPaidIntegrationEventHandler paidHandler,
            OrderStatusChangedToShippedIntegrationEventHandler shippedHandler,
            ProductPriceChangedIntegrationEventHandler priceChangedHandler) {
        this.objectMapper = objectMapper;
        this.paidHandler = paidHandler;
        this.shippedHandler = shippedHandler;
        this.priceChangedHandler = priceChangedHandler;
    }

    @RabbitListener(queues = "webhooks-service_queue")
    public void onMessage(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String body = new String(message.getBody());

        log.info("Received event: {} from queue", routingKey);

        try {
            switch (routingKey) {
                case "OrderStatusChangedToPaidIntegrationEvent" -> {
                    var event = objectMapper.readValue(body, OrderStatusChangedToPaidIntegrationEvent.class);
                    paidHandler.handle(event);
                }
                case "OrderStatusChangedToShippedIntegrationEvent" -> {
                    var event = objectMapper.readValue(body, OrderStatusChangedToShippedIntegrationEvent.class);
                    shippedHandler.handle(event);
                }
                case "ProductPriceChangedIntegrationEvent" -> {
                    var event = objectMapper.readValue(body, ProductPriceChangedIntegrationEvent.class);
                    priceChangedHandler.handle(event);
                }
                default -> log.warn("Unknown event type: {}", routingKey);
            }
        } catch (Exception e) {
            log.error("Error processing event {}: {}", routingKey, e.getMessage(), e);
        }
    }
}
