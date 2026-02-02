/**
 * Converted from: src/Catalog.API/Infrastructure/CatalogContext.cs
 * .NET Class: eShop.Catalog.API.Infrastructure.CatalogContext
 *
 * Repository for CatalogType entities.
 */
package com.eshop.catalog.repository;

import com.eshop.catalog.model.CatalogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogTypeRepository extends JpaRepository<CatalogType, Integer> {

    List<CatalogType> findAllByOrderByTypeAsc();

    Optional<CatalogType> findByType(String type);
}
