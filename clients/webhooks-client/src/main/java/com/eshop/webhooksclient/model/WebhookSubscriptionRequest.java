package com.eshop.webhooksclient.model;

public class WebhookSubscriptionRequest {
    private String url;
    private String token;
    private String event;
    private String grantUrl;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getGrantUrl() { return grantUrl; }
    public void setGrantUrl(String grantUrl) { this.grantUrl = grantUrl; }
}
