/**
 * Converted from: src/Catalog.API/IntegrationEvents/Events/OrderStatusChangedToPaidIntegrationEvent.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.Events.OrderStatusChangedToPaidIntegrationEvent
 *
 * Integration event published when an order status changes to paid.
 */
package com.eshop.catalog.events;

import com.eshop.catalog.model.OrderStockItem;
import com.eshop.eventbus.IntegrationEvent;

import java.util.List;

public class OrderStatusChangedToPaidIntegrationEvent extends IntegrationEvent {

    private final long orderId;
    private final List<OrderStockItem> orderStockItems;

    public OrderStatusChangedToPaidIntegrationEvent(long orderId, List<OrderStockItem> orderStockItems) {
        this.orderId = orderId;
        this.orderStockItems = orderStockItems;
    }

    public long getOrderId() {
        return orderId;
    }

    public List<OrderStockItem> getOrderStockItems() {
        return orderStockItems;
    }
}
