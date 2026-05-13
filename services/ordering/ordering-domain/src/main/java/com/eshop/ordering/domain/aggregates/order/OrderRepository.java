package com.eshop.ordering.domain.aggregates.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    /**
     * Find all orders for a given Keycloak identity (sub claim).
     * Joins through ordering.buyers."IdentityGuid".
     */
    List<Order> findByIdentityGuid(String identityGuid);
}
