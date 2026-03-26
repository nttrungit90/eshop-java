package com.eshop.webhooks.events.handlers;

import com.eshop.eventbus.IntegrationEventHandler;
import com.eshop.webhooks.events.ProductPriceChangedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ProductPriceChangedIntegrationEventHandler
        implements IntegrationEventHandler<ProductPriceChangedIntegrationEvent> {

    private static final Logger log = LoggerFactory.getLogger(ProductPriceChangedIntegrationEventHandler.class);

    @Override
    public CompletableFuture<Void> handle(ProductPriceChangedIntegrationEvent event) {
        log.info("Received ProductPriceChangedIntegrationEvent (no-op)");
        return CompletableFuture.completedFuture(null);
    }
}
