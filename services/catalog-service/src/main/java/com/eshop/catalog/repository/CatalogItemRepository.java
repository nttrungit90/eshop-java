/**
 * Converted from: src/Catalog.API/Infrastructure/CatalogContext.cs
 * .NET Class: eShop.Catalog.API.Infrastructure.CatalogContext
 *
 * Repository for CatalogItem entities.
 */
package com.eshop.catalog.repository;

import com.eshop.catalog.model.CatalogItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogItemRepository extends JpaRepository<CatalogItem, Integer> {

    @Override
    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    Page<CatalogItem> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    Optional<CatalogItem> findById(Integer id);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    Page<CatalogItem> findByCatalogTypeId(Integer typeId, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    Page<CatalogItem> findByCatalogBrandId(Integer brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    Page<CatalogItem> findByCatalogTypeIdAndCatalogBrandId(Integer typeId, Integer brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    @Query("SELECT c FROM CatalogItem c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<CatalogItem> findByNameContaining(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    @Query("SELECT c FROM CatalogItem c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))")
    Page<CatalogItem> findByNameStartingWith(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    @Query("SELECT c FROM CatalogItem c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.catalogType.id = :typeId")
    Page<CatalogItem> findByNameContainingAndCatalogTypeId(String name, Integer typeId, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    @Query("SELECT c FROM CatalogItem c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.catalogBrand.id = :brandId")
    Page<CatalogItem> findByNameContainingAndCatalogBrandId(String name, Integer brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    @Query("SELECT c FROM CatalogItem c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.catalogType.id = :typeId AND c.catalogBrand.id = :brandId")
    Page<CatalogItem> findByNameContainingAndCatalogTypeIdAndCatalogBrandId(String name, Integer typeId, Integer brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"catalogType", "catalogBrand"})
    List<CatalogItem> findByIdIn(List<Integer> ids);

    @Query(value = "SELECT * FROM \"Catalog\" ORDER BY \"Embedding\" <-> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<CatalogItem> findBySemanticRelevance(float[] embedding, int limit);
    // Note: Native query uses quoted identifiers for PostgreSQL case-sensitivity
}
