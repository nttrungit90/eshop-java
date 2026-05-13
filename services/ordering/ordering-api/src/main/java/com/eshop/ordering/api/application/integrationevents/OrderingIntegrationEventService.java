package com.eshop.ordering.api.application.integrationevents;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Mirrors .NET OrderingIntegrationEventService. The single entry point that
 * application code uses to emit integration events through the outbox.
 *
 * <p><b>Outbox pattern:</b> {@link #addAndSaveEvent} writes the event to
 * {@code ordering."IntegrationEventLog"} (state=NotPublished) inside the
 * caller's transaction. A separate background relay
 * ({@link IntegrationEventOutboxRelay}) publishes pending entries to RabbitMQ.
 *
 * <p>Never call {@code eventBus.publishAsync} directly from a command handler
 * or controller — go through this service so broker outages and crashes
 * between commit-and-publish can't drop an event.
 */
@Service
public class OrderingIntegrationEventService {

    private static final Logger log = LoggerFactory.getLogger(OrderingIntegrationEventService.class);

    private final OrderingIntegrationEventLogRepository repository;
    private final EventBus eventBus;
    private final ObjectMapper eventBusObjectMapper;

    public OrderingIntegrationEventService(OrderingIntegrationEventLogRepository repository,
                                           EventBus eventBus,
                                           @Qualifier("eventBusObjectMapper") ObjectMapper eventBusObjectMapper) {
        this.repository = repository;
        this.eventBus = eventBus;
        this.eventBusObjectMapper = eventBusObjectMapper;
    }

    /**
     * Persist an integration event to the outbox inside the caller's @Transactional.
     * The relay will publish it shortly after commit. Required to be called from within
     * an active transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void addAndSaveEvent(IntegrationEvent event) {
        try {
            String content = eventBusObjectMapper.writeValueAsString(event);
            // .NET uses a per-transaction UUID; for us the event id IS the transaction id
            // (good enough for relay deduplication and matches the event's identity).
            OrderingIntegrationEventLogEntry entry = new OrderingIntegrationEventLogEntry(
                    event.getId(),
                    event.getClass().getName(),
                    content,
                    event.getId());
            repository.save(entry);
            log.info("Outbox: enqueue {} (id={})", event.getClass().getSimpleName(), event.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue integration event", e);
        }
    }

    /**
     * Best-effort publish for a single pending entry. Called by the background relay.
     * Marks {@code InProgress} → {@code Published} or {@code PublishedFailed} depending on outcome.
     */
    @Transactional
    public void publishThroughEventBus(OrderingIntegrationEventLogEntry entry) {
        UUID eventId = entry.getEventId();
        try {
            entry.setState(OrderingIntegrationEventLogEntry.EventState.InProgress);
            repository.save(entry);

            IntegrationEvent event = (IntegrationEvent) eventBusObjectMapper.readValue(
                    entry.getContent(), Class.forName(entry.getEventTypeName()));
            log.info("Outbox: publishing {} (id={})", entry.getEventTypeName(), eventId);
            eventBus.publishAsync(event).join();

            entry.setState(OrderingIntegrationEventLogEntry.EventState.Published);
            entry.setTimesSent(entry.getTimesSent() + 1);
            repository.save(entry);
        } catch (Exception ex) {
            log.error("Outbox: publish failed for event {}: {}", eventId, ex.getMessage(), ex);
            entry.setState(OrderingIntegrationEventLogEntry.EventState.PublishedFailed);
            entry.setTimesSent(entry.getTimesSent() + 1);
            repository.save(entry);
        }
    }
}
