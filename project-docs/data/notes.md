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

## Q&A

### Why migrate to Java?

Team expertise and ecosystem alignment. Spring Boot provides equivalent capabilities to .NET Aspire with better fit for the team.

### Why keep .NET running during migration?

Strangler fig pattern — migrate incrementally without breaking the running system. Services are swapped one-by-one with a boolean flag in the .NET AppHost.

### Why is Identity Service migrated last?

All other services depend on it for JWT validation. Changing the auth server is high risk and should only happen after all consumers are Java and tested.
