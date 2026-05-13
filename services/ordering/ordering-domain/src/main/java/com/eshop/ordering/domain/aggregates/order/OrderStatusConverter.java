package com.eshop.ordering.domain.aggregates.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        if (status == null) return null;
        return switch (status) {
            case SUBMITTED -> "Submitted";
            case AWAITING_VALIDATION -> "AwaitingValidation";
            case STOCK_CONFIRMED -> "StockConfirmed";
            case PAID -> "Paid";
            case SHIPPED -> "Shipped";
            case CANCELLED -> "Cancelled";
        };
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isEmpty()) return null;
        return switch (dbValue) {
            case "Submitted" -> OrderStatus.SUBMITTED;
            case "AwaitingValidation" -> OrderStatus.AWAITING_VALIDATION;
            case "StockConfirmed" -> OrderStatus.STOCK_CONFIRMED;
            case "Paid" -> OrderStatus.PAID;
            case "Shipped" -> OrderStatus.SHIPPED;
            case "Cancelled" -> OrderStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Unknown OrderStatus from DB: " + dbValue);
        };
    }
}
