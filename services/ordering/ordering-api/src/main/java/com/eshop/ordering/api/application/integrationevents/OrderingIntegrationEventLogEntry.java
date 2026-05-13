package com.eshop.ordering.api.application.integrationevents;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps to {@code ordering."IntegrationEventLog"} exactly as created by the .NET
 * Ordering migrations (PascalCase columns, {@code State} as INTEGER ordinal,
 * {@code TransactionId} as UUID).
 *
 * <p>The generic {@code com.eshop.eventbus.outbox.IntegrationEventLogEntry} from the
 * shared module has a different table/column layout (snake_case, state as string),
 * which is fine for fresh greenfield services. Ordering needs to read/write the
 * existing .NET-owned table, so it provides its own entity.
 */
@Entity
@Table(name = "IntegrationEventLog", schema = "ordering")
public class OrderingIntegrationEventLogEntry {

    /** Mirrors .NET EventStateEnum order: NotPublished=0, InProgress=1, Published=2, PublishedFailed=3. */
    public enum EventState { NotPublished, InProgress, Published, PublishedFailed }

    @Id
    @Column(name = "EventId")
    private UUID eventId;

    @Column(name = "EventTypeName", nullable = false)
    private String eventTypeName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "State", nullable = false)
    private EventState state;

    @Column(name = "TimesSent", nullable = false)
    private int timesSent;

    @Column(name = "CreationTime", nullable = false)
    private Instant creationTime;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "TransactionId", nullable = false)
    private UUID transactionId;

    protected OrderingIntegrationEventLogEntry() {}

    public OrderingIntegrationEventLogEntry(UUID eventId, String eventTypeName, String content, UUID transactionId) {
        this.eventId = eventId;
        this.eventTypeName = eventTypeName;
        this.content = content;
        this.transactionId = transactionId;
        this.state = EventState.NotPublished;
        this.timesSent = 0;
        this.creationTime = Instant.now();
    }

    public UUID getEventId() { return eventId; }
    public String getEventTypeName() { return eventTypeName; }
    public EventState getState() { return state; }
    public void setState(EventState state) { this.state = state; }
    public int getTimesSent() { return timesSent; }
    public void setTimesSent(int timesSent) { this.timesSent = timesSent; }
    public Instant getCreationTime() { return creationTime; }
    public String getContent() { return content; }
    public UUID getTransactionId() { return transactionId; }
}
