package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.seedwork.DomainEvent;

import java.time.Instant;

/**
 * Mirrors .NET OrderStartedDomainEvent. Raised from {@code Order}'s constructor;
 * picked up by {@code ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler}
 * which uses {@code userId}/{@code userName} to find-or-create a {@code Buyer}
 * and the card details to register a {@code PaymentMethod}.
 */
public class OrderStartedDomainEvent extends DomainEvent {

    private final Order order;
    private final String userId;
    private final String userName;
    private final int cardTypeId;
    private final String cardNumber;
    private final String cardSecurityNumber;
    private final String cardHolderName;
    private final Instant cardExpiration;

    public OrderStartedDomainEvent(Order order, String userId, String userName, int cardTypeId,
                                   String cardNumber, String cardSecurityNumber,
                                   String cardHolderName, Instant cardExpiration) {
        this.order = order;
        this.userId = userId;
        this.userName = userName;
        this.cardTypeId = cardTypeId;
        this.cardNumber = cardNumber;
        this.cardSecurityNumber = cardSecurityNumber;
        this.cardHolderName = cardHolderName;
        this.cardExpiration = cardExpiration;
    }

    public Order getOrder() { return order; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getCardTypeId() { return cardTypeId; }
    public String getCardNumber() { return cardNumber; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
}
