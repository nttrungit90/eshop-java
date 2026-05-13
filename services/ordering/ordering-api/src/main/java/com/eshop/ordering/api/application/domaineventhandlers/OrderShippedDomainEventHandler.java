package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStatusChangedToShippedIntegrationEvent;
import com.eshop.ordering.domain.events.OrderShippedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Mirrors .NET OrderShippedDomainEventHandler. */
@Component
public class OrderShippedDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderShippedDomainEventHandler.class);

    private final OrderingIntegrationEventService integrationEventService;

    public OrderShippedDomainEventHandler(OrderingIntegrationEventService integrationEventService) {
        this.integrationEventService = integrationEventService;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(OrderShippedDomainEvent event) {
        Long id = event.getOrder().getId();
        integrationEventService.addAndSaveEvent(new OrderStatusChangedToShippedIntegrationEvent(
                id != null ? id : 0L,
                event.getOrder().getStatus() != null ? event.getOrder().getStatus().getName() : null));
        log.info("Order {} -> Shipped integration event enqueued", id);
    }
}
