/**
 * Converted from: src/EventBus/Abstractions/EventBusSubscriptionInfo.cs
 * .NET Class: eShop.EventBus.Abstractions.EventBusSubscriptionInfo
 *
 * Information about an event bus subscription.
 */
package com.eshop.eventbus;

public class EventBusSubscriptionInfo {

    private final Class<? extends IntegrationEvent> eventType;
    private final Class<? extends IntegrationEventHandler<?>> handlerType;

    public EventBusSubscriptionInfo(
            Class<? extends IntegrationEvent> eventType,
            Class<? extends IntegrationEventHandler<?>> handlerType) {
        this.eventType = eventType;
        this.handlerType = handlerType;
    }

    public Class<? extends IntegrationEvent> getEventType() {
        return eventType;
    }

    public Class<? extends IntegrationEventHandler<?>> getHandlerType() {
        return handlerType;
    }
}
