/**
 * Converted from: src/Ordering.Domain/AggregatesModel/OrderAggregate/OrderItem.cs
 * .NET Class: eShop.Ordering.Domain.AggregatesModel.OrderAggregate.OrderItem
 *
 * Entity representing an item in an order.
 */
package com.eshop.ordering.domain.aggregates.order;

import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@jakarta.persistence.Entity
@Table(name = "order_items")
public class OrderItem extends Entity {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false)
    private int units;

    private String pictureUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    protected OrderItem() {
    }

    public OrderItem(Long productId, String productName, BigDecimal unitPrice, BigDecimal discount, String pictureUrl, int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Invalid number of units");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid unit price");
        }

        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.pictureUrl = pictureUrl;
        this.units = units;
    }

    public void setNewDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount is not valid");
        }
        this.discount = discount;
    }

    public void addUnits(int units) {
        if (units < 0) {
            throw new IllegalArgumentException("Invalid units");
        }
        this.units += units;
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(units)).subtract(discount);
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getDiscount() { return discount; }
    public int getUnits() { return units; }
    public String getPictureUrl() { return pictureUrl; }
    public Order getOrder() { return order; }
    void setOrder(Order order) { this.order = order; }
}
