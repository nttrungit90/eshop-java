/**
 * Converted from: src/Ordering.Infrastructure/Repositories/OrderRepository.cs
 * .NET Class: eShop.Ordering.Infrastructure.Repositories.OrderRepository
 *
 * JPA implementation of OrderRepository.
 */
package com.eshop.ordering.infrastructure.repositories;

import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {

    @Override
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findById(Long orderId);

    @Override
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.buyerId = :buyerId ORDER BY o.orderDate DESC")
    List<Order> findByBuyerId(String buyerId);
}
