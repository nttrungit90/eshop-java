package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregates.order.OrderItem;
import com.eshop.ordering.domain.seedwork.DomainEvent;

import java.util.List;

/** Mirrors .NET OrderStatusChangedToPaidDomainEvent. */
public class OrderStatusChangedToPaidDomainEvent extends DomainEvent {
    private final long orderId;
    private final List<OrderItem> orderItems;

    public OrderStatusChangedToPaidDomainEvent(long orderId, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.orderItems = orderItems;
    }

    public long getOrderId() { return orderId; }
    public List<OrderItem> getOrderItems() { return orderItems; }
}
