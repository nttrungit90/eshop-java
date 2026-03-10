# Migration Progress

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
- **P1-T5**: Updated application.yml with Aspire-managed RabbitMQ credentials (`WBpzyj95KTuVkpGxR5Fx1j`), OTLP tracing/metrics, server address `0.0.0.0`
- **P1-T6**: Verified compilation and tests pass (BUILD SUCCESS)
- **P1-T7**: Added payment-processor to docker-compose.yml with Aspire infra via host.docker.internal
- **P1-T8**: Updated .NET AppHost Program.cs with `useJavaPaymentProcessor = true` flag

### Phase 2: Order Processor

- **P2-T1**: Created GracePeriodConfirmedIntegrationEvent class
- **P2-T2**: Created BackgroundTaskOptions configuration class + enabled `@EnableScheduling`
- **P2-T3**: Implemented GracePeriodManagerService — `@Scheduled` task using JdbcTemplate to query orderingdb, publishes GracePeriodConfirmedIntegrationEvent
- **P2-T4**: Removed old stub event handler (OrderStatusChangedEventHandler)
- **P2-T5**: Updated application.yml with Aspire credentials, OTLP, background-task config
- **P2-T6**: Verified compilation and tests pass (BUILD SUCCESS)
- **P2-T7**: Added order-processor to docker-compose.yml with Postgres + RabbitMQ via host.docker.internal
- **P2-T8**: Updated .NET AppHost Program.cs with `useJavaOrderProcessor = true` flag

### Phase 3: Webhooks Service

- **P3-T1**: Updated application.yml with Aspire credentials, OTLP, quoted identifiers, ddl-auto: none
- **P3-T2**: Verified compilation and tests pass (BUILD SUCCESS)
- **P3-T3**: Added webhooks-service to docker-compose.yml
- **P3-T4**: Updated .NET AppHost Program.cs with `useJavaWebhooks = true` flag (re-enabled after fixing identity.url to point to .NET Identity)

### Cross-cutting: .NET/Java Interop Fixes

- **JWT auth**: Fixed identity.url to point to .NET Identity HTTP endpoint (localhost:5223) in catalog-service and webhooks-service
- **JWT security**: Added selective BearerTokenResolver to skip token validation on permitAll endpoints
- **Event bus serialization**: Configured ObjectMapper with PascalCase naming + ISO dates to match .NET System.Text.Json
- **Event bus double-encoding**: Fixed RabbitMQEventBus to send event objects directly (not pre-serialized strings)
- **RabbitMQ routing**: Replaced empty routing key binding with per-event-type bindings (EventBusSubscriptions) matching .NET direct exchange pattern
- **Event listeners**: Added CatalogEventListener and raw Message listener for payment-processor to dispatch RabbitMQ events to handlers
- **Event deserialization**: Added default constructors to event classes, ignore unknown .NET fields (FAIL_ON_UNKNOWN_PROPERTIES=false)
- **OpenTelemetry**: Removed all OTLP dependencies and config (not used)

## Next Task

**P4-T1**: Update ordering-api application.yml with Aspire-managed Postgres/RabbitMQ credentials and OTLP config (Phase 4 — Ordering API)

## Notes

- Phase 0 was completed before the formal migration plan was created
- Payment-processor and order-processor are the only stub services needing real implementation
- Other services (webhooks, ordering, basket, identity, webapp) have full implementations but need config updates
- All Java services consuming .NET events must use raw Message + ObjectMapper pattern (not typed @RabbitListener params)
- identity.url must point to .NET Identity HTTP endpoint (localhost:5223) during migration
