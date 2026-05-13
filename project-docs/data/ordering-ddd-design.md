# Ordering Service — DDD / CQRS / Outbox Design Notes

_This document captures the architectural patterns that the migrated Java
`services/ordering` module must preserve from the original .NET
`Ordering.API`. The Ordering service is the **showcase** of the migration
project — it exists to demonstrate that the canonical eShop patterns
(DDD aggregates, domain events, CQRS, idempotency, transactional outbox)
translate cleanly from C#/EF Core/MediatR to Java/Spring._

If you are tempted to inline business logic into a controller for
"simplicity," **don't**. Re-read this file first.

---

## 1. Aggregates and value objects

| .NET (`Ordering.Domain`) | Java (`ordering-domain`) | Notes |
|---|---|---|
| `Order : Entity, IAggregateRoot` | `Order extends Entity` | Aggregate root. Owns `_orderItems` privately; only mutated via `AddOrderItem()`. |
| `OrderItem : Entity` | `OrderItem extends Entity` | Inside `Order` aggregate. |
| `Buyer : Entity, IAggregateRoot` | `Buyer extends Entity` | Aggregate root. Owns `_paymentMethods` privately; only mutated via `VerifyOrAddPaymentMethod()`. |
| `PaymentMethod : Entity` | `PaymentMethod extends Entity` | Inside `Buyer` aggregate. |
| `Address` (EF owned entity) | `@Embeddable Address` | Immutable value object, structural equality. |
| `OrderStatus` (enumeration class) | `OrderStatus` (enum + converter) | Stored as `"Submitted"`, `"AwaitingValidation"`, … via `OrderStatusConverter`. |

**Rule:** Cross-aggregate updates never happen synchronously inside one
aggregate's methods. The `Order` aggregate doesn't reach into `Buyer` — it
raises a **domain event** and a handler does the work in the `Buyer`
aggregate's own transaction phase.

---

## 2. Domain events

Domain events are raised inside aggregates via `Entity.addDomainEvent(event)`
and dispatched by the persistence infrastructure when the aggregate is
saved — **never** by the controller or command handler.

| .NET event | Raised in | Handler |
|---|---|---|
| `OrderStartedDomainEvent` | `Order` constructor | `ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler` — find-or-create `Buyer`, add `PaymentMethod` |
| `BuyerAndPaymentMethodVerifiedDomainEvent` | `Buyer.VerifyOrAddPaymentMethod` | `UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler` — back-fills `Order.BuyerId` + `Order.PaymentMethodId`, publishes `OrderStatusChangedToSubmittedIntegrationEvent` to the outbox |
| `OrderStatusChangedToAwaitingValidationDomainEvent` | `Order.setAwaitingValidationStatus` | Publishes `OrderStatusChangedToAwaitingValidationIntegrationEvent` (with stock items) to the outbox |
| `OrderStatusChangedToStockConfirmedDomainEvent` | `Order.setStockConfirmedStatus` | Publishes `OrderStatusChangedToStockConfirmedIntegrationEvent` to the outbox |
| `OrderStatusChangedToPaidDomainEvent` | `Order.setPaidStatus` | Publishes `OrderStatusChangedToPaidIntegrationEvent` (with stock items) to the outbox |
| `OrderShippedDomainEvent` | `Order.setShippedStatus` | Publishes `OrderStatusChangedToShippedIntegrationEvent` to the outbox |
| `OrderCancelledDomainEvent` | `Order.setCancelledStatus` | Publishes `OrderStatusChangedToCancelledIntegrationEvent` to the outbox |

### Dispatch mechanism (Java)

.NET: `OrderingContext.SaveChangesAsync` calls `_mediator.DispatchDomainEventsAsync(this)`.

Java: We use Spring's `ApplicationEventPublisher` from inside a custom
`save()` wrapper in the repository layer. Handlers are
`@Component` classes with `@TransactionalEventListener(phase = BEFORE_COMMIT)`
so they participate in the same DB transaction as the aggregate.

```
Order ord = new Order(userId, userName, address, …);   // raises OrderStartedDomainEvent
ord.addOrderItem(…);
orderRepository.save(ord);                              // collects events
unitOfWork.dispatchDomainEvents(ord);                  // publishes via ApplicationEventPublisher
// inside same transaction:
//   ValidateOrAddBuyerAggregate…Handler runs → buyerRepository.save(buyer)
//   which raises BuyerAndPaymentMethodVerifiedDomainEvent
//   UpdateOrderWhen…Handler runs → ord.setBuyerId(), ord.setPaymentMethodId()
//                                  → orderingIntegrationEventService.saveEventToOutbox(...)
```

---

## 3. CQRS commands

Every state-changing operation goes through a command + command handler.
The controller is a thin dispatcher; business logic lives in the handler.

| .NET command | Java handler | Trigger |
|---|---|---|
| `CreateOrderCommand` | `CreateOrderCommandHandler` | `POST /api/orders` |
| `CreateOrderDraftCommand` | `CreateOrderDraftCommandHandler` | `POST /api/orders/draft` — returns DTO, no persist |
| `CancelOrderCommand` | `CancelOrderCommandHandler` | `PUT /api/orders/cancel` |
| `ShipOrderCommand` | `ShipOrderCommandHandler` | `PUT /api/orders/ship` |
| `SetAwaitingValidationOrderStatusCommand` | `SetAwaitingValidationOrderStatusCommandHandler` | Triggered by `GracePeriodConfirmedIntegrationEvent` |
| `SetStockConfirmedOrderStatusCommand` | `SetStockConfirmedOrderStatusCommandHandler` | Triggered by `OrderStockConfirmedIntegrationEvent` |
| `SetPaidOrderStatusCommand` | `SetPaidOrderStatusCommandHandler` | Triggered by `OrderPaymentSucceededIntegrationEvent` |

Integration event handlers (`OrderingEventListener`) translate the inbound
event into the appropriate command and dispatch it via the
`CommandBus` (a thin `ApplicationContext`-based dispatcher). They do
**not** mutate aggregates directly.

---

## 4. Idempotency — the `RequestManager` pattern

.NET wraps every command in `IdentifiedCommand<TCommand, TResult>`. The
`IdentifiedCommandHandler` looks up `requestId` in `ordering.requests`;
if it's already there, it returns success without re-executing.

| .NET | Java |
|---|---|
| `ClientRequest` entity | `Request` entity → `ordering.requests` |
| `IRequestManager.ExistAsync` / `CreateRequestForCommandAsync` | `RequestManager.exists` / `createRequestForCommand` |
| `IdentifiedCommand<TCmd, TResult>` | `IdempotentCommandExecutor` wraps any `CommandHandler` |

Flow:

```
controller receives x-requestid header (UUID)
  → if blank / not a valid GUID → 400
  → CommandBus.dispatch(new IdempotentCommand(requestId, CreateOrderCommand(...)))
  → IdempotentCommandExecutor:
      if requestManager.exists(requestId): return previous-result (e.g., true) without re-running
      else: handler.execute(); requestManager.createRequest(requestId, command.getClass().getSimpleName())
```

Applies to **all** state-changing commands: create, cancel, ship.

---

## 5. Transactional outbox

Integration events MUST be written to `ordering.IntegrationEventLog` in
the **same DB transaction** as the aggregate change. A background relay
publishes them to RabbitMQ separately.

```
@Transactional
handleCreateOrder(cmd) {
  orderRepository.save(order)                          // → ordering.orders
  // domain event handlers run BEFORE_COMMIT:
  //   buyerRepository.save(buyer)                     // → ordering.buyers + paymentmethods
  //   integrationEventLogService.saveEvent(           // → ordering.IntegrationEventLog
  //       new OrderStartedIntegrationEvent(userId)
  //   )
  // commit happens here — all four tables updated atomically
}

// later, in a separate background thread:
@Scheduled(fixedDelay = 5_000)
publishPendingIntegrationEvents() {
  for entry in integrationEventLogService.getAllPendingEvents():
    integrationEventLogService.markEventAsInProgress(entry.eventId)
    try:
      eventBus.publishAsync(rehydrate(entry))          // → RabbitMQ
      integrationEventLogService.markEventAsPublished(entry.eventId)
    catch:
      integrationEventLogService.markEventAsFailed(entry.eventId)   // retry next tick
}
```

**Never call `eventBus.publishAsync` directly from a command handler or
controller.** Always go through the outbox so the broker outage / app
crash between commit-and-publish can't silently drop an event.

The relay deduplicates against the `state` column — only `NOT_PUBLISHED`
and recently-`PUBLISHED_FAILED` (timesSent < 3) entries are republished.

---

## 6. Validation

FluentValidation in .NET → Jakarta Bean Validation in Java, applied to
the **command** (not the DTO), via Spring's `@Validated` on the handler.

| .NET validator | Java equivalent |
|---|---|
| `CreateOrderCommandValidator` (card 12–19, expiration future, items non-empty) | `CreateOrderCommandValidator` + Bean Validation annotations on command fields |
| `CancelOrderCommandValidator` (`OrderNumber > 0`) | Same |
| `ShipOrderCommandValidator` (`OrderNumber > 0`) | Same |
| `CreateOrderDraftCommandValidator` (BuyerId / Items) | Same |

Validation runs before the handler executes; failures throw
`OrderingDomainException` which the global `@ControllerAdvice` translates
to HTTP 400 + Problem Details.

---

## 7. Repositories and UnitOfWork

| .NET | Java |
|---|---|
| `IOrderRepository.UnitOfWork.SaveEntitiesAsync()` dispatches domain events then commits | `OrderingUnitOfWork.saveEntities(aggregate)` — drains `aggregate.getDomainEvents()` via `ApplicationEventPublisher`, then `entityManager.flush()` |
| `IBuyerRepository.FindAsync(identity)` | `BuyerRepository.findByIdentityGuid(identity)` |
| `IOrderRepository.Add(order)` / `Update(order)` | `OrderRepository.save(order)` |

Repositories return aggregates only — never DTOs.

---

## 8. Wire-format compatibility

The Java ordering-api must remain wire-compatible with .NET clients
(WebApp, mobile-bff) and with the existing data in the shared
`orderingdb`. See `eshop-java/services/ordering/ordering-api/src/main/resources/application.yml`:

- `globally_quoted_identifiers: true` — column names sent quoted, matching `"Id"`, `"BuyerId"`, etc.
- `PhysicalNamingStrategyStandardImpl` — disables Spring Boot's default snake-case rewrite
- `hibernate.id.optimizer.pooled.prefer_lo: true` — matches EF Core HiLo on Postgres sequences (`ordering.orderseq`, `ordering.orderitemseq`, `ordering.buyerseq`, `ordering.paymentseq`)
- JWT subject (`sub` claim) maps to `buyers."IdentityGuid"`; the common `JwtSecurityConfig` sets principal to `preferred_username` so we read `sub` directly via `@AuthenticationPrincipal Jwt`
- `OrderStatusConverter` maps Java `enum SUBMITTED` ↔ DB string `"Submitted"`

---

## 9. Anti-patterns explicitly avoided

1. **No business logic in controllers.** Controllers only: parse DTO, build command, dispatch. They must not call repositories or publish events directly.
2. **No `eventBus.publishAsync` outside the outbox relay.** Every integration event flows through `IntegrationEventLog`.
3. **No cross-aggregate mutation inside one aggregate's methods.** Cross-aggregate work goes through a domain event handler.
4. **No setter-based hydration from controllers.** Aggregate state changes go through methods that enforce invariants (`Order.addOrderItem`, `Buyer.verifyOrAddPaymentMethod`, `Order.setShippedStatus`).
5. **No skipping the `RequestManager`** even for "internal" commands triggered by integration events — those commands carry the integration event id as their request id.

---

## 10. File layout

```
ordering-domain/                     (no Spring deps — pure model)
  seedwork/
    Entity.java
    DomainEvent.java
  aggregates/
    order/
      Order.java
      OrderItem.java
      OrderStatus.java + OrderStatusConverter.java
      Address.java
      OrderRepository.java                   (interface)
      events/                                (domain events)
        OrderStartedDomainEvent.java
        OrderStatusChangedToAwaitingValidationDomainEvent.java
        OrderStatusChangedToStockConfirmedDomainEvent.java
        OrderStatusChangedToPaidDomainEvent.java
        OrderShippedDomainEvent.java
        OrderCancelledDomainEvent.java
    buyer/
      Buyer.java
      PaymentMethod.java
      CardType.java                           (reference data)
      BuyerRepository.java                    (interface)
      events/
        BuyerAndPaymentMethodVerifiedDomainEvent.java
  exceptions/
    OrderingDomainException.java

ordering-infrastructure/             (Spring Data JPA, repositories)
  repositories/
    JpaOrderRepository.java
    JpaBuyerRepository.java
    JpaCardTypeRepository.java
  unitofwork/
    OrderingUnitOfWork.java                   (drains + dispatches domain events)
  idempotency/
    Request.java                              (entity for ordering.requests)
    RequestRepository.java
    RequestManager.java

ordering-api/                        (Spring Boot app)
  controller/
    OrdersController.java                     (thin — dispatches to commands)
  application/
    commands/
      CreateOrderCommand.java + Handler
      CreateOrderDraftCommand.java + Handler
      CancelOrderCommand.java + Handler
      ShipOrderCommand.java + Handler
      SetAwaitingValidationOrderStatusCommand.java + Handler
      SetStockConfirmedOrderStatusCommand.java + Handler
      SetPaidOrderStatusCommand.java + Handler
    commandbus/
      CommandBus.java
      IdempotentCommandExecutor.java
    validators/
      CreateOrderCommandValidator.java
      …
    domaineventhandlers/
      ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler.java
      UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler.java
      OrderStatusChangedToAwaitingValidationDomainEventHandler.java
      OrderStatusChangedToStockConfirmedDomainEventHandler.java
      OrderStatusChangedToPaidDomainEventHandler.java
      OrderShippedDomainEventHandler.java
      OrderCancelledDomainEventHandler.java
    integrationevents/
      OrderingIntegrationEventService.java   (writes outbox + provides relay loop)
      IntegrationEventOutboxRelay.java       (@Scheduled drain + publish)
      events/                                 (integration event POJOs — wire shape)
        OrderStartedIntegrationEvent.java
        OrderStatusChangedToSubmittedIntegrationEvent.java
        OrderStatusChangedToAwaitingValidationIntegrationEvent.java
        OrderStatusChangedToStockConfirmedIntegrationEvent.java
        OrderStatusChangedToPaidIntegrationEvent.java
        OrderStatusChangedToShippedIntegrationEvent.java
        OrderStatusChangedToCancelledIntegrationEvent.java
        OrderStockItem.java + ConfirmedOrderStockItem.java
        GracePeriodConfirmedIntegrationEvent.java          (inbound)
        OrderStockConfirmedIntegrationEvent.java           (inbound)
        OrderStockRejectedIntegrationEvent.java            (inbound)
        OrderPaymentSucceededIntegrationEvent.java         (inbound)
        OrderPaymentFailedIntegrationEvent.java            (inbound)
      handlers/
        OrderingEventListener.java           (RabbitMQ listener → CommandBus)
    dto/
      CreateOrderRequest.java
      OrderDto.java
      OrderSummaryDto.java
      OrderDraftDto.java
      CardTypeDto.java
```

---

## 11. References to .NET originals

The .NET source we're mirroring lives at `eShop/src/Ordering.{API,Domain,Infrastructure}/`. When in doubt, the .NET file is the source of truth — find its Java counterpart and verify the shape matches.

Key starting points:
- `eShop/src/Ordering.API/Apis/OrdersApi.cs` — endpoint surface
- `eShop/src/Ordering.API/Application/Commands/CreateOrderCommandHandler.cs` — orchestration shape
- `eShop/src/Ordering.API/Application/DomainEventHandlers/*.cs` — domain → integration translation
- `eShop/src/Ordering.API/Application/IntegrationEvents/EventHandling/*.cs` — inbound integration event handling
- `eShop/src/Ordering.Infrastructure/Idempotency/RequestManager.cs` — idempotency
- `eShop/src/IntegrationEventLogEF/Services/IntegrationEventLogService.cs` — outbox
