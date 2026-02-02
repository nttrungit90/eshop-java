/**
 * Converted from: src/Ordering.API/Program.cs
 * .NET Class: eShop.Ordering.API
 *
 * Order management REST API following DDD patterns.
 */
package com.eshop.ordering.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.ordering.api",
    "com.eshop.ordering.domain",
    "com.eshop.ordering.infrastructure",
    "com.eshop.servicedefaults",
    "com.eshop.eventbus"
})
@EntityScan(basePackages = {"com.eshop.ordering", "com.eshop.eventbus.outbox"})
@EnableJpaRepositories(basePackages = {"com.eshop.ordering.infrastructure", "com.eshop.eventbus.outbox"})
public class OrderingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderingApiApplication.class, args);
    }
}
