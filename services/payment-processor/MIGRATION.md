# Payment Processor Migration

**Status:** DONE
**.NET Source:** `src/PaymentProcessor/`
**Java Module:** `services/payment-processor`
**Port:** 9106

## Technology Mapping

| .NET | Java |
|------|------|
| BackgroundService (IHostedService) | Spring Boot + @RabbitListener |
| RabbitMQ event handling | Spring AMQP |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `PaymentProcessor/Program.cs` | `PaymentProcessorApplication.java` |
| `IntegrationEvents/EventHandling/OrderStatusChangedToStockConfirmedIntegrationEventHandler.cs` | `events/OrderStockConfirmedEventHandler.java` |
| `IntegrationEvents/Events/OrderStatusChangedToStockConfirmedIntegrationEvent.cs` | `events/OrderStockConfirmedIntegrationEvent.java` |
| `IntegrationEvents/Events/OrderPaymentSucceededIntegrationEvent.cs` | `events/OrderPaymentSucceededIntegrationEvent.java` |
| `IntegrationEvents/Events/OrderPaymentFailedIntegrationEvent.cs` | `events/OrderPaymentFailedIntegrationEvent.java` |

## Dependencies

- RabbitMQ (event bus only, no database)

## Integration Events

| Event | Direction |
|-------|-----------|
| `OrderStatusChangedToStockConfirmedIntegrationEvent` | Consumes |
| `OrderPaymentSucceededIntegrationEvent` | Publishes |
| `OrderPaymentFailedIntegrationEvent` | Publishes |

## How to Run

While migrating (run separately for debugging):
```bash
# Terminal 1: .NET infra + remaining .NET services
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services
cd eshop-java && docker compose up -d

# Terminal 3: This service (debug)
cd eshop-java && ./mvnw -pl services/payment-processor spring-boot:run
```

Once stable, add to `docker-compose.yml` and remove from Terminal 3.

## Migration Notes

- Simplest service to migrate — no database, only RabbitMQ
- .NET AppHost flag: `bool useJavaPaymentProcessor = true`
- Must ensure RabbitMQ exchange/queue names match .NET conventions
