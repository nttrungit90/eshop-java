/**
 * Converted from: src/Basket.API/Program.cs
 * .NET Class: eShop.Basket.API
 *
 * Shopping basket service with Redis and gRPC support.
 */
package com.eshop.basket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.basket",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
public class BasketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasketServiceApplication.class, args);
    }
}

