package com.eshop.paymentprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment")
public class PaymentOptions {

    private boolean paymentSucceeded = true;

    public boolean isPaymentSucceeded() {
        return paymentSucceeded;
    }

    public void setPaymentSucceeded(boolean paymentSucceeded) {
        this.paymentSucceeded = paymentSucceeded;
    }
}
