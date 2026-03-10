# Migration Progress

## Current Phase: Phase 2 — Order Processor

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

## Next Task

**P2-T6**: Verify compilation and run tests

## Notes

- Phase 0 was completed before the formal migration plan was created
- Payment-processor and order-processor are the only stub services needing real implementation
- Other services (webhooks, ordering, basket, identity, webapp) have full implementations but need config updates
