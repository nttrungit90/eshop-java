/**
 * Converted from: src/Webhooks.API/Program.cs
 * .NET Class: eShop.Webhooks.API
 *
 * Webhook management and delivery service.
 */
package com.eshop.webhooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.webhooks",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
@EntityScan(basePackages = "com.eshop.webhooks")
@EnableJpaRepositories(basePackages = "com.eshop.webhooks")
public class WebhooksServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebhooksServiceApplication.class, args);
    }
}
