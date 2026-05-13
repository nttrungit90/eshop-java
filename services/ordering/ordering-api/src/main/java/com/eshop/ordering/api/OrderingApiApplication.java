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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.ordering.api",
    "com.eshop.ordering.domain",
    "com.eshop.ordering.infrastructure",
    "com.eshop.servicedefaults",
    "com.eshop.eventbus"
})
// Ordering provides its own outbox (OrderingIntegrationEventService + OrderingIntegrationEventLogEntry
// mapped to ordering."IntegrationEventLog"). Skip the generic com.eshop.eventbus.outbox classes from
// the shared module so they don't compete for an unused repository bean.
@ComponentScan(
    basePackages = {"com.eshop.ordering.api", "com.eshop.ordering.domain", "com.eshop.ordering.infrastructure",
                    "com.eshop.servicedefaults", "com.eshop.eventbus"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.eshop\\.eventbus\\.outbox\\..*"))
// Outbox uses ordering-api's own entity mapped to ordering."IntegrationEventLog" — the shared
// com.eshop.eventbus.outbox.* generic entity is excluded so it doesn't try to create a
// second integration_event_log table.
@EntityScan(basePackages = {"com.eshop.ordering"})
@EnableJpaRepositories(basePackages = {
        "com.eshop.ordering.infrastructure",
        "com.eshop.ordering.api.application.integrationevents"
})
@EnableScheduling   // IntegrationEventOutboxRelay drains ordering.integration_event_log
public class OrderingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderingApiApplication.class, args);
    }
}
