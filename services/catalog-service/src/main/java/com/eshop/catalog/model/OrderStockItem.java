/**
 * Converted from: src/Catalog.API/IntegrationEvents/Events/OrderStockItem.cs
 * .NET Class: eShop.Catalog.API.IntegrationEvents.Events.OrderStockItem
 *
 * Represents an item in an order for stock validation.
 */
package com.eshop.catalog.model;

public record OrderStockItem(int productId, int units) {
}
