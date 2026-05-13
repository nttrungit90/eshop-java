package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderStockConfirmedIntegrationEvent extends IntegrationEvent {
    private long orderId;
    public OrderStockConfirmedIntegrationEvent() {}
    public OrderStockConfirmedIntegrationEvent(long orderId) { this.orderId = orderId; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
}
