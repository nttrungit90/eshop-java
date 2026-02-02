/**
 * Converted from: src/Webhooks.API/Model/WebhookSubscription.cs
 * .NET Class: eShop.Webhooks.API.Model.WebhookSubscription
 *
 * Webhook subscription entity.
 */
package com.eshop.webhooks.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String destUrl;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookType type;

    @Column(nullable = false)
    private Instant createdAt;

    private String token;

    public WebhookSubscription() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDestUrl() { return destUrl; }
    public void setDestUrl(String destUrl) { this.destUrl = destUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public WebhookType getType() { return type; }
    public void setType(WebhookType type) { this.type = type; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public enum WebhookType {
        ORDER_SHIPPED,
        ORDER_PAID
    }
}
