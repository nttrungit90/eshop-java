package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStatusChangedToSubmittedIntegrationEvent;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import com.eshop.ordering.domain.events.BuyerAndPaymentMethodVerifiedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mirrors .NET UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler.
 *
 * <p>Once the {@code Buyer} aggregate is persisted (assigning real DB IDs to
 * Buyer + PaymentMethod), back-fill the in-flight Order's {@code BuyerId} +
 * {@code PaymentMethodId} so the FK columns end up populated.
 *
 * <p>Then enqueue {@code OrderStatusChangedToSubmittedIntegrationEvent}
 * through the outbox so downstream services hear about the new Submitted order
 * once the surrounding transaction commits.
 */
@Component
public class UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler.class);

    private final OrderRepository orderRepository;
    private final OrderingIntegrationEventService integrationEventService;

    public UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler(
            OrderRepository orderRepository,
            OrderingIntegrationEventService integrationEventService) {
        this.orderRepository = orderRepository;
        this.integrationEventService = integrationEventService;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(BuyerAndPaymentMethodVerifiedDomainEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order " + event.getOrderId() + " not found after buyer verification"));
        // Order is a managed JPA entity in this tx — mutations flush at commit; no need to call save()
        // (and doing so would trigger a recursive @DomainEvents publish).
        order.setBuyerId(event.getBuyer().getId());
        order.setPaymentMethodId(event.getPayment().getId());

        integrationEventService.addAndSaveEvent(new OrderStatusChangedToSubmittedIntegrationEvent(
                order.getId(),
                order.getStatus() != null ? order.getStatus().getName() : null,
                event.getBuyer().getName(),
                event.getBuyer().getIdentityGuid()));

        log.info("Order {} linked to buyer {} payment {} and OrderStatusChangedToSubmitted enqueued",
                order.getId(), event.getBuyer().getId(), event.getPayment().getId());
    }
}
