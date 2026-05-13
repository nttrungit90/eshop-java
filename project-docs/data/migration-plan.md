# Migration Plan

## Overview

Incrementally migrate .NET eShop services to Java/Spring Boot. Infrastructure (Postgres, Redis, RabbitMQ) stays on .NET Aspire during migration. Services are migrated one-by-one, tested, then added to docker-compose.

Most Java services already have full implementations. The two stub services (payment-processor, order-processor) need real logic. Already-implemented services need config updates and integration testing against .NET Aspire infra.

**Direction update (2026-05-13):** Identity migration target shifted from Spring Authorization Server to **Keycloak** (Phase 6 below). Two new modules were added beyond the original plan: **mobile-bff** (Spring Cloud Gateway, Phase 9) and **webhooks-client** (OAuth2 subscriber demo, Phase 10).

## Phase 0: Common Libraries & Catalog Service (completed pre-plan)

### Common Libraries

- [x] P0-T1: Implement service-defaults (ServiceDefaultsAutoConfiguration, JwtSecurityConfig, ResilientHttpClientConfig)
- [x] P0-T2: Implement event-bus (EventBus interface, IntegrationEvent, IntegrationEventHandler)
- [x] P0-T3: Implement event-bus-rabbitmq (RabbitMQEventBus, RabbitMQConfig, RabbitMQTelemetry)
- [x] P0-T4: Implement integration-event-log (IntegrationEventLogEntry, IntegrationEventLogService)

### Catalog Service

- [x] P0-T5: Implement catalog-service (CatalogController, entities, repositories, AI service)
- [x] P0-T6: Implement catalog integration events and handlers (ProductPriceChanged, OrderStockConfirmed, OrderStockRejected)
- [x] P0-T7: Configure application.yml for Aspire-managed infra
- [x] P0-T8: Add catalog-service to docker-compose.yml
- [x] P0-T9: Update .NET AppHost with `useJavaCatalog` flag and fixed infra ports

## Phase 1: Payment Processor (simplest — no DB, only RabbitMQ)

- [x] P1-T1: Create integration event classes (OrderStatusChangedToStockConfirmedIntegrationEvent, OrderPaymentSucceededIntegrationEvent, OrderPaymentFailedIntegrationEvent)
- [x] P1-T2: Create PaymentOptions configuration class with `paymentSucceeded` property
- [x] P1-T3: Implement OrderStatusChangedToStockConfirmedIntegrationEventHandler (replace stub) — consume event, check PaymentOptions, publish success/failure event
- [x] P1-T4: Remove old stub event handler (OrderStatusChangedToPaidEventHandler)
- [x] P1-T5: Update application.yml with Aspire-managed RabbitMQ credentials and OTLP config
- [x] P1-T6: Verify compilation and run tests
- [x] P1-T7: Add payment-processor to docker-compose.yml
- [x] P1-T8: Update .NET AppHost Program.cs with `useJavaPaymentProcessor` flag

## Phase 2: Order Processor (background worker — RabbitMQ + Postgres)

- [x] P2-T1: Create GracePeriodConfirmedIntegrationEvent class
- [x] P2-T2: Create BackgroundTaskOptions configuration class (gracePeriodTime, checkUpdateTime)
- [x] P2-T3: Implement GracePeriodManagerService — scheduled task that queries orderingdb for orders past grace period and publishes GracePeriodConfirmedIntegrationEvent
- [x] P2-T4: Remove old stub event handler (OrderStatusChangedEventHandler)
- [x] P2-T5: Update application.yml with Aspire-managed Postgres/RabbitMQ credentials and OTLP config
- [x] P2-T6: Verify compilation and run tests
- [x] P2-T7: Add order-processor to docker-compose.yml
- [x] P2-T8: Update .NET AppHost Program.cs with `useJavaOrderProcessor` flag

## Phase 3: Webhooks Service (already implemented — config + integration)

- [x] P3-T0: Implement webhooks-service (controller, model, repository, events)
- [x] P3-T1: Update application.yml with Aspire-managed Postgres/RabbitMQ credentials and OTLP config
- [x] P3-T2: Verify compilation and run tests
- [x] P3-T3: Add webhooks-service to docker-compose.yml
- [x] P3-T4: Update .NET AppHost Program.cs with `useJavaWebhooks` flag

## Phase 4: Ordering API (already implemented — config + integration)

- [x] P4-T0a: Implement ordering-domain (aggregates, entities, value objects, domain events)
- [x] P4-T0b: Implement ordering-infrastructure (repositories, JPA mappings, DB context)
- [x] P4-T0c: Implement ordering-api (controllers, commands, queries, event handlers)
- [ ] P4-T1: Update application.yml with Aspire-managed Postgres/RabbitMQ credentials and OTLP config
- [ ] P4-T2: Verify compilation and run tests
- [ ] P4-T3: Add ordering-api to docker-compose.yml
- [ ] P4-T4: Update .NET AppHost Program.cs with `useJavaOrdering` flag

## Phase 5: Basket Service (already implemented — config + integration)

- [x] P5-T0: Implement basket-service (REST, gRPC, Redis, protobuf)
- [x] P5-T1: Update application.yml with Aspire-managed Redis/RabbitMQ credentials and OTLP config
- [x] P5-T2: Verify compilation and run tests
- [x] P5-T3: Add basket-service to docker-compose.yml
- [x] P5-T4: Update .NET AppHost Program.cs with `useJavaBasket` flag

## Phase 6: Identity — Pivoted to Keycloak (replaces Spring Authorization Server)

The Java `identity-service` module was removed in favour of running Keycloak 26.1 in docker-compose. All Java services now validate tokens against `http://localhost:8180/realms/eshop`.

- [x] P6-T1: Migrate identity-api to Keycloak (realm `eshop`, import from `infrastructure/keycloak/eshop-realm.json`)
- [x] P6-T2: Fix Keycloak claims, logout redirect, enable Jaeger tracing
- [x] P6-T3: Add Keycloak service to docker-compose.yml (port 8180, `start-dev --import-realm`)
- [x] P6-T4: Point all Java services to Keycloak (`IDENTITY_URL`, `IDENTITY_JWK_SET_URI` env)
- [x] P6-T5: Accept `at+jwt` token type from .NET Identity (transition compat)

## Phase 7: WebApp (React SPA — after all backends)

- [x] P7-T0: Implement webapp (React components, API clients, routing, auth)
- [ ] P7-T1: Update environment config to point to Java backend URLs
- [ ] P7-T2: Verify build and test
- [ ] P7-T3: Add webapp to docker-compose.yml

## Phase 8: Migrate Infrastructure to docker-compose

Keycloak is already in docker-compose (Phase 6). This phase moves the remaining Aspire-managed infra (Postgres, Redis, RabbitMQ) into the same compose file.

- [ ] P8-T1: Add Postgres (pgvector) to docker-compose.yml with init scripts
- [ ] P8-T2: Add Redis to docker-compose.yml
- [ ] P8-T3: Add RabbitMQ to docker-compose.yml
- [ ] P8-T4: Update all service configs to use docker-compose network hostnames (replace `host.docker.internal`)
- [ ] P8-T5: Verify full system starts with single `docker compose up --build`
- [ ] P8-T6: Remove .NET AppHost dependency — document final startup procedure

## Phase 9: Mobile BFF (out-of-plan addition — completed)

Spring Cloud Gateway Server MVC routing proxy that fronts the catalog and ordering APIs for mobile clients.

- [x] P9-T1: Create `services/mobile-bff` module (Spring Cloud Gateway Server MVC)
- [x] P9-T2: Add mobile-bff to docker-compose.yml on port 11632 with routes for `/catalog-api/**`, `/api/catalog/**`, `/api/orders/**`

## Phase 10: Webhooks Client (out-of-plan addition — completed)

OAuth2 client demo that subscribes to and receives webhook callbacks from `webhooks-service`.

- [x] P10-T1: Create `clients/webhooks-client` module (OAuth2 client + webhook receiver)
- [x] P10-T2: Fix login flow and webhooksdb schema creation
- [x] P10-T3: Add webhooks-client to docker-compose.yml on port 9107 with Keycloak OAuth2 wiring
