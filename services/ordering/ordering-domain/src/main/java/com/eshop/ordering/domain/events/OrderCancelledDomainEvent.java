package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.seedwork.DomainEvent;

/** Mirrors .NET OrderCancelledDomainEvent. */
public class OrderCancelledDomainEvent extends DomainEvent {
    private final Order order;

    public OrderCancelledDomainEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() { return order; }
}
