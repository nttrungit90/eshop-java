/**
 * Converted from: src/EventBusRabbitMQ/RabbitMQEventBus.cs
 * .NET Class: eShop.EventBusRabbitMQ.RabbitMQEventBus
 *
 * RabbitMQ implementation of the event bus.
 */
package com.eshop.eventbus.rabbitmq;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class RabbitMQEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventBus.class);

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMQEventBus(AmqpTemplate amqpTemplate, ObjectMapper objectMapper) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<Void> publishAsync(IntegrationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                String eventName = event.getClass().getSimpleName();
                String message = objectMapper.writeValueAsString(event);

                log.info("Publishing event {} with id {}", eventName, event.getId());
                amqpTemplate.convertAndSend("eshop_event_bus", eventName, message);
                log.info("Event {} published successfully", event.getId());
            } catch (Exception e) {
                log.error("Error publishing event {}: {}", event.getId(), e.getMessage(), e);
                throw new RuntimeException("Failed to publish event", e);
            }
        });
    }
}
