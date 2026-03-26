package com.eshop.webhooks.config;

import com.eshop.eventbus.rabbitmq.EventBusSubscriptions;
import com.eshop.webhooks.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.webhooks.events.OrderStatusChangedToShippedIntegrationEvent;
import com.eshop.webhooks.events.ProductPriceChangedIntegrationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

    @Bean
    public EventBusSubscriptions eventBusSubscriptions() {
        return new EventBusSubscriptions()
                .addSubscription(OrderStatusChangedToPaidIntegrationEvent.class)
                .addSubscription(OrderStatusChangedToShippedIntegrationEvent.class)
                .addSubscription(ProductPriceChangedIntegrationEvent.class);
    }
}
