package com.eshop.ordering.api.application.commandbus;

import java.util.UUID;

/**
 * Mirrors .NET IdentifiedCommand&lt;TCmd, TResult&gt;. Wraps an inner command with a
 * client-supplied request id (from the {@code x-requestid} header). The
 * {@link IdempotentCommandExecutor} short-circuits duplicate requestIds.
 */
public final class IdentifiedCommand<C extends Command<R>, R> {
    private final UUID requestId;
    private final C command;

    public IdentifiedCommand(UUID requestId, C command) {
        this.requestId = requestId;
        this.command = command;
    }

    public UUID getRequestId() { return requestId; }
    public C getCommand() { return command; }
}
