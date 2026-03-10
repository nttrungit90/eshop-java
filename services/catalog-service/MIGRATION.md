# Catalog Service Migration

**Status:** DONE
**.NET Source:** `src/Catalog.API/`
**Java Module:** `services/catalog-service`
**Port:** 9101

## Technology Mapping

| .NET | Java |
|------|------|
| ASP.NET Core Minimal APIs | Spring Boot + Spring MVC (RestController) |
| Entity Framework Core | Spring Data JPA + Hibernate |
| EF Core Migrations | Flyway (disabled — uses .NET schema) |
| ProblemDetails | Spring ProblemDetail |
| IOptions\<T\> | @ConfigurationProperties |
| OpenTelemetry | Micrometer + OTLP |

## File Mapping

| .NET File | Java File |
|-----------|-----------|
| `Catalog.API/Program.cs` | `CatalogServiceApplication.java` |
| `Catalog.API/Apis/CatalogApi.cs` | `api/CatalogController.java` |
| `Catalog.API/Model/CatalogItem.cs` | `model/CatalogItem.java` |
| `Catalog.API/Model/CatalogBrand.cs` | `model/CatalogBrand.java` |
| `Catalog.API/Model/CatalogType.cs` | `model/CatalogType.java` |
| `Catalog.API/Infrastructure/CatalogContext.cs` | `repository/CatalogItemRepository.java` |
| `Catalog.API/Infrastructure/EntityConfigurations/*` | JPA annotations on entity classes |
| `Catalog.API/Services/CatalogAI.cs` | `services/CatalogAIService.java` |
| `Catalog.API/IntegrationEvents/*` | `events/*.java` |
| `Catalog.API/appsettings.json` | `application.yml` |

## Database

- **Database:** `catalogdb` (PostgreSQL, managed by .NET Aspire)
- **Schema:** Created by .NET EF Core migrations — Java uses `ddl-auto: none`
- **Naming:** PascalCase columns preserved via `PhysicalNamingStrategyStandardImpl` + `globally_quoted_identifiers`
- **Flyway:** Disabled — schema owned by .NET

## Integration Events

| Event | Direction | Exchange |
|-------|-----------|----------|
| `ProductPriceChangedIntegrationEvent` | Publishes | RabbitMQ |
| `OrderStockConfirmedIntegrationEvent` | Consumes | RabbitMQ |
| `OrderStockRejectedIntegrationEvent` | Consumes | RabbitMQ |

## How to Run

Catalog-service is stable and runs via docker-compose alongside other migrated services:
```bash
docker compose up -d    # starts catalog-service + observability
```

For debugging/development (run outside docker-compose instead):
```bash
./mvnw -pl services/catalog-service spring-boot:run
```

## Migration Notes

- .NET AppHost flag: `bool useJavaCatalog = true` in `Program.cs`
- WebApp connects via env var `services__catalog-api__http__0=http://localhost:9101`
- YARP mobile-bff uses catalog-less route overload when Java catalog is active
- Aspire infra ports pinned: Postgres 5432, RabbitMQ 5672, Redis 6379
