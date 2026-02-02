/**
 * Converted from: src/Catalog.API/Infrastructure/CatalogContextSeed.cs
 * .NET Class: eShop.Catalog.API.Infrastructure.CatalogContextSeed
 *
 * Initial data seeder for catalog items from catalog.json.
 */
package com.eshop.catalog.config;

import com.eshop.catalog.model.CatalogBrand;
import com.eshop.catalog.model.CatalogItem;
import com.eshop.catalog.model.CatalogType;
import com.eshop.catalog.repository.CatalogBrandRepository;
import com.eshop.catalog.repository.CatalogItemRepository;
import com.eshop.catalog.repository.CatalogTypeRepository;
import com.eshop.catalog.service.CatalogAI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CatalogItemRepository itemRepository;
    private final CatalogTypeRepository typeRepository;
    private final CatalogBrandRepository brandRepository;
    private final CatalogAI catalogAI;
    private final ObjectMapper objectMapper;

    public DataInitializer(
            CatalogItemRepository itemRepository,
            CatalogTypeRepository typeRepository,
            CatalogBrandRepository brandRepository,
            CatalogAI catalogAI,
            ObjectMapper objectMapper) {
        this.itemRepository = itemRepository;
        this.typeRepository = typeRepository;
        this.brandRepository = brandRepository;
        this.catalogAI = catalogAI;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        if (itemRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        try {
            seedFromJson();
        } catch (Exception e) {
            log.error("Failed to seed from catalog.json, using fallback data", e);
            seedFallbackData();
        }
    }

    private void seedFromJson() throws Exception {
        log.info("Loading catalog data from catalog.json...");

        ClassPathResource resource = new ClassPathResource("setup/catalog.json");
        List<CatalogSourceEntry> sourceItems;

        try (InputStream inputStream = resource.getInputStream()) {
            sourceItems = objectMapper.readValue(inputStream, new TypeReference<List<CatalogSourceEntry>>() {});
        }

        // Extract unique types and brands
        List<String> typeNames = sourceItems.stream()
                .map(CatalogSourceEntry::getType)
                .filter(t -> t != null)
                .distinct()
                .sorted()
                .toList();

        List<String> brandNames = sourceItems.stream()
                .map(CatalogSourceEntry::getBrand)
                .filter(b -> b != null)
                .distinct()
                .sorted()
                .toList();

        // Seed types
        log.info("Seeding {} catalog types...", typeNames.size());
        Map<String, CatalogType> typesByName = new HashMap<>();
        for (String typeName : typeNames) {
            CatalogType type = new CatalogType(typeName);
            type = typeRepository.save(type);
            typesByName.put(typeName, type);
        }

        // Seed brands
        log.info("Seeding {} catalog brands...", brandNames.size());
        Map<String, CatalogBrand> brandsByName = new HashMap<>();
        for (String brandName : brandNames) {
            CatalogBrand brand = new CatalogBrand(brandName);
            brand = brandRepository.save(brand);
            brandsByName.put(brandName, brand);
        }

        // Seed items
        log.info("Seeding {} catalog items...", sourceItems.size());
        List<CatalogItem> items = sourceItems.stream()
                .filter(source -> source.getName() != null && source.getBrand() != null && source.getType() != null)
                .map(source -> {
                    CatalogItem item = new CatalogItem();
                    item.setName(source.getName());
                    item.setDescription(source.getDescription());
                    item.setPrice(BigDecimal.valueOf(source.getPrice()));
                    item.setCatalogType(typesByName.get(source.getType()));
                    item.setCatalogBrand(brandsByName.get(source.getBrand()));
                    item.setAvailableStock(100);
                    item.setMaxStockThreshold(200);
                    item.setRestockThreshold(10);
                    item.setPictureFileName(source.getId() + ".webp");
                    return item;
                })
                .collect(Collectors.toList());

        // Generate AI embeddings if enabled
        if (catalogAI.isEnabled()) {
            log.info("Generating {} embeddings...", items.size());
            List<float[]> embeddings = catalogAI.getEmbeddings(items);
            if (embeddings != null) {
                for (int i = 0; i < items.size() && i < embeddings.size(); i++) {
                    items.get(i).setEmbedding(embeddings.get(i));
                }
            }
        }

        itemRepository.saveAll(items);
        log.info("Successfully seeded {} catalog items", items.size());
    }

    private void  seedFallbackData() {
        log.info("Using fallback seed data...");

        // Seed types
        List<CatalogType> types = List.of(
                new CatalogType("Mug"),
                new CatalogType("T-Shirt"),
                new CatalogType("Sheet"),
                new CatalogType("USB Memory Stick")
        );
        typeRepository.saveAll(types);

        // Seed brands
        List<CatalogBrand> brands = List.of(
                new CatalogBrand("Azure"),
                new CatalogBrand(".NET"),
                new CatalogBrand("Visual Studio"),
                new CatalogBrand("SQL Server"),
                new CatalogBrand("Other")
        );
        brandRepository.saveAll(brands);

        // Seed items
        CatalogType mugType = typeRepository.findByType("Mug").orElseThrow();
        CatalogType tshirtType = typeRepository.findByType("T-Shirt").orElseThrow();
        CatalogType sheetType = typeRepository.findByType("Sheet").orElseThrow();
        CatalogBrand dotnetBrand = brandRepository.findByBrand(".NET").orElseThrow();
        CatalogBrand azureBrand = brandRepository.findByBrand("Azure").orElseThrow();

        List<CatalogItem> items = List.of(
                createItem(".NET Bot Black Hoodie", ".NET Bot Black Hoodie", new BigDecimal("19.50"), tshirtType, dotnetBrand, "1.webp", 100),
                createItem(".NET Black & White Mug", ".NET Black & White Mug", new BigDecimal("8.50"), mugType, dotnetBrand, "2.webp", 89),
                createItem("Prism White T-Shirt", "Prism White T-Shirt", new BigDecimal("12.00"), tshirtType, dotnetBrand, "3.webp", 56),
                createItem(".NET Foundation T-shirt", ".NET Foundation T-shirt", new BigDecimal("12.00"), tshirtType, dotnetBrand, "4.webp", 120),
                createItem("Roslyn Red Sheet", "Roslyn Red Sheet", new BigDecimal("8.50"), sheetType, dotnetBrand, "5.webp", 55),
                createItem(".NET Blue Hoodie", ".NET Blue Hoodie", new BigDecimal("12.00"), tshirtType, dotnetBrand, "6.webp", 17),
                createItem("Roslyn Red T-Shirt", "Roslyn Red T-Shirt", new BigDecimal("12.00"), tshirtType, dotnetBrand, "7.webp", 8),
                createItem("Kudu Purple Hoodie", "Kudu Purple Hoodie", new BigDecimal("8.50"), tshirtType, dotnetBrand, "8.webp", 34),
                createItem("Cup<T> White Mug", "Cup<T> White Mug", new BigDecimal("12.00"), mugType, azureBrand, "9.webp", 76),
                createItem(".NET Foundation Sheet", ".NET Foundation Sheet", new BigDecimal("12.00"), sheetType, dotnetBrand, "10.webp", 11),
                createItem("Cup<T> Sheet", "Cup<T> Sheet", new BigDecimal("8.50"), sheetType, azureBrand, "11.webp", 3),
                createItem("Prism White TShirt", "Prism White TShirt", new BigDecimal("12.00"), tshirtType, dotnetBrand, "12.webp", 0)
        );
        itemRepository.saveAll(items);
        log.info("Seeded {} fallback catalog items", items.size());
    }

    private CatalogItem createItem(String name, String description, BigDecimal price,
                                   CatalogType type, CatalogBrand brand, String picture, int stock) {
        CatalogItem item = new CatalogItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCatalogType(type);
        item.setCatalogBrand(brand);
        item.setPictureFileName(picture);
        item.setAvailableStock(stock);
        item.setRestockThreshold(10);
        item.setMaxStockThreshold(200);
        return item;
    }

    // DTO for parsing catalog.json
    private static class CatalogSourceEntry {
        private int id;
        private String type;
        private String brand;
        private String name;
        private String description;
        private double price;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}
