package com.eshop.ordering.api.application.commandbus;

/** Mirrors .NET MediatR IRequestHandler&lt;TCommand, TResult&gt;. */
public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);

    Class<C> commandType();
}
