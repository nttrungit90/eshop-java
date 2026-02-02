/**
 * Converted from: src/OrderProcessor/Program.cs
 * .NET Class: eShop.OrderProcessor
 *
 * Background worker for processing order events.
 */
package com.eshop.orderprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.orderprocessor",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
public class OrderProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessorApplication.class, args);
    }
}
