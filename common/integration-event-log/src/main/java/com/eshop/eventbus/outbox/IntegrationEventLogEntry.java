/**
 * Converted from: src/IntegrationEventLogEF/IntegrationEventLogEntry.cs
 * .NET Class: eShop.IntegrationEventLogEF.IntegrationEventLogEntry
 *
 * Entity for storing integration events in the outbox.
 */
package com.eshop.eventbus.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_event_log")
public class IntegrationEventLogEntry {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private String eventTypeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state;

    @Column(nullable = false)
    private int timesSent;

    @Column(nullable = false)
    private Instant creationTime;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String transactionId;

    public IntegrationEventLogEntry() {
    }

    public IntegrationEventLogEntry(UUID eventId, String eventTypeName, String content, String transactionId) {
        this.eventId = eventId;
        this.eventTypeName = eventTypeName;
        this.content = content;
        this.transactionId = transactionId;
        this.state = EventState.NOT_PUBLISHED;
        this.timesSent = 0;
        this.creationTime = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public int getTimesSent() {
        return timesSent;
    }

    public void setTimesSent(int timesSent) {
        this.timesSent = timesSent;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public enum EventState {
        NOT_PUBLISHED,
        IN_PROGRESS,
        PUBLISHED,
        PUBLISHED_FAILED
    }
}
