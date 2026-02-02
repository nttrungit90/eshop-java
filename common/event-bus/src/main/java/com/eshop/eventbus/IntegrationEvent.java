/**
 * Converted from: src/EventBus/Events/IntegrationEvent.cs
 * .NET Class: eShop.EventBus.Events.IntegrationEvent
 *
 * Base class for all integration events.
 */
package com.eshop.eventbus;

import java.time.Instant;
import java.util.UUID;

public class IntegrationEvent {

    private UUID id;
    private Instant creationDate;

    public IntegrationEvent() {
        this.id = UUID.randomUUID();
        this.creationDate = Instant.now();
    }

    public IntegrationEvent(UUID id, Instant creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }
}
