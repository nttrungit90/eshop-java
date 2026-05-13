package com.eshop.ordering.api.application.commands;

import com.eshop.ordering.api.application.commandbus.CommandHandler;
import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStartedIntegrationEvent;
import com.eshop.ordering.domain.aggregates.order.Address;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mirrors .NET CreateOrderCommandHandler.
 *
 * <p>Flow:
 * <ol>
 *   <li>Enqueue {@code OrderStartedIntegrationEvent} in the outbox (basket-service will clear basket on publish).</li>
 *   <li>Build the {@code Order} aggregate. Constructor raises {@code OrderStartedDomainEvent}.</li>
 *   <li>Add line items via the aggregate root (invariants enforced).</li>
 *   <li>{@code orderRepository.save(order)} — Spring Data publishes domain events via {@code @DomainEvents};
 *       handlers run synchronously inside this transaction and create Buyer + PaymentMethod, then back-fill the order.</li>
 *   <li>All four rows ({@code orders}, {@code orderItems}, {@code buyers}, {@code paymentmethods}) and one outbox
 *       row ({@code integration_event_log}) are committed atomically.</li>
 * </ol>
 */
@Component
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderCommandHandler.class);

    private final OrderRepository orderRepository;
    private final OrderingIntegrationEventService integrationEventService;

    public CreateOrderCommandHandler(OrderRepository orderRepository,
                                     OrderingIntegrationEventService integrationEventService) {
        this.orderRepository = orderRepository;
        this.integrationEventService = integrationEventService;
    }

    @Override
    public Class<CreateOrderCommand> commandType() { return CreateOrderCommand.class; }

    @Override
    @Transactional
    public Boolean handle(CreateOrderCommand cmd) {
        // 1. Outbox the basket-clear event (transactional — only published on commit)
        integrationEventService.addAndSaveEvent(new OrderStartedIntegrationEvent(cmd.getUserId()));

        // 2. Build aggregate
        Address address = new Address(cmd.getStreet(), cmd.getCity(), cmd.getState(),
                cmd.getCountry(), cmd.getZipCode());
        Instant expiration = cmd.getCardExpiration() != null
                ? cmd.getCardExpiration() : Instant.now().plusSeconds(31_536_000L);

        Order order = new Order(cmd.getUserId(), cmd.getUserName(), address,
                cmd.getCardTypeId() != 0 ? cmd.getCardTypeId() : 1,
                maskCardNumber(cmd.getCardNumber()),
                cmd.getCardSecurityNumber(),
                cmd.getCardHolderName(),
                expiration);

        // 3. Items via aggregate root
        for (CreateOrderCommand.OrderItemDto item : cmd.getItems()) {
            order.addOrderItem(item.getProductId(), item.getProductName(),
                    item.getUnitPrice(),
                    item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO,
                    item.getPictureUrl(),
                    item.getUnits());
        }

        // 4. Save — triggers @DomainEvents → ValidateOrAddBuyerAggregate… handler →
        //    Buyer save → BuyerAndPaymentMethodVerified… handler → Order back-fill +
        //    OrderStatusChangedToSubmitted enqueued.
        orderRepository.save(order);
        log.info("Created order {} for user {}", order.getId(), cmd.getUserId());
        return Boolean.TRUE;
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() <= 4) return cardNumber;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cardNumber.length() - 4; i++) sb.append('X');
        sb.append(cardNumber.substring(cardNumber.length() - 4));
        return sb.toString();
    }
}
