package com.eshop.ordering.api.application.integrationevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Background relay that drains the outbox.
 *
 * <p>Polls {@code ordering."IntegrationEventLog"} every few seconds for entries in
 * {@code NotPublished} or {@code PublishedFailed} state and publishes them to
 * RabbitMQ via {@link OrderingIntegrationEventService#publishThroughEventBus}.
 */
@Component
public class IntegrationEventOutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEventOutboxRelay.class);

    private final OrderingIntegrationEventLogRepository repository;
    private final OrderingIntegrationEventService integrationEventService;

    public IntegrationEventOutboxRelay(OrderingIntegrationEventLogRepository repository,
                                       OrderingIntegrationEventService integrationEventService) {
        this.repository = repository;
        this.integrationEventService = integrationEventService;
    }

    @Scheduled(fixedDelay = 3000)
    public void drain() {
        List<OrderingIntegrationEventLogEntry> pending = repository.findAllPendingEvents();
        List<OrderingIntegrationEventLogEntry> retries = repository.findFailedEventsForRetry();
        if (pending.isEmpty() && retries.isEmpty()) return;

        log.debug("Outbox tick: {} pending, {} retries", pending.size(), retries.size());
        for (OrderingIntegrationEventLogEntry e : pending) integrationEventService.publishThroughEventBus(e);
        for (OrderingIntegrationEventLogEntry e : retries) integrationEventService.publishThroughEventBus(e);
    }
}
