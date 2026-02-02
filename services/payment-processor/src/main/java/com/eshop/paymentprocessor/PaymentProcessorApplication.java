/**
 * Converted from: src/PaymentProcessor/Program.cs
 * .NET Class: eShop.PaymentProcessor
 *
 * Background worker for processing payment events.
 */
package com.eshop.paymentprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.eshop.paymentprocessor",
    "com.eshop.eventbus",
    "com.eshop.servicedefaults"
})
public class PaymentProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessorApplication.class, args);
    }
}
