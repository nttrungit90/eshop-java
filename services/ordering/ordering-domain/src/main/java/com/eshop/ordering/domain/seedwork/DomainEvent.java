/**
 * Converted from: src/Ordering.Domain/SeedWork/INotification.cs
 *
 * Base interface for domain events.
 */
package com.eshop.ordering.domain.seedwork;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID id;
    private final Instant occurredOn;

    protected DomainEvent() {
        this.id = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}
