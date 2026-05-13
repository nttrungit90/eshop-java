package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

/**
 * Published when an order is created. Consumed by basket-service to clear the buyer's basket.
 * Mirrors .NET eShop.Ordering.API.Application.IntegrationEvents.Events.OrderStartedIntegrationEvent.
 */
public class OrderStartedIntegrationEvent extends IntegrationEvent {

    private String userId;

    public OrderStartedIntegrationEvent() {}

    public OrderStartedIntegrationEvent(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
