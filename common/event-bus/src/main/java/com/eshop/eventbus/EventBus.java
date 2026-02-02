/**
 * Converted from: src/EventBus/Abstractions/IEventBus.cs
 * .NET Interface: eShop.EventBus.Abstractions.IEventBus
 *
 * Event bus interface for publishing integration events.
 */
package com.eshop.eventbus;

import java.util.concurrent.CompletableFuture;

public interface EventBus {

    /**
     * Publishes an integration event to the message broker.
     *
     * @param event the integration event to publish
     * @return a CompletableFuture that completes when the event is published
     */
    CompletableFuture<Void> publishAsync(IntegrationEvent event);
}
