/**
 * Converted from: src/OrderProcessor/Program.cs
 * .NET Class: eShop.OrderProcessor
 *
 * Background worker for processing order events.
 */
package com.eshop.orderprocessor;

import com.eshop.orderprocessor.config.BackgroundTaskOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.orderprocessor",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
@EnableConfigurationProperties(BackgroundTaskOptions.class)
@EnableScheduling
public class OrderProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessorApplication.class, args);
    }
}
