/**
 * Converted from: src/Catalog.API/Model/CatalogType.cs
 * .NET Class: eShop.Catalog.API.Model.CatalogType
 *
 * Catalog type entity (e.g., Mug, T-Shirt, Sheet).
 */
package com.eshop.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "CatalogType")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CatalogType {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Type", nullable = false, length = 100)
    private String type;

    public CatalogType() {
    }

    public CatalogType(String type) {
        this.type = type;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
