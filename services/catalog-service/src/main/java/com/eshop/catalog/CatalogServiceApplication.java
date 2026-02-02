/**
 * Converted from: src/Catalog.API/Program.cs
 * .NET Class: eShop.Catalog.API
 *
 * Product catalog service with AI semantic search capabilities.
 */
package com.eshop.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.catalog",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
@EntityScan(basePackages = {"com.eshop.catalog", "com.eshop.eventbus.outbox"})
@EnableJpaRepositories(basePackages = {"com.eshop.catalog", "com.eshop.eventbus.outbox"})
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
