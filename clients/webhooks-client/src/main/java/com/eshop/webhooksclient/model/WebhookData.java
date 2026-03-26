package com.eshop.webhooksclient.model;

import java.time.Instant;

public class WebhookData {
    private Instant when;
    private String payload;
    private String type;

    public Instant getWhen() { return when; }
    public void setWhen(Instant when) { this.when = when; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
