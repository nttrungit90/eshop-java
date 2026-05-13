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

## Pending Phases

- **Phase 4 — Ordering API**: `application.yml` still uses localhost defaults; not in docker-compose; AppHost flag not added
- **Phase 7 — WebApp**: not configured to point at Java backends; not in docker-compose
- **Phase 8 — Infra to docker-compose**: Postgres/Redis/RabbitMQ still managed by .NET Aspire (Keycloak is already in compose)

## Next Task

**P4-T1**: Update `services/ordering/ordering-api/src/main/resources/application.yml` with Aspire-managed Postgres/RabbitMQ credentials (Phase 4 — Ordering API)

## Notes

- The migration direction shifted from "Spring Authorization Server for identity" to **Keycloak** — chosen for production-grade OIDC and easier admin/realm management
- New modules introduced beyond the original plan: **mobile-bff** (Spring Cloud Gateway routing proxy) and **webhooks-client** (OAuth2 subscriber demo)
- Payment-processor and order-processor were the only stub services needing real implementation; others had full implementations needing config + compose entries
- All Java services consuming .NET events must use raw Message + ObjectMapper pattern (not typed @RabbitListener params)
- Phase 8 is still pending: Postgres, Redis, RabbitMQ remain on .NET Aspire infrastructure for now
