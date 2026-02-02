package com.eshop.catalog.dto;

import java.math.BigDecimal;

/**
 * CatalogItem response DTO.
 * Java version returns full type/brand objects (improvement over .NET which returns null for list operations).
 */
public record CatalogItemDto(
    Integer id,
    String name,
    String description,
    BigDecimal price,
    String pictureFileName,
    Integer catalogTypeId,
    CatalogTypeDto catalogType,
    Integer catalogBrandId,
    CatalogBrandDto catalogBrand,
    int availableStock,
    int restockThreshold,
    int maxStockThreshold,
    boolean onReorder
) {}
