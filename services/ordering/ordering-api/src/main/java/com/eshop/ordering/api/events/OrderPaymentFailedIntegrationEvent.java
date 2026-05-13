package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderPaymentFailedIntegrationEvent extends IntegrationEvent {
    private int orderId;
    public OrderPaymentFailedIntegrationEvent() {}
    public OrderPaymentFailedIntegrationEvent(int orderId) { this.orderId = orderId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
}
