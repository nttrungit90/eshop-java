package com.eshop.ordering.api.dto;

import java.math.BigDecimal;
import java.util.List;

/** Mirrors .NET OrderDraftDTO. */
public class OrderDraftDto {
    private List<Item> orderItems;
    private BigDecimal total;

    public OrderDraftDto() {}
    public OrderDraftDto(List<Item> orderItems, BigDecimal total) {
        this.orderItems = orderItems;
        this.total = total;
    }

    public List<Item> getOrderItems() { return orderItems; }
    public void setOrderItems(List<Item> orderItems) { this.orderItems = orderItems; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public static class Item {
        private long productId;
        private String productName;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private int units;
        private String pictureUrl;

        public Item() {}
        public Item(long productId, String productName, BigDecimal unitPrice, BigDecimal discount, int units, String pictureUrl) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.units = units;
            this.pictureUrl = pictureUrl;
        }

        public long getProductId() { return productId; }
        public void setProductId(long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }
        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
