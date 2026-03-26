package com.eshop.webhooks.events;

import com.eshop.eventbus.IntegrationEvent;

public class OrderStatusChangedToShippedIntegrationEvent extends IntegrationEvent {

    private long orderId;
    private String orderStatus;
    private String buyerName;

    public OrderStatusChangedToShippedIntegrationEvent() {
    }

    public OrderStatusChangedToShippedIntegrationEvent(long orderId, String orderStatus, String buyerName) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.buyerName = buyerName;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
}
