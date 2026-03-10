# Order Processor Migration

**Status:** TODO (priority #3)
**.NET Source:** `src/OrderProcessor/`
**Java Module:** `services/order-processor`
**Port:** 9105

## Technology Mapping

| .NET | Java |
|------|------|
| BackgroundService (IHostedService) | Spring Boot + @RabbitListener |
| EF Core (orderingdb) | Spring Data JPA |
| MediatR | Spring Events / ApplicationEventPublisher |
| GracePeriodManagerService (timer) | @Scheduled |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `OrderProcessor/Program.cs` | `OrderProcessorApplication.java` |
| `Events/GracePeriodConfirmedIntegrationEvent.cs` | `events/GracePeriodConfirmedIntegrationEvent.java` |
| `Events/OrderStatusChangedToAwaitingValidationIntegrationEventHandler.cs` | `events/OrderAwaitingValidationEventHandler.java` |
| `Events/OrderStatusChangedToPaidIntegrationEventHandler.cs` | `events/OrderPaidEventHandler.java` |

## Dependencies

- RabbitMQ (event bus)
- PostgreSQL (orderingdb — shared with ordering-api)

## Integration Events

| Event | Direction |
|-------|-----------|
| `GracePeriodConfirmedIntegrationEvent` | Publishes |
| `OrderStatusChangedToAwaitingValidationIntegrationEvent` | Consumes |
| `OrderStatusChangedToPaidIntegrationEvent` | Consumes |

## How to Run

While migrating (run separately for debugging):
```bash
# Terminal 1: .NET infra + remaining .NET services
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services
cd eshop-java && docker compose up -d

# Terminal 3: This service (debug)
cd eshop-java && ./mvnw -pl services/order-processor spring-boot:run
```

Once stable, add to `docker-compose.yml` and remove from Terminal 3.

## Migration Notes

- Shares orderingdb with ordering-api — same schema, same JPA entities
- Reuse `ordering-domain` and `ordering-infrastructure` modules
- Background timer checks for orders past grace period
- .NET AppHost flag: `bool useJavaOrderProcessor = true`
