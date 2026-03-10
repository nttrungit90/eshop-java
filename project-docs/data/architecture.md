# Architecture

## System Overview

eShop is a microservices e-commerce application being migrated from .NET Aspire to Java Spring Boot. It demonstrates a production-style architecture with DDD, CQRS, event-driven communication, and OAuth2/OIDC security.

## Services

| Service | Port | Technology | Database |
|---------|------|-----------|----------|
| Identity | 9100 | Spring Authorization Server | PostgreSQL (identitydb) |
| Catalog | 9101 | Spring Boot + Spring MVC | PostgreSQL (catalogdb) |
| Ordering | 9102 | Spring Boot + DDD/CQRS | PostgreSQL (orderingdb) |
| Basket | 9103 (HTTP) / 9113 (gRPC) | Spring Boot + gRPC | Redis |
| Webhooks | 9104 | Spring Boot + Spring MVC | PostgreSQL (webhooksdb) |
| Order Processor | 9105 | Spring Boot (background worker) | PostgreSQL (orderingdb) |
| Payment Processor | 9106 | Spring Boot (background worker) | None |
| WebApp | 8080 | React + TypeScript | None |

## Infrastructure

| Component | Port | Purpose |
|-----------|------|---------|
| PostgreSQL (pgvector) | 5432 | Primary database |
| Redis | 6379 | Basket cache |
| RabbitMQ | 5672 / 15672 | Event bus / Management UI |
| Spring Boot Admin | 9090 | Service monitoring |
| Jaeger | 16686 / 4317-4318 | Distributed tracing |

## Business Domain

### What the App Does

eShop is an online store where users can:
1. Browse a product catalog with filtering by brand/type
2. Add items to a shopping basket (persisted in Redis)
3. Check out — creating an order with shipping address and payment info
4. Track order status through its lifecycle
5. Cancel orders (before payment)
6. Subscribe to webhooks for order events

### User Accounts (Seeded Test Data)

| User | Email | Password | Card |
|------|-------|----------|------|
| alice | AliceSmith@email.com | Pass123$ | 4012888888881881 / 123 / 12/25 |
| bob | BobSmith@email.com | Pass123$ | 4012888888881881 / 456 / 12/25 |

## Service Details

### Identity Service (Port 9100)

OAuth2/OIDC authorization server using Spring Authorization Server.

**Registered Clients:**

| Client | Auth Flow | Scopes | Purpose |
|--------|-----------|--------|---------|
| webapp | Authorization Code + Refresh | openid, profile, orders, basket | React SPA |
| mobilebff | Authorization Code + Refresh | openid, profile, orders, basket | Mobile BFF |
| basket | Client Credentials | basket | Service-to-service |
| orders | Client Credentials | orders | Service-to-service |

- RSA 2048-bit JWT signing
- BCrypt password hashing
- Form-based login at `/login`

### Catalog Service (Port 9101)

Product catalog management with stock tracking and AI semantic search (optional).

**Key Endpoints:**
- `GET /api/catalog/items` — paginated product list (supports v1 and v2 API)
- `GET /api/catalog/items/{id}` — single product
- `GET /api/catalog/items/{id}/pic` — product image
- `GET /api/catalog/catalogTypes` — product categories
- `GET /api/catalog/catalogBrands` — product brands
- `POST/PUT/DELETE /api/catalog/items` — CRUD operations

**Domain Model:** CatalogItem (name, description, price, availableStock, restockThreshold, maxStockThreshold, catalogType, catalogBrand, pictureFileName, embedding for AI search)

**Stock Management:**
- `removeStock(quantity)` — reduces stock, throws if insufficient
- `addStock(quantity)` — increases stock up to max threshold

### Ordering Service (Port 9102)

Order management using DDD with a rich domain model.

**Key Endpoints:**
- `POST /api/orders` — create order
- `GET /api/orders` — get user's orders
- `GET /api/orders/{id}` — get order details
- `PUT /api/orders/{id}/ship` — mark shipped
- `PUT /api/orders/{id}/cancel` — cancel order

**Domain Model (DDD):**
- **Order** (Aggregate Root) — orderId, orderDate, address, status, buyerId, orderItems
- **OrderItem** (Entity) — productId, productName, unitPrice, discount, units, pictureUrl
- **Address** (Value Object) — street, city, state, country, zipCode
- **OrderStatus** (Enum) — Submitted → AwaitingValidation → StockConfirmed → Paid → Shipped | Cancelled

**Architecture Layers:**
- `ordering-domain` — aggregates, entities, value objects, domain events, repository interfaces
- `ordering-infrastructure` — JPA repositories, DB mappings
- `ordering-api` — REST controllers, DTOs, event handlers

### Basket Service (Port 9103 HTTP / 9113 gRPC)

Shopping basket with dual API (REST + gRPC). gRPC is the primary API used by WebApp.

**Key Endpoints (REST):**
- `GET /api/basket/{buyerId}` — get basket
- `POST /api/basket` — update basket
- `DELETE /api/basket/{buyerId}` — delete basket

**gRPC RPCs:** GetBasket, UpdateBasket, DeleteBasket

**Storage:** Redis with JSON serialization, 30-day TTL, key pattern `basket:{buyerId}`

**Model:** CustomerBasket (buyerId, List\<BasketItem\>) where BasketItem has productId, productName, unitPrice, oldUnitPrice, quantity, pictureUrl

### Order Processor (Port 9105)

Background worker that enforces the grace period on new orders.

**Behavior:** Polls orderingdb every `checkUpdateTime` seconds (default: 30). Finds orders in `Submitted` status older than `gracePeriodTime` minutes (default: 1). Publishes `GracePeriodConfirmedIntegrationEvent` for each — this triggers the order to advance to stock validation.

**Tech:** `@Scheduled` + `JdbcTemplate` (raw SQL, no ORM)

### Payment Processor (Port 9106)

Simulated payment gateway — no real payment processing.

**Behavior:** Listens for `OrderStatusChangedToStockConfirmedIntegrationEvent`. Checks `PaymentOptions.paymentSucceeded` config flag (default: true). Publishes either `OrderPaymentSucceededIntegrationEvent` or `OrderPaymentFailedIntegrationEvent`.

### Webhooks Service (Port 9104)

Webhook subscription management for external integrations.

**Supported Events:** ORDER_SHIPPED, ORDER_PAID

**Key Endpoints:**
- `GET /api/webhooks` — list subscriptions (user-scoped)
- `POST /api/webhooks` — create subscription (destUrl, type, token)
- `DELETE /api/webhooks/{id}` — delete subscription

### WebApp (Port 8080)

React + TypeScript SPA with OIDC authentication.

**Pages:**
- `/catalog` — product browsing with brand/type filters, pagination, add-to-cart
- `/cart` — basket management (quantity adjustment, remove, clear)
- `/checkout` — shipping address + payment form, order submission
- `/orders` — order history with status, cancel capability

**Auth:** react-oidc-context with Authorization Code flow against Identity Service (client: `webapp`)

**API Clients:** catalogApi, basketApi, orderingApi — configured via `VITE_*` env vars

## Order Flow (Complete Lifecycle)

```
1. BROWSE & ADD TO CART
   WebApp → Catalog.API (GET /api/catalog/items)
   WebApp → Basket.API (POST /api/basket) — items stored in Redis

2. CHECKOUT
   WebApp → Ordering.API (POST /api/orders)
   ├── Creates Order aggregate (status: SUBMITTED)
   ├── OrderStartedDomainEvent raised
   └── Basket cleared

3. GRACE PERIOD (Order Processor)
   Polls orderingdb every 30s
   ├── Finds SUBMITTED orders older than 1 minute
   └── Publishes GracePeriodConfirmedIntegrationEvent
       └── Ordering.API receives → status: AWAITING_VALIDATION

4. STOCK VALIDATION (Catalog Service)
   Receives OrderStatusChangedToAwaitingValidationIntegrationEvent
   ├── Checks availableStock >= ordered units for each item
   ├── If ALL OK → publishes OrderStockConfirmedIntegrationEvent
   │   └── Ordering.API receives → status: STOCK_CONFIRMED
   └── If ANY FAIL → publishes OrderStockRejectedIntegrationEvent
       └── Ordering.API receives → status: CANCELLED

5. PAYMENT (Payment Processor)
   Receives OrderStatusChangedToStockConfirmedIntegrationEvent
   ├── Checks PaymentOptions.paymentSucceeded config
   ├── If true → publishes OrderPaymentSucceededIntegrationEvent
   │   └── Ordering.API receives → status: PAID
   └── If false → publishes OrderPaymentFailedIntegrationEvent
       └── Ordering.API receives → status: CANCELLED

6. INVENTORY REDUCTION (Catalog Service)
   Receives OrderStatusChangedToPaidIntegrationEvent
   └── Calls removeStock() for each ordered item

7. SHIPPING (Manual)
   Admin → Ordering.API (PUT /api/orders/{id}/ship)
   └── Status: PAID → SHIPPED
   └── Webhooks: ORDER_SHIPPED notifications sent

8. CANCELLATION (Optional)
   User → Ordering.API (PUT /api/orders/{id}/cancel)
   └── Only from SUBMITTED or AWAITING_VALIDATION
```

## Integration Events Map

| Event | Publisher | Consumer(s) |
|-------|-----------|-------------|
| GracePeriodConfirmedIntegrationEvent | Order Processor | Ordering API |
| OrderStatusChangedToAwaitingValidationIntegrationEvent | Ordering API | Catalog |
| OrderStockConfirmedIntegrationEvent | Catalog | Ordering API |
| OrderStockRejectedIntegrationEvent | Catalog | Ordering API |
| OrderStatusChangedToStockConfirmedIntegrationEvent | Ordering API | Payment Processor |
| OrderPaymentSucceededIntegrationEvent | Payment Processor | Ordering API |
| OrderPaymentFailedIntegrationEvent | Payment Processor | Ordering API |
| OrderStatusChangedToPaidIntegrationEvent | Ordering API | Catalog, Webhooks |
| ProductPriceChangedIntegrationEvent | Catalog | Basket |
| OrderStartedIntegrationEvent | Ordering API | Basket |

## Event-Driven Architecture

Services communicate asynchronously via RabbitMQ using integration events.

- Exchange: `eshop_event_bus` (direct)
- Queue per service: `{service-name}_queue`
- Events extend `IntegrationEvent` base class
- Publishing via `EventBus.publishAsync(event)`
- Consuming via `@RabbitListener`
- Dead letter exchange: `eshop_event_bus_dlx`

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
├── project-docs/              # Documentation portal
│   ├── data/                  # .md files and tasks.yaml
│   └── server.js              # Node.js server
└── docker-compose.yml         # Service orchestration
```
