/**
 * Converted from: src/Ordering.Domain/AggregatesModel/OrderAggregate/IOrderRepository.cs
 * .NET Interface: eShop.Ordering.Domain.AggregatesModel.OrderAggregate.IOrderRepository
 *
 * Repository interface for Order aggregate.
 */
package com.eshop.ordering.domain.aggregates.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    List<Order> findByBuyerId(String buyerId);
}
