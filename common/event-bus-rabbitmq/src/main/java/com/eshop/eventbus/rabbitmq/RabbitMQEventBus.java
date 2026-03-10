/**
 * Converted from: src/EventBusRabbitMQ/RabbitMQEventBus.cs
 * .NET Class: eShop.EventBusRabbitMQ.RabbitMQEventBus
 *
 * RabbitMQ implementation of the event bus.
 */
package com.eshop.eventbus.rabbitmq;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class RabbitMQEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventBus.class);

    private final AmqpTemplate amqpTemplate;

    public RabbitMQEventBus(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public CompletableFuture<Void> publishAsync(IntegrationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                String eventName = event.getClass().getSimpleName();

                log.info("Publishing event {} with id {}", eventName, event.getId());
                // Send event object directly — Jackson2JsonMessageConverter handles serialization.
                // Do NOT pre-serialize to String, as that causes double-encoding.
                amqpTemplate.convertAndSend("eshop_event_bus", eventName, event);
                log.info("Event {} published successfully", event.getId());
            } catch (Exception e) {
                log.error("Error publishing event {}: {}", event.getId(), e.getMessage(), e);
                throw new RuntimeException("Failed to publish event", e);
            }
        });
    }
}
