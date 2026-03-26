package com.eshop.webhooks.events;

import com.eshop.eventbus.IntegrationEvent;

import java.util.List;

public class OrderStatusChangedToPaidIntegrationEvent extends IntegrationEvent {

    private long orderId;
    private List<OrderStockItem> orderStockItems;

    public OrderStatusChangedToPaidIntegrationEvent() {
    }

    public OrderStatusChangedToPaidIntegrationEvent(long orderId, List<OrderStockItem> orderStockItems) {
        this.orderId = orderId;
        this.orderStockItems = orderStockItems;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public List<OrderStockItem> getOrderStockItems() { return orderStockItems; }
    public void setOrderStockItems(List<OrderStockItem> orderStockItems) { this.orderStockItems = orderStockItems; }
}
