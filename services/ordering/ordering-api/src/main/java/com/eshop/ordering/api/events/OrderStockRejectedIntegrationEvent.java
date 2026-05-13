package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

import java.util.List;

public class OrderStockRejectedIntegrationEvent extends IntegrationEvent {
    private long orderId;
    private List<ConfirmedOrderStockItem> orderStockItems;

    public OrderStockRejectedIntegrationEvent() {}

    public OrderStockRejectedIntegrationEvent(long orderId, List<ConfirmedOrderStockItem> orderStockItems) {
        this.orderId = orderId;
        this.orderStockItems = orderStockItems;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public List<ConfirmedOrderStockItem> getOrderStockItems() { return orderStockItems; }
    public void setOrderStockItems(List<ConfirmedOrderStockItem> orderStockItems) { this.orderStockItems = orderStockItems; }
}
