package com.eshop.paymentprocessor.events;

import com.eshop.eventbus.IntegrationEvent;

/**
 * Converted from: src/PaymentProcessor/IntegrationEvents/Events/OrderStatusChangedToStockConfirmedIntegrationEvent.cs
 *
 * Event received when an order's stock has been confirmed.
 * Triggers payment processing.
 */
public class OrderStatusChangedToStockConfirmedIntegrationEvent extends IntegrationEvent {

    private int orderId;

    public OrderStatusChangedToStockConfirmedIntegrationEvent() {
    }

    public OrderStatusChangedToStockConfirmedIntegrationEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
