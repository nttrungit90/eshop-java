/**
 * Converted from: src/Ordering.Domain/AggregatesModel/OrderAggregate/Order.cs
 * .NET Class: eShop.Ordering.Domain.AggregatesModel.OrderAggregate.Order
 *
 * Order aggregate root.
 */
package com.eshop.ordering.domain.aggregates.order;

import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "orders")
public class Order extends Entity {

    @Column(nullable = false)
    private Instant orderDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "address_street")),
            @AttributeOverride(name = "city", column = @Column(name = "address_city")),
            @AttributeOverride(name = "state", column = @Column(name = "address_state")),
            @AttributeOverride(name = "country", column = @Column(name = "address_country")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "address_zip_code"))
    })
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String buyerId;

    private String description;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Long paymentMethodId;

    protected Order() {
        this.orderDate = Instant.now();
        this.status = OrderStatus.SUBMITTED;
    }

    public Order(String buyerId, Address address, int cardTypeId, String cardNumber,
                 String cardSecurityNumber, String cardHolderName, Instant cardExpiration,
                 Long paymentMethodId) {
        this();
        this.buyerId = buyerId;
        this.address = address;
        this.paymentMethodId = paymentMethodId;

        // Add domain event for order started
        addDomainEvent(new OrderStartedDomainEvent(this, buyerId, cardTypeId, cardNumber,
                cardSecurityNumber, cardHolderName, cardExpiration));
    }

    public void addOrderItem(Long productId, String productName, BigDecimal unitPrice,
                             BigDecimal discount, String pictureUrl, int units) {
        OrderItem existingItem = orderItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            if (discount.compareTo(existingItem.getDiscount()) > 0) {
                existingItem.setNewDiscount(discount);
            }
            existingItem.addUnits(units);
        } else {
            OrderItem orderItem = new OrderItem(productId, productName, unitPrice, discount, pictureUrl, units);
            orderItem.setOrder(this);
            orderItems.add(orderItem);
        }
    }

    public void setAwaitingValidationStatus() {
        if (status == OrderStatus.SUBMITTED) {
            status = OrderStatus.AWAITING_VALIDATION;
        }
    }

    public void setStockConfirmedStatus() {
        if (status == OrderStatus.AWAITING_VALIDATION) {
            status = OrderStatus.STOCK_CONFIRMED;
            description = "All items were confirmed with available stock.";
        }
    }

    public void setPaidStatus() {
        if (status == OrderStatus.STOCK_CONFIRMED) {
            status = OrderStatus.PAID;
            description = "The payment was performed at a]simulation]";
        }
    }

    public void setShippedStatus() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Cannot ship order that is not paid");
        }
        status = OrderStatus.SHIPPED;
        description = "The order was shipped.";
    }

    public void setCancelledStatus() {
        if (status == OrderStatus.PAID || status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel a paid or shipped order");
        }
        status = OrderStatus.CANCELLED;
        description = "The order was cancelled.";
    }

    public BigDecimal getTotal() {
        return orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Instant getOrderDate() { return orderDate; }
    public Address getAddress() { return address; }
    public OrderStatus getStatus() { return status; }
    public String getBuyerId() { return buyerId; }
    public String getDescription() { return description; }
    public List<OrderItem> getOrderItems() { return Collections.unmodifiableList(orderItems); }
    public Long getPaymentMethodId() { return paymentMethodId; }
}
