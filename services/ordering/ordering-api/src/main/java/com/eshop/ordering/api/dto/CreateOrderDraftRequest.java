package com.eshop.ordering.api.dto;

import java.math.BigDecimal;
import java.util.List;

/** JSON-friendly shape for POST /api/orders/draft (mutable, default ctor). */
public class CreateOrderDraftRequest {
    private String buyerId;
    private List<DraftItem> items;

    public CreateOrderDraftRequest() {}

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public List<DraftItem> getItems() { return items; }
    public void setItems(List<DraftItem> items) { this.items = items; }

    public static class DraftItem {
        private long productId;
        private String productName;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private int quantity;
        private String pictureUrl;

        public DraftItem() {}

        public long getProductId() { return productId; }
        public void setProductId(long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
