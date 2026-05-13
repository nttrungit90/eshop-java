package com.eshop.ordering.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * List-shape matching the .NET WebApp's OrderRecord(int OrderNumber, DateTime Date, string Status, decimal Total).
 * Jackson serializes these camelCase fields; .NET's JsonSerializerDefaults.Web is case-insensitive on read.
 */
public class OrderSummaryDto {
    private long orderNumber;
    private Instant date;
    private String status;
    private BigDecimal total;

    public OrderSummaryDto() {}

    public OrderSummaryDto(long orderNumber, Instant date, String status, BigDecimal total) {
        this.orderNumber = orderNumber;
        this.date = date;
        this.status = status;
        this.total = total;
    }

    public long getOrderNumber() { return orderNumber; }
    public void setOrderNumber(long orderNumber) { this.orderNumber = orderNumber; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
