package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.ordering.api.events.OrderStockItem;
import com.eshop.ordering.domain.events.OrderStatusChangedToPaidDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Mirrors .NET OrderStatusChangedToPaidDomainEventHandler. */
@Component
public class OrderStatusChangedToPaidDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToPaidDomainEventHandler.class);

    private final OrderingIntegrationEventService integrationEventService;

    public OrderStatusChangedToPaidDomainEventHandler(OrderingIntegrationEventService integrationEventService) {
        this.integrationEventService = integrationEventService;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(OrderStatusChangedToPaidDomainEvent event) {
        List<OrderStockItem> stockItems = event.getOrderItems().stream()
                .map(oi -> new OrderStockItem(oi.getProductId().intValue(), oi.getUnits()))
                .collect(Collectors.toList());
        integrationEventService.addAndSaveEvent(new OrderStatusChangedToPaidIntegrationEvent(event.getOrderId(), stockItems));
        log.info("Order {} -> Paid integration event enqueued", event.getOrderId());
    }
}
