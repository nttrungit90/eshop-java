# Ordering Service Migration

**Status:** TODO (priority #5)
**.NET Source:** `src/Ordering.API/`, `src/Ordering.Domain/`, `src/Ordering.Infrastructure/`
**Java Modules:** `services/ordering/ordering-api`, `ordering-domain`, `ordering-infrastructure`
**Port:** 9102

## Technology Mapping

| .NET | Java |
|------|------|
| ASP.NET Core Minimal APIs | Spring Boot + Spring MVC (RestController) |
| EF Core (orderingdb) | Spring Data JPA |
| MediatR (CQRS) | Spring Events / ApplicationEventPublisher |
| FluentValidation | Jakarta Bean Validation |
| Domain Events | Spring @DomainEvents / ApplicationEventPublisher |
| DDD aggregates | DDD aggregates (plain Java) |

## File Mapping

### ordering-api
| .NET File | Java File |
|-----------|-----------|
| `Ordering.API/Program.cs` | `OrderingApiApplication.java` |
| `Ordering.API/Apis/OrdersApi.cs` | `api/OrdersController.java` |
| `Ordering.API/Application/Commands/CreateOrderCommand.cs` | `commands/CreateOrderCommand.java` |
| `Ordering.API/Application/Commands/CreateOrderCommandHandler.cs` | `commands/CreateOrderCommandHandler.java` |
| `Ordering.API/Application/Queries/GetOrdersFromUserQuery.cs` | `queries/OrderQueries.java` |
| `Ordering.API/Application/Validations/CreateOrderCommandValidator.cs` | (Jakarta Bean Validation on command) |
| `Ordering.API/IntegrationEvents/*` | `events/*.java` |

### ordering-domain
| .NET File | Java File |
|-----------|-----------|
| `Ordering.Domain/AggregatesModel/OrderAggregate/Order.cs` | `aggregates/order/Order.java` |
| `Ordering.Domain/AggregatesModel/OrderAggregate/OrderItem.cs` | `aggregates/order/OrderItem.java` |
| `Ordering.Domain/AggregatesModel/OrderAggregate/OrderStatus.cs` | `aggregates/order/OrderStatus.java` |
| `Ordering.Domain/AggregatesModel/BuyerAggregate/Buyer.cs` | `aggregates/buyer/Buyer.java` |
| `Ordering.Domain/AggregatesModel/BuyerAggregate/PaymentMethod.cs` | `aggregates/buyer/PaymentMethod.java` |
| `Ordering.Domain/SeedWork/Entity.cs` | `seedwork/Entity.java` |
| `Ordering.Domain/SeedWork/IAggregateRoot.cs` | `seedwork/AggregateRoot.java` |
| `Ordering.Domain/Events/*` | `events/*.java` |

### ordering-infrastructure
| .NET File | Java File |
|-----------|-----------|
| `Ordering.Infrastructure/OrderingContext.cs` | JPA annotations on entities |
| `Ordering.Infrastructure/Repositories/OrderRepository.cs` | `repositories/OrderRepository.java` |
| `Ordering.Infrastructure/Repositories/BuyerRepository.cs` | `repositories/BuyerRepository.java` |
| `Ordering.Infrastructure/EntityConfigurations/*` | JPA annotations on entities |

## Dependencies

- PostgreSQL (orderingdb)
- RabbitMQ (event bus)
- Identity service (auth — JWT for user identity)

## Integration Events

| Event | Direction |
|-------|-----------|
| `OrderStartedIntegrationEvent` | Publishes |
| `OrderStatusChangedToAwaitingValidationIntegrationEvent` | Publishes |
| `OrderStatusChangedToStockConfirmedIntegrationEvent` | Publishes |
| `OrderStatusChangedToPaidIntegrationEvent` | Publishes |
| `OrderStatusChangedToShippedIntegrationEvent` | Publishes |
| `OrderStatusChangedToCancelledIntegrationEvent` | Publishes |
| `UserCheckoutAcceptedIntegrationEvent` | Consumes |
| `OrderStockConfirmedIntegrationEvent` | Consumes |
| `OrderStockRejectedIntegrationEvent` | Consumes |
| `OrderPaymentSucceededIntegrationEvent` | Consumes |
| `OrderPaymentFailedIntegrationEvent` | Consumes |

## How to Run

While migrating (run separately for debugging):
```bash
# Terminal 1: .NET infra + remaining .NET services
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services
cd eshop-java && docker compose up -d

# Terminal 3: This service (debug)
cd eshop-java && ./mvnw -pl services/ordering/ordering-api spring-boot:run
```

Once stable, add to `docker-compose.yml` and remove from Terminal 3.

## Migration Notes

- DDD architecture: domain and infrastructure layers already exist in Java
- Only the API layer + integration event handlers need wiring
- Uses CQRS pattern — commands via MediatR mapped to Spring Events
- Shares orderingdb with order-processor
- Requires Identity for user context (Bearer token → user ID)
- .NET AppHost flag: `bool useJavaOrdering = true`
