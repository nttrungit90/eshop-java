package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import com.eshop.ordering.api.events.OrderStockItem;
import com.eshop.ordering.domain.aggregates.buyer.Buyer;
import com.eshop.ordering.domain.aggregates.buyer.BuyerRepository;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import com.eshop.ordering.domain.events.OrderStatusChangedToAwaitingValidationDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mirrors .NET OrderStatusChangedToAwaitingValidationDomainEventHandler.
 *
 * <p>When the Order aggregate transitions to AwaitingValidation, publish the
 * corresponding integration event (with order stock items) to the outbox so
 * catalog-service can run the stock check.
 */
@Component
public class OrderStatusChangedToAwaitingValidationDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToAwaitingValidationDomainEventHandler.class);

    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;
    private final OrderingIntegrationEventService integrationEventService;

    public OrderStatusChangedToAwaitingValidationDomainEventHandler(
            OrderRepository orderRepository,
            BuyerRepository buyerRepository,
            OrderingIntegrationEventService integrationEventService) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.integrationEventService = integrationEventService;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(OrderStatusChangedToAwaitingValidationDomainEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        Buyer buyer = order != null && order.getBuyerId() != null
                ? buyerRepository.findById(order.getBuyerId()).orElse(null) : null;

        List<OrderStockItem> stockItems = event.getOrderItems().stream()
                .map(oi -> new OrderStockItem(oi.getProductId().intValue(), oi.getUnits()))
                .collect(Collectors.toList());

        integrationEventService.addAndSaveEvent(new OrderStatusChangedToAwaitingValidationIntegrationEvent(
                event.getOrderId(), stockItems));

        log.info("Order {} -> AwaitingValidation integration event enqueued (buyer {})",
                event.getOrderId(), buyer != null ? buyer.getIdentityGuid() : "?");
    }
}
