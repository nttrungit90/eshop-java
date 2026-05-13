package com.eshop.ordering.infrastructure.idempotency;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors .NET ClientRequest entity in the ordering.requests table.
 * One row per processed command. When a command with an existing requestId
 * arrives, the handler skips execution and returns the prior outcome.
 */
@Entity
@Table(name = "requests", schema = "ordering")
public class Request {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Time", nullable = false)
    private Instant time;

    protected Request() {}

    public Request(UUID id, String name, Instant time) {
        this.id = id;
        this.name = name;
        this.time = time;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getTime() { return time; }
}
