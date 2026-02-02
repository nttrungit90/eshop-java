/**
 * Converted from: src/Catalog.API/IntegrationEvents/Events/OrderStockRejectedIntegrationEvent.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.Events.OrderStockRejectedIntegrationEvent
 *
 * Integration event published when order stock is rejected.
 */
package com.eshop.catalog.events;

import com.eshop.catalog.model.ConfirmedOrderStockItem;
import com.eshop.eventbus.IntegrationEvent;

import java.util.List;

public class OrderStockRejectedIntegrationEvent extends IntegrationEvent {

    private final long orderId;
    private final List<ConfirmedOrderStockItem> orderStockItems;

    public OrderStockRejectedIntegrationEvent(long orderId, List<ConfirmedOrderStockItem> orderStockItems) {
        this.orderId = orderId;
        this.orderStockItems = orderStockItems;
    }

    public long getOrderId() {
        return orderId;
    }

    public List<ConfirmedOrderStockItem> getOrderStockItems() {
        return orderStockItems;
    }
}
