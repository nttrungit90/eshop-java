/**
 * Converted from: src/Catalog.API/IntegrationEvents/EventHandling/OrderStatusChangedToAwaitingValidationIntegrationEventHandler.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.EventHandling.OrderStatusChangedToAwaitingValidationIntegrationEventHandler
 *
 * Handles stock validation when an order is awaiting validation.
 */
package com.eshop.catalog.events.handlers;

import com.eshop.catalog.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import com.eshop.catalog.events.OrderStockConfirmedIntegrationEvent;
import com.eshop.catalog.events.OrderStockRejectedIntegrationEvent;
import com.eshop.catalog.model.CatalogItem;
import com.eshop.catalog.model.ConfirmedOrderStockItem;
import com.eshop.catalog.model.OrderStockItem;
import com.eshop.catalog.repository.CatalogItemRepository;
import com.eshop.eventbus.EventBus;
import com.eshop.eventbus.IntegrationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OrderStatusChangedToAwaitingValidationIntegrationEventHandler
        implements IntegrationEventHandler<OrderStatusChangedToAwaitingValidationIntegrationEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToAwaitingValidationIntegrationEventHandler.class);

    private final CatalogItemRepository catalogItemRepository;
    private final EventBus eventBus;

    public OrderStatusChangedToAwaitingValidationIntegrationEventHandler(
            CatalogItemRepository catalogItemRepository,
            EventBus eventBus) {
        this.catalogItemRepository = catalogItemRepository;
        this.eventBus = eventBus;
    }

    @Override
    public CompletableFuture<Void> handle(OrderStatusChangedToAwaitingValidationIntegrationEvent event) {
        return CompletableFuture.runAsync(() -> {
            log.info("Handling integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

            List<ConfirmedOrderStockItem> confirmedOrderStockItems = new ArrayList<>();

            for (OrderStockItem orderStockItem : event.getOrderStockItems()) {
                CatalogItem catalogItem = catalogItemRepository.findById(orderStockItem.productId()).orElse(null);
                if (catalogItem != null) {
                    boolean hasStock = catalogItem.getAvailableStock() >= orderStockItem.units();
                    confirmedOrderStockItems.add(new ConfirmedOrderStockItem(catalogItem.getId(), hasStock));
                }
            }

            // Check if any item doesn't have sufficient stock
            boolean anyItemWithoutStock = confirmedOrderStockItems.stream()
                    .anyMatch(item -> !item.hasStock());

            if (anyItemWithoutStock) {
                log.info("Order {} stock rejected - insufficient stock for some items", event.getOrderId());
                eventBus.publishAsync(new OrderStockRejectedIntegrationEvent(event.getOrderId(), confirmedOrderStockItems));
            } else {
                log.info("Order {} stock confirmed", event.getOrderId());
                eventBus.publishAsync(new OrderStockConfirmedIntegrationEvent(event.getOrderId()));
            }
        });
    }
}
