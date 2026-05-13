package com.eshop.ordering.infrastructure.idempotency;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors .NET RequestManager.
 *
 * <p>Persists a row in {@code ordering.requests} when a command is processed.
 * Subsequent commands with the same {@code requestId} short-circuit so duplicates
 * never re-execute side effects (no double orders, no double payments).
 */
@Component
public class RequestManager {

    private final RequestRepository repository;

    public RequestManager(RequestRepository repository) {
        this.repository = repository;
    }

    public boolean exists(UUID requestId) {
        return repository.existsById(requestId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createRequestForCommand(UUID requestId, String commandName) {
        if (repository.existsById(requestId)) {
            throw new IllegalStateException("Request " + requestId + " already exists");
        }
        repository.save(new Request(requestId, commandName, Instant.now()));
    }
}
