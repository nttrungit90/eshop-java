package com.eshop.ordering.domain.aggregates.buyer;

import com.eshop.ordering.domain.events.BuyerAndPaymentMethodVerifiedDomainEvent;
import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "buyers", schema = "ordering")
public class Buyer extends Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buyerseq")
    @SequenceGenerator(name = "buyerseq", sequenceName = "ordering.buyerseq", allocationSize = 10)
    @Column(name = "Id")
    private Long id;

    @Column(name = "IdentityGuid", nullable = false, length = 200, unique = true)
    private String identityGuid;

    @Column(name = "Name")
    private String name;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    protected Buyer() {}

    public Buyer(String identityGuid, String name) {
        if (identityGuid == null || identityGuid.isBlank()) throw new IllegalArgumentException("identityGuid");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name");
        this.identityGuid = identityGuid;
        this.name = name;
    }

    /**
     * Mirrors .NET Buyer.VerifyOrAddPaymentMethod: returns the existing payment method if one with the same
     * (cardTypeId, cardNumber, expiration) already exists; otherwise adds and returns a new one.
     * <p>Raises {@link BuyerAndPaymentMethodVerifiedDomainEvent} so
     * {@code UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler} can back-fill the
     * in-flight order's {@code BuyerId}/{@code PaymentMethodId} once the buyer is persisted.
     */
    public PaymentMethod verifyOrAddPaymentMethod(int cardTypeId, String alias, String cardNumber,
                                                  String securityNumber, String cardHolderName,
                                                  Instant expiration, long orderId) {
        PaymentMethod existing = paymentMethods.stream()
                .filter(p -> p.isEqualTo(cardTypeId, cardNumber, expiration))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            addDomainEvent(new BuyerAndPaymentMethodVerifiedDomainEvent(this, existing, orderId));
            return existing;
        }
        PaymentMethod payment = new PaymentMethod(cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration);
        payment.setBuyer(this);
        paymentMethods.add(payment);
        addDomainEvent(new BuyerAndPaymentMethodVerifiedDomainEvent(this, payment, orderId));
        return payment;
    }

    @Override public Long getId() { return id; }
    public String getIdentityGuid() { return identityGuid; }
    public String getName() { return name; }
    public List<PaymentMethod> getPaymentMethods() { return Collections.unmodifiableList(paymentMethods); }
}
