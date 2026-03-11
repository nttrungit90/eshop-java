# Notes

## Development Notes

### Running the System (During Migration)

```bash
# Terminal 1: .NET infra + unmigrated services
cd eShop && dotnet run --project src/eShop.AppHost

# Terminal 2: Migrated Java services (stable)
cd eshop-java && docker compose up -d

# Terminal 3: Service being debugged
cd eshop-java && ./mvnw -pl services/<name> spring-boot:run
```

### Aspire-Managed Credentials

These are stable and used in Java application.yml files:

- PostgreSQL: `postgres` / `71UhdH_{f7C+yPyrh92RRW`
- RabbitMQ: `guest` / `WBpzyj95KTuVkpGxR5Fx1j`

### Database Schema

Java services use .NET-created schema (Flyway disabled, `ddl-auto: none`). PascalCase column names preserved via `globally_quoted_identifiers: true` and `PhysicalNamingStrategyStandardImpl`.

### Fixed Infrastructure Ports

Pinned in .NET AppHost so Java services can connect without Aspire service discovery:

- PostgreSQL: 5432
- RabbitMQ: 5672
- RabbitMQ Management UI: 15672
- Redis: 6379

### RabbitMQ Management UI

- URL: http://localhost:15672
- Login: `guest` / `WBpzyj95KTuVkpGxR5Fx1j`
- Enabled via `.WithManagementPlugin(port: 15672)` in .NET AppHost
- Useful for inspecting exchanges, queues, bindings, and message rates during migration

## Issues & Solutions

### JWT / Authentication

#### 401 on catalog-service from .NET WebApp
- **Cause**: `identity.url` pointed to `http://localhost:9100` (Java identity, not running) instead of .NET Identity at `http://localhost:5223`
- **Fix**: Changed `identity.url` to `http://localhost:5223` in application.yml; in Docker, set `IDENTITY_URL=http://host.docker.internal:5223`

#### 401 on permitAll endpoints when Bearer token present
- **Cause**: Spring Security's `BearerTokenAuthenticationFilter` runs BEFORE authorization checks. If a Bearer token is present and JWKS validation fails, it returns 401 even on `permitAll` endpoints.
- **Fix**: Added selective `BearerTokenResolver` that returns null for public paths (skips token extraction entirely)
- **File**: `common/service-defaults/.../security/JwtSecurityConfig.java`

#### 401 on catalog GET endpoints in Docker but not locally
- **Cause**: Spring Security 6.x uses `MvcRequestMatcher` by default when Spring MVC is on classpath. `requestMatchers(HttpMethod.GET, "/api/catalog/**")` didn't match correctly in Docker (likely DispatcherServlet initialization timing).
- **Fix**: Switch to explicit `new AntPathRequestMatcher("/api/catalog/**", HttpMethod.GET.name())` which matches raw URL patterns directly
- **Lesson**: Always use `AntPathRequestMatcher` for URL-based matching in Spring Security 6.x to avoid `MvcRequestMatcher` inconsistencies

### RabbitMQ / Event Bus Interop (.NET ↔ Java)

#### Catalog-service not receiving events from .NET
- **Cause 1**: No `@RabbitListener` on any catalog-service handler
- **Cause 2**: Queue bound with empty routing key `""` but .NET publishes with event class simple name as routing key on a direct exchange
- **Fix**: Created `EventBusSubscriptions` bean for per-event-type routing key bindings, created `CatalogEventListener` with `@RabbitListener` + routing key dispatch

#### JSON serialization mismatch (.NET vs Java)
- **Cause**: Java used camelCase (`orderId`), .NET used PascalCase (`OrderId`). Java `Instant` serialized as epoch number, .NET `DateTime` as ISO string.
- **Fix**: Configured `ObjectMapper` with `UPPER_CAMEL_CASE` naming strategy and `WRITE_DATES_AS_TIMESTAMPS=false`
- **File**: `common/event-bus-rabbitmq/.../RabbitMQConfig.java`

#### Double-encoded JSON messages
- **Cause**: `RabbitMQEventBus` pre-serialized events to String via ObjectMapper, then `Jackson2JsonMessageConverter` serialized the String again (wrapping in extra quotes)
- **Fix**: Pass event objects directly to `amqpTemplate.convertAndSend()`, let the message converter do the serialization once

#### Jackson deserialization failures
- **"No Creators" error**: Event classes had no default constructor. Fix: Added no-arg constructors, changed fields from `final` to non-final
- **"Unrecognized field OrderStatus"**: .NET events have extra fields Java doesn't need. Fix: `mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)`
- **Payment-processor typed listener failure**: `@RabbitListener` with typed parameter couldn't handle .NET extra fields. Fix: Switched to raw `Message` + `objectMapper.readValue()` pattern

### Docker-Compose Specific

#### Services work locally but fail in Docker
- **Cause**: `docker-compose.yml` had stale/wrong credentials and URLs that override `application.yml`:
  - catalog-service Postgres password was `postgres` instead of `71UhdH_{f7C+yPyrh92RRW`
  - catalog-service RabbitMQ password was `guest` instead of `WBpzyj95KTuVkpGxR5Fx1j`
  - catalog-service missing `IDENTITY_URL` env var
  - webhooks-service `IDENTITY_URL` pointed to port 9100 instead of 5223
- **Fix**: Updated all credentials and URLs in docker-compose.yml to match application.yml values
- **Lesson**: Always keep docker-compose.yml env vars in sync with application.yml after making config changes

#### OTLP metrics "connection refused" spam in logs
- **Cause**: `spring-boot-admin-client:3.4.1` transitively pulls in `micrometer-registry-otlp` which auto-publishes to `localhost:4318`
- **Fix**: Disabled in all service application.yml:
  ```yaml
  management:
    otlp:
      metrics:
        export:
          enabled: false
      tracing:
        endpoint: ""
  ```
- **Note**: Explicit OTLP dependencies were removed from pom.xml, but transitive dep from spring-boot-admin-client remains

### Redis (Aspire → Java)

#### Aspire Redis uses TLS — Java gets "wrong version number" error
- **Cause**: Aspire's Redis is started with `--tls-port 6379 --port 6380`. Port 6379 requires TLS with Aspire-managed certificates. Java's Lettuce client connects with plain TCP and gets `SSL routines::wrong version number`.
- **Fix**: Connect to port **6380** (the plain TCP port) instead of 6379. Also requires the Redis password (`REDIS_PASSWORD` env var from the Aspire Redis container).
- **Config**:
  ```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6380           # plain TCP (not 6379 which is TLS)
        password: <from Aspire Redis container env>
  ```
- **Docker-compose**: Use `SPRING_DATA_REDIS_HOST=host.docker.internal`, `SPRING_DATA_REDIS_PORT=6380`, `SPRING_DATA_REDIS_PASSWORD=<password>`

#### Aspire Redis container ports bound to 127.0.0.1 only
- **Cause**: Aspire containers bind to `127.0.0.1` by default. Docker containers using `host.docker.internal` can't reach ports bound to loopback only.
- **Fix**: Pin ports in the .NET AppHost with `.WithEndpoint(...)`. Then **remove the old Redis container** and **restart the AppHost** so it gets recreated with the new port bindings. Aspire containers with `ContainerLifetime.Persistent` or already-running containers won't pick up port changes automatically.
- **How to find Redis credentials**: `docker inspect <redis-container> --format '{{range .Config.Env}}{{println .}}{{end}}' | grep REDIS`

### Established Patterns

- **Raw Message listener**: All Java services consuming .NET events must use `@RabbitListener` with raw `Message` + `ObjectMapper.readValue()` (not typed parameters)
- **EventBusSubscriptions**: Each service declares subscribed event types via bean; creates per-event-type routing key bindings matching .NET's direct exchange pattern
- **identity.url**: Must point to .NET Identity HTTP endpoint (`localhost:5223`) during migration
- **Build before Docker**: Always run `./mvnw package -DskipTests` before `docker compose up --build`

## Q&A

### Why migrate to Java?

Team expertise and ecosystem alignment. Spring Boot provides equivalent capabilities to .NET Aspire with better fit for the team.

### Why keep .NET running during migration?

Strangler fig pattern — migrate incrementally without breaking the running system. Services are swapped one-by-one with a boolean flag in the .NET AppHost.

### Why is Identity Service migrated last?

All other services depend on it for JWT validation. Changing the auth server is high risk and should only happen after all consumers are Java and tested.
