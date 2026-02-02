/**
 * Converted from: src/EventBus/Abstractions/IIntegrationEventHandler.cs
 * .NET Interface: eShop.EventBus.Abstractions.IIntegrationEventHandler
 *
 * Handler interface for processing integration events.
 */
package com.eshop.eventbus;

import java.util.concurrent.CompletableFuture;

public interface IntegrationEventHandler<T extends IntegrationEvent> {

    /**
     * Handles an integration event.
     *
     * @param event the integration event to handle
     * @return a CompletableFuture that completes when the event is handled
     */
    CompletableFuture<Void> handle(T event);
}
