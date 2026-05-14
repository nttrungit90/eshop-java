# Migration Progress

_Last updated: 2026-05-13_

## Current Phase: Phase 4 — Ordering API

## Completed Tasks

### Phase 0: Common Libraries & Catalog Service (pre-plan)

- **P0-T1**: Implemented service-defaults (ServiceDefaultsAutoConfiguration, JwtSecurityConfig, ResilientHttpClientConfig)
- **P0-T2**: Implemented event-bus (EventBus interface, IntegrationEvent, IntegrationEventHandler)
- **P0-T3**: Implemented event-bus-rabbitmq (RabbitMQEventBus, RabbitMQConfig)
- **P0-T4**: Implemented integration-event-log (IntegrationEventLogEntry, IntegrationEventLogService)
- **P0-T5**: Implemented catalog-service (CatalogController, entities, repositories, AI service)
- **P0-T6**: Implemented catalog integration events and handlers
- **P0-T7**: Configured catalog application.yml for Aspire infra
- **P0-T8**: Added catalog-service to docker-compose.yml
- **P0-T9**: Updated .NET AppHost with useJavaCatalog flag and fixed infra ports

### Phase 1: Payment Processor

- **P1-T1**: Created integration event classes (OrderStatusChangedToStockConfirmedIntegrationEvent, OrderPaymentSucceededIntegrationEvent, OrderPaymentFailedIntegrationEvent)
- **P1-T2**: Created PaymentOptions configuration class with `paymentSucceeded` property (`@ConfigurationProperties`)
- **P1-T3**: Implemented OrderStatusChangedToStockConfirmedIntegrationEventHandler — consumes stock confirmed event, checks PaymentOptions, publishes success/failure event
- **P1-T4**: Removed old stub event handler (OrderStatusChangedToPaidEventHandler)
- **P1-T5**: Updated application.yml with Aspire-managed RabbitMQ credentials, server address `0.0.0.0`
- **P1-T6**: Verified compilation and tests pass (BUILD SUCCESS)
- **P1-T7**: Added payment-processor to docker-compose.yml with Aspire infra via host.docker.internal
- **P1-T8**: Updated .NET AppHost Program.cs with `useJavaPaymentProcessor = true` flag

### Phase 2: Order Processor

- **P2-T1**: Created GracePeriodConfirmedIntegrationEvent class
- **P2-T2**: Created BackgroundTaskOptions configuration class + enabled `@EnableScheduling`
- **P2-T3**: Implemented GracePeriodManagerService — `@Scheduled` task using JdbcTemplate to query orderingdb, publishes GracePeriodConfirmedIntegrationEvent
- **P2-T4**: Removed old stub event handler (OrderStatusChangedEventHandler)
- **P2-T5**: Updated application.yml with Aspire credentials, background-task config
- **P2-T6**: Verified compilation and tests pass (BUILD SUCCESS)
- **P2-T7**: Added order-processor to docker-compose.yml with Postgres + RabbitMQ via host.docker.internal
- **P2-T8**: Updated .NET AppHost Program.cs with `useJavaOrderProcessor = true` flag

### Phase 3: Webhooks Service

- **P3-T1**: Updated application.yml with Aspire credentials, quoted identifiers, ddl-auto: none
- **P3-T2**: Verified compilation and tests pass (BUILD SUCCESS)
- **P3-T3**: Added webhooks-service to docker-compose.yml
- **P3-T4**: Updated .NET AppHost Program.cs with `useJavaWebhooks = true` flag

### Phase 5: Basket Service

- **P5-T1**: Updated application.yml with Aspire-managed Redis/RabbitMQ credentials
- **P5-T2**: Verified compilation and tests pass (BUILD SUCCESS)
- **P5-T3**: Added basket-service to docker-compose.yml (ports 9103 REST + 9113 gRPC)
- **P5-T4**: Updated .NET AppHost Program.cs with `useJavaBasket = true` flag — WebApp connects via explicit HTTP/gRPC URLs
- **P5-T5**: Fixed basket-service gRPC interop with .NET WebApp (commits `97b9e5f`, `a4beffa`)

### Phase 6: Identity — Pivoted to Keycloak

The Java `identity-service` module was **removed**. Identity is now served by **Keycloak 26.1** running in docker-compose, importing `infrastructure/keycloak/eshop-realm.json`.

- **P6-T1**: Migrated identity-api to Keycloak (commit `82d0ad9`)
- **P6-T2**: Fixed Keycloak claims, logout redirect, enabled Jaeger tracing (commit `e09c2fb`)
- **P6-T3**: Added Keycloak service to docker-compose.yml (port 8180, `start-dev --import-realm`)
- **P6-T4**: All Java services point to Keycloak realm: `http://host.docker.internal:8180/realms/eshop`
- **P6-T5**: Accept `at+jwt` token type from .NET Identity (Duende IdentityServer) for transition compat (commit `f659e92`)

### Phase 9 (new): Mobile BFF

- **P9-T1**: Created `services/mobile-bff` — Spring Cloud Gateway Server MVC routing proxy (commit `9323ace`)
- **P9-T2**: Added mobile-bff to docker-compose.yml on port 11632 with routes for catalog and ordering

### Phase 10 (new): Webhooks Client

- **P10-T1**: Created `clients/webhooks-client` — OAuth2 client demo that subscribes to and receives webhook callbacks (commit `55df561`)
- **P10-T2**: Fixed webhooks-client login flow and webhooksdb schema creation (commit `66db68c`)
- **P10-T3**: Added webhooks-client to docker-compose.yml on port 9107 with Keycloak OAuth2 wiring

### Cross-cutting: .NET/Java Interop Fixes

- **JWT auth**: Fixed identity.url to point to .NET Identity HTTP endpoint (localhost:5223) in catalog-service and webhooks-service; later migrated to Keycloak
- **JWT security**: Added selective BearerTokenResolver to skip token validation on permitAll endpoints
- **Event bus serialization**: Configured ObjectMapper with PascalCase naming + ISO dates to match .NET System.Text.Json
- **Event bus double-encoding**: Fixed RabbitMQEventBus to send event objects directly (not pre-serialized strings)
- **RabbitMQ routing**: Replaced empty routing key binding with per-event-type bindings (EventBusSubscriptions) matching .NET direct exchange pattern
- **Event listeners**: Added CatalogEventListener and raw Message listener for payment-processor to dispatch RabbitMQ events to handlers
- **Event deserialization**: Added default constructors to event classes, ignore unknown .NET fields (FAIL_ON_UNKNOWN_PROPERTIES=false)
- **OpenTelemetry/OTLP**: Removed OTLP metrics/tracing export from Java service configs (commit `b614c08`); Jaeger receives traces via `MANAGEMENT_OTLP_TRACING_ENDPOINT` env in compose
- **Catalog GET endpoints**: Use AntPathRequestMatcher for permitAll (commit `4ade30f`)

### Phase 4 — Ordering API (commits `9100fc3` and follow-ups)

The showcase migration — DDD aggregates, domain events, CQRS, idempotency, transactional outbox. See [`ordering-ddd-design.md`](ordering-ddd-design.md) for the full design.

- **P4-T1**: `application.yml` with Aspire-managed Postgres/RabbitMQ + Keycloak OIDC
- **P4-T2**: Maven `BUILD SUCCESS` on all 9 ordering modules
- **P4-T3**: `ordering-api` added to docker-compose.yml on port 9102
- **P4-T4**: `.NET AppHost` flag `useJavaOrdering = true` added
- **P4-T5**: Initial state-machine handlers (`OrderingEventListener`) — GracePeriodConfirmed → AwaitingValidation → StockConfirmed → Paid; StockRejected / PaymentFailed → Cancelled
- **P4-T6**: Full DDD/CQRS/Outbox refactor (replaces inline controller logic):
  - `Order` / `OrderItem` / `Buyer` / `PaymentMethod` aggregates with private collections and invariants in methods
  - 8 domain events raised by aggregates (OrderStartedDomainEvent, OrderStatusChangedTo{AwaitingValidation,StockConfirmed,Paid}DomainEvent, OrderShipped/CancelledDomainEvent, BuyerAndPaymentMethodVerifiedDomainEvent); dispatched via Spring Data `@DomainEvents` synchronously inside the originating tx
  - 7 domain event handlers (`ValidateOrAddBuyerAggregate…`, `UpdateOrderWhenBuyerAndPaymentMethodVerified…`, `OrderStatusChangedTo*DomainEventHandler`, `OrderShippedDomainEventHandler`, `OrderCancelledDomainEventHandler`)
  - CQRS `CommandBus` + 7 commands/handlers (CreateOrder, CreateOrderDraft, CancelOrder, ShipOrder, Set{AwaitingValidation,StockConfirmed,Paid}OrderStatus)
  - Idempotency: `Request` entity → `ordering.requests` + `IdempotentCommandExecutor` wraps every state-changing command
  - Transactional outbox: `OrderingIntegrationEventLogEntry` → `ordering."IntegrationEventLog"` (PascalCase, ordinal state); background `@Scheduled` relay drains and publishes to RabbitMQ
  - Tolerant `DefaultClassMapper` in shared `RabbitMQConfig` so cross-service `__TypeId__` headers don't DLX messages
  - Schema: `ordering.*` PascalCase columns, HiLo sequences with `pooled-lo` optimizer matching EF Core
  - New endpoints: `POST /api/orders/draft`, `GET /api/orders/cardtypes`
  - Validators: cardNumber 12–19, `@FutureOrPresent` cardExpiration, items non-empty

### Phase 8 — Infra to docker-compose (commit `92c1f42`)

Postgres / Redis / RabbitMQ are now owned by `docker-compose.yml`; Aspire only launches the (now-legacy) Blazor WebApp.

- **P8-T1**: Add `postgres` (ankane/pgvector:latest, port 5432), `redis` (8.2, 6379), `rabbitmq` (4.2-management, 5672/15672) to compose with healthchecks
- **P8-T2**: `infrastructure/postgres/init.sql` creates `catalogdb` / `orderingdb` / `webhooksdb` on first boot of an empty volume
- **P8-T3**: Reuse the existing `eshop-postgres-data` named volume so prior order/buyer data survived the cut-over
- **P8-T4**: Every Java service's `host.docker.internal:{5432,5672}` → compose service names (`postgres`, `rabbitmq`); basket Redis 6380 → 6379; mobile-bff route → `ordering-api:9102`
- **P8-T5**: `depends_on: condition: service_healthy` health gates added to all infra-dependent Java services
- **P8-T6**: `.NET AppHost` rewritten — drops `AddPostgres/AddRedis/AddRabbitMQ`, gives WebApp explicit `ConnectionStrings__eventbus` env

### Phase 7 — React WebApp (this commit)

Replaces the .NET Blazor WebApp with the existing React SPA at `clients/webapp/`. The entire stack now runs from `docker compose up -d` with no `dotnet run` required.

- **P7-T1**: Centralised axios client (`src/api/client.ts`) — appends `?api-version=1.0`, injects `Authorization: Bearer <token>` from the OIDC session via `setAuthToken()`
- **P7-T2**: Type fixes for the Java wire format — `PaginatedResponse<T>` switches to `{pageIndex, pageSize, count, data}` shape, new `OrderSummary` type for the list endpoint, `Order` keeps the detail shape
- **P7-T3**: Catalog API client — fixes endpoint paths (`/catalogTypes`, `/catalogBrands`, `/items/by/{name}`), picture URL helper hits `/api/catalog/items/{id}/pic`
- **P7-T4**: Ordering API client — sends `x-requestid` UUID on every POST/PUT (idempotency), converts `MM/YY` → ISO Instant for `cardExpiration`, sends `quantity` (not `units`) in items, fills `userId`/`userName`/`buyer` from the JWT
- **P7-T5**: `CartContext` only syncs with basket-service when authenticated (avoids 401 for anonymous browsers)
- **P7-T6**: `OrdersPage` rewritten to use the summary shape (orderNumber/date/status/total), with retry-cancel for in-flight orders
- **P7-T7**: `webapp` service added to docker-compose on port 8080 — nginx serves the SPA + proxies `/api/{catalog,basket,orders}` to the Java backends, keeping the browser same-origin
- **P7-T8**: Dockerfile switched to copy-prebuilt pattern (build via `npm run build` on the host) to sidestep npm-registry network issues inside the docker builder
- **P7-T9**: Keycloak `webapp-spa` realm client already configured with `redirectUris=[http://localhost:8080/authentication/login-callback]` and `webOrigins=[http://localhost:8080]`; no realm change needed
- **P7-T10** (visual parity): Ported Blazor brand assets — AdventureWorks logo SVGs, hero image (`header-home.webp`), Plus Jakarta Sans woff2s, icon SVGs (cart/user/etc.) — into `clients/webapp/public/`. Hero banner ("Ready for a new adventure?") restored on the catalog page; header/footer now use AdventureWorks logo on the brand purple bar; Tailwind theme switched to brand colors (`primary: #11118c`, `accent: #fde047`) and Plus Jakarta Sans as the default font
- **P7-T11**: Fixed product image bug — `<img src>` requests bypass axios, so `?api-version=1.0` is now inlined in `catalogApi.pictureUrl()`. Catalog cards and the cart now render product photos correctly

End-to-end verified after Phase 7: SPA loads at http://localhost:8080 with AdventureWorks visual brand, hero banner, real product images; catalog renders, login redirects through Keycloak, checkout posts via x-requestid idempotency, Submitted → Paid transitions visible on /orders.

## Pending Phases

_None — migration complete. The .NET WebApp (Blazor Server) can be removed from the Aspire AppHost; the eShop stack runs from `docker compose up -d` alone._

## Optional follow-ups

- Real-time order status in the React SPA (SSE/WebSocket endpoint on ordering-api, or just poll)
- Remove `.NET AppHost` entirely — only kept to launch the Blazor WebApp; React replaces that
- Tests for the DDD path (@DataJpaTest for domain events, slice tests per handler)
- Production hardening: secrets out of `docker-compose.yml`, separate prod profile, K8s manifests
- Remaining audit items (logged status-guard no-ops, FluentValidation parity for cancel/ship commands)

## Notes

- The migration direction shifted from "Spring Authorization Server for identity" to **Keycloak** — chosen for production-grade OIDC and easier admin/realm management
- New modules introduced beyond the original plan: **mobile-bff** (Spring Cloud Gateway routing proxy) and **webhooks-client** (OAuth2 subscriber demo)
- Payment-processor and order-processor were the only stub services needing real implementation; others had full implementations needing config + compose entries
- All Java services consuming .NET events must use raw Message + ObjectMapper pattern (not typed @RabbitListener params)
- Cross-service integration-event interop requires a tolerant `DefaultClassMapper` in `RabbitMQConfig` because each service has its own FQN for "the same" event class
- SPA architecture: nginx serves the SPA and proxies `/api/*` to the Java backends, so the browser is always same-origin to backend calls — only the Keycloak login flow is cross-origin (handled by the realm's `webOrigins` list)
