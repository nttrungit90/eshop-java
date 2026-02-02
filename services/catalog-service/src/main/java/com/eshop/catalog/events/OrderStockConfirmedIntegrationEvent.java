/**
 * Converted from: src/Catalog.API/IntegrationEvents/Events/OrderStockConfirmedIntegrationEvent.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.Events.OrderStockConfirmedIntegrationEvent
 *
 * Integration event published when order stock is confirmed.
 */
package com.eshop.catalog.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderStockConfirmedIntegrationEvent extends IntegrationEvent {

    private final long orderId;

    public OrderStockConfirmedIntegrationEvent(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }
}
