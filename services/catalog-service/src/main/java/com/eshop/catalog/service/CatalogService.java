/**
 * Converted from: src/Catalog.API/Apis/CatalogApi.cs
 * .NET Class: eShop.Catalog.API
 *
 * Service for catalog operations including price change events.
 */
package com.eshop.catalog.service;

import com.eshop.catalog.events.ProductPriceChangedIntegrationEvent;
import com.eshop.catalog.model.CatalogItem;
import com.eshop.catalog.repository.CatalogItemRepository;
import com.eshop.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CatalogItemRepository itemRepository;
    private final EventBus eventBus;

    public CatalogService(CatalogItemRepository itemRepository, EventBus eventBus) {
        this.itemRepository = itemRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public CatalogItem createItem(CatalogItem item) {
        return itemRepository.save(item);
    }

    @Transactional
    public Optional<CatalogItem> updateItem(CatalogItem item) {
        return itemRepository.findById(item.getId())
                .map(existing -> {
                    BigDecimal oldPrice = existing.getPrice();
                    BigDecimal newPrice = item.getPrice();

                    existing.setName(item.getName());
                    existing.setDescription(item.getDescription());
                    existing.setPrice(newPrice);
                    existing.setPictureFileName(item.getPictureFileName());
                    existing.setCatalogType(item.getCatalogType());
                    existing.setCatalogBrand(item.getCatalogBrand());
                    existing.setAvailableStock(item.getAvailableStock());
                    existing.setRestockThreshold(item.getRestockThreshold());
                    existing.setMaxStockThreshold(item.getMaxStockThreshold());

                    CatalogItem saved = itemRepository.save(existing);

                    // Publish price changed event if price changed
                    if (oldPrice.compareTo(newPrice) != 0) {
                        log.info("Publishing price changed event for item {}: {} -> {}",
                                item.getId(), oldPrice, newPrice);
                        ProductPriceChangedIntegrationEvent event = new ProductPriceChangedIntegrationEvent(
                                item.getId(), oldPrice, newPrice);
                        eventBus.publishAsync(event);
                    }

                    return saved;
                });
    }
}
