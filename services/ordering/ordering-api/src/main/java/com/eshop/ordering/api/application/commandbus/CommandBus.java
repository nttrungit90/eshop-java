package com.eshop.ordering.api.application.commandbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Spring-managed command bus. Each {@link CommandHandler @Component}
 * is registered by its declared {@link CommandHandler#commandType()} at startup;
 * {@link #send(Command)} dispatches the command to the matching handler.
 *
 * <p>Mirrors the {@code IMediator.Send(IRequest&lt;TResult&gt;)} contract from .NET
 * MediatR but only covers the command-side (queries stay direct repository calls).
 */
@Component
public class CommandBus {

    private static final Logger log = LoggerFactory.getLogger(CommandBus.class);

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Command>, CommandHandler> handlers = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public CommandBus(List<CommandHandler> all) {
        for (CommandHandler h : all) {
            handlers.put(h.commandType(), h);
            log.info("CommandBus: registered handler {} for {}",
                    h.getClass().getSimpleName(), h.commandType().getSimpleName());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <C extends Command<R>, R> R send(C command) {
        CommandHandler handler = handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler registered for " + command.getClass().getName());
        }
        return (R) handler.handle(command);
    }
}
