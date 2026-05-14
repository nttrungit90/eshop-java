# eShop — Java migration of [dotnet/eShop](https://github.com/dotnet/eShop)

A faithful Java / Spring Boot port of the .NET reference shop, runnable end-to-end from a single `docker compose up -d`. The Ordering service is the architectural showcase — DDD aggregates, domain events, CQRS commands, idempotency, and a transactional outbox — preserved 1:1 from the .NET original. See [`project-docs/data/ordering-ddd-design.md`](project-docs/data/ordering-ddd-design.md) for the full pattern description.

## Quick Start

```bash
# Build the React SPA bundle (the SPA Dockerfile copies the prebuilt dist/)
(cd clients/webapp && npm install && npm run build)

# Build the Java fat jars
./mvnw clean package -DskipTests

# Bring everything up — infra + Java services + Keycloak + observability + SPA
docker compose up -d --build
```

The stack is self-contained — there's no `dotnet run` requirement.

## Access Points

| | URL | Credentials |
|---|---|---|
| **WebApp (React)** | http://localhost:8080 | Log in via Keycloak: `alice` / `Pass123$` or `bob` / `Pass123$` |
| **Mobile BFF (Spring Cloud Gateway)** | http://localhost:11632 | bearer token from Keycloak |
| **Keycloak admin** | http://localhost:8180/admin | `admin` / `admin` |
| **Spring Boot Admin** | http://localhost:9090 | – |
| **Jaeger** | http://localhost:16686 | – |
| **RabbitMQ management** | http://localhost:15672 | `guest` / `WBpzyj95KTuVkpGxR5Fx1j` |
| **Project docs portal** | http://localhost:3333 | – |
| **Webhooks Client demo** | http://localhost:9107 | login via Keycloak |

### Service health checks

```bash
curl -s http://localhost:9101/actuator/health | jq    # Catalog
curl -s http://localhost:9102/actuator/health | jq    # Ordering
curl -s http://localhost:9103/actuator/health | jq    # Basket
curl -s http://localhost:9104/actuator/health | jq    # Webhooks
curl -s http://localhost:9105/actuator/health | jq    # Order Processor
curl -s http://localhost:9106/actuator/health | jq    # Payment Processor
curl -s http://localhost:9107/actuator/health | jq    # Webhooks Client
curl -s http://localhost:11632/actuator/health | jq   # Mobile BFF
```

## Architecture at a glance

```
Browser ──▶ React SPA (nginx :8080)
                │
                ├── /api/catalog ──▶ catalog-service  :9101 ──▶ Postgres (catalogdb)
                ├── /api/basket  ──▶ basket-service   :9103 ──▶ Redis
                └── /api/orders  ──▶ ordering-api     :9102 ──▶ Postgres (orderingdb)
                                                              ──▶ outbox → RabbitMQ
                                                                     │
        ┌────────────────────────────────────────────────────────────┘
        ▼                                ▼                            ▼
catalog-service           payment-processor                    webhooks-service ──▶ webhooks-client
(stock check)             (succeeded/failed)                   (fan-out)               :9107

           ▲ Keycloak realm `eshop` at :8180 issues OIDC tokens for the SPA and validates them at every backend.
```

Full service / infra port map, business flow, and integration event topology: [`project-docs/data/architecture.md`](project-docs/data/architecture.md).

## What's in the box

| | |
|---|---|
| **Language** | Java 21 LTS |
| **Framework** | Spring Boot 3.4.x |
| **Build** | Maven (multi-module reactor) |
| **Database** | PostgreSQL 17 (`ankane/pgvector`) |
| **Cache** | Redis 8.2 |
| **Message broker** | RabbitMQ 4.2 |
| **Identity** | Keycloak 26.1 (realm `eshop`) — replaces Duende IdentityServer |
| **Tracing** | Jaeger + OpenTelemetry OTLP |
| **Monitoring** | Spring Boot Admin (codecentric) |
| **Frontend** | React 18 + Vite + Tailwind + nginx — replaces the .NET Blazor Server WebApp |

## Project Structure

```
eshop-java/
├── common/                              # Shared Spring Boot libraries
│   ├── service-defaults/                # Autoconfig + JwtSecurityConfig (Keycloak)
│   ├── event-bus/                       # IntegrationEvent base + abstractions
│   ├── event-bus-rabbitmq/              # RabbitMQ EventBus (tolerant class mapper)
│   └── integration-event-log/           # Outbox primitives (generic)
│
├── services/                            # Microservices
│   ├── catalog-service/                 # Catalog + stock check  (port 9101)
│   ├── basket-service/                  # REST + gRPC            (9103 / 9113)
│   ├── ordering/                        # DDD showcase           (port 9102)
│   │   ├── ordering-domain/             #   Aggregates + value objects + domain events
│   │   ├── ordering-infrastructure/     #   JPA repos + RequestManager (idempotency)
│   │   └── ordering-api/                #   Controller (thin) + CQRS commands/handlers
│   │                                    #     + outbox relay + domain event handlers
│   ├── order-processor/                 # Grace-period @Scheduled  (port 9105)
│   ├── payment-processor/               # Saga step                (port 9106)
│   ├── webhooks-service/                # Subscription fan-out     (port 9104)
│   └── mobile-bff/                      # Spring Cloud Gateway     (port 11632)
│
├── clients/
│   ├── webapp/                          # React 18 SPA + nginx     (port 8080)
│   └── webhooks-client/                 # OAuth2 subscriber demo   (port 9107)
│
├── infrastructure/
│   ├── keycloak/eshop-realm.json        # Imported on first boot
│   └── postgres/init.sql                # Creates catalogdb / orderingdb / webhooksdb on fresh volume
│
├── project-docs/                        # Documentation portal — http://localhost:3333
│   ├── data/                            # markdown sources
│   └── Dockerfile                       # node server image
│
└── docker-compose.yml                   # Single-command startup
```

## The Ordering showcase

The Ordering service is the centrepiece of this migration — it demonstrates that the canonical eShop architectural patterns translate cleanly from C#/EF Core/MediatR to Java/Spring:

| .NET | Java |
|---|---|
| Aggregates with private collections + invariants in methods | Same — `Order.addOrderItem()`, `Buyer.verifyOrAddPaymentMethod()` |
| Domain events raised inside aggregates, dispatched at save time | Spring Data `@DomainEvents` + `@EventListener` (BEFORE_COMMIT semantics) |
| 7 MediatR commands + handlers (`CreateOrderCommand`, `Set*OrderStatusCommand`, …) | Spring `CommandBus` + 7 handlers with the same names |
| `IdentifiedCommand<TCmd, TResult>` for idempotency | `IdempotentCommandExecutor` + `RequestManager` (`ordering.requests` table) |
| Transactional outbox via `IntegrationEventLogEntry` | `OrderingIntegrationEventLogEntry` mapped to `ordering."IntegrationEventLog"`, drained by `@Scheduled` relay |
| EF Core HiLo on Postgres sequences | Hibernate `@SequenceGenerator(allocationSize=10)` + `hibernate.id.optimizer.pooled.prefer_lo=true` |
| FluentValidation | Jakarta Bean Validation (`@Size`, `@FutureOrPresent`, `@Min`) |

See [`project-docs/data/ordering-ddd-design.md`](project-docs/data/ordering-ddd-design.md) for the full pattern walkthrough and file layout.

## Migration journey

11 phases total, all complete. Progress + per-task notes: [`project-docs/data/migration-progress.md`](project-docs/data/migration-progress.md).

| Phase | What |
|---|---|
| 0 | Common libraries + Catalog |
| 1 | Payment Processor |
| 2 | Order Processor |
| 3 | Webhooks Service |
| **4** | **Ordering API — DDD / CQRS / Outbox / Idempotency** |
| 5 | Basket Service (REST + gRPC) |
| 6 | Identity → Keycloak |
| 7 | React SPA replaces Blazor WebApp |
| 8 | Postgres / Redis / RabbitMQ moved to docker-compose |
| 9 | Mobile BFF (out-of-plan) |
| 10 | Webhooks Client demo (out-of-plan) |

## Development

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker / Docker Compose
- Node.js 20+ (for the React SPA)

### Build

```bash
# All Java modules
./mvnw clean package -DskipTests

# Single module
./mvnw -pl services/catalog-service -am clean package -DskipTests

# Common library — needs `install` so consumer fat-jars pick up the new SNAPSHOT
./mvnw -pl common/event-bus-rabbitmq clean install -DskipTests

# React SPA (Dockerfile copies the prebuilt dist/)
cd clients/webapp && npm install && npm run build
```

### Run

```bash
docker compose up -d --build
```

That's it. Brings up Postgres / Redis / RabbitMQ / Keycloak / Spring Boot Admin / Jaeger / all 8 Java services / React SPA.

### Watch the saga end-to-end

After placing an order at http://localhost:8080/checkout, follow the state machine:

```bash
# Postgres password is the Aspire-generated default kept stable across the cut-over
docker exec -e PGPASSWORD='71UhdH_{f7C+yPyrh92RRW' eshop-postgres \
  psql -U postgres -d orderingdb \
  -c 'SELECT "Id", "OrderStatus", "OrderDate" FROM ordering.orders ORDER BY "Id" DESC LIMIT 5'

# Tail the outbox relay drain
docker logs -f eshop-ordering | grep -E "Outbox|->"
```

After ~1 minute (grace period) you'll see `Submitted → AwaitingValidation → StockConfirmed → Paid`, the webhooks-client UI receive the `ORDER_PAID` callback, and Jaeger trace the whole pipeline.

## .NET → Java mapping

Detailed file-by-file mapping: [`project-docs/data/conversion-map.md`](project-docs/data/conversion-map.md). High-level:

| Aspect | .NET Aspire | Java / Spring |
|---|---|---|
| Orchestration | `eShop.AppHost` (`DistributedApplication`) | `docker-compose.yml` |
| Dashboard | Aspire dashboard | Spring Boot Admin (port 9090) |
| DI | `IServiceCollection` | Spring IoC |
| ORM | EF Core | Spring Data JPA / Hibernate |
| Identity | Duende IdentityServer | **Keycloak 26.1** |
| Events / mediation | MediatR | `CommandBus` + `ApplicationEventPublisher` |
| Validation | FluentValidation | Jakarta Bean Validation |
| Resilience | Polly | Resilience4j |
| Tracing | OpenTelemetry → Aspire dashboard | OpenTelemetry → Jaeger |
| WebApp | Blazor Server (.NET 10) | React 18 + Vite + Tailwind |

## License

MIT — same as the original [`dotnet/eShop`](https://github.com/dotnet/eShop).
