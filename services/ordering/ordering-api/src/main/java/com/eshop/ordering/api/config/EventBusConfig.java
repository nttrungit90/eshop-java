package com.eshop.ordering.api.config;

import com.eshop.eventbus.rabbitmq.EventBusSubscriptions;
import com.eshop.ordering.api.events.GracePeriodConfirmedIntegrationEvent;
import com.eshop.ordering.api.events.OrderPaymentFailedIntegrationEvent;
import com.eshop.ordering.api.events.OrderPaymentSucceededIntegrationEvent;
import com.eshop.ordering.api.events.OrderStockConfirmedIntegrationEvent;
import com.eshop.ordering.api.events.OrderStockRejectedIntegrationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

    @Bean
    public EventBusSubscriptions eventBusSubscriptions() {
        return new EventBusSubscriptions()
                .addSubscription(GracePeriodConfirmedIntegrationEvent.class)
                .addSubscription(OrderStockConfirmedIntegrationEvent.class)
                .addSubscription(OrderStockRejectedIntegrationEvent.class)
                .addSubscription(OrderPaymentSucceededIntegrationEvent.class)
                .addSubscription(OrderPaymentFailedIntegrationEvent.class);
    }
}
