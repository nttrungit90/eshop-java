package com.eshop.catalog.config;

import com.eshop.catalog.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import com.eshop.catalog.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.eventbus.rabbitmq.EventBusSubscriptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

    @Bean
    public EventBusSubscriptions eventBusSubscriptions() {
        return new EventBusSubscriptions()
                .addSubscription(OrderStatusChangedToAwaitingValidationIntegrationEvent.class)
                .addSubscription(OrderStatusChangedToPaidIntegrationEvent.class);
    }
}
