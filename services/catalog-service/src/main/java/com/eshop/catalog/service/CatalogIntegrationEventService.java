/**
 * Converted from: src/Catalog.API/IntegrationEvents/CatalogIntegrationEventService.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.CatalogIntegrationEventService
 *
 * Service for publishing integration events with transactional outbox pattern.
 */
package com.eshop.catalog.service;

import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.eshop.eventbus.outbox.IntegrationEventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CatalogIntegrationEventService {

    private static final Logger log = LoggerFactory.getLogger(CatalogIntegrationEventService.class);

    private final EventBus eventBus;
    private final IntegrationEventLogService integrationEventLogService;

    public CatalogIntegrationEventService(
            EventBus eventBus,
            IntegrationEventLogService integrationEventLogService) {
        this.eventBus = eventBus;
        this.integrationEventLogService = integrationEventLogService;
    }

    /**
     * Publishes an integration event through the event bus.
     * Marks the event as in-progress, publishes it, then marks as published.
     */
    public void publishThroughEventBus(IntegrationEvent event) {
        try {
            log.info("Publishing integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

            integrationEventLogService.markEventAsInProgress(event.getId());
            eventBus.publishAsync(event);
            integrationEventLogService.markEventAsPublished(event.getId());
        } catch (Exception ex) {
            log.error("Error publishing integration event: {} - ({})", event.getId(), event.getClass().getSimpleName(), ex);
            integrationEventLogService.markEventAsFailed(event.getId());
        }
    }

    /**
     * Saves the integration event and catalog context changes atomically.
     * Uses transactional outbox pattern for reliable event publishing.
     */
    @Transactional
    public void saveEventAndCatalogContextChanges(IntegrationEvent event, String transactionId) {
        log.info("Saving integration event: {} with transaction: {}", event.getId(), transactionId);

        // Save the event to the outbox table within the same transaction
        integrationEventLogService.saveEvent(event, transactionId);
    }

    /**
     * Saves the integration event with a generated transaction ID.
     */
    @Transactional
    public void saveEventAndCatalogContextChanges(IntegrationEvent event) {
        saveEventAndCatalogContextChanges(event, UUID.randomUUID().toString());
    }
}
