package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderStatusChangedToStockConfirmedIntegrationEvent extends IntegrationEvent {
    private long orderId;
    public OrderStatusChangedToStockConfirmedIntegrationEvent() {}
    public OrderStatusChangedToStockConfirmedIntegrationEvent(long orderId) { this.orderId = orderId; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
}
