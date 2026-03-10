package com.eshop.catalog.events.handlers;

import com.eshop.catalog.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import com.eshop.catalog.events.OrderStatusChangedToPaidIntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener for the catalog-service queue.
 * Dispatches messages to the appropriate handler based on the routing key (event name).
 */
@Component
public class CatalogEventListener {

    private static final Logger log = LoggerFactory.getLogger(CatalogEventListener.class);

    private final ObjectMapper objectMapper;
    private final OrderStatusChangedToAwaitingValidationIntegrationEventHandler awaitingValidationHandler;
    private final OrderStatusChangedToPaidIntegrationEventHandler paidHandler;

    public CatalogEventListener(
            ObjectMapper objectMapper,
            OrderStatusChangedToAwaitingValidationIntegrationEventHandler awaitingValidationHandler,
            OrderStatusChangedToPaidIntegrationEventHandler paidHandler) {
        this.objectMapper = objectMapper;
        this.awaitingValidationHandler = awaitingValidationHandler;
        this.paidHandler = paidHandler;
    }

    @RabbitListener(queues = "catalog-service_queue")
    public void onMessage(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String body = new String(message.getBody());

        log.info("Received event: {} from queue", routingKey);

        try {
            switch (routingKey) {
                case "OrderStatusChangedToAwaitingValidationIntegrationEvent" -> {
                    var event = objectMapper.readValue(body, OrderStatusChangedToAwaitingValidationIntegrationEvent.class);
                    awaitingValidationHandler.handle(event);
                }
                case "OrderStatusChangedToPaidIntegrationEvent" -> {
                    var event = objectMapper.readValue(body, OrderStatusChangedToPaidIntegrationEvent.class);
                    paidHandler.handle(event);
                }
                default -> log.warn("Unknown event type: {}", routingKey);
            }
        } catch (Exception e) {
            log.error("Error processing event {}: {}", routingKey, e.getMessage(), e);
        }
    }
}
