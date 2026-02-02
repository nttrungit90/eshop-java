/**
 * Converted from: src/Catalog.API/IntegrationEvents/Events/ProductPriceChangedIntegrationEvent.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.Events.ProductPriceChangedIntegrationEvent
 *
 * Integration event for product price changes.
 */
package com.eshop.catalog.events;

import com.eshop.eventbus.IntegrationEvent;

import java.math.BigDecimal;

public class ProductPriceChangedIntegrationEvent extends IntegrationEvent {

    private Integer productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;

    public ProductPriceChangedIntegrationEvent() {
        super();
    }

    public ProductPriceChangedIntegrationEvent(Integer productId, BigDecimal oldPrice, BigDecimal newPrice) {
        super();
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }
}
