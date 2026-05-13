package com.eshop.ordering.api.application.domaineventhandlers;

import com.eshop.ordering.api.application.integrationevents.OrderingIntegrationEventService;
import com.eshop.ordering.api.events.OrderStatusChangedToCancelledIntegrationEvent;
import com.eshop.ordering.domain.events.OrderCancelledDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Mirrors .NET OrderCancelledDomainEventHandler. */
@Component
public class OrderCancelledDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledDomainEventHandler.class);

    private final OrderingIntegrationEventService integrationEventService;

    public OrderCancelledDomainEventHandler(OrderingIntegrationEventService integrationEventService) {
        this.integrationEventService = integrationEventService;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(OrderCancelledDomainEvent event) {
        Long id = event.getOrder().getId();
        integrationEventService.addAndSaveEvent(new OrderStatusChangedToCancelledIntegrationEvent(
                id != null ? id : 0L,
                event.getOrder().getStatus() != null ? event.getOrder().getStatus().getName() : null));
        log.info("Order {} -> Cancelled integration event enqueued", id);
    }
}
