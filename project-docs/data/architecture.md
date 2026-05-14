# Architecture

## System Overview

eShop is a microservices e-commerce application migrated from .NET Aspire to Java Spring Boot. It demonstrates a production-style architecture with DDD aggregates, CQRS commands, transactional-outbox messaging, idempotent command processing, and OAuth2/OIDC security. The Ordering service is the architectural showcase — see [`ordering-ddd-design.md`](ordering-ddd-design.md) for the full pattern description.

The full stack runs from `docker compose up -d` with no .NET dependency at runtime.

## Services

| Service | Port | Technology | Database |
|---------|------|-----------|----------|
| Keycloak (Identity) | 8180 | Keycloak 26.1 (OIDC realm `eshop`) | Keycloak built-in |
| Catalog | 9101 | Spring Boot + Spring MVC | PostgreSQL (catalogdb) |
| Ordering | 9102 | Spring Boot — DDD aggregates / CQRS commands / outbox / idempotency | PostgreSQL (orderingdb) |
| Basket | 9103 (HTTP) / 9113 (gRPC) | Spring Boot + gRPC | Redis |
| Webhooks | 9104 | Spring Boot + Spring MVC | PostgreSQL (webhooksdb) |
| Order Processor | 9105 | Spring Boot (background `@Scheduled`) | PostgreSQL (orderingdb) |
| Payment Processor | 9106 | Spring Boot (event consumer) | None |
| Webhooks Client | 9107 | Spring Boot (OAuth2 subscriber demo) | PostgreSQL (webhooksdb) |
| Mobile BFF | 11632 | Spring Cloud Gateway Server MVC | None |
| WebApp | 8080 | React 18 + Vite + Tailwind + nginx | None |

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

### Identity — Keycloak (Port 8180)

OIDC issuer for the realm `eshop`. Replaces the original `Identity.API` (Duende IdentityServer) and the earlier short-lived Spring Authorization Server prototype. Started by `docker-compose.yml` with `start-dev --import-realm` against `infrastructure/keycloak/eshop-realm.json`.

**Registered Clients (realm `eshop`):**

| Client | Auth Flow | Scopes | Purpose |
|--------|-----------|--------|---------|
| webapp-spa | Authorization Code + PKCE (public) | openid, profile, orders, basket | React SPA at http://localhost:8080 |
| webapp | Authorization Code (confidential) | openid, profile, orders, basket | .NET Blazor reference (kept for parity) |
| webhooksclient | Authorization Code (confidential) | openid, profile | Webhooks Client demo |
| mobilebff / basket / orders | various | — | service-to-service |

**Seeded users:** `alice` / `Pass123$`, `bob` / `Pass123$`. Both have `address_*` and `card_*` user attributes mapped into JWT claims via the `eshop-custom-claims` scope — the SPA's checkout page pre-fills the address from these.

**Notable token quirk:** Keycloak emits `iss=http://localhost:8180/realms/eshop`; Java services configure `IDENTITY_URL=http://host.docker.internal:8180/realms/eshop` so containers can reach Keycloak's published host port while still validating tokens.

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

### Ordering Service (Port 9102) — DDD / CQRS / Outbox showcase

The marquee migration. Full design notes: [`ordering-ddd-design.md`](ordering-ddd-design.md).

**Key Endpoints:**
- `POST /api/orders` — create order (requires `x-requestid` UUID header for idempotency)
- `POST /api/orders/draft` — compute totals without persisting (UI preview)
- `GET /api/orders` — list user's orders (returns `OrderSummary[]`)
- `GET /api/orders/{id}` — order detail (`OrderDto`)
- `GET /api/orders/cardtypes` — reference data (Amex / Visa / MasterCard)
- `PUT /api/orders/cancel` — cancel order (idempotent)
- `PUT /api/orders/ship` — mark shipped (idempotent)

**Domain Model (DDD):**
- **Order** (Aggregate Root) — `id`, `orderDate`, embedded `Address`, `status`, `buyerId` (FK), `orderItems`, `paymentMethodId`
- **OrderItem** (Entity inside Order) — `productId`, `productName`, `unitPrice`, `discount`, `units`, `pictureUrl`
- **Buyer** (Aggregate Root) — `id`, `identityGuid` (Keycloak sub), `name`, `paymentMethods`
- **PaymentMethod** (Entity inside Buyer) — card details + `alias`
- **CardType** — reference entity (Amex/Visa/MasterCard)
- **Address** (Value Object) — `street`, `city`, `state`, `country`, `zipCode`
- **OrderStatus** (Enum + `OrderStatusConverter`) — `Submitted → AwaitingValidation → StockConfirmed → Paid → Shipped | Cancelled`

**Domain events** raised by aggregates (8): `OrderStartedDomainEvent`, `OrderStatusChangedTo{AwaitingValidation,StockConfirmed,Paid}DomainEvent`, `OrderShipped/CancelledDomainEvent`, `BuyerAndPaymentMethodVerifiedDomainEvent`. Dispatched synchronously inside the originating transaction via Spring Data `@DomainEvents`.

**CQRS commands** (7): `CreateOrderCommand`, `CreateOrderDraftCommand`, `CancelOrderCommand`, `ShipOrderCommand`, `SetAwaitingValidation/StockConfirmed/PaidOrderStatusCommand`. Dispatched through a Spring-managed `CommandBus`; state-changing commands are wrapped in `IdempotentCommandExecutor` which persists `x-requestid` to `ordering.requests`.

**Transactional outbox**: integration events are written to `ordering."IntegrationEventLog"` in the **same DB transaction** as the aggregate change. A background `@Scheduled` `IntegrationEventOutboxRelay` drains pending entries and publishes to RabbitMQ — guarantees no event is lost if the broker is down at commit time.

**Architecture Layers:**
- `ordering-domain` — aggregates, entities, value objects, domain events, repository interfaces (pure POJO, no Spring deps beyond `@DomainEvents`)
- `ordering-infrastructure` — JPA repositories, `RequestManager` (idempotency)
- `ordering-api` — thin REST controller (dispatch-only), CQRS commands + handlers, domain event handlers, integration events, outbox relay

**Schema parity with .NET:** maps to the existing `ordering.*` schema (PascalCase quoted columns, HiLo sequences with `pooled-lo` optimizer matching EF Core, FK relationships preserved). Existing .NET-created orders/buyers continue to be readable.

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

React 18 + TypeScript SPA, served by nginx in compose. Mirrors the .NET Blazor reference visually (AdventureWorks branding, Plus Jakarta Sans, hero "Ready for a new adventure?" banner) and flow-wise.

**Pages:**
- `/` or `/catalog` — Hero banner + paginated product grid. Cards are clickable Links (no inline add-to-cart).
- `/item/:itemId` — product detail page; "Log in to purchase" or "Add to shopping bag" button depending on auth state
- `/cart` — Shopping bag with 3-column flex layout (Products / Quantity / Total), per-row Update button, right-side summary sidebar with Check out + Continue shopping
- `/checkout` — address-only form pre-filled from JWT claims (`address_street`, `address_city`, …); card details ride from JWT; expiration hardcoded to 1y-from-now (matches `BasketState.CheckoutAsync`)
- `/orders` — order history showing status chip + total per row

**Auth:** `react-oidc-context` + `oidc-client-ts` with Authorization Code + PKCE flow against Keycloak's `webapp-spa` client. `App.tsx` pushes the access token into a shared axios interceptor via `setAuthToken()` so every backend call carries `Authorization: Bearer …`.

**Same-origin proxy:** nginx serves the SPA + proxies `/api/{catalog,basket,orders}` to the Java backends inside the compose network. The browser never sees the Java backends directly, so no CORS is needed. Keycloak (port 8180) is the only cross-origin endpoint — the `webapp-spa` realm client has `http://localhost:8080` in `webOrigins`.

**API Clients:** `src/api/{catalog,basket,ordering}Api.ts` over a centralised `src/api/client.ts`. POST/PUT to ordering sends `x-requestid` UUID header for idempotency; `cardExpiration` is converted from `MM/YY` to ISO Instant before send; items use `quantity` (not `units`) to match the Java DTO.

**Per-page titles:** `useDocumentTitle` hook applied per page (`AdventureWorks`, `{Name} | AdventureWorks`, `Shopping Bag | AdventureWorks`, etc.)

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
- **Publishing (Ordering, the showcase):** transactional outbox — events written to `ordering."IntegrationEventLog"` in the same tx as the aggregate, then drained by `IntegrationEventOutboxRelay` (`@Scheduled` every 3 s)
- **Publishing (other services):** direct `EventBus.publishAsync(event)` (simpler, no outbox)
- Consuming via `@RabbitListener` on the service queue; each service has its own copy of "the same" event class under its own package
- **Cross-service tolerance:** the shared `RabbitMQConfig` installs a `DefaultClassMapper` that falls back to `LinkedHashMap` when the publisher's `__TypeId__` header points to a class not on the subscriber's classpath. Listeners read the raw `Message` and re-parse the body with the service-local class.
- Dead letter exchange: `eshop_event_bus_dlx`

## Project Structure

```
eshop-java/
├── common/                    # Shared libraries
│   ├── service-defaults/      # Spring Boot autoconfiguration + JwtSecurityConfig
│   ├── event-bus/             # Event bus abstractions
│   ├── event-bus-rabbitmq/    # RabbitMQ implementation (tolerant class mapper)
│   └── integration-event-log/ # Outbox pattern (generic shape, ordering has its own)
├── services/                  # Microservices
│   ├── catalog-service/
│   ├── basket-service/        # REST + gRPC
│   ├── ordering/              # Multi-module (domain, infrastructure, api)
│   │   ├── ordering-domain/   # Aggregates, value objects, domain events
│   │   ├── ordering-infrastructure/  # JPA repositories, RequestManager (idempotency)
│   │   └── ordering-api/      # Controllers, CQRS commands+handlers, outbox, domain event handlers
│   ├── order-processor/
│   ├── payment-processor/
│   ├── webhooks-service/
│   └── mobile-bff/            # Spring Cloud Gateway (out-of-plan addition)
├── clients/
│   ├── webapp/                # React SPA (Vite + Tailwind + nginx, replaces .NET Blazor)
│   └── webhooks-client/       # OAuth2 subscriber demo (out-of-plan)
├── infrastructure/
│   ├── keycloak/              # Realm import
│   └── postgres/              # init.sql for fresh-volume bootstrap
├── project-docs/              # Documentation portal
│   ├── data/                  # .md files and tasks.yaml (served at http://localhost:3333)
│   └── server.js              # Node.js server
└── docker-compose.yml         # Single-command startup
```

## Single-command startup

```bash
docker compose up -d --build
```

Brings up Postgres / Redis / RabbitMQ / Keycloak / Spring Boot Admin / Jaeger / all Java services / React SPA. No `dotnet run` required.
