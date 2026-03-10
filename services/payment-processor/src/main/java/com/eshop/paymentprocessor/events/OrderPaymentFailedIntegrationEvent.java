package com.eshop.paymentprocessor.events;

import com.eshop.eventbus.IntegrationEvent;

/**
 * Converted from: src/PaymentProcessor/IntegrationEvents/Events/OrderPaymentFailedIntegrationEvent.cs
 *
 * Published when payment for an order fails.
 */
public class OrderPaymentFailedIntegrationEvent extends IntegrationEvent {

    private int orderId;

    public OrderPaymentFailedIntegrationEvent() {
    }

    public OrderPaymentFailedIntegrationEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
