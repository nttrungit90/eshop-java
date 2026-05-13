package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.Command;
import com.eshop.ordering.api.dto.OrderDraftDto;

import java.math.BigDecimal;
import java.util.List;

/** Mirrors .NET CreateOrderDraftCommand. Returns a computed draft with total — no persistence. */
public final class CreateOrderDraftCommand implements Command<OrderDraftDto> {
    private final String buyerId;
    private final List<DraftItem> items;

    public CreateOrderDraftCommand(String buyerId, List<DraftItem> items) {
        this.buyerId = buyerId;
        this.items = items;
    }

    public String getBuyerId() { return buyerId; }
    public List<DraftItem> getItems() { return items; }

    public static final class DraftItem {
        private final long productId;
        private final String productName;
        private final BigDecimal unitPrice;
        private final BigDecimal discount;
        private final int quantity;
        private final String pictureUrl;

        public DraftItem(long productId, String productName, BigDecimal unitPrice,
                         BigDecimal discount, int quantity, String pictureUrl) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.quantity = quantity;
            this.pictureUrl = pictureUrl;
        }

        public long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getDiscount() { return discount; }
        public int getQuantity() { return quantity; }
        public String getPictureUrl() { return pictureUrl; }
    }
}
