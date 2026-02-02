/**
 * Converted from: src/PaymentProcessor/IntegrationEvents/OrderStatusChangedToStockConfirmedIntegrationEventHandler.cs
 * .NET Class: eShop.PaymentProcessor event handlers
 *
 * Event handler for processing payments.
 */
package com.eshop.paymentprocessor.events;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedToPaidEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToPaidEventHandler.class);

    private final EventBus eventBus;

    public OrderStatusChangedToPaidEventHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @RabbitListener(queues = "payment-processor_queue")
    public void handleOrderStockConfirmed(String message) {
        log.info("Received stock confirmed event: {}", message);

        // Simulate payment processing
        try {
            Thread.sleep(500); // Simulate processing time
            log.info("Payment processed successfully");

            // In a real implementation, publish OrderPaidIntegrationEvent
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
        }
    }
}
