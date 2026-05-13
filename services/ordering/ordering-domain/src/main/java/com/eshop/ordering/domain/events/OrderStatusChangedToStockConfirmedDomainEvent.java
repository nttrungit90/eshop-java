package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.seedwork.DomainEvent;

/** Mirrors .NET OrderStatusChangedToStockConfirmedDomainEvent. */
public class OrderStatusChangedToStockConfirmedDomainEvent extends DomainEvent {
    private final long orderId;

    public OrderStatusChangedToStockConfirmedDomainEvent(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() { return orderId; }
}
