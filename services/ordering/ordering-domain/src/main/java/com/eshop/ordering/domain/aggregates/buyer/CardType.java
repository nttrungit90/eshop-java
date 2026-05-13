package com.eshop.ordering.domain.aggregates.buyer;

import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

/**
 * Reference data — only used by GET /api/orders/cardtypes and PaymentMethod FK.
 * Mirrors .NET CardType enumeration class.
 */
@jakarta.persistence.Entity
@Table(name = "cardtypes", schema = "ordering")
public class CardType extends Entity {

    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    protected CardType() {}

    public CardType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public Long getId() { return id; }
    public String getName() { return name; }
}
