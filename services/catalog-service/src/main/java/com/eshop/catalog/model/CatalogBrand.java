/**
 * Converted from: src/Catalog.API/Model/CatalogBrand.cs
 * .NET Class: eShop.Catalog.API.Model.CatalogBrand
 *
 * Catalog brand entity (e.g., .NET, Azure, Visual Studio).
 */
package com.eshop.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "CatalogBrand")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CatalogBrand {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Brand", nullable = false, length = 100)
    private String brand;

    public CatalogBrand() {
    }

    public CatalogBrand(String brand) {
        this.brand = brand;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
}
