/**
 * Repository for WebhookSubscription entities.
 */
package com.eshop.webhooks.repository;

import com.eshop.webhooks.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {

    List<WebhookSubscription> findByUserId(String userId);

    List<WebhookSubscription> findByType(WebhookSubscription.WebhookType type);
}
