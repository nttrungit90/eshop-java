/**
 * Converted from: src/Catalog.API/IntegrationEvents/Events/ConfirmedOrderStockItem.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.Events.ConfirmedOrderStockItem
 *
 * Represents the stock confirmation status for an order item.
 */
package com.eshop.catalog.model;

public record ConfirmedOrderStockItem(int productId, boolean hasStock) {
}
