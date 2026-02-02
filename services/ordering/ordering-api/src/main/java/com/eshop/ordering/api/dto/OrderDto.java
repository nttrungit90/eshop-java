/**
 * Converted from: src/Ordering.API/Application/Queries/OrderViewModel.cs
 * .NET Class: eShop.Ordering.API.Application.Queries.OrderViewModel
 *
 * DTO for order responses.
 */
package com.eshop.ordering.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDto {

    private Long orderId;
    private Instant date;
    private String status;
    private String description;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private List<OrderItemDto> orderItems;
    private BigDecimal total;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public List<OrderItemDto> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDto> orderItems) { this.orderItems = orderItems; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public static class OrderItemDto {
        private String productName;
        private int units;
        private BigDecimal unitPrice;
        private String pictureUrl;

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
