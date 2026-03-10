package com.eshop.eventbus.rabbitmq;

import com.eshop.eventbus.IntegrationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Declares which integration events a service subscribes to.
 * Each service should provide a bean of this type listing its event classes.
 * The event class simple names are used as RabbitMQ routing keys on the direct exchange.
 */
public class EventBusSubscriptions {

    private final List<String> eventNames = new ArrayList<>();

    public EventBusSubscriptions addSubscription(Class<? extends IntegrationEvent> eventType) {
        eventNames.add(eventType.getSimpleName());
        return this;
    }

    public List<String> getEventNames() {
        return eventNames;
    }
}
