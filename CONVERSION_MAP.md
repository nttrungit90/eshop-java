# .NET to Java Conversion Map

This document maps each .NET component in the original eShop application to its Java equivalent.

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
| Duende IdentityServer | Spring Authorization Server |
| MediatR | Spring Events + ApplicationEventPublisher |
| FluentValidation | Jakarta Bean Validation |
| gRPC (C#) | grpc-spring-boot-starter |
| OpenTelemetry (.NET) | Micrometer + OpenTelemetry |
| Polly (Resilience) | Resilience4j |
| Blazor SSR | React + TypeScript |

## Detailed File Mappings

### Common Libraries

#### service-defaults
| .NET File | Java File |
|-----------|-----------|
| `src/eShop.ServiceDefaults/Extensions.cs` | `common/service-defaults/.../config/ServiceDefaultsAutoConfiguration.java` |
| `src/eShop.ServiceDefaults/AuthenticationExtensions.cs` | `common/service-defaults/.../security/JwtSecurityConfig.java` |
| `src/eShop.ServiceDefaults/HttpClientExtensions.cs` | `common/service-defaults/.../http/ResilientHttpClientConfig.java` |

#### event-bus
| .NET File | Java File |
|-----------|-----------|
| `src/EventBus/Abstractions/IEventBus.cs` | `common/event-bus/.../EventBus.java` |
| `src/EventBus/Abstractions/IIntegrationEventHandler.cs` | `common/event-bus/.../IntegrationEventHandler.java` |
| `src/EventBus/Events/IntegrationEvent.cs` | `common/event-bus/.../IntegrationEvent.java` |

#### event-bus-rabbitmq
| .NET File | Java File |
|-----------|-----------|
| `src/EventBusRabbitMQ/RabbitMQEventBus.cs` | `common/event-bus-rabbitmq/.../RabbitMQEventBus.java` |
| `src/EventBusRabbitMQ/RabbitMQTelemetry.cs` | `common/event-bus-rabbitmq/.../RabbitMQTelemetry.java` |

#### integration-event-log
| .NET File | Java File |
|-----------|-----------|
| `src/IntegrationEventLogEF/IntegrationEventLogEntry.cs` | `common/integration-event-log/.../IntegrationEventLogEntry.java` |
| `src/IntegrationEventLogEF/Services/IntegrationEventLogService.cs` | `common/integration-event-log/.../IntegrationEventLogService.java` |

### Services

#### Identity Service
| .NET File | Java File |
|-----------|-----------|
| `src/Identity.API/Program.cs` | `services/identity-service/.../IdentityServiceApplication.java` |
| `src/Identity.API/Configuration/Config.cs` | `services/identity-service/.../config/AuthorizationServerConfig.java` |
| `src/Identity.API/Models/ApplicationUser.cs` | `services/identity-service/.../model/ApplicationUser.java` |
| `src/Identity.API/Data/ApplicationDbContext.cs` | (JPA repositories) |

#### Catalog Service
| .NET File | Java File |
|-----------|-----------|
| `src/Catalog.API/Program.cs` | `services/catalog-service/.../CatalogServiceApplication.java` |
| `src/Catalog.API/Apis/CatalogApi.cs` | `services/catalog-service/.../api/CatalogController.java` |
| `src/Catalog.API/Model/CatalogItem.cs` | `services/catalog-service/.../model/CatalogItem.java` |
| `src/Catalog.API/Services/CatalogAI.cs` | `services/catalog-service/.../services/CatalogAIService.java` |

#### Basket Service
| .NET File | Java File |
|-----------|-----------|
| `src/Basket.API/Program.cs` | `services/basket-service/.../BasketServiceApplication.java` |
| `src/Basket.API/Grpc/BasketService.cs` | `services/basket-service/.../grpc/BasketGrpcService.java` |
| `src/Basket.API/Model/CustomerBasket.cs` | `services/basket-service/.../model/CustomerBasket.java` |
| `src/Basket.API/Proto/basket.proto` | `services/basket-service/.../proto/basket.proto` |

#### Ordering Service
| .NET File | Java File |
|-----------|-----------|
| `src/Ordering.API/Program.cs` | `services/ordering/ordering-api/.../OrderingApiApplication.java` |
| `src/Ordering.API/Apis/OrdersApi.cs` | `services/ordering/ordering-api/.../api/OrdersController.java` |
| `src/Ordering.Domain/AggregatesModel/OrderAggregate/Order.cs` | `services/ordering/ordering-domain/.../aggregates/order/Order.java` |
| `src/Ordering.Domain/AggregatesModel/BuyerAggregate/Buyer.cs` | `services/ordering/ordering-domain/.../aggregates/buyer/Buyer.java` |
| `src/Ordering.Infrastructure/Repositories/OrderRepository.cs` | `services/ordering/ordering-infrastructure/.../repositories/OrderRepository.java` |

#### Order Processor
| .NET File | Java File |
|-----------|-----------|
| `src/OrderProcessor/Program.cs` | `services/order-processor/.../OrderProcessorApplication.java` |
| `src/OrderProcessor/Events/*` | `services/order-processor/.../events/*.java` |

#### Payment Processor
| .NET File | Java File |
|-----------|-----------|
| `src/PaymentProcessor/Program.cs` | `services/payment-processor/.../PaymentProcessorApplication.java` |
| `src/PaymentProcessor/IntegrationEvents/*` | `services/payment-processor/.../events/*.java` |

#### Webhooks Service
| .NET File | Java File |
|-----------|-----------|
| `src/Webhooks.API/Program.cs` | `services/webhooks-service/.../WebhooksServiceApplication.java` |
| `src/Webhooks.API/Apis/*` | `services/webhooks-service/.../api/*.java` |

### WebApp (Blazor → React)
| Blazor File | React File |
|-------------|------------|
| `src/WebApp/Components/Layout/HeaderBar.razor` | `clients/webapp/src/components/layout/Header.tsx` |
| `src/WebApp/Components/Pages/Catalog.razor` | `clients/webapp/src/components/catalog/CatalogPage.tsx` |
| `src/WebApp/Components/Pages/Cart.razor` | `clients/webapp/src/components/cart/CartPage.tsx` |
| `src/WebApp/Services/BasketService.cs` | `clients/webapp/src/api/basketApi.ts` |
| `src/WebApp/Services/OrderingService.cs` | `clients/webapp/src/api/orderingApi.ts` |

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
