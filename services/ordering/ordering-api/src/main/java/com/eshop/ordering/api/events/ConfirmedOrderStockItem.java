package com.eshop.ordering.api.events;

public record ConfirmedOrderStockItem(int productId, boolean hasStock) {
    public ConfirmedOrderStockItem() { this(0, false); }
}
