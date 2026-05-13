package com.eshop.ordering.domain.aggregates.order;

import com.eshop.ordering.domain.events.OrderCancelledDomainEvent;
import com.eshop.ordering.domain.events.OrderShippedDomainEvent;
import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToAwaitingValidationDomainEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToPaidDomainEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToStockConfirmedDomainEvent;
import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "orders", schema = "ordering")
public class Order extends Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderseq")
    @SequenceGenerator(name = "orderseq", sequenceName = "ordering.orderseq", allocationSize = 10)
    @Column(name = "Id")
    private Long id;

    @Column(name = "OrderDate", nullable = false)
    private Instant orderDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "Address_Street")),
            @AttributeOverride(name = "city", column = @Column(name = "Address_City")),
            @AttributeOverride(name = "state", column = @Column(name = "Address_State")),
            @AttributeOverride(name = "country", column = @Column(name = "Address_Country")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "Address_ZipCode"))
    })
    private Address address;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "OrderStatus", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "BuyerId")
    private Long buyerId;

    @Column(name = "Description")
    private String description;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "PaymentMethodId")
    private Long paymentMethodId;

    protected Order() {
        this.orderDate = Instant.now();
        this.status = OrderStatus.SUBMITTED;
    }

    /**
     * Mirrors .NET Order(userId, userName, address, cardTypeId, cardNumber, …) ctor.
     * <p>BuyerId and PaymentMethodId are NOT set here — they're back-filled by
     * {@code UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler} once the
     * {@code OrderStartedDomainEvent} → {@code ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler}
     * chain has persisted the Buyer + PaymentMethod aggregates.
     */
    public Order(String userId, String userName, Address address, int cardTypeId,
                 String cardNumber, String cardSecurityNumber, String cardHolderName,
                 Instant cardExpiration) {
        this();
        this.address = address;
        addDomainEvent(new OrderStartedDomainEvent(this, userId, userName, cardTypeId,
                cardNumber, cardSecurityNumber, cardHolderName, cardExpiration));
    }

    /** Called by UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler. */
    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    /** Called by UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler. */
    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
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
            addDomainEvent(new OrderStatusChangedToAwaitingValidationDomainEvent(getId(), orderItems));
            status = OrderStatus.AWAITING_VALIDATION;
        }
    }

    public void setStockConfirmedStatus() {
        if (status == OrderStatus.AWAITING_VALIDATION) {
            addDomainEvent(new OrderStatusChangedToStockConfirmedDomainEvent(getId()));
            status = OrderStatus.STOCK_CONFIRMED;
            description = "All items were confirmed with available stock.";
        }
    }

    public void setPaidStatus() {
        if (status == OrderStatus.STOCK_CONFIRMED) {
            addDomainEvent(new OrderStatusChangedToPaidDomainEvent(getId(), orderItems));
            status = OrderStatus.PAID;
            description = "The payment was performed at a simulated \"American Bank checking bank account ending on XX35071\"";
        }
    }

    public void setShippedStatus() {
        if (status != OrderStatus.PAID) throw new IllegalStateException("Cannot ship order that is not paid");
        addDomainEvent(new OrderShippedDomainEvent(this));
        status = OrderStatus.SHIPPED;
        description = "The order was shipped.";
    }

    public void setCancelledStatus() {
        if (status == OrderStatus.PAID || status == OrderStatus.SHIPPED)
            throw new IllegalStateException("Cannot cancel a paid or shipped order");
        addDomainEvent(new OrderCancelledDomainEvent(this));
        status = OrderStatus.CANCELLED;
        description = "The order was cancelled.";
    }

    /** Used by SetCancelledStatusWhenStockRejectedDomainEventHandler — overload that captures which items failed. */
    public void setCancelledStatusWhenStockIsRejected(java.util.List<Integer> orderStockRejectedItems) {
        if (status == OrderStatus.AWAITING_VALIDATION) {
            String itemsStockRejectedProductNames = orderItems.stream()
                    .filter(oi -> orderStockRejectedItems.contains(oi.getProductId().intValue()))
                    .map(OrderItem::getProductName)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            description = "The order was cancelled — items without stock: " + itemsStockRejectedProductNames;
            addDomainEvent(new OrderCancelledDomainEvent(this));
            status = OrderStatus.CANCELLED;
        }
    }

    public BigDecimal getTotal() {
        return orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override public Long getId() { return id; }
    public Instant getOrderDate() { return orderDate; }
    public Address getAddress() { return address; }
    public OrderStatus getStatus() { return status; }
    public Long getBuyerId() { return buyerId; }
    public String getDescription() { return description; }
    public List<OrderItem> getOrderItems() { return Collections.unmodifiableList(orderItems); }
    public Long getPaymentMethodId() { return paymentMethodId; }
}
