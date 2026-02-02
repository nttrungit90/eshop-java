/**
 * Converted from: src/Ordering.Domain/Events/OrderStartedDomainEvent.cs
 * .NET Class: eShop.Ordering.Domain.Events.OrderStartedDomainEvent
 *
 * Domain event raised when an order is started.
 */
package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.seedwork.DomainEvent;

import java.time.Instant;

public class OrderStartedDomainEvent extends DomainEvent {

    private final Order order;
    private final String userId;
    private final int cardTypeId;
    private final String cardNumber;
    private final String cardSecurityNumber;
    private final String cardHolderName;
    private final Instant cardExpiration;

    public OrderStartedDomainEvent(Order order, String userId, int cardTypeId,
                                   String cardNumber, String cardSecurityNumber,
                                   String cardHolderName, Instant cardExpiration) {
        super();
        this.order = order;
        this.userId = userId;
        this.cardTypeId = cardTypeId;
        this.cardNumber = cardNumber;
        this.cardSecurityNumber = cardSecurityNumber;
        this.cardHolderName = cardHolderName;
        this.cardExpiration = cardExpiration;
    }

    public Order getOrder() { return order; }
    public String getUserId() { return userId; }
    public int getCardTypeId() { return cardTypeId; }
    public String getCardNumber() { return cardNumber; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
}
