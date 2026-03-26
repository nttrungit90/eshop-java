package com.eshop.webhooksclient.model;

import java.time.Instant;

public class WebhookReceived {
    private Instant when;
    private String data;
    private String token;

    public WebhookReceived() {}

    public WebhookReceived(Instant when, String data, String token) {
        this.when = when;
        this.data = data;
        this.token = token;
    }

    public Instant getWhen() { return when; }
    public void setWhen(Instant when) { this.when = when; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
