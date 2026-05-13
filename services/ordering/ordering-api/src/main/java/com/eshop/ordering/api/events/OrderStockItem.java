package com.eshop.ordering.api.events;

public record OrderStockItem(int productId, int units) {
    public OrderStockItem() { this(0, 0); }
}
