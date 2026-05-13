package com.eshop.ordering.domain.aggregates.order;

import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@jakarta.persistence.Entity
@Table(name = "orderItems", schema = "ordering")
public class OrderItem extends Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderitemseq")
    @SequenceGenerator(name = "orderitemseq", sequenceName = "ordering.orderitemseq", allocationSize = 10)
    @Column(name = "Id")
    private Long id;

    @Column(name = "ProductId", nullable = false)
    private Long productId;

    @Column(name = "ProductName", nullable = false, length = 200)
    private String productName;

    @Column(name = "UnitPrice", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "Discount", nullable = false)
    private BigDecimal discount;

    @Column(name = "Units", nullable = false)
    private int units;

    @Column(name = "PictureUrl")
    private String pictureUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId")
    private Order order;

    protected OrderItem() {}

    public OrderItem(Long productId, String productName, BigDecimal unitPrice, BigDecimal discount, String pictureUrl, int units) {
        if (units <= 0) throw new IllegalArgumentException("Invalid number of units");
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Invalid unit price");

        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.pictureUrl = pictureUrl;
        this.units = units;
    }

    public void setNewDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Discount is not valid");
        this.discount = discount;
    }

    public void addUnits(int units) {
        if (units < 0) throw new IllegalArgumentException("Invalid units");
        this.units += units;
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(units)).subtract(discount);
    }

    @Override public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getDiscount() { return discount; }
    public int getUnits() { return units; }
    public String getPictureUrl() { return pictureUrl; }
    public Order getOrder() { return order; }
    void setOrder(Order order) { this.order = order; }
}
