/**
 * Converted from: src/Catalog.API/Model/CatalogItem.cs
 * .NET Class: eShop.Catalog.API.Model.CatalogItem
 *
 * Catalog item entity representing a product.
 */
package com.eshop.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "Catalog")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CatalogItem {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Description")
    private String description;

    @NotNull
    @Positive
    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    @Column(name = "PictureFileName")
    private String pictureFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogTypeId")
    private CatalogType catalogType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogBrandId")
    private CatalogBrand catalogBrand;

    @Column(name = "AvailableStock", nullable = false)
    private int availableStock;

    @Column(name = "RestockThreshold", nullable = false)
    private int restockThreshold;

    @Column(name = "MaxStockThreshold", nullable = false)
    private int maxStockThreshold;

    @Column(name = "OnReorder")
    private boolean onReorder;

    // Embedding vector for AI semantic search (pgvector)
    // .NET uses 384 dimensions (text-embedding-3-small truncated)
    @Column(name = "Embedding", columnDefinition = "vector(384)")
    private float[] embedding;

    public CatalogItem() {
    }

    public int removeStock(int quantityDesired) {
        if (availableStock == 0) {
            throw new IllegalStateException("Empty stock, product item " + name + " is sold out");
        }

        if (quantityDesired <= 0) {
            throw new IllegalArgumentException("Item count desired should be greater than zero");
        }

        int removed = Math.min(quantityDesired, availableStock);
        availableStock -= removed;
        return removed;
    }

    public int addStock(int quantity) {
        int original = availableStock;
        if (availableStock + quantity > maxStockThreshold) {
            availableStock = maxStockThreshold;
        } else {
            availableStock += quantity;
        }
        onReorder = false;
        return availableStock - original;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getPictureFileName() { return pictureFileName; }
    public void setPictureFileName(String pictureFileName) { this.pictureFileName = pictureFileName; }
    public CatalogType getCatalogType() { return catalogType; }
    public void setCatalogType(CatalogType catalogType) { this.catalogType = catalogType; }
    public CatalogBrand getCatalogBrand() { return catalogBrand; }
    public void setCatalogBrand(CatalogBrand catalogBrand) { this.catalogBrand = catalogBrand; }
    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }
    public int getRestockThreshold() { return restockThreshold; }
    public void setRestockThreshold(int restockThreshold) { this.restockThreshold = restockThreshold; }
    public int getMaxStockThreshold() { return maxStockThreshold; }
    public void setMaxStockThreshold(int maxStockThreshold) { this.maxStockThreshold = maxStockThreshold; }
    public boolean isOnReorder() { return onReorder; }
    public void setOnReorder(boolean onReorder) { this.onReorder = onReorder; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
