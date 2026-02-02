/**
 * Converted from: src/Ordering.API/Application/Commands/CreateOrderCommand.cs
 * .NET Class: eShop.Ordering.API.Application.Commands.CreateOrderCommand
 *
 * DTO for creating a new order.
 */
package com.eshop.ordering.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public class CreateOrderRequest {

    @NotBlank
    private String city;

    @NotBlank
    private String street;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @NotBlank
    private String zipCode;

    @NotBlank
    private String cardNumber;

    @NotBlank
    private String cardHolderName;

    private Instant cardExpiration;

    @NotBlank
    private String cardSecurityNumber;

    private int cardTypeId;

    @NotEmpty
    private List<OrderItemDto> items;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
    public void setCardExpiration(Instant cardExpiration) { this.cardExpiration = cardExpiration; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public void setCardSecurityNumber(String cardSecurityNumber) { this.cardSecurityNumber = cardSecurityNumber; }
    public int getCardTypeId() { return cardTypeId; }
    public void setCardTypeId(int cardTypeId) { this.cardTypeId = cardTypeId; }
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public static class OrderItemDto {
        private Long productId;
        private String productName;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal discount;
        private int units;
        private String pictureUrl;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public java.math.BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public java.math.BigDecimal getDiscount() { return discount; }
        public void setDiscount(java.math.BigDecimal discount) { this.discount = discount; }
        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }
        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
