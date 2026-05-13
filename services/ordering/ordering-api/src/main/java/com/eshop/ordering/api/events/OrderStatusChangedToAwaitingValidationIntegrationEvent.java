package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

import java.util.List;

public class OrderStatusChangedToAwaitingValidationIntegrationEvent extends IntegrationEvent {
    private long orderId;
    private List<OrderStockItem> orderStockItems;

    public OrderStatusChangedToAwaitingValidationIntegrationEvent() {}

    public OrderStatusChangedToAwaitingValidationIntegrationEvent(long orderId, List<OrderStockItem> orderStockItems) {
        this.orderId = orderId;
        this.orderStockItems = orderStockItems;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public List<OrderStockItem> getOrderStockItems() { return orderStockItems; }
    public void setOrderStockItems(List<OrderStockItem> orderStockItems) { this.orderStockItems = orderStockItems; }
}
