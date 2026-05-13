package com.eshop.ordering.api.application.commandbus;

import com.eshop.ordering.infrastructure.idempotency.RequestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mirrors .NET IdentifiedCommandHandler. Implements the idempotency wrapper for any
 * state-changing command.
 *
 * <p>Flow:
 * <ol>
 *   <li>If {@code requestId} already in {@code ordering.requests} → return success without re-executing.</li>
 *   <li>Otherwise dispatch through the {@link CommandBus}, then record the requestId.</li>
 * </ol>
 *
 * <p>Result for the duplicate-request case mirrors {@code CreateOrderIdentifiedCommandHandler.CreateResultForDuplicateRequest}
 * which returns {@code true} — duplicates are reported as success without side effects.
 */
@Component
public class IdempotentCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(IdempotentCommandExecutor.class);

    private final CommandBus commandBus;
    private final RequestManager requestManager;

    public IdempotentCommandExecutor(CommandBus commandBus, RequestManager requestManager) {
        this.commandBus = commandBus;
        this.requestManager = requestManager;
    }

    @Transactional
    public <C extends Command<R>, R> R execute(IdentifiedCommand<C, R> identified, R duplicateResult) {
        if (requestManager.exists(identified.getRequestId())) {
            log.info("Duplicate request {} for {} — short-circuiting", identified.getRequestId(),
                    identified.getCommand().getClass().getSimpleName());
            return duplicateResult;
        }
        R result = commandBus.send(identified.getCommand());
        requestManager.createRequestForCommand(identified.getRequestId(),
                identified.getCommand().getClass().getSimpleName());
        return result;
    }
}
