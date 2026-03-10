# Basket Service Migration

**Status:** TODO (priority #6)
**.NET Source:** `src/Basket.API/`
**Java Module:** `services/basket-service`
**Port:** 9103 (HTTP), 9113 (gRPC)

## Technology Mapping

| .NET | Java |
|------|------|
| ASP.NET Core + gRPC | Spring Boot + grpc-spring-boot-starter |
| StackExchange.Redis | Spring Data Redis (Lettuce) |
| Protobuf (C# codegen) | Protobuf (protobuf-maven-plugin) |
| IDistributedCache | RedisTemplate / @Cacheable |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `Basket.API/Program.cs` | `BasketServiceApplication.java` |
| `Basket.API/Grpc/BasketService.cs` | `grpc/BasketGrpcService.java` |
| `Basket.API/Model/CustomerBasket.cs` | `model/CustomerBasket.java` |
| `Basket.API/Model/BasketItem.cs` | `model/BasketItem.java` |
| `Basket.API/Repositories/RedisBasketRepository.cs` | `repository/RedisBasketRepository.java` |
| `Basket.API/IntegrationEvents/*` | `events/*.java` |
| `Basket.API/Proto/basket.proto` | `src/main/proto/basket.proto` |

## Dependencies

- Redis (basket data cache)
- RabbitMQ (event bus)
- Identity service (auth — user identity from JWT)

## Integration Events

| Event | Direction |
|-------|-----------|
| `UserCheckoutAcceptedIntegrationEvent` | Publishes |
| `OrderStartedIntegrationEvent` | Consumes (clears basket) |
| `ProductPriceChangedIntegrationEvent` | Consumes (updates prices) |

## How to Run

While migrating (run separately for debugging):
```bash
# Terminal 1: .NET infra + remaining .NET services
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services
cd eshop-java && docker compose up -d

# Terminal 3: This service (debug)
cd eshop-java && ./mvnw -pl services/basket-service spring-boot:run
```

Once stable, add to `docker-compose.yml` and remove from Terminal 3.

## Migration Notes

- gRPC service is the primary API (used by WebApp via gRPC-Web)
- Redis stores basket as JSON, keyed by user ID
- Requires Identity for auth — last service before identity migration
- .NET AppHost flag: `bool useJavaBasket = true`
