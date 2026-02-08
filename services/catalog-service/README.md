# Catalog Service

A Spring Boot microservice that manages the product catalog for the eShop application. It provides REST APIs for product management, inventory tracking, and AI-powered semantic search capabilities.

## Features

- **Product Catalog Management** - Full CRUD operations for products, types, and brands
- **Inventory Tracking** - Stock management with reorder thresholds
- **API Versioning** - Supports v1.0 and v2.0 with backward compatibility
- **Event-Driven Architecture** - Publishes price changes and handles order validation events
- **Semantic Search** - pgvector-based similarity search (AI integration ready)
- **Image Serving** - Product picture endpoints with multiple format support

## Technology Stack

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL with pgvector extension
- RabbitMQ for messaging
- Flyway for database migrations
- MapStruct for DTO mapping
- SpringDoc OpenAPI 3

## Getting Started

### Prerequisites

- Java 21+
- Docker and Docker Compose
- Maven 3.9+

### Running with Docker Compose

From the project root:

```bash
docker compose -f docker-compose-catalog.yml up -d
```

This starts:
- PostgreSQL (port 5432)
- RabbitMQ (ports 5672, 15672)
- Identity Service (port 9100)
- Catalog Service (port 9101)

### Running Locally

1. Start the infrastructure:
```bash
docker compose -f docker-compose-catalog.yml up -d postgres rabbitmq
```

2. Run the service:
```bash
cd services/catalog-service
mvn spring-boot:run
```

The service will be available at `http://localhost:9101`

## API Endpoints

All endpoints require the `api-version` query parameter (1.0 or 2.0).

### Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/catalog/items` | List items (paginated) |
| GET | `/api/catalog/items/{id}` | Get item by ID |
| GET | `/api/catalog/items/by?ids=1&ids=2` | Get items by multiple IDs |
| GET | `/api/catalog/items/{id}/pic` | Get product image |
| POST | `/api/catalog/items` | Create item |
| PUT | `/api/catalog/items/{id}` | Update item |
| DELETE | `/api/catalog/items/{id}` | Delete item |

### Filtering & Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/catalog/items/type/{typeId}/brand/{brandId}` | Filter by type and brand |
| GET | `/api/catalog/items/type/{typeId}` | Filter by type |
| GET | `/api/catalog/items/type/all/brand/{brandId}` | Filter by brand |
| GET | `/api/catalog/items/withsemanticrelevance?text=...` | Semantic search |

### Reference Data

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/catalog/catalogtypes` | List all product types |
| GET | `/api/catalog/catalogbrands` | List all product brands |

### Example Request

```bash
curl "http://localhost:9101/api/catalog/items?pageIndex=0&pageSize=10&api-version=2.0"
```

## Configuration

Key configuration in `application.yml`:

```yaml
server:
  port: 9101

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/catalogdb
  rabbitmq:
    host: localhost
    port: 5672

catalog:
  pics-path: classpath:pics
  ai:
    enabled: false
```

## Database Schema

The service uses three main tables:

- **Catalog** - Products with stock info and AI embeddings
- **CatalogType** - Product categories (Mug, T-Shirt, Sheet, etc.)
- **CatalogBrand** - Product brands (.NET, Azure, Visual Studio, etc.)

Database migrations are managed by Flyway in `src/main/resources/db/migration/`.

## Integration Events

### Published Events
- `ProductPriceChangedIntegrationEvent` - When product price is updated

### Consumed Events
- `OrderStatusChangedToAwaitingValidationIntegrationEvent` - Validates stock for orders
- `OrderStatusChangedToPaidIntegrationEvent` - Processes paid orders

## Project Structure

```
catalog-service/
├── src/main/java/com/eshop/catalog/
│   ├── api/           # REST controllers
│   ├── config/        # Configuration classes
│   ├── dto/           # Data transfer objects
│   ├── events/        # Integration events and handlers
│   ├── exception/     # Custom exceptions
│   ├── model/         # JPA entities
│   ├── repository/    # Spring Data repositories
│   └── service/       # Business logic
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/  # Flyway migrations
│   ├── pics/          # Product images
│   └── setup/         # Seed data
└── pom.xml
```

## API Documentation

OpenAPI documentation is available at:
- Swagger UI: `http://localhost:9101/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9101/v3/api-docs`

## Health Check

```bash
curl http://localhost:9101/actuator/health
```
