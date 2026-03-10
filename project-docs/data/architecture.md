# Architecture

## System Overview

eShop is a microservices e-commerce application being migrated from .NET Aspire to Java Spring Boot.

## Services

| Service | Port | Technology | Database |
|---------|------|-----------|----------|
| Identity | 9100 | Spring Authorization Server | PostgreSQL (identitydb) |
| Catalog | 9101 | Spring Boot + Spring MVC | PostgreSQL (catalogdb) |
| Ordering | 9102 | Spring Boot + DDD/CQRS | PostgreSQL (orderingdb) |
| Basket | 9103 | Spring Boot + gRPC | Redis |
| Webhooks | 9104 | Spring Boot + Spring MVC | PostgreSQL (webhooksdb) |
| Order Processor | 9105 | Spring Boot (background worker) | PostgreSQL (orderingdb) |
| Payment Processor | 9106 | Spring Boot (background worker) | None |
| WebApp | 8080 | React + TypeScript | None |

## Infrastructure

| Component | Port | Purpose |
|-----------|------|---------|
| PostgreSQL (pgvector) | 5432 | Primary database |
| Redis | 6379 | Basket cache |
| RabbitMQ | 5672 / 15672 | Event bus |
| Spring Boot Admin | 9090 | Service monitoring |
| Jaeger | 16686 / 4317-4318 | Distributed tracing |

## Event-Driven Architecture

Services communicate asynchronously via RabbitMQ using integration events.

- Exchange: `eshop_event_bus` (direct)
- Queue per service: `{service-name}_queue`
- Events extend `IntegrationEvent` base class
- Publishing via `EventBus.publishAsync(event)`
- Consuming via `@RabbitListener`

## Order Flow

```
WebApp → Basket.API (add items)
       → Basket.API (checkout) → publishes UserCheckoutAcceptedIntegrationEvent
       → Ordering.API (creates order) → publishes OrderStartedIntegrationEvent
       → Catalog.API (validates stock) → publishes OrderStockConfirmed/RejectedIntegrationEvent
       → PaymentProcessor (processes payment) → publishes OrderPaymentSucceeded/FailedIntegrationEvent
       → OrderProcessor (monitors grace period) → publishes GracePeriodConfirmedIntegrationEvent
```

## Project Structure

```
eshop-java/
├── common/                    # Shared libraries
│   ├── service-defaults/      # Spring Boot autoconfiguration
│   ├── event-bus/             # Event bus abstractions
│   ├── event-bus-rabbitmq/    # RabbitMQ implementation
│   └── integration-event-log/ # Outbox pattern
├── services/                  # Microservices
│   ├── catalog-service/
│   ├── identity-service/
│   ├── basket-service/
│   ├── ordering/              # Multi-module (domain, infra, api)
│   ├── order-processor/
│   ├── payment-processor/
│   └── webhooks-service/
├── clients/
│   └── webapp/                # React SPA
├── project-docs/              # This documentation portal
│   ├── data/                  # .md files and tasks.yaml
│   └── server.js              # Node.js server
└── docker-compose.yml         # Service orchestration
```
