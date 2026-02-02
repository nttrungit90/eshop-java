/**
 * Converted from: src/Catalog.API/Infrastructure/CatalogContext.cs
 * .NET Class: eShop.Catalog.API.Infrastructure.CatalogContext
 *
 * Repository for CatalogBrand entities.
 */
package com.eshop.catalog.repository;

import com.eshop.catalog.model.CatalogBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogBrandRepository extends JpaRepository<CatalogBrand, Integer> {

    List<CatalogBrand> findAllByOrderByBrandAsc();

    Optional<CatalogBrand> findByBrand(String brand);
}
