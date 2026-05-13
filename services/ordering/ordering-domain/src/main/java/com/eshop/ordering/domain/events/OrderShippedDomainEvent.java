package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.seedwork.DomainEvent;

/** Mirrors .NET OrderShippedDomainEvent. */
public class OrderShippedDomainEvent extends DomainEvent {
    private final Order order;

    public OrderShippedDomainEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() { return order; }
}
