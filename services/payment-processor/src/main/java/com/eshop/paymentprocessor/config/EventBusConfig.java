package com.eshop.paymentprocessor.config;

import com.eshop.eventbus.rabbitmq.EventBusSubscriptions;
import com.eshop.paymentprocessor.events.OrderStatusChangedToStockConfirmedIntegrationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

    @Bean
    public EventBusSubscriptions eventBusSubscriptions() {
        return new EventBusSubscriptions()
                .addSubscription(OrderStatusChangedToStockConfirmedIntegrationEvent.class);
    }
}
