# .NET to Java Migration Map

This document provides the overall migration plan and project mapping for converting the .NET eShop application to Java/Spring Boot. For detailed per-service migration notes, see the `MIGRATION.md` file in each service directory.

## Migration Plan

### Strategy: Incremental Strangler Fig

Migrate services one-by-one while keeping the .NET Aspire AppHost running infrastructure and unmigrated services. Java services connect to Aspire-managed infra on fixed ports.

### Phases

#### Phase 1 — Migrate Services (current)

.NET Aspire AppHost manages all infrastructure (Postgres, Redis, RabbitMQ) and unmigrated .NET services. Migrated Java services run via docker-compose, while the service being actively developed/debugged runs separately via Maven.

**3 terminals during development:**
```bash
# Terminal 1: .NET infra + remaining .NET services
cd eShop
dotnet run --project src/eShop.AppHost

# Terminal 2: Already-migrated Java services (stable, run together)
cd eshop-java
docker compose up -d

# Terminal 3: Service being migrated (run separately for debugging)
cd eshop-java
./mvnw -pl services/<service-being-migrated> spring-boot:run
```

The `docker-compose.yml` contains all migrated and tested Java services. The service currently being migrated is NOT in docker-compose — it runs via Maven so you can debug, hot-reload, and iterate quickly. Once a service is stable, move it into docker-compose.

The .NET AppHost (`eShop/src/eShop.AppHost/Program.cs`) has per-service flags to switch between .NET and Java:
```csharp
bool useJavaCatalog = true;    // flip when migrated
bool useJavaOrdering = false;  // flip when migrated
// ...
```

**Workflow for each service migration:**
1. Set the AppHost flag to `true` for the service being migrated
2. Run the Java service via Maven (Terminal 3) for development/debugging
3. Once stable and tested, add the service to `docker-compose.yml`
4. Move on to the next service

#### Phase 2 — Migrate Infrastructure

After all backend services are Java, move infra to `docker-compose.yml` and drop the .NET AppHost dependency.

```bash
cd eshop-java
docker compose up -d                  # infra + observability
./mvnw spring-boot:run                # all Java services
```

#### Phase 3 — Single Command Startup

All services added to `docker-compose.yml`:
```bash
cd eshop-java
docker compose up --build
```

### Migration Order

| # | Service | Port | Dependencies | Status |
|---|---------|------|--------------|--------|
| 1 | catalog-service | 9101 | Postgres, RabbitMQ | DONE |
| 2 | payment-processor | 9106 | RabbitMQ | TODO |
| 3 | order-processor | 9105 | RabbitMQ, Postgres (orderingdb) | TODO |
| 4 | webhooks-service | 9104 | RabbitMQ, Postgres (webhooksdb) | TODO |
| 5 | ordering-api | 9102 | RabbitMQ, Postgres (orderingdb), Identity | TODO |
| 6 | basket-service | 9103 | Redis, RabbitMQ, Identity (gRPC) | TODO |
| 7 | identity-service | 9100 | Postgres (identitydb) | TODO |
| 8 | webapp | 8080 | All backend services | TODO |

### Per-Service Migration Docs

Each service has a `MIGRATION.md` with detailed file mappings, technology mapping, and migration notes:

- [common/service-defaults/MIGRATION.md](../../common/service-defaults/MIGRATION.md)
- [common/event-bus/MIGRATION.md](../../common/event-bus/MIGRATION.md)
- [common/event-bus-rabbitmq/MIGRATION.md](../../common/event-bus-rabbitmq/MIGRATION.md)
- [common/integration-event-log/MIGRATION.md](../../common/integration-event-log/MIGRATION.md)
- [services/catalog-service/MIGRATION.md](../../services/catalog-service/MIGRATION.md)
- [services/identity-service/MIGRATION.md](../../services/identity-service/MIGRATION.md)
- [services/basket-service/MIGRATION.md](../../services/basket-service/MIGRATION.md)
- [services/ordering/MIGRATION.md](../../services/ordering/MIGRATION.md)
- [services/order-processor/MIGRATION.md](../../services/order-processor/MIGRATION.md)
- [services/payment-processor/MIGRATION.md](../../services/payment-processor/MIGRATION.md)
- [services/webhooks-service/MIGRATION.md](../../services/webhooks-service/MIGRATION.md)
- [clients/webapp/MIGRATION.md](../../clients/webapp/MIGRATION.md)

## Project Structure Mapping

| .NET Project | Java Module | Package | Port |
|--------------|-------------|---------|------|
| eShop.AppHost | docker-compose.yml | - | - |
| (Aspire Dashboard) | Spring Boot Admin | - | 9090 |
| eShop.ServiceDefaults | common/service-defaults | `com.eshop.servicedefaults` | - |
| EventBus | common/event-bus | `com.eshop.eventbus` | - |
| EventBusRabbitMQ | common/event-bus-rabbitmq | `com.eshop.eventbus.rabbitmq` | - |
| IntegrationEventLogEF | common/integration-event-log | `com.eshop.eventbus.outbox` | - |
| Identity.API | services/identity-service | `com.eshop.identity` | 9100 |
| Catalog.API | services/catalog-service | `com.eshop.catalog` | 9101 |
| Basket.API | services/basket-service | `com.eshop.basket` | 9103 |
| Ordering.Domain | services/ordering/ordering-domain | `com.eshop.ordering.domain` | - |
| Ordering.Infrastructure | services/ordering/ordering-infrastructure | `com.eshop.ordering.infrastructure` | - |
| Ordering.API | services/ordering/ordering-api | `com.eshop.ordering.api` | 9102 |
| OrderProcessor | services/order-processor | `com.eshop.orderprocessor` | 9105 |
| PaymentProcessor | services/payment-processor | `com.eshop.paymentprocessor` | 9106 |
| Webhooks.API | services/webhooks-service | `com.eshop.webhooks` | 9104 |
| WebApp | clients/webapp | - | 8080 |

## Technology Mapping

| .NET Technology | Java Equivalent |
|-----------------|-----------------|
| .NET Aspire | Docker Compose |
| Aspire Dashboard | Spring Boot Admin |
| ASP.NET Core | Spring Boot |
| Entity Framework Core | Spring Data JPA + Hibernate |
| EF Core Migrations | Flyway |
| Duende IdentityServer | Spring Authorization Server |
| MediatR | Spring Events + ApplicationEventPublisher |
| FluentValidation | Jakarta Bean Validation |
| gRPC (C#) | grpc-spring-boot-starter |
| OpenTelemetry (.NET) | Micrometer + OpenTelemetry |
| Polly (Resilience) | Resilience4j |
| Blazor SSR | React + TypeScript |
| xUnit | JUnit 5 + Testcontainers |

## Integration Events Mapping

| .NET Event | Java Event |
|------------|------------|
| `ProductPriceChangedIntegrationEvent` | `com.eshop.catalog.events.ProductPriceChangedIntegrationEvent` |
| `OrderStartedIntegrationEvent` | `com.eshop.ordering.api.events.OrderStartedEvent` |
| `OrderStatusChangedToAwaitingValidationIntegrationEvent` | `com.eshop.ordering.api.events.OrderAwaitingValidationEvent` |
| `OrderStatusChangedToStockConfirmedIntegrationEvent` | `com.eshop.ordering.api.events.OrderStockConfirmedEvent` |
| `OrderStatusChangedToPaidIntegrationEvent` | `com.eshop.ordering.api.events.OrderPaidEvent` |
| `OrderStatusChangedToShippedIntegrationEvent` | `com.eshop.ordering.api.events.OrderShippedEvent` |
| `OrderStatusChangedToCancelledIntegrationEvent` | `com.eshop.ordering.api.events.OrderCancelledEvent` |

## Dashboard & Observability

| Aspire Dashboard Feature | Java Equivalent |
|--------------------------|-----------------|
| Service list & health | Spring Boot Admin - Applications |
| Logs aggregation | Spring Boot Admin - Logfile |
| Metrics | Spring Boot Admin - Metrics (Micrometer) |
| Distributed traces | Jaeger UI (http://localhost:16686) |
| Environment variables | Spring Boot Admin - Environment |

## Access URLs

| Service | URL |
|---------|-----|
| WebApp | http://localhost:8080 |
| Spring Boot Admin (Dashboard) | http://localhost:9090 |
| Jaeger (Tracing) | http://localhost:16686 |
| RabbitMQ Management | http://localhost:15672 |
| Identity Service | http://localhost:9100 |
| Catalog Service | http://localhost:9101 |
| Ordering Service | http://localhost:9102 |
| Basket Service | http://localhost:9103 |
| Webhooks Service | http://localhost:9104 |
| Order Processor | http://localhost:9105 |
| Payment Processor | http://localhost:9106 |
