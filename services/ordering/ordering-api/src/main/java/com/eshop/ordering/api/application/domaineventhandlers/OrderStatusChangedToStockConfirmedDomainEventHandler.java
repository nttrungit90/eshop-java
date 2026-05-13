package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStatusChangedToStockConfirmedIntegrationEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToStockConfirmedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Mirrors .NET OrderStatusChangedToStockConfirmedDomainEventHandler. */
@Component
public class OrderStatusChangedToStockConfirmedDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToStockConfirmedDomainEventHandler.class);

    private final OrderingIntegrationEventService integrationEventService;

    public OrderStatusChangedToStockConfirmedDomainEventHandler(OrderingIntegrationEventService integrationEventService) {
        this.integrationEventService = integrationEventService;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(OrderStatusChangedToStockConfirmedDomainEvent event) {
        integrationEventService.addAndSaveEvent(new OrderStatusChangedToStockConfirmedIntegrationEvent(event.getOrderId()));
        log.info("Order {} -> StockConfirmed integration event enqueued", event.getOrderId());
    }
}
