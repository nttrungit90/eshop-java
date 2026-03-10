/**
 * Converted from: src/PaymentProcessor/Program.cs
 * .NET Class: eShop.PaymentProcessor
 *
 * Background worker for processing payment events.
 */
package com.eshop.paymentprocessor;

import com.eshop.paymentprocessor.config.PaymentOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.paymentprocessor",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
@EnableConfigurationProperties(PaymentOptions.class)
public class PaymentProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessorApplication.class, args);
    }
}
