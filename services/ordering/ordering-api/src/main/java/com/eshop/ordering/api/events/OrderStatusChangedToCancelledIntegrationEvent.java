package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderStatusChangedToCancelledIntegrationEvent extends IntegrationEvent {
    private long orderId;
    private String orderStatus;

    public OrderStatusChangedToCancelledIntegrationEvent() {}
    public OrderStatusChangedToCancelledIntegrationEvent(long orderId, String orderStatus) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}
