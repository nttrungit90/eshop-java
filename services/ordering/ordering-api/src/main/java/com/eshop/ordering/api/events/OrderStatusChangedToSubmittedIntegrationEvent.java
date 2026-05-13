package com.eshop.ordering.api.events;

import com.eshop.eventbus.IntegrationEvent;

/**
 * Published after the order is persisted, signaling it has entered the Submitted state.
 * Mirrors .NET eShop.Ordering.API.Application.IntegrationEvents.Events.OrderStatusChangedToSubmittedIntegrationEvent.
 */
public class OrderStatusChangedToSubmittedIntegrationEvent extends IntegrationEvent {

    private long orderId;
    private String orderStatus;
    private String buyerName;
    private String buyerIdentityGuid;

    public OrderStatusChangedToSubmittedIntegrationEvent() {}

    public OrderStatusChangedToSubmittedIntegrationEvent(long orderId, String orderStatus, String buyerName, String buyerIdentityGuid) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.buyerName = buyerName;
        this.buyerIdentityGuid = buyerIdentityGuid;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerIdentityGuid() { return buyerIdentityGuid; }
    public void setBuyerIdentityGuid(String buyerIdentityGuid) { this.buyerIdentityGuid = buyerIdentityGuid; }
}
