package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregates.buyer.Buyer;
import com.eshop.ordering.domain.aggregates.buyer.PaymentMethod;
import com.eshop.ordering.domain.seedwork.DomainEvent;

/**
 * Mirrors .NET BuyerAndPaymentMethodVerifiedDomainEvent — raised inside Buyer.verifyOrAddPaymentMethod()
 * once the buyer/payment graph is ready. The handler back-fills Order.BuyerId / Order.PaymentMethodId
 * for the in-flight order whose orderId is carried on the event.
 */
public class BuyerAndPaymentMethodVerifiedDomainEvent extends DomainEvent {
    private final Buyer buyer;
    private final PaymentMethod payment;
    private final long orderId;

    public BuyerAndPaymentMethodVerifiedDomainEvent(Buyer buyer, PaymentMethod payment, long orderId) {
        this.buyer = buyer;
        this.payment = payment;
        this.orderId = orderId;
    }

    public Buyer getBuyer() { return buyer; }
    public PaymentMethod getPayment() { return payment; }
    public long getOrderId() { return orderId; }
}
