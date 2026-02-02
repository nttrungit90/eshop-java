/**
 * Converted from: src/Ordering.Domain/AggregatesModel/OrderAggregate/OrderStatus.cs
 * .NET Class: eShop.Ordering.Domain.AggregatesModel.OrderAggregate.OrderStatus
 *
 * Order status enumeration.
 */
package com.eshop.ordering.domain.aggregates.order;

public enum OrderStatus {
    SUBMITTED(1, "Submitted"),
    AWAITING_VALIDATION(2, "Awaiting Validation"),
    STOCK_CONFIRMED(3, "Stock Confirmed"),
    PAID(4, "Paid"),
    SHIPPED(5, "Shipped"),
    CANCELLED(6, "Cancelled");

    private final int id;
    private final String name;

    OrderStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static OrderStatus fromId(int id) {
        for (OrderStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status id: " + id);
    }
}
