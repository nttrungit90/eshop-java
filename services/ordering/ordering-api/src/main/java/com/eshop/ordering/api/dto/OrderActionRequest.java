package com.eshop.ordering.api.dto;

/** Generic body for PUT /api/orders/ship and /api/orders/cancel. */
public class OrderActionRequest {
    private long orderNumber;

    public OrderActionRequest() {}
    public OrderActionRequest(long orderNumber) { this.orderNumber = orderNumber; }

    public long getOrderNumber() { return orderNumber; }
    public void setOrderNumber(long orderNumber) { this.orderNumber = orderNumber; }
}
