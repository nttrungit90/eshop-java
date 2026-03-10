# Webhooks Service Migration

**Status:** DONE
**.NET Source:** `src/Webhooks.API/`
**Java Module:** `services/webhooks-service`
**Port:** 9104

## Technology Mapping

| .NET | Java |
|------|------|
| ASP.NET Core Minimal APIs | Spring Boot + Spring MVC (RestController) |
| EF Core (webhooksdb) | Spring Data JPA |
| IHttpClientFactory | RestTemplate / WebClient |
| BackgroundService for delivery | @Async + @RabbitListener |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `Webhooks.API/Program.cs` | `WebhooksServiceApplication.java` |
| `Apis/WebhooksApi.cs` | `api/WebhooksController.java` |
| `Model/WebhookSubscription.cs` | `model/WebhookSubscription.java` |
| `Model/WebhookData.cs` | `model/WebhookData.java` |
| `Services/WebhooksSender.cs` | `services/WebhooksSender.java` |
| `IntegrationEvents/*` | `events/*.java` |

## Dependencies

- PostgreSQL (webhooksdb)
- RabbitMQ (event bus)
- Identity service (auth)

## Integration Events

| Event | Direction |
|-------|-----------|
| `OrderStatusChangedToPaidIntegrationEvent` | Consumes |
| `OrderStatusChangedToShippedIntegrationEvent` | Consumes |
| `ProductPriceChangedIntegrationEvent` | Consumes |

## How to Run

While migrating (run separately for debugging):
```bash
# Terminal 1: .NET infra + remaining .NET services
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services
cd eshop-java && docker compose up -d

# Terminal 3: This service (debug)
cd eshop-java && ./mvnw -pl services/webhooks-service spring-boot:run
```

Once stable, add to `docker-compose.yml` and remove from Terminal 3.

## Migration Notes

- Requires Identity service for auth (JWT validation)
- Webhook delivery uses HTTP callbacks to registered URLs
- .NET AppHost flag: `bool useJavaWebhooks = true`
