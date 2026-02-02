/**
 * Converted from: src/eShop.ServiceDefaults/Extensions.cs
 * .NET Class: eShop.ServiceDefaults.Extensions
 *
 * Spring Boot autoconfiguration for common service defaults including
 * health checks, OpenTelemetry, and Spring Boot Admin integration.
 */
package com.eshop.servicedefaults.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration(after = HealthEndpointAutoConfiguration.class)
@Configuration
public class ServiceDefaultsAutoConfiguration {

    /**
     * Default liveness health indicator.
     * Equivalent to .NET's AddCheck("self", () => HealthCheckResult.Healthy(), ["live"])
     */
    @Bean
    @ConditionalOnMissingBean(name = "livenessHealthIndicator")
    public HealthIndicator livenessHealthIndicator() {
        return () -> org.springframework.boot.actuate.health.Health.up()
                .withDetail("status", "Service is alive")
                .build();
    }
}
