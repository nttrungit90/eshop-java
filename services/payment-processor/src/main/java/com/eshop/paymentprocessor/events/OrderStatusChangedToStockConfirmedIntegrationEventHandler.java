package com.eshop.paymentprocessor.events;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.eshop.paymentprocessor.config.PaymentOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedToStockConfirmedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToStockConfirmedIntegrationEventHandler.class);

    private final EventBus eventBus;
    private final PaymentOptions paymentOptions;
    private final ObjectMapper objectMapper;

    public OrderStatusChangedToStockConfirmedIntegrationEventHandler(EventBus eventBus, PaymentOptions paymentOptions, @Qualifier("eventBusObjectMapper") ObjectMapper objectMapper) {
        this.eventBus = eventBus;
        this.paymentOptions = paymentOptions;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "payment-processor_queue")
    public void onMessage(Message message) {
        try {
            String body = new String(message.getBody());
            var event = objectMapper.readValue(body, OrderStatusChangedToStockConfirmedIntegrationEvent.class);

            log.info("Handling integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

            IntegrationEvent orderPaymentIntegrationEvent;

            if (paymentOptions.isPaymentSucceeded()) {
                orderPaymentIntegrationEvent = new OrderPaymentSucceededIntegrationEvent(event.getOrderId());
            } else {
                orderPaymentIntegrationEvent = new OrderPaymentFailedIntegrationEvent(event.getOrderId());
            }

            log.info("Publishing integration event: {} - ({})", orderPaymentIntegrationEvent.getId(),
                    orderPaymentIntegrationEvent.getClass().getSimpleName());

            eventBus.publishAsync(orderPaymentIntegrationEvent);
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
        }
    }
}
