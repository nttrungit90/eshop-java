package com.eshop.orderprocessor.events;

import com.eshop.eventbus.IntegrationEvent;

public class GracePeriodConfirmedIntegrationEvent extends IntegrationEvent {

    private int orderId;

    public GracePeriodConfirmedIntegrationEvent() {
    }

    public GracePeriodConfirmedIntegrationEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
