/**
 * Converted from: src/Catalog.API/IntegrationEvents/EventHandling/OrderStatusChangedToPaidIntegrationEventHandler.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.EventHandling.OrderStatusChangedToPaidIntegrationEventHandler
 *
 * Handles stock reduction when an order is paid.
 */
package com.eshop.catalog.events.handlers;

import com.eshop.catalog.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.catalog.model.CatalogItem;
import com.eshop.catalog.model.OrderStockItem;
import com.eshop.catalog.repository.CatalogItemRepository;
import com.eshop.eventbus.IntegrationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderStatusChangedToPaidIntegrationEventHandler
        implements IntegrationEventHandler<OrderStatusChangedToPaidIntegrationEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedToPaidIntegrationEventHandler.class);

    private final CatalogItemRepository catalogItemRepository;

    public OrderStatusChangedToPaidIntegrationEventHandler(CatalogItemRepository catalogItemRepository) {
        this.catalogItemRepository = catalogItemRepository;
    }

    @Override
    @Transactional
    public CompletableFuture<Void> handle(OrderStatusChangedToPaidIntegrationEvent event) {
        return CompletableFuture.runAsync(() -> {
            log.info("Handling integration event: {} - ({})", event.getId(), event.getClass().getSimpleName());

            // We're not blocking stock/inventory - just reduce stock when paid
            for (OrderStockItem orderStockItem : event.getOrderStockItems()) {
                CatalogItem catalogItem = catalogItemRepository.findById(orderStockItem.productId()).orElse(null);
                if (catalogItem != null) {
                    try {
                        catalogItem.removeStock(orderStockItem.units());
                        catalogItemRepository.save(catalogItem);
                        log.debug("Reduced stock for product {}: {} units", orderStockItem.productId(), orderStockItem.units());
                    } catch (IllegalStateException e) {
                        log.warn("Failed to reduce stock for product {}: {}", orderStockItem.productId(), e.getMessage());
                    }
                }
            }
        });
    }
}
