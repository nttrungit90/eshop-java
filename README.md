# eShop Java - Reference Application

This is a Java/Spring Boot conversion of the [.NET eShop Reference Application](https://github.com/dotnet/eShop), demonstrating a microservices architecture with Docker Compose orchestration.

## Quick Start

```bash
# Start everything (like dotnet run --project eShop.AppHost)
docker compose up --build

# Or start infrastructure only first
docker compose up postgres redis rabbitmq spring-boot-admin jaeger -d
```

## Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **WebApp** | http://localhost:8080 | - |
| **Admin Dashboard** | http://localhost:9090 | - |
| **Jaeger Tracing** | http://localhost:16686 | - |
| **RabbitMQ Management** | http://localhost:15672 | guest/guest |

### Service Health Checks

```bash
curl http://localhost:9100/actuator/health  # Identity
curl http://localhost:9101/actuator/health  # Catalog
curl http://localhost:9102/actuator/health  # Ordering
curl http://localhost:9103/actuator/health  # Basket
curl http://localhost:9104/actuator/health  # Webhooks
```

## Architecture

This application follows a microservices architecture with the following components:

```
┌─────────────────────────────────────────────────────────────────┐
│                         WebApp (React)                          │
│                        localhost:8080                           │
└─────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
        ▼                         ▼                         ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│   Identity    │      │    Catalog    │      │    Basket     │
│   Service     │      │    Service    │      │    Service    │
│  :9100        │      │  :9101        │      │  :9103        │
└───────────────┘      └───────────────┘      └───────────────┘
        │                      │                      │
        │                      ▼                      ▼
        │              ┌───────────────┐      ┌───────────────┐
        │              │   Ordering    │      │     Redis     │
        │              │     API       │      │    (Cache)    │
        │              │  :9102        │      └───────────────┘
        │              └───────────────┘
        │                      │
        ▼                      ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│  PostgreSQL   │      │   RabbitMQ    │◄────►│    Order      │
│  (Databases)  │      │  (EventBus)   │      │   Processor   │
└───────────────┘      └───────────────┘      └───────────────┘
                               │
                               ▼
                       ┌───────────────┐
                       │    Payment    │
                       │   Processor   │
                       └───────────────┘
```

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 LTS |
| Framework | Spring Boot 3.4.x |
| Build Tool | Maven |
| Database | PostgreSQL 17 + pgvector |
| Cache | Redis 7 |
| Message Broker | RabbitMQ 4 |
| API Documentation | SpringDoc OpenAPI |
| Tracing | Jaeger + OpenTelemetry |
| Monitoring | Spring Boot Admin |
| Frontend | React 18 + TypeScript + Vite |

## Project Structure

```
eshop-java/
├── pom.xml                              # Parent POM
├── docker-compose.yml                   # Orchestration
├── CONVERSION_MAP.md                    # .NET → Java mapping
│
├── common/                              # Shared libraries
│   ├── service-defaults/                # Common Spring Boot config
│   ├── event-bus/                       # Event bus abstractions
│   ├── event-bus-rabbitmq/              # RabbitMQ implementation
│   └── integration-event-log/           # Outbox pattern
│
├── services/                            # Microservices
│   ├── identity-service/                # OAuth2/OIDC (port 9100)
│   ├── catalog-service/                 # Product catalog (port 9101)
│   ├── basket-service/                  # Shopping cart (port 9103)
│   ├── ordering/                        # Order management
│   │   ├── ordering-domain/             # DDD domain layer
│   │   ├── ordering-infrastructure/     # Data access layer
│   │   └── ordering-api/                # REST API (port 9102)
│   ├── order-processor/                 # Background processor (port 9105)
│   ├── payment-processor/               # Payment handling (port 9106)
│   └── webhooks-service/                # Webhooks (port 9104)
│
├── clients/
│   └── webapp/                          # React SPA (port 8080)
│
└── scripts/
    └── init-databases.sql               # Database initialization
```

## Development

### Prerequisites

- Java 21 or later
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (for webapp)

### Build

```bash
# Build all Java modules
./mvnw clean package

# Build without tests
./mvnw clean package -DskipTests

# Build single module
./mvnw -pl services/catalog-service clean package
```

### Run Locally

```bash
# Start infrastructure
docker compose up postgres redis rabbitmq -d

# Run single service
./mvnw -pl services/catalog-service spring-boot:run
```

### Debug

Remote debugging is enabled on ports 5005-5011 in docker-compose.override.yml.

## .NET to Java Mapping

This project is a faithful conversion of the .NET eShop reference application. See [CONVERSION_MAP.md](CONVERSION_MAP.md) for detailed mappings between .NET and Java components.

### Key Differences

| Aspect | .NET Aspire | Java/Spring |
|--------|-------------|-------------|
| Orchestration | AppHost project | docker-compose.yml |
| Dashboard | Aspire Dashboard | Spring Boot Admin |
| DI Container | Microsoft.Extensions.DI | Spring IoC |
| ORM | Entity Framework Core | Spring Data JPA |
| Auth Server | Duende IdentityServer | Spring Authorization Server |
| Events | MediatR | Spring ApplicationEventPublisher |
| Validation | FluentValidation | Jakarta Bean Validation |
| Resilience | Polly | Resilience4j |

## License

This project is licensed under the MIT License - see the original [eShop repository](https://github.com/dotnet/eShop) for details.
