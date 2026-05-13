package com.eshop.ordering.domain.aggregates.buyer;

import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

import java.time.Instant;

@jakarta.persistence.Entity
@Table(name = "paymentmethods", schema = "ordering")
public class PaymentMethod extends Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paymentseq")
    @SequenceGenerator(name = "paymentseq", sequenceName = "ordering.paymentseq", allocationSize = 10)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Alias", nullable = false, length = 200)
    private String alias;

    @Column(name = "CardNumber", nullable = false, length = 25)
    private String cardNumber;

    // The .NET schema doesn't expose SecurityNumber as a column — it's part of the domain
    // but only used during VerifyOrAddPaymentMethod equality / domain-event payload. Skip persisting it.
    @Transient
    private String securityNumber;

    @Column(name = "CardHolderName", nullable = false, length = 200)
    private String cardHolderName;

    @Column(name = "Expiration", nullable = false)
    private Instant expiration;

    @Column(name = "CardTypeId", nullable = false)
    private int cardTypeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BuyerId", nullable = false)
    private Buyer buyer;

    protected PaymentMethod() {}

    public PaymentMethod(int cardTypeId, String alias, String cardNumber, String securityNumber,
                         String cardHolderName, Instant expiration) {
        if (cardNumber == null || cardNumber.isBlank()) throw new IllegalArgumentException("cardNumber");
        if (cardHolderName == null || cardHolderName.isBlank()) throw new IllegalArgumentException("cardHolderName");
        if (expiration == null || expiration.isBefore(Instant.now())) throw new IllegalArgumentException("expiration");

        this.cardTypeId = cardTypeId;
        this.alias = alias;
        this.cardNumber = cardNumber;
        this.securityNumber = securityNumber;
        this.cardHolderName = cardHolderName;
        this.expiration = expiration;
    }

    public boolean isEqualTo(int cardTypeId, String cardNumber, Instant expiration) {
        return this.cardTypeId == cardTypeId
                && this.cardNumber.equals(cardNumber)
                && this.expiration.equals(expiration);
    }

    void setBuyer(Buyer buyer) { this.buyer = buyer; }

    @Override public Long getId() { return id; }
    public int getCardTypeId() { return cardTypeId; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public Instant getExpiration() { return expiration; }
    public String getAlias() { return alias; }
    public Buyer getBuyer() { return buyer; }
}
