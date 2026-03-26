package com.eshop.webhooksclient.model;

import java.time.Instant;

public class WebhookResponse {
    private Instant date;
    private String destUrl;
    private String token;

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
    public String getDestUrl() { return destUrl; }
    public void setDestUrl(String destUrl) { this.destUrl = destUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
