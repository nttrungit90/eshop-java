/**
 * Converted from: src/Basket.API/Model/BasketItem.cs
 * .NET Class: eShop.Basket.API.Model.BasketItem
 *
 * Individual item in a customer's basket.
 */
package com.eshop.basket.model;

import java.math.BigDecimal;

public class BasketItem {

    private String id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal oldUnitPrice;
    private int quantity;
    private String pictureUrl;

    public BasketItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getOldUnitPrice() {
        return oldUnitPrice;
    }

    public void setOldUnitPrice(BigDecimal oldUnitPrice) {
        this.oldUnitPrice = oldUnitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
