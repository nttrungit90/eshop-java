/**
 * Converted from: src/Ordering.API/Apis/OrdersApi.cs
 * .NET Class: eShop.Ordering.API.Apis.OrdersApi
 *
 * REST API controller for order operations.
 */
package com.eshop.ordering.api.controller;

import com.eshop.ordering.api.dto.CreateOrderRequest;
import com.eshop.ordering.api.dto.OrderDto;
import com.eshop.ordering.domain.aggregates.order.Address;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management API")
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrderRepository orderRepository;

    public OrdersController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order")
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal) {

        String buyerId = principal != null ? principal.getName() : "anonymous";

        Address address = new Address(
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getCountry(),
                request.getZipCode()
        );

        Order order = new Order(
                buyerId,
                address,
                request.getCardTypeId(),
                request.getCardNumber(),
                request.getCardSecurityNumber(),
                request.getCardHolderName(),
                request.getCardExpiration() != null ? request.getCardExpiration() : Instant.now().plusSeconds(31536000),
                null
        );

        for (CreateOrderRequest.OrderItemDto item : request.getItems()) {
            order.addOrderItem(
                    item.getProductId(),
                    item.getProductName(),
                    item.getUnitPrice(),
                    item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO,
                    item.getPictureUrl(),
                    item.getUnits()
            );
        }

        Order saved = orderRepository.save(order);
        log.info("Created order {} for buyer {}", saved.getId(), buyerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Get order by ID")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok(toDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get user orders", description = "Get all orders for the current user")
    public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {
        String buyerId = principal != null ? principal.getName() : "anonymous";
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        List<OrderDto> dtos = orders.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{orderId}/ship")
    @Operation(summary = "Ship order", description = "Mark order as shipped")
    public ResponseEntity<Void> shipOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setShippedStatus();
                    orderRepository.save(order);
                    log.info("Order {} shipped", orderId);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setCancelledStatus();
                    orderRepository.save(order);
                    log.info("Order {} cancelled", orderId);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getId());
        dto.setDate(order.getOrderDate());
        dto.setStatus(order.getStatus().getName());
        dto.setDescription(order.getDescription());

        if (order.getAddress() != null) {
            dto.setStreet(order.getAddress().getStreet());
            dto.setCity(order.getAddress().getCity());
            dto.setState(order.getAddress().getState());
            dto.setCountry(order.getAddress().getCountry());
            dto.setZipCode(order.getAddress().getZipCode());
        }

        dto.setOrderItems(order.getOrderItems().stream().map(item -> {
            OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
            itemDto.setProductName(item.getProductName());
            itemDto.setUnits(item.getUnits());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setPictureUrl(item.getPictureUrl());
            return itemDto;
        }).collect(Collectors.toList()));

        dto.setTotal(order.getTotal());
        return dto;
    }
}
