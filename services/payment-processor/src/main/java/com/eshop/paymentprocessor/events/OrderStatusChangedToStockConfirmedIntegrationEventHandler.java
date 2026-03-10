package com.eshop.paymentprocessor.events;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.eshop.paymentprocessor.config.PaymentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedToStockConfirmedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToStockConfirmedIntegrationEventHandler.class);

    private final EventBus eventBus;
    private final PaymentOptions paymentOptions;

    public OrderStatusChangedToStockConfirmedIntegrationEventHandler(EventBus eventBus, PaymentOptions paymentOptions) {
        this.eventBus = eventBus;
        this.paymentOptions = paymentOptions;
    }

    @RabbitListener(queues = "payment-processor_queue")
    public void handle(OrderStatusChangedToStockConfirmedIntegrationEvent event) {
        log.info("Handling integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

        IntegrationEvent orderPaymentIntegrationEvent;

        // Simulate payment processing against a payment gateway.
        // Instead of a real payment, use the config flag to simulate success/failure.
        if (paymentOptions.isPaymentSucceeded()) {
            orderPaymentIntegrationEvent = new OrderPaymentSucceededIntegrationEvent(event.getOrderId());
        } else {
            orderPaymentIntegrationEvent = new OrderPaymentFailedIntegrationEvent(event.getOrderId());
        }

        log.info("Publishing integration event: {} - ({})", orderPaymentIntegrationEvent.getId(),
                orderPaymentIntegrationEvent.getClass().getSimpleName());

        eventBus.publishAsync(orderPaymentIntegrationEvent);
    }
}
