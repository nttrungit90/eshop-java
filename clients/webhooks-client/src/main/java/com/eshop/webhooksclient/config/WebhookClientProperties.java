package com.eshop.webhooksclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "webhooks")
public class WebhookClientProperties {

    private String apiUrl = "http://localhost:9104";
    private String token = "6168DB8D-DC58-4094-AF24-483278923590";
    private String selfUrl = "http://localhost:9107";
    private boolean validateToken = true;

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getSelfUrl() { return selfUrl; }
    public void setSelfUrl(String selfUrl) { this.selfUrl = selfUrl; }
    public boolean isValidateToken() { return validateToken; }
    public void setValidateToken(boolean validateToken) { this.validateToken = validateToken; }
}
