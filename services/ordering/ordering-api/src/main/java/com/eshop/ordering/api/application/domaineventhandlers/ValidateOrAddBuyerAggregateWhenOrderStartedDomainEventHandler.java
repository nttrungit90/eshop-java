package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.domain.aggregates.buyer.Buyer;
import com.eshop.ordering.domain.aggregates.buyer.BuyerRepository;
import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Mirrors .NET ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler.
 *
 * <p>Fires when a new {@code Order} aggregate raises {@code OrderStartedDomainEvent}
 * (during {@code repository.save(order)}). Looks up the {@code Buyer} by Keycloak
 * subject ({@code IdentityGuid}); creates one if absent, then registers the order's
 * payment method on the buyer aggregate. Saving the buyer raises
 * {@code BuyerAndPaymentMethodVerifiedDomainEvent} → the next handler
 * back-fills the order's {@code BuyerId}/{@code PaymentMethodId}.
 *
 * <p>Runs inside the same DB transaction as the originating order save
 * (synchronous {@code @EventListener}).
 */
@Component
public class ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler.class);
    private static final DateTimeFormatter ALIAS_TS = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss").withZone(ZoneOffset.UTC);

    private final BuyerRepository buyerRepository;

    public ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(OrderStartedDomainEvent event) {
        int cardTypeId = event.getCardTypeId() != 0 ? event.getCardTypeId() : 1;
        Buyer buyer = buyerRepository.findByIdentityGuid(event.getUserId())
                .orElseGet(() -> new Buyer(event.getUserId(), event.getUserName()));

        long orderId = event.getOrder().getId() != null ? event.getOrder().getId() : 0L;
        buyer.verifyOrAddPaymentMethod(
                cardTypeId,
                "Payment Method on " + ALIAS_TS.format(Instant.now()),
                event.getCardNumber(),
                event.getCardSecurityNumber(),
                event.getCardHolderName(),
                event.getCardExpiration(),
                orderId);

        // Saving the buyer cascades the payment method AND raises BuyerAndPaymentMethodVerifiedDomainEvent
        // (which Spring Data publishes via @DomainEvents on Entity).
        buyerRepository.save(buyer);
        log.info("Buyer {} verified/added for order {}", buyer.getIdentityGuid(), orderId);
    }
}
