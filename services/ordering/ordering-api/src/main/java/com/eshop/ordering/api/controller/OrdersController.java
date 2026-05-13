package com.eshop.ordering.api.controller;

import com.eshop.ordering.api.application.commandbus.CommandBus;
import com.eshop.ordering.api.application.commandbus.IdempotentCommandExecutor;
import com.eshop.ordering.api.application.commandbus.IdentifiedCommand;
import com.eshop.ordering.api.application.commands.CancelOrderCommand;
import com.eshop.ordering.api.application.commands.CreateOrderCommand;
import com.eshop.ordering.api.application.commands.CreateOrderDraftCommand;
import com.eshop.ordering.api.application.commands.ShipOrderCommand;
import com.eshop.ordering.api.dto.CardTypeDto;
import com.eshop.ordering.api.dto.CreateOrderDraftRequest;
import com.eshop.ordering.api.dto.CreateOrderRequest;
import com.eshop.ordering.api.dto.OrderActionRequest;
import com.eshop.ordering.api.dto.OrderDraftDto;
import com.eshop.ordering.api.dto.OrderDto;
import com.eshop.ordering.api.dto.OrderSummaryDto;
import com.eshop.ordering.domain.aggregates.order.Order;
import com.eshop.ordering.domain.aggregates.order.OrderRepository;
import com.eshop.ordering.infrastructure.repositories.JpaCardTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Thin controller — parses request, builds command, dispatches via {@link IdempotentCommandExecutor}
 * for write commands or {@link CommandBus} for read/preview commands. No business logic, no
 * repository writes, no event publishing here.
 */
@RestController
@RequestMapping({"/api/orders", "/api/Orders"})
@Tag(name = "Orders", description = "Order management API")
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrderRepository orderRepository;
    private final JpaCardTypeRepository cardTypeRepository;
    private final CommandBus commandBus;
    private final IdempotentCommandExecutor idempotentExecutor;

    public OrdersController(OrderRepository orderRepository,
                            JpaCardTypeRepository cardTypeRepository,
                            CommandBus commandBus,
                            IdempotentCommandExecutor idempotentExecutor) {
        this.orderRepository = orderRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.commandBus = commandBus;
        this.idempotentExecutor = idempotentExecutor;
    }

    @PostMapping
    @Operation(summary = "Create order")
    public ResponseEntity<Void> createOrder(
            @RequestHeader(value = "x-requestid", required = false) String requestIdHeader,
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID requestId = parseGuid(requestIdHeader);
        if (requestId == null || requestId.equals(new UUID(0L, 0L))) {
            log.warn("RequestId missing/empty on createOrder");
            return ResponseEntity.badRequest().build();
        }

        String userId = request.getUserId();
        if ((userId == null || userId.isBlank()) && jwt != null) userId = jwt.getSubject();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var items = request.getItems().stream()
                .map(i -> new CreateOrderCommand.OrderItemDto(
                        i.getProductId(), i.getProductName(), i.getUnitPrice(),
                        BigDecimal.ZERO, i.getPictureUrl(), i.getQuantity()))
                .collect(Collectors.toList());

        CreateOrderCommand cmd = new CreateOrderCommand(
                userId,
                request.getUserName() != null ? request.getUserName() : "unknown",
                request.getCity(), request.getStreet(), request.getState(), request.getCountry(), request.getZipCode(),
                request.getCardNumber(), request.getCardHolderName(), request.getCardExpiration(),
                request.getCardSecurityNumber(), request.getCardTypeId(),
                items);

        Boolean ok = idempotentExecutor.execute(new IdentifiedCommand<>(requestId, cmd), Boolean.TRUE);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.unprocessableEntity().build();
    }

    @PostMapping("/draft")
    @Operation(summary = "Create order draft (no persist) — returns total + line items for UI preview")
    public ResponseEntity<OrderDraftDto> createOrderDraft(@RequestBody CreateOrderDraftRequest request) {
        var items = request.getItems().stream()
                .map(i -> new CreateOrderDraftCommand.DraftItem(
                        i.getProductId(), i.getProductName(), i.getUnitPrice(),
                        i.getDiscount(), i.getQuantity(), i.getPictureUrl()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(commandBus.send(new CreateOrderDraftCommand(request.getBuyerId(), items)));
    }

    @GetMapping("/cardtypes")
    @Operation(summary = "List card types (Amex / Visa / MasterCard)")
    public ResponseEntity<List<CardTypeDto>> getCardTypes() {
        return ResponseEntity.ok(cardTypeRepository.findAll().stream()
                .map(ct -> new CardTypeDto(ct.getId(), ct.getName()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok(toDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get the current user's orders")
    public ResponseEntity<List<OrderSummaryDto>> getUserOrders(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.ok(List.of());
        String identityGuid = jwt.getSubject();
        List<Order> orders = orderRepository.findByIdentityGuid(identityGuid);
        return ResponseEntity.ok(orders.stream()
                .map(o -> new OrderSummaryDto(o.getId(), o.getOrderDate(),
                        o.getStatus() != null ? o.getStatus().getName() : null, o.getTotal()))
                .collect(Collectors.toList()));
    }

    @PutMapping("/cancel")
    @Operation(summary = "Cancel order — body: {orderNumber}")
    public ResponseEntity<Void> cancelOrder(
            @RequestHeader(value = "x-requestid", required = false) String requestIdHeader,
            @RequestBody OrderActionRequest body) {
        UUID requestId = parseGuid(requestIdHeader);
        if (requestId == null || requestId.equals(new UUID(0L, 0L))) return ResponseEntity.badRequest().build();
        Boolean ok = idempotentExecutor.execute(
                new IdentifiedCommand<>(requestId, new CancelOrderCommand(body.getOrderNumber())), Boolean.TRUE);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.unprocessableEntity().build();
    }

    @PutMapping("/ship")
    @Operation(summary = "Ship order — body: {orderNumber}")
    public ResponseEntity<Void> shipOrder(
            @RequestHeader(value = "x-requestid", required = false) String requestIdHeader,
            @RequestBody OrderActionRequest body) {
        UUID requestId = parseGuid(requestIdHeader);
        if (requestId == null || requestId.equals(new UUID(0L, 0L))) return ResponseEntity.badRequest().build();
        Boolean ok = idempotentExecutor.execute(
                new IdentifiedCommand<>(requestId, new ShipOrderCommand(body.getOrderNumber())), Boolean.TRUE);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.unprocessableEntity().build();
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

    private static UUID parseGuid(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
    }
}
