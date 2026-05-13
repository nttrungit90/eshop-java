package com.eshop.ordering.api.application.commandbus;

/** Marker for CQRS commands. {@code R} is the handler result type (e.g. {@code Boolean}, {@code OrderDraftDto}). */
public interface Command<R> {
}
