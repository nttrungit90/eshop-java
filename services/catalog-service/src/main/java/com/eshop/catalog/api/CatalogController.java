/**
 * Converted from: src/Catalog.API/Apis/CatalogApi.cs
 * .NET Class: eShop.Catalog.API.Apis.CatalogApi
 *
 * REST API controller for catalog operations.
 * Supports api-version=1.0 and api-version=2.0 query parameter.
 */
package com.eshop.catalog.api;

import com.eshop.catalog.dto.*;
import com.eshop.catalog.model.CatalogItem;
import com.eshop.catalog.repository.CatalogBrandRepository;
import com.eshop.catalog.repository.CatalogItemRepository;
import com.eshop.catalog.repository.CatalogTypeRepository;
import com.eshop.catalog.service.CatalogAI;
import com.eshop.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalog", description = "Product catalog API")
public class CatalogController {

    private final CatalogItemRepository itemRepository;
    private final CatalogTypeRepository typeRepository;
    private final CatalogBrandRepository brandRepository;
    private final CatalogService catalogService;
    private final CatalogAI catalogAI;
    private final CatalogMapper catalogMapper;

    public CatalogController(
            CatalogItemRepository itemRepository,
            CatalogTypeRepository typeRepository,
            CatalogBrandRepository brandRepository,
            CatalogService catalogService,
            CatalogAI catalogAI,
            CatalogMapper catalogMapper) {
        this.itemRepository = itemRepository;
        this.typeRepository = typeRepository;
        this.brandRepository = brandRepository;
        this.catalogService = catalogService;
        this.catalogAI = catalogAI;
        this.catalogMapper = catalogMapper;
    }

    // ==================== Items Endpoints ====================

    /**
     * GET /items - V1: no filters, V2: with name/type/brand filters
     */
    @GetMapping("/items")
    @Operation(summary = "Get catalog items", description = "Returns paginated list of catalog items. V2 supports name/type/brand filters.")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItems(
            HttpServletRequest request,
            @Parameter(description = "API version (1.0 or 2.0)", required = true)
            @RequestParam("api-version") String apiVersion,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer brand) {

        Page<CatalogItem> page;

        // V1: ignore filters, V2: apply filters
        if ("1.0".equals(apiVersion)) {
            page = itemRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        } else {
            // V2 with filters - .NET uses StartsWith for name filter
            if (name != null && type != null && brand != null) {
                page = itemRepository.findByNameStartingWithAndCatalogTypeIdAndCatalogBrandId(
                        name, type, brand, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else if (name != null && type != null) {
                page = itemRepository.findByNameStartingWithAndCatalogTypeId(
                        name, type, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else if (name != null && brand != null) {
                page = itemRepository.findByNameStartingWithAndCatalogBrandId(
                        name, brand, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else if (type != null && brand != null) {
                page = itemRepository.findByCatalogTypeIdAndCatalogBrandId(
                        type, brand, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else if (name != null) {
                page = itemRepository.findByNameStartingWith(name, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else if (type != null) {
                page = itemRepository.findByCatalogTypeId(type, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else if (brand != null) {
                page = itemRepository.findByCatalogBrandId(brand, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            } else {
                page = itemRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            }
        }

        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    /**
     * GET /items/{id} - Both V1 and V2
     */
    @GetMapping("/items/{id}")
    @Operation(summary = "Get item by ID", description = "Returns a single catalog item")
    public ResponseEntity<CatalogItemDto> getItemById(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return itemRepository.findById(id)
                .map(catalogMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /items/by - Both V1 and V2
     */
    @GetMapping("/items/by")
    @Operation(summary = "Get items by IDs", description = "Returns catalog items matching the specified IDs")
    public ResponseEntity<List<CatalogItemDto>> getItemsByIds(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @RequestParam List<Integer> ids) {
        List<CatalogItem> items = itemRepository.findByIdIn(ids);
        return ResponseEntity.ok(catalogMapper.toDtoList(items));
    }

    /**
     * GET /items/by/{name} - V1 only
     */
    @GetMapping("/items/by/{name}")
    @Operation(summary = "Get items by name (V1)", description = "Search catalog items by name prefix")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsByName(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CatalogItem> page = itemRepository.findByNameStartingWith(
                name, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    /**
     * GET /items/{id}/pic - Both V1 and V2
     */
    @GetMapping("/items/{id}/pic")
    @Operation(summary = "Get item picture", description = "Returns the picture for a catalog item")
    public ResponseEntity<Resource> getItemPictureById(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer id) {
        return itemRepository.findById(id)
                .map(item -> {
                    if (item.getPictureFileName() == null) {
                        return ResponseEntity.notFound().<Resource>build();
                    }
                    try {
                        Resource resource = new ClassPathResource("pics/" + item.getPictureFileName());
                        if (!resource.exists()) {
                            return ResponseEntity.notFound().<Resource>build();
                        }
                        String mimeType = getImageMimeType(item.getPictureFileName());
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(mimeType))
                                .body(resource);
                    } catch (Exception e) {
                        return ResponseEntity.notFound().<Resource>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /items/withsemanticrelevance/{text} - V1: text in path
     */
    @GetMapping("/items/withsemanticrelevance/{text}")
    @Operation(summary = "Search items by semantic relevance (V1)", description = "Search catalog items using AI semantic search")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsBySemanticRelevanceV1(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable String text,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return doSemanticSearch(text, pageIndex, pageSize);
    }

    /**
     * GET /items/withsemanticrelevance?text= - V2: text as query param
     */
    @GetMapping("/items/withsemanticrelevance")
    @Operation(summary = "Search items by semantic relevance (V2)", description = "Search catalog items using AI semantic search")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsBySemanticRelevanceV2(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return doSemanticSearch(text, pageIndex, pageSize);
    }

    private ResponseEntity<PaginatedItemsDto<CatalogItemDto>> doSemanticSearch(String text, int pageIndex, int pageSize) {
        if (!catalogAI.isEnabled()) {
            Page<CatalogItem> page = itemRepository.findByNameStartingWith(
                    text, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            return ResponseEntity.ok(new PaginatedItemsDto<>(
                    pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
        }

        float[] vector = catalogAI.getEmbedding(text);
        if (vector == null) {
            Page<CatalogItem> page = itemRepository.findByNameStartingWith(
                    text, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
            return ResponseEntity.ok(new PaginatedItemsDto<>(
                    pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
        }

        // TODO: Implement pgvector similarity search when AI is enabled
        Page<CatalogItem> page = itemRepository.findByNameStartingWith(
                text, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    /**
     * GET /items/type/{typeId}/brand/{brandId} - V1 only (with brand)
     */
    @GetMapping("/items/type/{typeId}/brand/{brandId}")
    @Operation(summary = "Get items by type and brand (V1)", description = "Filter catalog items by type and brand")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsByTypeAndBrand(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer typeId,
            @PathVariable Integer brandId,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<CatalogItem> page = itemRepository.findByCatalogTypeIdAndCatalogBrandId(
                typeId, brandId, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    /**
     * GET /items/type/{typeId}/brand - V1 only (without brand, optional in .NET as {brandId?})
     */
    @GetMapping("/items/type/{typeId}/brand")
    @Operation(summary = "Get items by type (V1)", description = "Filter catalog items by type only")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsByType(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer typeId,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<CatalogItem> page = itemRepository.findByCatalogTypeId(
                typeId, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    /**
     * GET /items/type/all/brand/{brandId} - V1 only (with brand)
     */
    @GetMapping("/items/type/all/brand/{brandId}")
    @Operation(summary = "Get items by brand (V1)", description = "Filter catalog items by brand")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsByBrand(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer brandId,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<CatalogItem> page = itemRepository.findByCatalogBrandId(
                brandId, PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    /**
     * GET /items/type/all/brand - V1 only (without brand, optional in .NET as {brandId?})
     */
    @GetMapping("/items/type/all/brand")
    @Operation(summary = "Get all items (V1)", description = "Get all catalog items")
    public ResponseEntity<PaginatedItemsDto<CatalogItemDto>> getItemsAllBrands(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<CatalogItem> page = itemRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new PaginatedItemsDto<>(
                pageIndex, pageSize, page.getTotalElements(), catalogMapper.toDtoList(page.getContent())));
    }

    // ==================== CRUD Operations ====================

    /**
     * POST /items - Both V1 and V2
     * Returns 201 Created with location header (matching .NET behavior)
     */
    @PostMapping("/items")
    @Operation(summary = "Create item", description = "Create a new catalog item")
    public ResponseEntity<Void> createItem(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @Valid @RequestBody CatalogItem item) {
        CatalogItem saved = catalogService.createItem(item);
        return ResponseEntity.created(
                java.net.URI.create("/api/catalog/items/" + saved.getId())).build();
    }

    /**
     * PUT /items/{id} - V2 only (id in path)
     * Returns 201 Created with location header (matching .NET behavior)
     */
    @PutMapping("/items/{id}")
    @Operation(summary = "Update item (V2)", description = "Update an existing catalog item")
    public ResponseEntity<Void> updateItem(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer id,
            @Valid @RequestBody CatalogItem item) {
        item.setId(id);
        return catalogService.updateItem(item)
                .<ResponseEntity<Void>>map(updated -> ResponseEntity.created(
                        java.net.URI.create("/api/catalog/items/" + id)).build())
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /items - V1 only (id in body)
     * Returns 201 Created with location header (matching .NET behavior)
     */
    @PutMapping("/items")
    @Operation(summary = "Update item (V1)", description = "Update an existing catalog item (id in body)")
    public ResponseEntity<Void> updateItemV1(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @Valid @RequestBody CatalogItem item) {
        if (item.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return catalogService.updateItem(item)
                .<ResponseEntity<Void>>map(updated -> ResponseEntity.created(
                        java.net.URI.create("/api/catalog/items/" + item.getId())).build())
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /items/{id} - Both V1 and V2
     */
    @DeleteMapping("/items/{id}")
    @Operation(summary = "Delete item", description = "Delete a catalog item")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion,
            @PathVariable Integer id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== Types and Brands ====================

    /**
     * GET /catalogtypes - Both V1 and V2
     */
    @GetMapping("/catalogTypes")
    @Operation(summary = "Get catalog types", description = "Returns all catalog types")
    public ResponseEntity<List<CatalogTypeDto>> getCatalogTypes(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion) {
        return ResponseEntity.ok(catalogMapper.toTypeDtoList(typeRepository.findAllByOrderByTypeAsc()));
    }

    /**
     * GET /catalogbrands - Both V1 and V2
     */
    @GetMapping("/catalogBrands")
    @Operation(summary = "Get catalog brands", description = "Returns all catalog brands")
    public ResponseEntity<List<CatalogBrandDto>> getCatalogBrands(
            @Parameter(description = "API version", required = true) @RequestParam("api-version") String apiVersion) {
        return ResponseEntity.ok(catalogMapper.toBrandDtoList(brandRepository.findAllByOrderByBrandAsc()));
    }

    // ==================== Helper Methods ====================

    private String getImageMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "jpg", "jpeg" -> "image/jpeg";
            case "bmp" -> "image/bmp";
            case "tiff" -> "image/tiff";
            case "wmf" -> "image/wmf";
            case "jp2" -> "image/jp2";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
