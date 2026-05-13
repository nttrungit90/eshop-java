package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderPaymentSucceededIntegrationEvent extends IntegrationEvent {
    private int orderId;
    public OrderPaymentSucceededIntegrationEvent() {}
    public OrderPaymentSucceededIntegrationEvent(int orderId) { this.orderId = orderId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
}
