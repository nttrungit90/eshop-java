package com.eshop.paymentprocessor.events;

import com.eshop.eventbus.IntegrationEvent;

/**
 * Converted from: src/PaymentProcessor/IntegrationEvents/Events/OrderPaymentSucceededIntegrationEvent.cs
 *
 * Published when payment for an order succeeds.
 */
public class OrderPaymentSucceededIntegrationEvent extends IntegrationEvent {

    private int orderId;

    public OrderPaymentSucceededIntegrationEvent() {
    }

    public OrderPaymentSucceededIntegrationEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
