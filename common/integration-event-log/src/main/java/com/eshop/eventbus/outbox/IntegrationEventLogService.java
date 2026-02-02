/**
 * Converted from: src/IntegrationEventLogEF/Services/IntegrationEventLogService.cs
 * .NET Class: eShop.IntegrationEventLogEF.Services.IntegrationEventLogService
 *
 * Service for managing integration event log entries (outbox pattern).
 */
package com.eshop.eventbus.outbox;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IntegrationEventLogService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEventLogService.class);

    private final IntegrationEventLogRepository repository;
    private final ObjectMapper objectMapper;

    public IntegrationEventLogService(IntegrationEventLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(IntegrationEvent event, String transactionId) {
        try {
            String content = objectMapper.writeValueAsString(event);
            IntegrationEventLogEntry entry = new IntegrationEventLogEntry(
                    event.getId(),
                    event.getClass().getName(),
                    content,
                    transactionId
            );
            repository.save(entry);
            log.info("Saved integration event {} to outbox", event.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize integration event {}", event.getId(), e);
            throw new RuntimeException("Failed to serialize integration event", e);
        }
    }

    @Transactional
    public void markEventAsPublished(UUID eventId) {
        repository.findById(eventId).ifPresent(entry -> {
            entry.setState(IntegrationEventLogEntry.EventState.PUBLISHED);
            entry.setTimesSent(entry.getTimesSent() + 1);
            repository.save(entry);
            log.info("Marked event {} as published", eventId);
        });
    }

    @Transactional
    public void markEventAsInProgress(UUID eventId) {
        repository.findById(eventId).ifPresent(entry -> {
            entry.setState(IntegrationEventLogEntry.EventState.IN_PROGRESS);
            repository.save(entry);
        });
    }

    @Transactional
    public void markEventAsFailed(UUID eventId) {
        repository.findById(eventId).ifPresent(entry -> {
            entry.setState(IntegrationEventLogEntry.EventState.PUBLISHED_FAILED);
            entry.setTimesSent(entry.getTimesSent() + 1);
            repository.save(entry);
            log.warn("Marked event {} as failed (attempt {})", eventId, entry.getTimesSent());
        });
    }

    public List<IntegrationEventLogEntry> getPendingEvents(String transactionId) {
        return repository.findPendingEventsByTransactionId(transactionId);
    }

    public List<IntegrationEventLogEntry> getAllPendingEvents() {
        return repository.findAllPendingEvents();
    }

    public List<IntegrationEventLogEntry> getFailedEventsForRetry() {
        return repository.findFailedEventsForRetry();
    }
}
